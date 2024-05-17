package de.atroo.backgroundrunnerng.sqlite

import android.content.Context
import android.content.res.AssetManager
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class UtilsFile {
    companion object {
        private val TAG = UtilsFile::class.java.name

        private fun copyFileFromFile(sourceFile: File, destFile: File) {
            if (!destFile.parentFile.exists() && !destFile.parentFile.mkdirs()) {
                throw IOException("Failed to create directory")
            }
            if (!destFile.exists() && !destFile.createNewFile()) {
                throw IOException("Failed to create new file")
            }

            FileInputStream(sourceFile).use { fis ->
                FileOutputStream(destFile).use { fos ->
                    fis.channel.use { source ->
                        fos.channel.use { destination ->
                            destination.transferFrom(source, 0, source.size())
                        }
                    }
                }
            }
        }
    }

    fun isFileExists(context: Context, dbName: String): Boolean =
        context.getDatabasePath(dbName).exists()

    fun isPathExists(filePath: String): Boolean =
        File(filePath).exists()

    fun getDatabaseDirectoryPath(context: Context): String =
        context.getDatabasePath("x").parent ?: ""

    fun getListOfFiles(context: Context): Array<String> =
        context.databaseList().filter { it.endsWith("SQLite.db") }.toTypedArray()

    fun deleteDatabase(context: Context, dbName: String): Boolean =
        context.deleteDatabase(dbName) && !isFileExists(context, dbName)

    fun deleteFile(filePath: String, dbName: String): Boolean =
        File(filePath, dbName).delete()

    fun deleteFile(file: File): Boolean =
        file.delete()

    @Throws(Exception::class)
    fun copyFromAssetsToDatabase(context: Context, overwrite: Boolean) {
        val assetManager = context.assets
        val assetsDatabasePath = "public/assets/databases"
        val filelist = assetManager.list(assetsDatabasePath) ?: throw Exception("Folder public/assets/databases does not exist or is empty")

        val pathDB = File(context.filesDir.parentFile, "databases").absolutePath
        val dirDB = File(pathDB)
        if (!dirDB.isDirectory && !dirDB.mkdir()) {
            throw Exception("Cannot create dir $pathDB")
        }

        filelist.forEach { fileName ->
            when {
                fileName.endsWith(".db") -> {
                    val toFileName = fileName.replace(".db", "SQLite.db")
                    if (!isFileExists(context, toFileName) || overwrite) {
                        if (overwrite) deleteDatabase(context, toFileName)
                        copyDatabaseFromAssets(assetManager, "$assetsDatabasePath/$fileName", context.getDatabasePath(toFileName).absolutePath)
                    }
                }
                fileName.endsWith(".zip") -> {
                    unzipCopyDatabase(getDatabaseDirectoryPath(context), assetManager, "$assetsDatabasePath/$fileName", overwrite)
                }
            }
        }
    }

    private fun unzipCopyDatabase(databasePath: String, assetManager: AssetManager, zipPath: String, overwrite: Boolean) {
        ZipInputStream(assetManager.open(zipPath)).use { zis ->
            var entry: ZipEntry?
            while (zis.nextEntry.also { entry = it } != null) {
                val fileName = entry!!.name
                if (fileName.endsWith(".db")) {
                    val toFileName = fileName.replace(".db", "SQLite.db")
                    val dbPath = "$databasePath/$toFileName"
                    if (!File(dbPath).exists() || overwrite) {
                        if (overwrite) File(dbPath).delete()
                        File(dbPath).outputStream().use { fos ->
                            zis.copyTo(fos)
                        }
                    }
                    zis.closeEntry()
                }
            }
        }
    }

    private fun copyDatabaseFromAssets(assetManager: AssetManager, inPath: String, outPath: String) {
        assetManager.open(inPath).use { inputStream ->
            FileOutputStream(outPath).use { fileOut ->
                inputStream.copyTo(fileOut)
            }
        }
    }
}
