@file:Suppress("unused")

package pers.shawxingkwok.phone

/**
 * See [doc](https://shawxingkwok.github.io/ITWorks/docs/multiplatform/phone).
 */
@Target
public annotation class Phone {
    /**
     * See [doc](https://shawxingkwok.github.io/ITWorks/docs/multiplatform/phone/#cipher).
     */
    public interface Cipher {
        public fun encrypt(bytes: ByteArray): ByteArray
        public fun decrypt(bytes: ByteArray): ByteArray
    }

    /**
     * See [doc](https://shawxingkwok.github.io/ITWorks/docs/multiplatform/phone/#http-methods).
     */
    public enum class Method {
        Default, Get, Post, Put, Delete, Patch
    }

    /**
     * See [doc](https://shawxingkwok.github.io/ITWorks/docs/multiplatform/phone).
     */
    @Retention(AnnotationRetention.SOURCE)
    @Target(AnnotationTarget.CLASS)
    public annotation class Api(val defaultMethod: Method = Method.Default)

    /**
     * See [doc](https://shawxingkwok.github.io/ITWorks/docs/multiplatform/phone/#calls).
     */
    @Target
    public annotation class Call {
        /**
         * See [doc](https://shawxingkwok.github.io/ITWorks/docs/multiplatform/phone/#common).
         */
        @Retention(AnnotationRetention.SOURCE)
        @Target(AnnotationTarget.FUNCTION)
        public annotation class Common<T>(
            val method: Method = Method.Default,
            val polymorphicId: String = ""
        )

        /**
         * See [doc](https://shawxingkwok.github.io/ITWorks/docs/multiplatform/phone/#manual).
         */
        @Retention(AnnotationRetention.SOURCE)
        @Target(AnnotationTarget.FUNCTION)
        public annotation class Manual<T>(
            val method: Method = Method.Default,
            val polymorphicId: String = ""
        )

        /**
         * See [doc](https://shawxingkwok.github.io/ITWorks/docs/multiplatform/phone/#websocket).
         */
        @Retention(AnnotationRetention.SOURCE)
        @Target(AnnotationTarget.FUNCTION)
        public annotation class WebSocket(
            val method: Method = Method.Get,
            val polymorphicId: String = "",
            val isRaw: Boolean = false
        )

        /**
         * See [doc](https://shawxingkwok.github.io/ITWorks/docs/multiplatform/phone/#partialcontent).
         */
        @Retention(AnnotationRetention.SOURCE)
        @Target(AnnotationTarget.FUNCTION)
        public annotation class PartialContent<T>(val polymorphicId: String = "")
    }

    /**
     * See [doc](https://shawxingkwok.github.io/ITWorks/docs/multiplatform/phone/#crypto).
     */
    @Retention(AnnotationRetention.SOURCE)
    @Target(
        AnnotationTarget.VALUE_PARAMETER,
        AnnotationTarget.TYPE,
        AnnotationTarget.FUNCTION,
        AnnotationTarget.CLASS
    )
    public annotation class Crypto

    /**
     * See [doc](https://shawxingkwok.github.io/ITWorks/docs/multiplatform/phone/#serialize-third-party-types).
     */
    @Retention(AnnotationRetention.SOURCE)
    @Target(AnnotationTarget.CLASS)
    public annotation class Serializer

    /**
     * See [doc](https://shawxingkwok.github.io/ITWorks/docs/multiplatform/phone/#auth).
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