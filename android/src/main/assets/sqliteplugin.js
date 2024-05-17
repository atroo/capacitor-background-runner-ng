class CapacitorSQLitePlugin {
    createConnection(options) {
        console.log("createConnection, CapacitorSQLitePlugin...");
        sqliteNative.createConnection(options);
    }
}

// SQLiteConnection Class
class SQLiteConnection {
  constructor(sqlite) {
    console.log("SQLiteConnection constructor...");
    this._connectionDict = new Map();
    this.sqlite = sqlite;
  }

  async createConnection(database, encrypted, mode, version, readonly) {
    console.log("createConnection...");
    try {
      if (database.endsWith('.db')) database = database.slice(0, -3);
      const conn = new SQLiteDBConnection(database, this.sqlite, encrypted, mode, version, readonly);
      const connName = readonly ? `RO_${database}` : `RW_${database}`;
      this._connectionDict.set(connName, conn);
      return conn;
    } catch (err) {
      throw err;
    }
  }

}

// SQLiteDBConnection Class
class SQLiteDBConnection {
  constructor(dbName, sqlite, encrypted, mode, version, readonly) {
    this.dbName = dbName;
    this.readonly = readonly;
    this.sqlite = sqlite;
    this.sqlite.createConnection({dbName: dbName, encrypted: encrypted, mode: mode, version: version, readonly: readonly});
  }

}

var CapacitorSQLite = new CapacitorSQLitePlugin();