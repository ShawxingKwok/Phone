import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.partialcontent.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import io.ktor.utils.io.streams.*
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

class ApplicationTest {
    val file = File("ktor_logo.png")
    val fileSize = file.readBytes().size
    @Test
    fun clientSendsMultipartData() = testApplication{
        application {
            routing {
                post("/upload") {
                    val data = call.receiveMultipart()
                    val descriptionPart = data.readPart() as PartData.FormItem
                    val filePart = data.readPart() as PartData.FileItem

                    call.respondText {
                        "${descriptionPart.name}:${descriptionPart.value}," +
                                "${filePart.name}:[${filePart.contentType}][${filePart.contentDisposition}]"
                    }
                }
            }
        }

        client.submitFormWithBinaryData(
            url = "upload",
            formData = formData {
                append("description", "Ktor logo")
                append("image", File("ktor_logo.png").readBytes(), Headers.build {
                    append(HttpHeaders.ContentType, "image/png")
                    append(HttpHeaders.ContentDisposition, "filename=\"ktor_logo.png\"")
                })
            }
        )
        .bodyAsText()
        .let(::println)

        client.post("upload") {
            setBody(MultiPartFormDataContent(
                formData {
                    append("description", "Ktor logo")
                    append("image", File("ktor_logo.png").readBytes(), Headers.build {
                        append(HttpHeaders.ContentType, "image/png")
                        append(HttpHeaders.ContentDisposition, "filename=\"ktor_logo.png\"")
                    })
                },
                boundary = "WebAppBoundary"
            )
            )
            onUpload { bytesSentTotal, contentLength ->
                println("Sent $bytesSentTotal bytes from $contentLength")
            }
        }
        .bodyAsText()
        .let(::println)
    }

    @Test
    fun clientSendsBinaryDataWithChannel() = testApplication {
        application {
            routing {
                post("/channel") {
                    assertEquals(call.request.header("type"), "png")
                    assertEquals(call.receiveChannel().toByteArray().size, fileSize)

                    return@post

                    val file = File("uploads/ktor_logo.png")

                    if (!file.parentFile.exists())
                        file.parentFile.mkdirs()

                    if (!file.exists())
                        file.createNewFile()

                    call.receiveChannel().copyAndClose(file.writeChannel())
                    call.respondText("done")
                }
            }
        }

        client.post("channel") {
            header("type", "png")
            println(File("ktor_logo.png").readBytes().size)
            setBody(File("ktor_logo.png").readChannel())
            // setBody(file.readBytes())
        }
    }

    @Test
    fun clientDownloads() = testApplication {
        application {
            install(PartialContent)
            install(AutoHeadResponse)

            routing {
                get("/download") {
                    call.response.header(
                        HttpHeaders.ContentDisposition,
                        ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, "ktor_logo.png")
                            .toString()
                    )
                    call.respondFile(file){
                    }
                }
            }
        }

        // whole
        run {
            val response = client.get("/download")
            assertEquals(
                "attachment; filename=ktor_logo.png",
                response.headers[HttpHeaders.ContentDisposition]
            )
            val readSize = response.readBytes().size
            assert(readSize == fileSize) {
                response.readBytes().size
            }
        }
    }

    @Test
    fun partialContent() = testApplication {
        // partial
        run {
            val length = client.head("download").headers[HttpHeaders.ContentLength]?.toLong() as Long
            val lastByte = length - 1
            val outFile = File("downloads")
            var start = outFile.length()
            val output = FileOutputStream(outFile, true)

            while (true) {
                val end = min(start + 1024 - 1, lastByte)
                val data = client.get("download") {
                    header("Range", "bytes=${start}-${end}")
                }.body<ByteArray>()
                output.write(data)
                if (end >= lastByte) break
                start += 1024
            }
        }
    }
}