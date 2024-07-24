package de.atroo.backgroundrunnerng

import android.util.Log
import com.getcapacitor.Bridge
import de.atroo.backgroundrunnerng.runnerengine.Context

class CapacitorContext(androidContext: android.content.Context, name: String, @Volatile private var bridge: Bridge) : Context(androidContext, name)  {
    companion object {
        const val TAG = "CapacitorContext"
    }
    lateinit var sqliteAdapter: QuickJSSQLiteAdapter
    init {
        Log.d(TAG, "CapacitorContext ctor...bridge is null: ${bridge == null}")
        setupCapacitorAPI()
    }

    override fun setupCapacitorAPI() {
        Log.d(TAG, "setupCapacitorAPI...bridge is null: ${bridge == null}")
        val currentThread = Thread.currentThread()
        Log.d(TAG, "setupCapacitorAPI, thread: ${currentThread.name}")
        sqliteAdapter = QuickJSSQLiteAdapter(context, bridge)
    }
}
