package pers.shawxingkwok.phone

@Target
public annotation class Phone{
    @Retention(AnnotationRetention.SOURCE)
    @Target(AnnotationTarget.CLASS)
    public annotation class Api

    @Retention(AnnotationRetention.SOURCE)
    @Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
    public annotation class WebSocket(val isRaw: Boolean = false)

    @Retention(AnnotationRetention.SOURCE)
    @Target(AnnotationTarget.FUNCTION)
    public annotation class Common<T>

    /**
     * [T] is the additional header info type. Use `Unit` if needless.
     */
    @Retention(AnnotationRetention.SOURCE)
    @Target(AnnotationTarget.FUNCTION)
    public annotation class File<T>

    @Retention(AnnotationRetention.SOURCE)
    @Target(AnnotationTarget.FUNCTION)
    public annotation class Polymorphic(val id: String)

    @Retention(AnnotationRetention.SOURCE)
    @Target(AnnotationTarget.CLASS)
    public annotation class Serializer

    @Retention(AnnotationRetention.SOURCE)
    @Target(
        AnnotationTarget.VALUE_PARAMETER,
        AnnotationTarget.TYPE,
        AnnotationTarget.FUNCTION,
        AnnotationTarget.CLASS
    )
    public annotation class Crypto

    @Retention(AnnotationRetention.SOURCE)
    @Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
    public annotation class Get

    @Retention(AnnotationRetention.SOURCE)
    @Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
    public annotation class Post

    /**
     * @param configurations "" represents `null` which
     * redirects to the default authentication.
     */
    @Retention(AnnotationRetention.SOURCE)
    @Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
    public annotation class Auth(
        val configurations: Array<String> = [""],
        val strategy: Strategy = Strategy.FirstSuccessful,
        val withToken: Boolean = false,
    ){
        public enum class Strategy{
            Optional, FirstSuccessful, Required
        }
    }

    public interface Cipher {
        public fun encrypt(bytes: ByteArray): ByteArray
        public fun decrypt(bytes: ByteArray): ByteArray
    }
}