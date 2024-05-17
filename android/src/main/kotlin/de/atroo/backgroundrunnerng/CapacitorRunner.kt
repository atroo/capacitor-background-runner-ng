package de.atroo.backgroundrunnerng

import android.util.Log

class CapacitorRunner {
    fun createContext(androidContext: android.content.Context, name: String): CapacitorContext {
        return CapacitorContext(androidContext, name)
    }

    fun destroyContext(name: String) {
        Log.d("CapacitorRunner", "unnecessary destroyContext: $name") ///???
//        context.destroy()
    }
}