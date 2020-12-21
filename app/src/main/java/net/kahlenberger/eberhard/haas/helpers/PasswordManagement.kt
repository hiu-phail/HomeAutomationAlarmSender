package net.kahlenberger.eberhard.haas.helpers

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.support.annotation.RequiresApi
import android.util.Base64
import android.util.Log
import java.security.Key
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec

@RequiresApi(Build.VERSION_CODES.M)
class PasswordManagement {
    private lateinit var srandom: SecureRandom
    private var keyStore: KeyStore
    private val AndroidKeyStore = "AndroidKeyStore"
    private val AES_MODE = "AES/GCM/NoPadding"
    private val KEY_ALIAS = "credKey"
    private val FIXED_IV = byteArrayOf(0x01, 0x02, 0x03, 0x04,  0x05,  0x06,  0x07,  0x08,  0x09,  0x10, 0x11, 0x12)

    constructor()
    {
        srandom = SecureRandom()
        keyStore = KeyStore.getInstance(AndroidKeyStore)
        keyStore.load(null)

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator: KeyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, AndroidKeyStore)
            keyGenerator.init(
                    KeyGenParameterSpec.Builder(KEY_ALIAS,
                            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .setRandomizedEncryptionRequired(false)
                            .build())
            keyGenerator.generateKey()
        }
    }

    private fun getSecretKey(context: Context): Key {
        return keyStore.getKey(KEY_ALIAS, null)
    }

    fun encrypt(context: Context, decrypted: String): String {
        val c: Cipher = Cipher.getInstance(AES_MODE)
        c.init(Cipher.ENCRYPT_MODE, getSecretKey(context), GCMParameterSpec(128, FIXED_IV))

        val encryptedBytes: ByteArray = c.doFinal(decrypted.toByteArray())
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }

    fun decrypt(context: Context, encrypted: String): String {
        val c = Cipher.getInstance(AES_MODE)
        c.init(Cipher.DECRYPT_MODE, getSecretKey(context), GCMParameterSpec(128, FIXED_IV));

        val decodedBytes: ByteArray = Base64.decode(encrypted, Base64.DEFAULT)
        return String(c.doFinal(decodedBytes))
    }
}