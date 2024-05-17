package de.atroo.backgroundrunnerng.sqlite

class ConnectionOptions {
    var database: String = ""
    var version: Int = 1
    var encrypted: Boolean = false
    var mode: String = "no-encryption"
    var readOnly: Boolean = false
}