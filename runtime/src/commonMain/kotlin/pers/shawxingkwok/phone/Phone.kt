package pers.shawxingkwok.phone

@Target
public annotation class Phone{
    @Target(AnnotationTarget.CLASS)
    public annotation class Api

    @Target(AnnotationTarget.CLASS)
    public annotation class Serializer

    @Target(
        AnnotationTarget.VALUE_PARAMETER,
        AnnotationTarget.TYPE,
        AnnotationTarget.FUNCTION,
        AnnotationTarget.CLASS
    )
    public annotation class Crypto

    public interface Cipher {
        public fun encode(bytes: ByteArray): ByteArray
        public fun decode(bytes: ByteArray): ByteArray
    }
}