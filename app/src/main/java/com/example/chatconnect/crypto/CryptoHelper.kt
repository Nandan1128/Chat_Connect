package com.example.chatconnect.crypto

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

object CryptoHelper {
    // Keystore alias prefix for RSA keys (per user)
    fun rsaAliasForUid(uid: String) = "rsa_key_$uid"

    // -------------------- RSA keypair generation in Android Keystore --------------------
    fun generateAndStoreRSAKeyPairIfNeeded(uid: String) {
        val alias = rsaAliasForUid(uid)
        val ks = KeyStore.getInstance("AndroidKeyStore")
        ks.load(null)
        if (ks.containsAlias(alias)) return // already exists

        val kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore")
        val spec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).run {
            setKeySize(2048)
            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
            setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
            build()
        }
        kpg.initialize(spec)
        kpg.generateKeyPair()
    }

    // Export public key (Base64) to upload to server
    fun getPublicKeyBase64(uid: String): String? {
        val alias = rsaAliasForUid(uid)
        val ks = KeyStore.getInstance("AndroidKeyStore")
        ks.load(null)
        val cert = ks.getCertificate(alias) ?: return null
        val pub = cert.publicKey
        return Base64.encodeToString(pub.encoded, Base64.NO_WRAP)
    }

    // RSA encrypt with recipient public key (base64)
    fun rsaEncryptWithPublicKeyBase64(publicKeyBase64: String, data: ByteArray): String {
        val pubBytes = Base64.decode(publicKeyBase64, Base64.NO_WRAP)
        val kf = KeyFactory.getInstance("RSA")
        val pubSpec = X509EncodedKeySpec(pubBytes)
        val pub = kf.generatePublic(pubSpec)
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, pub)
        val encrypted = cipher.doFinal(data)
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    // RSA decrypt with private key (stored in Keystore)
    fun rsaDecryptWithPrivateKey(uid: String, encryptedBase64: String): ByteArray {
        val alias = rsaAliasForUid(uid)
        val ks = KeyStore.getInstance("AndroidKeyStore")
        ks.load(null)
        val privateKey = ks.getKey(alias, null) // PrivateKey
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val enc = Base64.decode(encryptedBase64, Base64.NO_WRAP)
        return cipher.doFinal(enc)
    }

    // -------------------- AES-256 GCM helpers --------------------
    private const val GCM_TAG_LEN = 128 // bits
    private const val IV_SIZE = 12 // bytes for GCM

    data class EncryptedPayload(val ciphertextB64: String, val ivB64: String)

    fun generateAESKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        return keyGen.generateKey()
    }

    fun aesKeyToBase64(key: SecretKey): String =
        Base64.encodeToString(key.encoded, Base64.NO_WRAP)

    fun aesKeyFromBase64(b64: String): SecretKey =
        SecretKeySpec(Base64.decode(b64, Base64.NO_WRAP), "AES")

    fun aesGcmEncrypt(secretKey: SecretKey, plaintext: ByteArray): EncryptedPayload {
        val iv = ByteArray(IV_SIZE).also { Random.nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_LEN, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)
        val cipherBytes = cipher.doFinal(plaintext)
        return EncryptedPayload(
            ciphertextB64 = Base64.encodeToString(cipherBytes, Base64.NO_WRAP),
            ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        )
    }

    fun aesGcmDecrypt(secretKey: SecretKey, ciphertextB64: String, ivB64: String): ByteArray {
        val cipherBytes = Base64.decode(ciphertextB64, Base64.NO_WRAP)
        val iv = Base64.decode(ivB64, Base64.NO_WRAP)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_LEN, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        return cipher.doFinal(cipherBytes)
    }

    // -------------------- EncryptedSharedPreferences helpers for storing per-chat AES keys --------------------
    // Store AES key bytes (base64) in EncryptedSharedPreferences under "chat_keys" pref
    fun getEncryptedPrefs(context: Context): androidx.security.crypto.EncryptedSharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            "chat_keys_pref",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    fun storeChatKey(context: Context, chatId: String, aesKey: SecretKey) {
        val prefs = getEncryptedPrefs(context)
        prefs.edit().putString(chatId, aesKeyToBase64(aesKey)).apply()
    }

    fun getChatKey(context: Context, chatId: String): SecretKey? {
        val prefs = getEncryptedPrefs(context)
        val b64 = prefs.getString(chatId, null) ?: return null
        return aesKeyFromBase64(b64)
    }
}
