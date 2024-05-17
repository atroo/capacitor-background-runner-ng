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
//      await this.sqlite.createConnection({dbName: database, encrypted, mode, version, readonly});
      const conn = new SQLiteDBConnection(database, this.sqlite, encrypted, mode, version, readonly);
      const connName = readonly ? `RO_${database}` : `RW_${database}`;
      this._connectionDict.set(connName, conn);
      return conn;
//        return null;
    } catch (err) {
      throw err;
    }
  }

  // async closeConnection(database, readonly) {
  //   try {
  //     if (database.endsWith('.db')) database = database.slice(0, -3);
  //     await this.sqlite.closeConnection({ database, readonly });
  //     const connName = readonly ? `RO_${database}` : `RW_${database}`;
  //     this._connectionDict.delete(connName);
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async isConnection(database, readonly) {
  //   const res = {};
  //   if (database.endsWith('.db')) database = database.slice(0, -3);
  //   const connName = readonly ? `RO_${database}` : `RW_${database}`;
  //   res.result = this._connectionDict.has(connName);
  //   return res;
  // }

  // async retrieveConnection(database, readonly) {
  //   if (database.endsWith('.db')) database = database.slice(0, -3);
  //   const connName = readonly ? `RO_${database}` : `RW_${database}`;
  //   if (this._connectionDict.has(connName)) {
  //     const conn = this._connectionDict.get(connName);
  //     if (conn) return conn;
  //     else throw new Error(`Connection ${database} is undefined`);
  //   } else {
  //     throw new Error(`Connection ${database} does not exist`);
  //   }
  // }

  // async retrieveAllConnections() {
  //   return this._connectionDict;
  // }

  // async closeAllConnections() {
  //   const delDict = new Map();
  //   try {
  //     for (const key of this._connectionDict.keys()) {
  //       const database = key.substring(3);
  //       const readonly = key.substring(0, 3) === 'RO_' ? true : false;
  //       await this.sqlite.closeConnection({ database, readonly });
  //       delDict.set(key, null);
  //     }
  //     for (const key of delDict.keys()) {
  //       this._connectionDict.delete(key);
  //     }
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async initWebStore() {
  //   try {
  //     await this.sqlite.initWebStore();
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async saveToStore(database) {
  //   try {
  //     await this.sqlite.saveToStore({ database });
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async saveToLocalDisk(database) {
  //   try {
  //     await this.sqlite.saveToLocalDisk({ database });
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async getFromLocalDiskToStore(overwrite = true) {
  //   try {
  //     await this.sqlite.getFromLocalDiskToStore({ overwrite });
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async echo(value) {
  //   try {
  //     return await this.sqlite.echo({ value });
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async isSecretStored() {
  //   try {
  //     return await this.sqlite.isSecretStored();
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async setEncryptionSecret(passphrase) {
  //   try {
  //     await this.sqlite.setEncryptionSecret({ passphrase });
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async changeEncryptionSecret(passphrase, oldpassphrase) {
  //   try {
  //     await this.sqlite.changeEncryptionSecret({ passphrase, oldpassphrase });
  //   } catch (err) {
  //     throw err;
  //   }
  //   }

  // async clearEncryptionSecret() {
  //   try {
  //     await this.sqlite.clearEncryptionSecret();
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async checkEncryptionSecret(passphrase) {
  //   try {
  //     return await this.sqlite.checkEncryptionSecret({ passphrase });
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async addUpgradeStatement(database, upgrade) {
  //   try {
  //     if (database.endsWith('.db')) database = database.slice(0, -3);
  //     await this.sqlite.addUpgradeStatement({ database, upgrade });
  //   } catch (err) {
  //     throw err;
  //   }
  // }



  // async checkConnectionsConsistency() {
  //   try {
  //     const keys = [...this._connectionDict.keys()];
  //     const openModes = [];
  //     const dbNames = [];
  //     for (const key of keys) {
  //       openModes.push(key.substring(0, 2));
  //       dbNames.push(key.substring(3));
  //     }
  //     const res = await this.sqlite.checkConnectionsConsistency({ dbNames, openModes });
  //     if (!res.result) this._connectionDict = new Map();
  //     return res;
  //   } catch (err) {
  //     this._connectionDict = new Map();
  //     throw err;
  //   }
  // }

  // async importFromJson(jsonstring) {
  //   try {
  //     return await this.sqlite.importFromJson({ jsonstring });
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async isJsonValid(jsonstring) {
  //   try {
  //     return await this.sqlite.isJsonValid({ jsonstring });
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async copyFromAssets(overwrite = true) {
  //   try {
  //     await this.sqlite.copyFromAssets({ overwrite });
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async getFromHTTPRequest(url, overwrite = true) {
  //   try {
  //     await this.sqlite.getFromHTTPRequest({ url, overwrite });
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async isDatabaseEncrypted(database) {
  //   if (database.endsWith('.db')) database = database.slice(0, -3);
  //   try {
  //     return await this.sqlite.isDatabaseEncrypted({ database });
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async isInConfigEncryption() {
  //   try {
  //     return await this.sqlite.isInConfigEncryption();
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async isInConfigBiometricAuth() {
  //   try {
  //     return await this.sqlite.isInConfigBiometricAuth();
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async isDatabase(database) {
  //   if (database.endsWith('.db')) database = database.slice(0, -3);
  //   try {
  //     return await this.sqlite.isDatabase({ database });
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async getDatabaseList() {
  //   try {
  //     const res = await this.sqlite.getDatabaseList();
  //     res.values.sort();
  //     return res;
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async getMigratableDbList(folderPath = 'default') {
  //   try {
  //     return await this.sqlite.getMigratableDbList({ folderPath });
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async addSQLiteSuffix(folderPath = 'default', dbNameList = []) {
  //   try {
  //     await this.sqlite.addSQLiteSuffix({ folderPath, dbNameList });
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async deleteOldDatabases(folderPath = 'default', dbNameList = []) {
  //   try {
  //     await this.sqlite.deleteOldDatabases({ folderPath, dbNameList });
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async moveDatabasesAndAddSuffix(folderPath = 'default', dbNameList = []) {
  //   return this.sqlite.moveDatabasesAndAddSuffix({ folderPath, dbNameList });
  // }

  // async getNCDatabasePath(path, database) {
  //   try {
  //     return await this.sqlite.getNCDatabasePath({ path, database });
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async createNCConnection(databasePath, version) {
  //   try {
  //     await this.sqlite.createNCConnection({ databasePath, version });
  //     const conn = new SQLiteDBConnection(databasePath, true, this.sqlite);
  //     const connName = `RO_${databasePath})`;
  //     this._connectionDict.set(connName, conn);
  //     return conn;
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async closeNCConnection(databasePath) {
  //   try {
  //     await this.sqlite.closeNCConnection({ databasePath });
  //     const connName = `RO_${databasePath})`;
  //     this._connectionDict.delete(connName);
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async isNCConnection(databasePath) {
  //   const res = {};
  //   const connName = `RO_${databasePath})`;
  //   res.result = this._connectionDict.has(connName);
  //   return res;
  // }

  // async retrieveNCConnection(databasePath) {
  //   if (this._connectionDict.has(databasePath)) {
  //     const connName = `RO_${databasePath})`;
  //     const conn = this._connectionDict.get(connName);
  //     if (conn) return conn;
  //     else throw new Error(`Connection ${databasePath} is undefined`);
  //   } else {
  //     throw new Error(`Connection ${databasePath} does not exist`);
  //   }
  // }

  // async isNCDatabase(databasePath) {
  //   try {
  //     return await this.sqlite.isNCDatabase({ databasePath });
  //   } catch (err) {
  //     throw err;
  //   }
  // }
}

// SQLiteDBConnection Class
class SQLiteDBConnection {
  constructor(dbName, sqlite, encrypted, mode, version, readonly) {
    this.dbName = dbName;
    this.readonly = readonly;
    this.sqlite = sqlite;
    this.sqlite.createConnection({dbName: dbName, encrypted: encrypted, mode: mode, version: version, readonly: readonly});
  }

  // getConnectionDBName() {
  //   return this.dbName;
  // }

  // getConnectionReadOnly() {
  //   return this.readonly;
  // }

  // async open() {
  //   try {
  //     await this.sqlite.open({ database: this.dbName, readonly: this.readonly });
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async close() {
  //   try {
  //     await this.sqlite.close({ database: this.dbName, readonly: this.readonly });
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async beginTransaction() {
  //   try {
  //     return await this.sqlite.beginTransaction({ database: this.dbName });
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async commitTransaction() {
  //   try {
  //     return await this.sqlite.commitTransaction({ database: this.dbName });
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async rollbackTransaction() {
  //   try {
  //     return await this.sqlite.rollbackTransaction({ database: this.dbName });
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async isTransactionActive() {
  //   try {
  //     return await this.sqlite.isTransactionActive({ database: this.dbName });
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async loadExtension(path) {
  //   try {
  //     await this.sqlite.loadExtension({ database: this.dbName, path, readonly: this.readonly });
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async enableLoadExtension(toggle) {
  //   try {
  //     await this.sqlite.enableLoadExtension({ database: this.dbName, toggle, readonly: this.readonly });
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async getUrl() {
  //   try {
  //     return await this.sqlite.getUrl({ database: this.dbName, readonly: this.readonly });
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async getVersion() {
  //   try {
  //     return await this.sqlite.getVersion({ database: this.dbName, readonly: this.readonly });
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async getTableList() {
  //   try {
  //     return await this.sqlite.getTableList({ database: this.dbName, readonly: this.readonly });
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async execute(statements, transaction = true, isSQL92 = true) {
  //   try {
  //     if (!this.readonly) {
  //       return await this.sqlite.execute({
  //         database: this.dbName,
  //         statements,
  //         transaction,
  //         readonly: false,
  //         isSQL92,
  //       });
  //     } else {
  //       throw new Error('not allowed in read-only mode');
  //     }
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async query(statement, values = [], isSQL92 = true) {
  //   try {
  //     let res;
  //     if (values.length > 0) {
  //       res = await this.sqlite.query({
  //         database: this.dbName,
  //         statement,
  //         values,
  //         readonly: this.readonly,
  //         isSQL92,
  //       });
  //     } else {
  //       res = await this.sqlite.query({
  //         database: this.dbName,
  //         statement,
  //         values: [],
  //         readonly: this.readonly,
  //         isSQL92,
  //       });
  //     }

  //     // reorder rows for ios
  //     res = await this.reorderRows(res);
  //     return res;
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async run(statement, values = [], transaction = true, returnMode = 'no', isSQL92 = true) {
  //   try {
  //     if (!this.readonly) {
  //       let res;
  //       if (values.length > 0) {
  //         res = await this.sqlite.run({
  //           database: this.dbName,
  //           statement,
  //           values,
  //           transaction,
  //           readonly: false,
  //           returnMode,
  //           isSQL92,
  //         });
  //       } else {
  //         res = await this.sqlite.run({
  //           database: this.dbName,
  //           statement,
  //           values: [],
  //           transaction,
  //           readonly: false,
  //           returnMode,
  //           isSQL92,
  //         });
  //       }

  //       // reorder rows for ios
  //       res.changes = await this.reorderRows(res.changes);
  //       return res;
  //     } else {
  //       throw new Error('not allowed in read-only mode');
  //     }
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async executeSet(set, transaction = true, returnMode = 'no', isSQL92 = true) {
  //   try {
  //     if (!this.readonly) {
  //       const res = await this.sqlite.executeSet({
  //         database: this.dbName,
  //         set,
  //         transaction,
  //         readonly: false,
  //         returnMode,
  //         isSQL92,
  //       });

  //       // reorder rows for ios
  //       res.changes = await this.reorderRows(res.changes);
  //       return res;
  //     } else {
  //       throw new Error('not allowed in read-only mode');
  //     }
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async isExists() {
  //   try {
  //     return await this.sqlite.isDBExists({ database: this.dbName, readonly: this.readonly });
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async isTable(table) {
  //   try {
  //     return await this.sqlite.isTableExists({ database: this.dbName, table, readonly: this.readonly });
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async isDBOpen() {
  //   try {
  //     return await this.sqlite.isDBOpen({ database: this.dbName, readonly: this.readonly });
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async delete() {
  //   try {
  //     if (!this.readonly) {
  //       await this.sqlite.deleteDatabase({ database: this.dbName, readonly: false });
  //     } else {
  //       throw new Error('not allowed in read-only mode');
  //     }
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async createSyncTable() {
  //   try {
  //     if (!this.readonly) {
  //       return await this.sqlite.createSyncTable({ database: this.dbName, readonly: false });
  //     } else {
  //       throw new Error('not allowed in read-only mode');
  //     }
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async setSyncDate(syncdate) {
  //   try {
  //     if (!this.readonly) {
  //       await this.sqlite.setSyncDate({ database: this.dbName, syncdate, readonly: false });
  //     } else {
  //       throw new Error('not allowed in read-only mode');
  //     }
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async getSyncDate() {
  //   try {
  //     const res = await this.sqlite.getSyncDate({ database: this.dbName, readonly: this.readonly });
  //     let retDate = '';
  //     if (res.syncDate > 0) retDate = new Date(res.syncDate * 1000).toISOString();
  //     return retDate;
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async exportToJson(mode, encrypted = false) {
  //   try {
  //     return await this.sqlite.exportToJson({ database: this.dbName, jsonexportmode: mode, readonly: this.readonly, encrypted });
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async deleteExportedRows() {
  //   try {
  //     if (!this.readonly) {
  //       await this.sqlite.deleteExportedRows({ database: this.dbName, readonly: false });
  //     } else {
  //       throw new Error('not allowed in read-only mode');
  //     }
  //   } catch (err) {
  //     throw err;
  //   }
  // }

  // async executeTransaction(txn, isSQL92 = true) {
  //   let changes = 0;
  //   let isActive = false;
  //   if (!this.readonly) {
  //     await this.sqlite.beginTransaction({
  //       database: this.dbName,
  //     });
  //     isActive = await this.sqlite.isTransactionActive({
  //       database: this.dbName,
  //     });
  //     if (!isActive) {
  //       return Promise.reject('After Begin Transaction, no transaction active');
  //     }
  //     try {
  //       for (const task of txn) {
  //         if (typeof task !== 'object' || !('statement' in task)) {
  //           throw new Error('Error a task.statement must be provided');
  //         }
  //         if ('values' in task && task.values && task.values.length > 0) {
  //           const retMode = task.statement.toUpperCase().includes('RETURNING')
  //             ? 'all'
  //             : 'no';
  //           const ret = await this.sqlite.run({
  //             database: this.dbName,
  //             statement: task.statement,
  //             values: task.values,
  //             transaction: false,
  //             readonly: false,
  //             returnMode: retMode,
  //             isSQL92: isSQL92,
  //           });
  //           if (ret.changes.changes < 0) {
  //             throw new Error('Error in transaction method run');
  //           }
  //           changes += ret.changes.changes;
  //         } else {
  //           const ret = await this.sqlite.execute({
  //             database: this.dbName,
  //             statements: task.statement,
  //             transaction: false,
  //             readonly: false,
  //           });
  //           if (ret.changes.changes < 0) {
  //             throw new Error('Error in transaction method execute');
  //           }
  //           changes += ret.changes.changes;
  //         }
  //       }
  //       // commit
  //       const retC = await this.sqlite.commitTransaction({
  //         database: this.dbName,
  //       });
  //       changes += retC.changes.changes;
  //       const retChanges = { changes: { changes: changes } };
  //       return Promise.resolve(retChanges);
  //     } catch (err) {
  //       // rollback
  //       const msg = err.message ? err.message : err;
  //       await this.sqlite.rollbackTransaction({
  //         database: this.dbName,
  //       });
  //       return Promise.reject(msg);
  //     }
  //   } else {
  //     return Promise.reject('not allowed in read-only mode');
  //   }
  // }

  // async reorderRows(res) {
  //   const retRes = res;
  //   if (res?.values && typeof res.values[0] === 'object') {
  //     if (Object.keys(res.values[0]).includes('ios_columns')) {
  //       const columnList = res.values[0]['ios_columns'];
  //       const iosRes = [];
  //       for (let i = 1; i < res.values.length; i++) {
  //         const rowJson = res.values[i];
  //         const resRowJson = {};
  //         for (const item of columnList) {
  //           resRowJson[item] = rowJson[item];
  //         }
  //         iosRes.push(resRowJson);
  //       }
  //       retRes['values'] = iosRes;
  //     }
  //   }
  //   return Promise.resolve(retRes);
  // }
}

var CapacitorSQLite = new CapacitorSQLitePlugin();