package de.atroo.backgroundrunnerng.sqlite

import android.util.Log
import com.whl.quickjs.wrapper.JSCallFunction
import com.whl.quickjs.wrapper.JSObject
import com.whl.quickjs.wrapper.QuickJSContext
import de.atroo.backgroundrunnerng.runnerengine.convertToMap
import org.json.JSONArray
import org.json.JSONObject

class CapacitorSQLite(val context: QuickJSContext, var sqlite: SQLite) {
    val TAG = "CapacitorSQLite"
    val mainSQLite: JSObject
    init {
        mainSQLite = context.createNewJSObject();
        setupCreateConnectionFun(mainSQLite)

        val globalObj: JSObject = context.getGlobalObject();
        globalObj.setProperty("sqliteNative", mainSQLite);
    }

    fun setupCreateConnectionFun(obj: JSObject) {
        Log.d(TAG, "setupCreateConnectionFun...")
        val createConnectionFun = JSCallFunction { args ->
            Log.d(TAG, "createConnectionFun..." + args::class.java.simpleName)
            for (element in args) {
                println((element as JSObject).getNames().toString())
            }
            val obj = args[0] as JSObject
            val dbName = obj.getString("dbName")
            val encrypted = obj.getBoolean("encrypted")
            val mode = obj.getString("mode")
            val version = obj.getInteger("version")
            val vUpgMap = mutableMapOf<Int, JSONObject>()
            val readonly = obj.getBoolean("readonly")
            Log.d(TAG, "createConnectionFun... dbName: $dbName, encrypted: $encrypted, mode: $mode, version: $version, vUpgMap: $vUpgMap, readonly: $readonly")
            sqlite.createConnection(dbName, encrypted, mode, version, vUpgMap, readonly)
            Log.d(TAG, "createConnectionFun... done")
        }
        obj.setProperty("createConnection", createConnectionFun)
    }

    fun setSharedPreferences() {
        sqlite.setSharedPreferences()
    }

}