package de.atroo.backgroundrunnerng.runnerengine

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.whl.quickjs.wrapper.JSObject
import org.json.JSONException
import org.json.JSONObject
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

import com.whl.quickjs.wrapper.JSArray
import com.whl.quickjs.wrapper.QuickJSContext
import com.getcapacitor.JSObject as CapJSObject
import com.getcapacitor.JSArray as CapJSArray
import kotlin.reflect.full.functions

val TAG = "Utils"

fun convertToMap(json: JSONObject): Map<Int, JSONObject> {
    val map = mutableMapOf<Int, JSONObject>()
    val keys = json.keys()
    while (keys.hasNext()) {
        val key = keys.next()
        try {
            val keyInt = key.toInt()
            val value = json.getJSONObject(key)
            map[keyInt] = value
        } catch (e: NumberFormatException) {
            continue
        } catch (e: JSONException) {
            continue
        }
    }
    return map
};

fun delayedCall(delayMillis: Long, function: () -> Unit) {
    Handler(Looper.getMainLooper()).postDelayed({
        function()
    }, delayMillis)
}

inline fun <reified T> getPrivateMember(instance: Any, memberName: String): T? {
    return instance::class.declaredMemberProperties.find { it.name == memberName }?.let {
        it.isAccessible = true
        it.getter.call(instance) as? T
    }
}

fun jsObject2CapJSObject(jsObject: JSObject): CapJSObject {
    val capJSObject = CapJSObject()

    Log.d(TAG, "jsObject2CapJSObject: ${jsObject.toString()}")
    val names = jsObject.getNames()
    for (i in 0 until names.length()) {
        val key = names.get(i)
        val value = jsObject.getProperty(key as String)
        Log.d(TAG, "pair: $key, ${value}")
    }
    for (i in 0 until names.length()) {
        val name = names.get(i) as String
        val value = jsObject.getProperty(name)

        when {
            value is String -> capJSObject.put(name, value)
            value is Int -> capJSObject.put(name, value)
            value is Double -> capJSObject.put(name, value)
            value is Boolean -> capJSObject.put(name, value)
            value is JSObject && jsObject.getJSArray(name) != null -> capJSObject.put(name, jsArray2CapJSArray(jsObject.getJSArray(name)))
            value is JSObject -> capJSObject.put(name, jsObject2CapJSObject(value))
            else -> capJSObject.put(name, value.toString())
        }
    }

    return capJSObject
}

fun jsArray2CapJSArray(jsArray: JSArray): CapJSArray {
    val capJSArray = CapJSArray()
    for (i in 0 until jsArray.length()) {
        val value = jsArray.get(i)

        when {
            value is String -> capJSArray.put(value)
            value is Int -> capJSArray.put(value)
            value is Double -> capJSArray.put(value)
            value is Boolean -> capJSArray.put(value)
            value is JSObject -> capJSArray.put(jsObject2CapJSObject(value))
            value is JSArray -> capJSArray.put(jsArray2CapJSArray(value))
            else -> capJSArray.put(value.toString())
        }
    }
    return capJSArray
}

fun cap2JSObject(context: QuickJSContext, map: CapJSObject): JSObject {
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

fun cap2JSArray(context: QuickJSContext, capArr: CapJSArray): JSArray {
    val jsArray = context.createNewJSArray()
    for (ind in 0 until capArr.length()) {
        val value = capArr.get(ind)
        when (value) {
            is String -> appendJSArray(context, jsArray, value)
            is Int -> appendJSArray(context, jsArray, value)
            is Double -> appendJSArray(context, jsArray, value)
            is Boolean -> appendJSArray(context, jsArray, value)
            is CapJSObject -> appendJSArray(context, jsArray, cap2JSObject(context, value as CapJSObject))
            is CapJSArray -> appendJSArray(context, jsArray, cap2JSArray(context, value as CapJSArray))
            else -> appendJSArray(context, jsArray, value)
        }
    }
    return jsArray
}

fun appendJSArray(context: QuickJSContext, jsArray: JSArray, value: Any) {
    when (value) {
        is String -> jsArray.set(value, jsArray.length())
        is Int -> jsArray.set(value, jsArray.length())
        is Double -> jsArray.set(value, jsArray.length())
        is Boolean -> jsArray.set(value, jsArray.length())
        is Map<*, *> -> jsArray.set(map2JSObject(context, value as Map<String, Any>), jsArray.length())
        is List<*> -> jsArray.set(list2JSArray(context, value as List<Any>), jsArray.length())
        else -> jsArray.set(value.toString(), jsArray.length())
    }
}

fun map2JSObject(context: QuickJSContext, map: Map<String, Any>): JSObject {
    val jsObject = context.createNewJSObject()
    for ((key, value) in map) {
        when (value) {
            is String -> jsObject.setProperty(key, value)
            is Int -> jsObject.setProperty(key, value)
            is Double -> jsObject.setProperty(key, value)
            is Boolean -> jsObject.setProperty(key, value)
            is Map<*, *> -> jsObject.setProperty(key, map2JSObject(context, value as Map<String, Any>))
            is List<*> -> jsObject.setProperty(key, list2JSArray(context, value as List<Any>))
            else -> jsObject.setProperty(key, value.toString())
        }
    }
    return jsObject
}

fun list2JSArray(context: QuickJSContext, list: List<Any>): JSArray {
    val jsArray = context.createNewJSArray()
    for ((index, value) in list.withIndex()) {
        when (value) {
            is String -> appendJSArray(context, jsArray, value)
            is Int -> appendJSArray(context, jsArray, value)
            is Double -> appendJSArray(context, jsArray, value)
            is Boolean -> appendJSArray(context, jsArray, value)
            is Map<*, *> -> appendJSArray(context, jsArray, map2JSObject(context, value as Map<String, Any>))
            is List<*> -> appendJSArray(context, jsArray, list2JSArray(context, value as List<Any>))
            else -> appendJSArray(context, jsArray, value)
        }
    }
    return jsArray
}

fun callSqliteMethod(sqlite: Any, methodName: String, capObj: CapJSObject): CapJSObject? {
    val method = sqlite::class.functions.find { it.name == methodName }

    if (method != null) {
        method.isAccessible = true
        Log.d(TAG, "Calling method $methodName")
        val res = method.call(sqlite, capObj) as? CapJSObject
        Log.d(TAG, "the res ${res}")
        return res
    } else {
        println("Method $methodName not found")
        return null
    }
}