package pers.shawxingkwok.phone

@Target
public annotation class Phone{
    @Retention(AnnotationRetention.SOURCE)
    @Target(AnnotationTarget.CLASS)
    public annotation class Api

    @Retention(AnnotationRetention.SOURCE)
    @Target(AnnotationTarget.CLASS)
    public annotation class Serializer

    @Target
    public annotation class Method{
        @Retention(AnnotationRetention.SOURCE)
        @Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
        public annotation class Get

        @Retention(AnnotationRetention.SOURCE)
        @Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
        public annotation class Post(val withForm: Boolean = true)

        @Retention(AnnotationRetention.SOURCE)
        @Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
        public annotation class Put(val withForm: Boolean = true)

        @Retention(AnnotationRetention.SOURCE)
        @Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
        public annotation class Delete(val withForm: Boolean = true)

        @Retention(AnnotationRetention.SOURCE)
        @Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
        public annotation class Patch(val withForm: Boolean = true)
    }

    @Target
    public annotation class Kind {
        @Retention(AnnotationRetention.SOURCE)
        @Target(AnnotationTarget.FUNCTION)
        public annotation class Common<T>

        /**
         * Commonly used with file transmission.
         * Note that when you upload files, annotate the function with the method `Post` or `Put`,
         * and set `withForm` to false.
         */
        @Retention(AnnotationRetention.SOURCE)
        @Target(AnnotationTarget.FUNCTION)
        public annotation class Manual<T>

        /**
         * Method is `Get` by default unless you annotate the function with another method.
         * Parameter positions are limited to `URL` rather than `form`.
         */
        @Retention(AnnotationRetention.SOURCE)
        @Target(AnnotationTarget.FUNCTION)
        public annotation class WebSocket(val isRaw: Boolean = false)

        /**
         * Method is limited `Get`, and parameter positions are limited to `URL`.
         */
        @Retention(AnnotationRetention.SOURCE)
        @Target(AnnotationTarget.FUNCTION)
        public annotation class PartialContent<T>
    }

    @Target
    public annotation class Feature {
        public annotation class ParametersPosition {
            @Retention(AnnotationRetention.SOURCE)
            @Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
            public annotation class Url

            @Retention(AnnotationRetention.SOURCE)
            @Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
            public annotation class Form
        }

        @Retention(AnnotationRetention.SOURCE)
        @Target(AnnotationTarget.FUNCTION)
        public annotation class Polymorphic(val id: String)

        @Retention(AnnotationRetention.SOURCE)
        @Target(
            AnnotationTarget.VALUE_PARAMETER,
            AnnotationTarget.TYPE,
            AnnotationTarget.FUNCTION,
            AnnotationTarget.CLASS
        )
        public annotation class Crypto{
            public interface Cipher {
                public fun encrypt(bytes: ByteArray): ByteArray
                public fun decrypt(bytes: ByteArray): ByteArray
            }
        }

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
        ) {
            public enum class Strategy {
                Optional, FirstSuccessful, Required
            }
        }
    }
}