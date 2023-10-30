package pers.shawxingkwok.phone

import pers.shawxingkwok.ksputil.Environment

internal object Args {
    val JwtAuthName = Environment.options["phone.jwt-auth-name"]

    val DefaultMethod = when(val v = Environment.options["phone.default-method"]){
        "get", "post", "put", "delete", "patch" -> {
            val name = v.replaceFirstChar { it.uppercase() }
            Phone.Method.valueOf(name)
        }

        "Get", "Post", "Put", "Delete", "Patch" -> Phone.Method.valueOf(v)

        else -> error("Set phone.default-method with `get`, `post`, `put`, `delete`, or `patch` in build.gradle(.kts).")
    }

    val ServerPackagePath = Environment.options["phone.server-package-path"]

    val ServerPackageName = Environment.options["phone.server-package-name"]
        ?.takeIf { it.any() } ?: error("Set phone.server-package-name in build.gradle(.kts).")

    val ClientPackagePath = Environment.options["phone.client-package-path"]

    val ClientPackageName = Environment.options["phone.client-package-name"]
        ?.takeIf { it.any() } ?: error("Set phone.client-package-name in build.gradle(.kts).")

    init {
        check(ClientPackageName != ServerPackageName){
            "phone.client-package-name can't equal to phone.server-package-name."
        }
        check(ClientPackagePath != ServerPackagePath){
            "phone.client-package-path can't equal to phone.server-package-path."
        }
    }
}