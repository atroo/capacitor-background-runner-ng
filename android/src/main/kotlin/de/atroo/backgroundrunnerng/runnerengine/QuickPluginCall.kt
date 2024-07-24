package de.atroo.backgroundrunnerng.runnerengine

import android.util.Log
import com.getcapacitor.JSObject
import com.getcapacitor.MessageHandler
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginResult

class QuickPluginCall(
    msgHandler: MessageHandler,
    pluginId: String,
    callbackId: String,
    methodName: String,
    data: JSObject
) : PluginCall(msgHandler, pluginId, callbackId, methodName, data) {
    var retValue: JSObject? = null

    companion object {
        private const val TAG = "QuickPluginCall"
    }

    override fun successCallback(successResult: PluginResult) {
        Log.d(TAG, "successCallback: $successResult")
    }

    override fun resolve(data: JSObject) {
        Log.d(TAG, "resolve1...$data")
        retValue = data
    }

    override fun resolve() {
        Log.d(TAG, "resolve2: ${this.data}")
        retValue = JSObject()
    }

    override fun errorCallback(msg: String) {
    }

    override fun reject(msg: String, code: String?, ex: Exception?, data: JSObject?) {
        Log.d(TAG, "reject: $msg $code $ex $data")
        retValue = JSObject()
        val errorValue = JSObject()
        errorValue.put("message", msg)
        errorValue.put("message", msg)
        errorValue.put("code", code)
        errorValue.put("exception", ex?.message)
        errorValue.put("data", data)
        retValue?.put("error", errorValue)
    }
}
