package de.atroo.backgroundrunnerng.sqlite

import android.content.Context;
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey;
import com.getcapacitor.PluginCall;
import org.json.JSONObject

class SQLite(private val context: Context, val config: SQLiteConfig) {

    companion object {
        private val TAG = SQLite::class.java.name
    }

    private lateinit var c: MasterKey

    private lateinit var masterKeyAlias: MasterKey
    private val isEncryption: Boolean
    private val dbDict: MutableMap<String, Database> = mutableMapOf()


    private lateinit var sharedPreferences: SharedPreferences;
    private lateinit var uSecret: UtilsSecret

    private var call: PluginCall? = null

    init {
        isEncryption = config.isEncryption
        try {
            if (isEncryption) {
                masterKeyAlias = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                setSharedPreferences()
            }
        } catch (e: Exception) {
            throw RuntimeException(e.message)
        }
    }

    // fun newConnection(): SQLiteConnection {
    //     val connection = SQLiteConnection(this)
    //     return connection
    // }

    @Throws(Exception::class)
    fun setSharedPreferences() {
        try {
            // Initialize EncryptedSharedPreferences
            sharedPreferences = EncryptedSharedPreferences.create(
                context,
                "sqlite_encrypted_shared_prefs",
                masterKeyAlias,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            uSecret = UtilsSecret(context, sharedPreferences!!)
        } catch (e: Exception) {
            throw Exception(e.message)
        }
    }

    /**
    * CreateConnection
    *
    * @param dbName database name
    * @param encrypted boolean
    * @param mode  "no-encryption", "secret", "encryption"
    * @param version database version
    * @param vUpgObject upgrade Object
    * @throws Exception message
    */
    @Throws(Exception::class)
    fun createConnection(
        dbName: String,
        encrypted: Boolean,
        mode: String,
        version: Int,
        vUpgObject: Map<Int, JSONObject>,
        readonly: Boolean
    ) {
        var dbName = getDatabaseName(dbName)
        val connName = if (readonly) "RO_$dbName" else "RW_$dbName"
        // check if connection already exists
        val conn: Database? = dbDict[connName]
        if (conn != null) {
            val msg = "Connection $dbName already exists"
            throw Exception(msg)
        }
        if (encrypted && !isEncryption) {
            throw Exception("Database cannot be encrypted as 'No Encryption' set in capacitor.config")
        }
        try {
            val db = Database(
                context = context,
                dbName = "$dbName.SQLite.db",
                encrypted = encrypted,
                mode = mode,
                version = version,
                isEncryption = isEncryption,
                vUpgObject = vUpgObject,
                sharedPreferences = sharedPreferences,
                readOnly = readonly
            )
            dbDict[connName] = db
        } catch (e: Exception) {
            throw Exception(e.message)
        }
    }

    private fun notifyBiometricEvent(success: Boolean, message: String?) {
        
    }

    private fun getDatabaseName(dbName: String): String {
        var retName = dbName
        if (!retName.contains("/")) {
            if (retName.endsWith(".db")) {
                retName = retName.dropLast(3)
            }
        }
        return retName
    }

}
