package de.atroo.backgroundrunnerng.runnerengine

import com.whl.quickjs.wrapper.JSObject
import org.json.JSONException
import org.json.JSONObject

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

//fun convertToMap(jsObject: JSObject): Map<Int, JSONObject> {
//    val map = mutableMapOf<Int, JSONObject>()
//    val keys = jsObject.keys()
//
//    while (keys.hasNext()) {
//        val key = keys.next()
//        try {
//            val keyInt = key.toInt()  // Convert the key to an integer
//            val value = jsObject.getJSObject(key) // Retrieves a JSObject which is basically a JSONObject
//            map[keyInt] = value
//        } catch (e: NumberFormatException) {
//            // Handle the case where the key is not an integer or conversion fails
//            continue
//        } catch (e: Exception) {
//            // Handle the case where the value is not a JSObject
//            continue
//        }
//    }
//    return map
//}

