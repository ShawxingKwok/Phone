@file:Suppress("unused")

package pers.shawxingkwok.phone

@Target
public annotation class Phone {
    public interface Cipher {
        public fun encrypt(bytes: ByteArray): ByteArray
        public fun decrypt(bytes: ByteArray): ByteArray
    }

    public enum class Method {
        Default, Get, Post, Put, Delete, Patch
    }

    @Retention(AnnotationRetention.SOURCE)
    @Target(AnnotationTarget.CLASS)
    public annotation class Api(val defaultMethod: Method = Method.Default)

    @Target
    public annotation class Call {
        @Retention(AnnotationRetention.SOURCE)
        @Target(AnnotationTarget.FUNCTION)
        public annotation class Common<T>(
            val method: Method = Method.Default,
            val polymorphicId: String = ""
        )

        /**
         * Commonly used with file downloads.
         */
        @Retention(AnnotationRetention.SOURCE)
        @Target(AnnotationTarget.FUNCTION)
        public annotation class Manual<T>(
            val method: Method = Method.Default,
            val polymorphicId: String = ""
        )

        @Retention(AnnotationRetention.SOURCE)
        @Target(AnnotationTarget.FUNCTION)
        public annotation class WebSocket(
            val method: Method = Method.Get,
            val polymorphicId: String = "",
            val isRaw: Boolean = false
        )

        @Retention(AnnotationRetention.SOURCE)
        @Target(AnnotationTarget.FUNCTION)
        public annotation class PartialContent<T>(val polymorphicId: String = "")
    }

    @Retention(AnnotationRetention.SOURCE)
    @Target(
        AnnotationTarget.VALUE_PARAMETER,
        AnnotationTarget.TYPE,
        AnnotationTarget.FUNCTION,
        AnnotationTarget.CLASS
    )
    public annotation class Crypto

    @Retention(AnnotationRetention.SOURCE)
    @Target(AnnotationTarget.CLASS)
    public annotation class Serializer

    /**
     * @param configurations "" represents `null` which
     * redirects to the default authentication.
     */
    @Retention(AnnotationRetention.SOURCE)
    @Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
    public annotation class Auth(
        vararg val configurations: String = [""],
        val strategy: Strategy = Strategy.FirstSuccessful,
    ) {
        public enum class Strategy {
            Optional, FirstSuccessful, Required
        }
    }
}