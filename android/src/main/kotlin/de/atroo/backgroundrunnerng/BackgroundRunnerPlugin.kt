package de.atroo.backgroundrunnerng;

import android.Manifest
import android.content.Context
import android.util.Log
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import com.getcapacitor.annotation.Permission
import com.getcapacitor.annotation.PermissionCallback
import com.whl.quickjs.android.QuickJSLoader
import de.atroo.backgroundrunnerng.sqlite.SQLite
import de.atroo.backgroundrunnerng.sqlite.SQLiteConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.json.JSONException
import org.json.JSONObject

@CapacitorPlugin(
    name = "BackgroundRunner",
    permissions = [
        Permission(
            strings = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION],
            alias = BackgroundRunnerPlugin.GEOLOCATION
        ),
        Permission(
            strings = [Manifest.permission.POST_NOTIFICATIONS],
            alias = BackgroundRunnerPlugin.NOTIFICATIONS
        )
    ]
)
class BackgroundRunnerPlugin: Plugin() {
    private var impl: BackgroundRunner? = null
    private lateinit var config: SQLiteConfig
    private lateinit var sqliteImpl: SQLite

    companion object {
        const val GEOLOCATION = "geolocation"
        const val NOTIFICATIONS = "notifications"
        const val TAG = "BackgroundRunnerPlugin"
    }

    override fun handleOnPause() {
        super.handleOnPause()
        Log.d("Background Runner", "registering runner workers")
        val ctx: Context
        impl?.scheduleBackgroundTask(this.context)
    }

    override fun handleOnStop() {
        super.handleOnStop()
        Log.d("Background Runner", "shutting down foreground runner")
       impl?.shutdown()
    }

    override fun handleOnResume() {
        super.handleOnResume()
        Log.d("Background Runner", "starting foreground runner")
       impl?.start()
    }

    override fun load() {
        Log.d(TAG, "load...")
        super.load()
        config = getSqliteConfig()
        Log.d(TAG, "load, config, encryption: ${config.isEncryption}")
        QuickJSLoader.init()
        sqliteImpl = SQLite(this.context, config)
        impl = BackgroundRunner(this.context)
        impl?.start()
        val currentThread = Thread.currentThread()
        Log.d(TAG, "load, currentThread: ${currentThread.name}")
    }

    @PluginMethod
    override fun checkPermissions(call: PluginCall) {
        super.checkPermissions(call)
    }

    @PluginMethod
    override fun requestPermissions(call: PluginCall) {
        val apiToRequest = call.getArray("apis").toList<String>()
        super.requestPermissionForAliases(apiToRequest.toTypedArray(), call, "completePermissionsCallback")
    }

    @PermissionCallback
    fun completePermissionsCallback(call: PluginCall) {
        super.checkPermissions(call)
    }

    @PluginMethod
    fun dispatchEvent(call: PluginCall) {
        try {
            val impl = this.impl ?: throw Exception("background runner not initialized")

            val runnerEvent = call.getString("event") ?: throw Exception("event is missing or invalid")
            val detailsArg = call.getObject("details")
            val config = impl.config ?: throw Exception("no runner config loaded")

            val runningConfig = config.copy()
            runningConfig.event = runnerEvent

            val details = JSONObject(detailsArg.toString() ?: "{}")
            runBlocking(Dispatchers.IO) {
                try {
                    val returnData = impl.execute(
                        runningConfig,
                        detailsArg,
                        call.callbackId
                    )

                    if (returnData != null) {
                        call.resolve(JSObject.fromJSONObject(returnData))
                    } else {
                        call.resolve()
                    }
                } catch (ex: Exception) {
                    call.reject(ex.message)
                }
            }
        } catch(ex: Exception) {
            call.reject(ex.message)
        }
    }

    @PluginMethod
    fun registerBackgroundTask(call: PluginCall) {
        call.resolve()
    }

    @Throws(JSONException::class)
    private fun getSqliteConfig(): SQLiteConfig {
        val config = SQLiteConfig()
        val json = getConfig().configJSON
        val sqliteConfig = json.getJSONObject("sqlite")
        val aConfig = sqliteConfig.getJSONObject("android")
        val isEncryption = if (aConfig.has("androidIsEncryption")) aConfig.getBoolean("androidIsEncryption") else config.isEncryption
        config.isEncryption = isEncryption
        val androidBiometric = if (aConfig.has("androidBiometric")) aConfig.getJSONObject("androidBiometric") else null
        androidBiometric?.let {
            val biometricAuth = if (it.has("biometricAuth") && isEncryption) it.getBoolean("biometricAuth") else config.biometricAuth
            config.biometricAuth = biometricAuth
            val biometricTitle = if (it.has("biometricTitle")) it.getString("biometricTitle") else config.biometricTitle
            config.biometricTitle = biometricTitle
            val biometricSubTitle = if (it.has("biometricSubTitle")) it.getString("biometricSubTitle") else config.biometricSubTitle
            config.biometricSubTitle = biometricSubTitle
        }
        return config
    }

}