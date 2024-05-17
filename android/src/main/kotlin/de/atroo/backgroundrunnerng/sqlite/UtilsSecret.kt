package de.atroo.backgroundrunnerng.sqlite

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import java.io.File

class UtilsSecret(private val context: Context, private val sharedPreferences: SharedPreferences) {
    companion object {
        private val TAG = UtilsFile::class.java.name
        fun getPassphrase(sharedPreferences: SharedPreferences): String =
            sharedPreferences.getString("secret", "") ?: ""

        fun isPassphrase(sharedPreferences: SharedPreferences): Boolean =
            getPassphrase(sharedPreferences).isNotEmpty()
    }

    private val uFile = UtilsFile()
    private val globVar = GlobalSQLite()
    private val uCipher = UtilsSQLCipher()

    @Throws(Exception::class)
    fun setEncryptionSecret(passphrase: String) {
        if (TextUtils.isEmpty(passphrase)) {
            throw Exception("passphrase must not be empty")
        }
        // Test if Encryption secret is already set
        val savedPassPhrase = getPassphrase(sharedPreferences)
        if (savedPassPhrase.isNotEmpty()) {
            throw Exception("a passphrase has already been set")
        }
        // Store encrypted passphrase in sharedPreferences
        setPassphrase(sharedPreferences, passphrase)

        // Get the list of databases
        val dbList = uFile.getListOfFiles(context)
        if (dbList.isNotEmpty()) {
            dbList.forEach { dbName ->
                val file = context.getDatabasePath(dbName)
                val state = uCipher.getDatabaseState(context, file, sharedPreferences, globVar)
                // Change password if encrypted with globVar.secret
                if (state == UtilsSQLCipher.State.ENCRYPTED_GLOBAL_SECRET) {
                    uCipher.changePassword(context, file, globVar.secret, passphrase)
                } else if (state == UtilsSQLCipher.State.DOES_NOT_EXIST || state == UtilsSQLCipher.State.UNKNOWN) {
                    throw Exception("State for: $dbName not correct")
                }
            }
        }
    }

    @Throws(Exception::class)
    fun changeEncryptionSecret(passphrase: String, oldPassphrase: String) {
        if (TextUtils.isEmpty(passphrase) || TextUtils.isEmpty(oldPassphrase)) {
            throw Exception("Passphrase and/or old passphrase must not be empty")
        }
        val secret = getPassphrase(sharedPreferences)
        if (secret.isEmpty()) {
            throw Exception("Encryption secret has not been set")
        } else if (secret != oldPassphrase) {
            throw Exception("Old passphrase is wrong secret")
        }

        val dbList = uFile.getListOfFiles(context)
        if (dbList.isNotEmpty()) {
            dbList.forEach { dbName ->
                val file = context.getDatabasePath(dbName)
                val state = uCipher.getDatabaseState(context, file, sharedPreferences, globVar)
                if (state == UtilsSQLCipher.State.ENCRYPTED_SECRET) {
                    uCipher.changePassword(context, file, oldPassphrase, passphrase)
                } else {
                    throw Exception("State for: $dbName not correct")
                }
            }
        }
        setPassphrase(sharedPreferences, passphrase)
    }

    @Throws(Exception::class)
    fun clearEncryptionSecret() {
        val savedPassPhrase = getPassphrase(sharedPreferences)
        if (savedPassPhrase.isNotEmpty()) {
            clearPassphrase(sharedPreferences)
        }
    }

    @Throws(Exception::class)
    fun checkEncryptionSecret(passphrase: String): Boolean {
        if (TextUtils.isEmpty(passphrase)) {
            throw Exception("passphrase must not be empty")
        }
        val savedPassPhrase = getPassphrase(sharedPreferences)
        if (savedPassPhrase.isEmpty()) {
            throw Exception("no passphrase stored in sharedPreferences")
        }
        return savedPassPhrase == passphrase
    }

    private fun setPassphrase(sharedPreferences: SharedPreferences, passphrase: String) {
        sharedPreferences.edit().putString("secret", passphrase).apply()
    }

    private fun clearPassphrase(sharedPreferences: SharedPreferences) {
        sharedPreferences.edit().remove("secret").apply()
    }
}
