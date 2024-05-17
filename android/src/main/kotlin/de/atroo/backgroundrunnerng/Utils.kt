package de.atroo.backgroundrunnerng

import android.content.Context
import android.content.res.AssetManager
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

fun readAssetFile(context: Context, folder: String, fileName: String): String {
    try {
        val assetManager: AssetManager = context.assets
        val inputStream: InputStream = assetManager.open("${folder}${fileName}")
        val reader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        var line: String? = reader.readLine()
        while (line != null) {
            stringBuilder.append(line)
            stringBuilder.append("\n")
            line = reader.readLine()
        }
        reader.close()
        return stringBuilder.toString()
    } catch (e: IOException) {
        e.printStackTrace()
        return ""
    }
}