package pers.shawxingkwok.phone

import pers.shawxingkwok.ksputil.Environment

internal object Args {
    val defaultMethod = when(val v = Environment.options["phone.default-method"]){
        "get", "post", "put", "delete", "patch" -> {
            val name = v.replaceFirstChar { it.uppercase() }
            Phone.Method.valueOf(name)
        }

        "Get", "Post", "Put", "Delete", "Patch" -> Phone.Method.valueOf(v)

        else -> error("Set phone.default-method with `get`, `post`, `put`, `delete`, or `patch` in build.gradle(.kts).")
    }

    val ServerPackagePath = Environment.options["phone.server-package-path"]

    val ServerPackageName = Environment.options["phone.server-package-name"]
        ?.takeIf { it.any() } ?: error(TODO())

    val ClientPackagePath = Environment.options["phone.client-package-path"]

    val ClientPackageName = Environment.options["phone.client-package-name"]
        ?.takeIf { it.any() } ?: error(TODO())

    init {
        check(ClientPackageName != ServerPackageName)
        check(ClientPackagePath != ServerPackagePath)
    }
}