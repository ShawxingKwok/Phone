package pers.shawxingkwok.phone

@Target
public annotation class Phone{
    @Target(AnnotationTarget.CLASS)
    public annotation class Api

    @Target(AnnotationTarget.CLASS)
    public annotation class WebSockets(
        val protocol: String = "",
        val isRaw: Boolean = false,
    )

    @Target(AnnotationTarget.CLASS)
    public annotation class Serializer

    @Target(
        AnnotationTarget.VALUE_PARAMETER,
        AnnotationTarget.TYPE,
        AnnotationTarget.FUNCTION,
        AnnotationTarget.CLASS
    )
    public annotation class Crypto

    @Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
    public annotation class Auth(
        val configurations: Array<String> = [""],
        val strategy: AuthenticationStrategy = AuthenticationStrategy.FirstSuccessful,
    ){
        public enum class AuthenticationStrategy{
            Optional, FirstSuccessful, Required
        }
    }

    public interface Cipher {
        public fun encrypt(bytes: ByteArray): ByteArray
        public fun decrypt(bytes: ByteArray): ByteArray
    }
}