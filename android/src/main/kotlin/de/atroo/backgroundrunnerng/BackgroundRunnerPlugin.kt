package de.atroo.backgroundrunnerng;

import android.Manifest
import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import com.getcapacitor.JSObject
import com.getcapacitor.Logger
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginHandle
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import com.getcapacitor.annotation.Permission
import com.getcapacitor.annotation.PermissionCallback
import com.whl.quickjs.android.QuickJSLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
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
//    private lateinit var config: SQLiteConfig
    private var initialized: Boolean = false
    private lateinit var workerFactory: RunnerWorkerFactory

    companion object {
        const val GEOLOCATION = "geolocation"
        const val NOTIFICATIONS = "notifications"
        const val TAG = "BackgroundRunnerPlugin"
    }

    override fun handleOnPause() {
        super.handleOnPause()
        Log.d(TAG, "registering runner workers")
        val ctx: Context
        impl?.scheduleBackgroundTask(this.context)
    }

    override fun load() {
        Log.d(TAG, "load...")
        super.load()
        val currentThread = Thread.currentThread()
        Log.d(TAG, "load, currentThread: ${currentThread.name}")
    }

    override fun handleOnStart() {
        super.handleOnStart()
        Log.d(TAG,"handleOnStart...")

        if (!initialized) {
            Log.d(TAG, "BackgroundRunner init...")
            // Start our plugin execution threads and handlers
            workerFactory = RunnerWorkerFactory(bridge)
            val config = Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build()


            WorkManager.initialize(context, config)
            QuickJSLoader.init()
            this.impl = BackgroundRunner(this.context, this.bridge)
            val impl = this.impl
            impl?.start()

            initialized = true
        }

//        delayedCall(10000) {
//            val data = JSObject().apply {
//                put("database", "dbName2")
//                put("version", 1)
//                put("encrypted", false)
//                put("readonly", false)
//            }
//            val callbackId = "theCallbackId"
//            val call = PluginCall(msgHandler, "CapacitorSQLite", callbackId, "createConnection", data)
//            Log.d(TAG, "handleOnStart, invoking...")
//            handle.invoke("createConnection",  call)
//            Log.d(TAG, "handleOnStart, pluginId: $pluginId")
//        }
    }

    override fun handleOnStop() {
        super.handleOnStop()
        Log.d(TAG, "shutting down foreground runner")
       impl?.shutdown()
    }

    override fun handleOnResume() {
        super.handleOnResume()
        Log.d(TAG, "starting foreground runner")
       impl?.start()
    }

    private fun getRegisteredPlugins(): Map<String, PluginHandle> {
        val pluginIds = mutableListOf<String>()

        // Use reflection to access the private plugins field
        val bridgeClass = bridge.javaClass
        val pluginsField = bridgeClass.getDeclaredField("plugins")
        pluginsField.isAccessible = true

        // @Suppress("UNCHECKED_CAST")
        val plugins = pluginsField.get(bridge) as Map<String, PluginHandle>

        return plugins
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
}