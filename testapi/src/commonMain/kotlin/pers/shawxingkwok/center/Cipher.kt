package pers.shawxingkwok.center

import pers.shawxingkwok.phone.Phone

@Phone.Feature.Crypto
object Cipher : Phone.Feature.Crypto.Cipher{
    override fun encrypt(bytes: ByteArray): ByteArray {
        return bytes + 1
    }

    override fun decrypt(bytes: ByteArray): ByteArray {
        return bytes.copyOfRange(0, bytes.lastIndex)
    }
}