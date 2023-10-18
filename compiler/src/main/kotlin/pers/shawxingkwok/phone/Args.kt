package pers.shawxingkwok.phone

import pers.shawxingkwok.ksputil.Environment
import pers.shawxingkwok.ksputil.Log

internal object Args {
    val defaultMethod = Environment.options["phone.default-method"]

    init {
        Log.check(null, defaultMethod == "get" || defaultMethod == "post"){
            "Set phone.default-method in build.gradle(.kts)."
        }
    }

    val ServerPackagePath = Environment.options["phone.server-package-path"]

    val ServerPackageName = Environment.options["phone.server-package-name"]
        ?.takeIf { it.any() } ?: error(TODO())

    val ClientPackagePath = Environment.options["phone.client-package-path"]

    val ClientPackageName = Environment.options["phone.client-package-name"]
        ?.takeIf { it.any() } ?: error(TODO())
}