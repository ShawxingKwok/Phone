package pers.shawxingkwok.phone

import pers.shawxingkwok.ksputil.Environment

internal object Args {
    val ServerPackagePath = Environment.options["phone.server-package-path"]

    val ServerPackageName = Environment.options["phone.server-package-name"]
        ?.takeIf { it.any() } ?: error(TODO())

    val ClientPackagePath = Environment.options["phone.client-package-path"]

    val ClientPackageName = Environment.options["phone.client-package-name"]
        ?.takeIf { it.any() } ?: error(TODO())
}