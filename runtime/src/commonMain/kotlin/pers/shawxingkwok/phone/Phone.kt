package pers.shawxingkwok.phone

@Target(AnnotationTarget.CLASS)
public annotation class Phone{
    @Target(AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER)
    public annotation class Encode

    @Target(AnnotationTarget.PROPERTY)
    public annotation class KSerializers
}