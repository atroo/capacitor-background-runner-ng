package de.atroo.backgroundrunnerng.runnerengine

import android.util.Log
import com.getcapacitor.JSExport
import com.getcapacitor.PluginHandle
import com.whl.quickjs.android.QuickJSLoader
import com.whl.quickjs.wrapper.JSCallFunction
import com.whl.quickjs.wrapper.JSFunction
import com.whl.quickjs.wrapper.JSObject
import com.whl.quickjs.wrapper.QuickJSContext
import java.util.*
import kotlin.concurrent.schedule
import com.getcapacitor.JSArray as CapJSArray
import com.getcapacitor.JSObject as CapJSObject


class JSRuntime(val androidContext: android.content.Context, val name: String) {
    private val TAG = "Context"
    private val timers = mutableMapOf<Int, Timer>()
    private var nextTimerId = 0
    val context: QuickJSContext = QuickJSContext.create()
    protected val globalObject: JSObject

    init {
        globalObject = context.getGlobalObject()
        val eventListenersObject = context.createNewJSObject()
        globalObject.setProperty("eventListeners", eventListenersObject)
        setupWebAPI()
    }

    fun execute(code: String): Any? {
        return try {
            context.evaluate(code) ///??? What to do with the result?
        } catch (ex: Exception) {
            throw EngineError.JsException("JavaScript error: ${ex.message}")
        }
    }

    @Throws(EngineError.JsException::class)
    fun dispatchEvent(event: String, details: JSObject? = null) {
        Log.d(TAG, "dispatchEvent: $event")
        val events = globalObject.getProperty("eventListeners") as? JSObject
        if (events == null) {
            Log.d(TAG, "addEventListener1: events is null")
        }

        val func = events?.getProperty(event) as? JSFunction
        if (func == null) {
            Log.d(TAG, "dispatchEvent: func is null")
        }

        val resolveFunc = object : JSCallFunction {
            override fun call(vararg args: Any?): Any? {
                Log.d(TAG, "resolveFunc: $args")
                val b = StringBuilder()
                for (o in args) {
                    b.append(o?.toString() ?: "null")
                }

                Log.d(TAG, b.toString())
                return null
            }
        }

        val rejectFunc = object : JSCallFunction {
            override fun call(vararg args: Any?): Any? {
                Log.d("tiny-console", "rejectFunc: $args")
                val b = StringBuilder()
                for (o in args) {
                    b.append(o?.toString() ?: "null")
                }

                Log.d("tiny-console", b.toString())
                return null
            }
        }

        globalObject.setProperty("resolve", resolveFunc)
        globalObject.setProperty("reject", rejectFunc)
        globalObject.setProperty("args", details)

        func?.call(resolveFunc, rejectFunc, details)
    }

    private fun setupWebAPI() {
        Log.d(TAG, "setupWebAPI...")
        QuickJSLoader.initConsoleLog(context)
        setupAddEventListener()
    }

    private fun addEventListener(eventName: String, callback: JSFunction) {
        Log.d(TAG, "addEventListener2: $eventName")
        val events = globalObject.getProperty("eventListeners") as? JSObject
        if (events != null) {
            events.setProperty(eventName, callback)
        } else {
            Log.d(TAG, "addEventListener3: events is null")
        }
    }

    private fun setupAddEventListener(): Unit {
        Log.d(TAG, "setupAddEventListener...")
        globalObject.setProperty("addEventListener") { args ->
            val name = args[0] as String
            val eventFunc = args[1] as JSFunction
            Log.d(TAG, "registering event: $name")
            addEventListener(name, eventFunc)
            null
        }
    }

    private fun initTimer(callback: (Array<Any?>) -> Unit, timeout: Int, isInterval: Boolean): Int {
        val timerId = nextTimerId++
        val timer = Timer().apply {
            schedule(if (isInterval) 0L else timeout.toLong(), timeout.toLong()) {
                callback(arrayOf())
                if (!isInterval) {
                    timers.remove(timerId)?.cancel()
                }
            }
        }
        timers[timerId] = timer
        return timerId
    }

    private fun setTimeout(callback: (Array<Any?>) -> Unit, timeout: Int): Int {
        return initTimer(callback, timeout, false)
    }

    private fun setInterval(callback: (Array<Any?>) -> Unit, timeout: Int): Int {
        return initTimer(callback, timeout, true)
    }

    private fun clearTimeout(id: Int) {
        timers.remove(id)?.cancel()
    }

    /**
     * Build the JSInjector that will be used to inject JS into files served to the app,
     * to ensure that Capacitor's JS and the JS for all the plugins is loaded each time.
     */
    fun setupCapApi(plugins: Collection<PluginHandle>, isLoggingEnabled: Boolean, isDevMode: Boolean) {
        try {
            val globalJS: String =
                JSExport.getGlobalJS(androidContext, isLoggingEnabled, isDevMode)

            // native-bridge must be replaced with our adapted impl
            val bridgeJS: String = JSExport.getBridgeJS(androidContext)
            val pluginJS: String = JSExport.getPluginJS(plugins)
            //val cordovaJS: String = JSExport.getCordovaJS(androidContext)
            //val cordovaPluginsJS: String = JSExport.getCordovaPluginJS(androidContext)
            //val cordovaPluginsFileJS: String = JSExport.getCordovaPluginsFileJS(androidContext)
            //val localUrlJS = "window.WEBVIEW_SERVER_URL = '$localUrl';"

            val js = (
                    globalJS +
                    "\n\n" +
                   // localUrlJS +
                    "\n\n" +
                    bridgeJS +
                    "\n\n" +
                    pluginJS +
                    "\n\n" +
                    //cordovaJS +
                    "\n\n" +
                    //cordovaPluginsFileJS +
                    "\n\n" // +
                    //cordovaPluginsJS
                )
            context.evaluate(js)
        } catch (ex: Exception) {
            Log.e(TAG,"Unable to export JS. App will not function!", ex)
        }
    }

    fun cap2JSObject(map: CapJSObject): JSObject {
        val jsObject = context.createNewJSObject()
        for (key in map.keys()) {
            when (val value = map.get(key)) {
                is String -> jsObject.setProperty(key, value)
                is Int -> jsObject.setProperty(key, value)
                is Double -> jsObject.setProperty(key, value)
                is Boolean -> jsObject.setProperty(key, value)
                is CapJSObject -> jsObject.setProperty(key, cap2JSObject(context, value as CapJSObject))
                is CapJSArray -> jsObject.setProperty(key, cap2JSArray(context, value as CapJSArray))
                else -> jsObject.setProperty(key, value.toString())
            }
        }
        return jsObject
    }
}

sealed class EngineError(message: String) : Exception(message) {
    class JsException(details: String) : EngineError("JavaScript Exception: $details")
}
