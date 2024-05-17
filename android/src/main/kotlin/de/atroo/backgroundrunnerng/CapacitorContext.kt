package de.atroo.backgroundrunnerng

import com.whl.quickjs.wrapper.JSObject
import de.atroo.backgroundrunnerng.runnerengine.Context
import de.atroo.backgroundrunnerng.sqlite.CapacitorSQLite
import de.atroo.backgroundrunnerng.sqlite.SQLite
import de.atroo.backgroundrunnerng.sqlite.SQLiteConfig

class CapacitorContext(androidContext: android.content.Context, name: String) : Context(androidContext, name)  {
    lateinit var sqlite: CapacitorSQLite
    override fun setupCapacitorAPI() {
        var obj: JSObject = context.createNewJSObject();
        sqlite = CapacitorSQLite(context, SQLite(androidContext, SQLiteConfig()))
    }

}
