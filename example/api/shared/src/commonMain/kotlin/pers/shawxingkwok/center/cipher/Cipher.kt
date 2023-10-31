package pers.shawxingkwok.center.cipher

import pers.shawxingkwok.phone.Phone

@Phone.Crypto
object Cipher : Phone.Cipher{
    override fun encrypt(bytes: ByteArray): ByteArray {
        return bytes + 1
    }

    override fun decrypt(bytes: ByteArray): ByteArray {
        return bytes.copyOf(bytes.size - 1)
    }
}