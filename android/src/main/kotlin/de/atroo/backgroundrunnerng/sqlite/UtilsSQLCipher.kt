package de.atroo.backgroundrunnerng.sqlite

import android.content.Context
import android.content.SharedPreferences
import net.sqlcipher.database.SQLiteDatabase
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class UtilsSQLCipher {

    companion object {
        private val TAG = UtilsSQLCipher::class.java.name
    }

    enum class State {
        DOES_NOT_EXIST,
        UNENCRYPTED,
        ENCRYPTED_SECRET,
        ENCRYPTED_GLOBAL_SECRET,
        UNKNOWN
    }

    fun getDatabaseState(context:Context, dbPath: File, sharedPreferences: SharedPreferences, globVar: GlobalSQLite): State {
        SQLiteDatabase.loadLibs(context)
        if (!dbPath.exists()) return State.DOES_NOT_EXIST

        try {
            SQLiteDatabase.openDatabase(dbPath.absolutePath, "", null, SQLiteDatabase.OPEN_READONLY).use { db ->
                db.version
                return State.UNENCRYPTED
            }
        } catch (e: Exception) {
            sharedPreferences.getString("secret", "")?.let { passphrase ->
                if (passphrase.isNotEmpty()) {
                    try {
                        SQLiteDatabase.openDatabase(dbPath.absolutePath, passphrase, null, SQLiteDatabase.OPEN_READONLY).use { db ->
                            db.version
                            return State.ENCRYPTED_SECRET
                        }
                    } catch (ignored: Exception) { }
                }
            }

            globVar.secret.takeIf { it.isNotEmpty() }?.let { globalSecret ->
                try {
                    SQLiteDatabase.openDatabase(dbPath.absolutePath, globalSecret, null, SQLiteDatabase.OPEN_READONLY).use { db ->
                        db.version
                        return State.ENCRYPTED_GLOBAL_SECRET
                    }
                } catch (ignored: Exception) { }
            }
        }
        return State.UNKNOWN
    }

    @Throws(IOException::class)
    fun encrypt(context:Context, originalFile: File, passphrase: ByteArray) {
        SQLiteDatabase.loadLibs(context)

        if (!originalFile.exists()) throw FileNotFoundException("${originalFile.absolutePath} not found")

        val newFile = File.createTempFile("sqlcipherutils", "tmp", context.cacheDir)
        SQLiteDatabase.openDatabase(originalFile.absolutePath, "", null, SQLiteDatabase.OPEN_READWRITE).use { db ->
            val version = db.version

            SQLiteDatabase.openDatabase(newFile.absolutePath, passphrase, null, SQLiteDatabase.OPEN_READWRITE, null, null).use { newDb ->
                newDb.rawExecSQL("ATTACH DATABASE '${originalFile.absolutePath}' AS plaintext KEY '';")
                newDb.rawExecSQL("SELECT sqlcipher_export('plaintext');")
                newDb.rawExecSQL("DETACH DATABASE plaintext;")

                newDb.version = version
            }
        }

        if (!originalFile.delete()) throw FileNotFoundException("${originalFile.absolutePath} not deleted")
        if (!newFile.renameTo(originalFile)) throw FileNotFoundException("${originalFile.absolutePath} not renamed")
    }

    @Throws(IOException::class)
    fun decrypt(context:Context, originalFile: File, passphrase: ByteArray) {
        SQLiteDatabase.loadLibs(context)

        if (!originalFile.exists()) throw FileNotFoundException("${originalFile.absolutePath} not found")

        val decryptedFile = File.createTempFile("sqlcipherutils", "tmp", context.cacheDir)
        SQLiteDatabase.openDatabase(originalFile.absolutePath, String(passphrase), null, SQLiteDatabase.OPEN_READWRITE).use { encryptedDb ->
            val version = encryptedDb.version

            SQLiteDatabase.openDatabase(decryptedFile.absolutePath, "", null, SQLiteDatabase.OPEN_READWRITE).use { decryptedDb ->
                decryptedDb.rawExecSQL("ATTACH DATABASE '${originalFile.absolutePath}' AS encrypted KEY '${String(passphrase)}';")
                decryptedDb.rawExecSQL("SELECT sqlcipher_export('encrypted');")
                decryptedDb.rawExecSQL("DETACH DATABASE encrypted;")

                decryptedDb.version = version
            }
        }

        if (!originalFile.delete()) throw FileNotFoundException("${originalFile.absolutePath} not deleted")
        if (!decryptedFile.renameTo(originalFile)) throw FileNotFoundException("${originalFile.absolutePath} not renamed")
    }

    fun changePassword(context:Context, file: File, password: String, newPassword: String) {
        SQLiteDatabase.loadLibs(context)

        if (!file.exists()) throw FileNotFoundException("${file.absolutePath} not found")

        SQLiteDatabase.openDatabase(file.absolutePath, password, null, SQLiteDatabase.OPEN_READWRITE).use { db ->
            db.changePassword(newPassword)
        }
    }
}
