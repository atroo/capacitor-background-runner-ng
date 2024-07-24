package de.atroo.backgroundrunnerng

import android.util.Log
import com.getcapacitor.Bridge
import com.getcapacitor.MessageHandler
import com.getcapacitor.PluginHandle
import com.getcapacitor.JSObject as CapJSObject
import com.whl.quickjs.wrapper.JSCallFunction
import com.whl.quickjs.wrapper.JSObject
import com.whl.quickjs.wrapper.QuickJSContext
import de.atroo.backgroundrunnerng.runnerengine.QuickPluginCall
import de.atroo.backgroundrunnerng.runnerengine.cap2JSObject
import de.atroo.backgroundrunnerng.runnerengine.getPrivateMember
import de.atroo.backgroundrunnerng.runnerengine.jsObject2CapJSObject

class QuickJSSQLiteAdapter(val context: QuickJSContext, val bridge: Bridge) {
    val TAG = "QuickJSSQLiteAdapter"
    val mainSQLite: JSObject
    private var msgHandler: MessageHandler? = null
    private lateinit var pluginHandle: PluginHandle
    init {
        Log.d(TAG, "QuickJSSQLiteAdapter...")
        mainSQLite = context.createNewJSObject()
//        setupCreateConnectionFun(mainSQLite)
        setupFun(mainSQLite, "createConnection")
        setupFun(mainSQLite, "closeConnection")
        setupFun(mainSQLite, "run")
        setupFun(mainSQLite, "query")
        setupFun(mainSQLite, "open")
        setupFun(mainSQLite, "close")


        val globalObj: JSObject = context.getGlobalObject()
        globalObj.setProperty("SQLiteNative", mainSQLite)
        pluginHandle = bridge.getPlugin("CapacitorSQLite")
        msgHandler = getPrivateMember<MessageHandler>(bridge, "msgHandler")
    }

//    fun setupCreateConnectionFun(obj: JSObject) {
//        Log.d(TAG, "setupCreateConnectionFun...")
//
//        val createConnectionFun = JSCallFunction { args ->
//            Log.d(TAG, "createConnectionFun..." + args::class.java.simpleName)
//            val capObj = jsObject2CapJSObject(args[0] as JSObject)
//            Log.d(TAG, "createConnectionFun...capObj: ${capObj.toString()}")
//            val ret = sqlite.createConnection(capObj)
//            Log.d(TAG, "createConnectionFun, return...")
//
//            val retObj: JSObject = cap2JSObject(context, ret ?: CapJSObject())
//
//            return@JSCallFunction retObj
//        }
//        obj.setProperty("createConnection", createConnectionFun)
//    }

    fun setupFun(obj: JSObject, methodName: String) {
        Log.d(TAG, "Setting up ${methodName}...")
        val callFun = JSCallFunction { args ->
            Log.d(TAG, "${methodName}...args: ${args.joinToString { it.toString() }}")
            val capObj = jsObject2CapJSObject(args[0] as JSObject)
            val ret = callFun(methodName, capObj)
//            val ret = callSqliteMethod(sqlite, methodName, capObj)
            if (ret != null) {
                Log.d(TAG, "${methodName}...ret: ${ret.toString()}")
                val retObj: JSObject = cap2JSObject(context, ret as CapJSObject ?: CapJSObject())
                return@JSCallFunction retObj
            }

            Log.d(TAG, "${methodName}...returning null")

            return@JSCallFunction null



        }
        obj.setProperty(methodName, callFun)
    }

    @Throws(Exception::class)
    fun callFun(
        fnName: String,
        capJson: CapJSObject
    ): CapJSObject? {
        Log.d(TAG, "${fnName}...")
        val callbackId = "theCallbackId"

        if (msgHandler == null) {
            Log.d(TAG, "${fnName}...msHandler is null")
            return null
        }

        val call = QuickPluginCall(msgHandler!!, "CapacitorSQLite", callbackId, fnName, capJson)
        pluginHandle.invoke(fnName,  call)

        return call.retValue
    }

//    fun setSharedPreferences() {
//        sqlite.setSharedPreferences()
//    }

}