package pers.shawxingkwok.center

import pers.shawxingkwok.phone.Phone

// @Phone.Crypto
object Cipher : Phone.Cipher{
    override fun encrypt(bytes: ByteArray): ByteArray {
        return bytes
    }

    override fun decrypt(bytes: ByteArray): ByteArray {
        return bytes
    }
}