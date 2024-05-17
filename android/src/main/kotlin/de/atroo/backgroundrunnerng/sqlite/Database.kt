package de.atroo.backgroundrunnerng.sqlite

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteStatement
import com.getcapacitor.JSArray
import com.getcapacitor.JSObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteException
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class Database(
    private val context: Context,
    private val dbName: String,
    private val encrypted: Boolean,
    private val mode: String,
    private val version: Int,
    private val isEncryption: Boolean,
    private val vUpgObject: Map<Int, JSONObject>,
    private val sharedPreferences: SharedPreferences,
    private val readOnly: Boolean
) {
    private val file: File = if (dbName.contains("/") && dbName.endsWith("SQLite.db")) {
        File(dbName)
    } else {
        context.getDatabasePath(dbName)
    }
    private val uSecret = if (isEncryption) UtilsSecret(context, sharedPreferences) else null
    private var db: SupportSQLiteDatabase? = null
    private var isOpen = false
    private var ncDB = false
    private var isAvailableTransaction = false

    init {
        initializeSQLCipher()
        if (!file.parentFile!!.exists() && !file.parentFile!!.mkdirs()) {
            Log.e(TAG, "Failed to create parent directories.")
        }
        Log.v(TAG, "&&& file path ${file.absolutePath}")
    }

    private fun initializeSQLCipher() {
        SQLiteDatabase.loadLibs(context)
    }

    fun getDb(): SupportSQLiteDatabase? = db

    fun isOpen(): Boolean = isOpen

    fun isNCDB(): Boolean = ncDB

    fun isAvailTrans(): Boolean = db?.inTransaction() ?: false

    @Throws(Exception::class)
    fun beginTransaction(): Int {
        db?.let {
            if (it.isOpen) {
                if (isAvailTrans()) throw Exception("Already in transaction")
                it.beginTransaction()
                return 0
            }
        }
        throw Exception("Database not opened")
    }

    @Throws(Exception::class)
    fun commitTransaction(): Int {
        db?.let {
            if (it.isOpen) {
                if (!isAvailTrans()) throw Exception("No transaction active")
                it.setTransactionSuccessful()
                it.endTransaction()
                return 0
            }
        }
        throw Exception("Database not opened")
    }

    @Throws(Exception::class)
    fun rollbackTransaction(): Int {
        db?.let {
            if (it.isOpen) {
                if (isAvailTrans()) {
                    it.endTransaction()
                    return 0
                }
            }
        }
        throw Exception("Database not opened")
    }

    fun getUrl(): String = "file://${file.absolutePath}"

    @Throws(Exception::class)
    fun close() {
        db?.let {
            if (it.isOpen) {
                it.close()
                isOpen = false
            } else throw Exception("Database not opened")
        } ?: throw Exception("Database not opened")
    }

    @Throws(Exception::class)
    fun getVersion(): Int {
        db?.let {
            if (it.isOpen) {
                return it.version
            }
        }
        throw Exception("Database not opened")
    }

    fun isDBExists(): Boolean = file.exists()

    companion object {
        private val TAG = Database::class.java.name
    }
}
