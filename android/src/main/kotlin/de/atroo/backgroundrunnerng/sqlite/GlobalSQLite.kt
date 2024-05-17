package de.atroo.backgroundrunnerng.sqlite

/**
 * !!! This is now deprecated in favor of a more secure storage !!!
 *
 * Since 3.0.0-beta.13
 *
 * You must follow the following process flow:
 * - Use setEncryptionSecret method with a passphrase:
 *   - to store the passphrase in Encrypted SharedPreferences
 *   - to change the password of all encrypted databases to the stored passphrase
 *
 * - Use changeEncryptionSecret method with a passphrase and the old passphrase:
 *   - to store the new passphrase in Encrypted SharedPreferences
 *   - to change the password of all encrypted databases to the stored passphrase
 *
 * Do not use for the passphrase the `secret` / `newsecret` parameter values.
 *
 * When you will do a next build:
 * - set the `secret` parameter value to ""
 * - remove the `newsecret` parameter
 */
class  GlobalSQLite {
    val secret = "sqlite secret"
    val newsecret = "sqlite new secret"
}
