package de.atroo.backgroundrunnerng.runnerengine

import android.util.Log
import com.whl.quickjs.android.QuickJSLoader
import com.whl.quickjs.wrapper.JSCallFunction
import com.whl.quickjs.wrapper.JSFunction
import com.whl.quickjs.wrapper.JSObject
import com.whl.quickjs.wrapper.QuickJSContext
import de.atroo.backgroundrunnerng.readAssetFile
import com.getcapacitor.JSObject as CapJSObject
import com.getcapacitor.JSArray as CapJSArray

import java.util.*
import kotlin.concurrent.schedule

abstract class Context(val androidContext: android.content.Context, val name: String) {
    private val TAG = "Context"
    private val timers = mutableMapOf<Int, Timer>()
    private var nextTimerId = 0
    protected val context: QuickJSContext
    protected val globalObject: JSObject

    init {
        context = QuickJSContext.create()
        globalObject = context.getGlobalObject()
        val eventListenersObject = context.createNewJSObject()
        globalObject.setProperty("eventListeners", eventListenersObject)
        setupWebAPI()
        setupModule()
    }

    abstract fun setupCapacitorAPI()

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

    fun setupModule() {
        Log.d(TAG, "setupModule...")
        try {
            Log.d(TAG, "evaluating sqliteplugin.js")
            val sqlitePluginSrc = readAssetFile(androidContext, "", "sqliteplugin.js")
            context.evaluate(sqlitePluginSrc)
            val sqliteProxySrc = readAssetFile(androidContext, "", "sqliteproxy.js")
            context.evaluate(sqliteProxySrc)
            Log.d(TAG, "evaluating sqliteproxy.js END")
        } catch (e: Exception) {
            Log.e(TAG, "Error evaluating script", e)
        }

        Log.d(TAG, "setupModule END...")
    }

    fun cap2JSObject(map: CapJSObject): JSObject {
        val jsObject = context.createNewJSObject()
        for (key in map.keys()) {
            val value = map.get(key)
            when (value) {
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
