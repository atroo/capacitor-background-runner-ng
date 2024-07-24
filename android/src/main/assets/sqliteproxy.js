class CapacitorSQLiteProxy {
  async createConnection(json) {
      return new Promise(async (resolve, reject) =>  {
        var ret = await SQLiteNative.createConnection(json);
        console.log("CapacitorSQLiteProxy, createConnection ret: ", JSON.stringify(ret));
        if (!ret || !ret.error) {
          resolve(ret);
        } else {
          reject(ret);
        }
      });
  }

  async closeConnection(json) {
    return new Promise(async (resolve, reject) =>  {
      var ret = await SQLiteNative.closeConnection(json);
      console.log("CapacitorSQLiteProxy, closeConnection ret: ", JSON.stringify(ret));
      if (ret.error) {
        reject(ret);
      } else {
        resolve(ret);
      }
    });
  }

  async run(json) {
    return new Promise(async (resolve, reject) =>  {
      var ret = await SQLiteNative.run(json);
      console.log("CapacitorSQLiteProxy, run ret: ", JSON.stringify(ret));
      if (ret.error) {
        reject(ret);
      } else {
        resolve(ret);
      }
    });
  }

  async query(json) {
    return new Promise(async (resolve, reject) =>  {
      var ret = await SQLiteNative.query(json);
      console.log("CapacitorSQLiteProxy, query ret: ", JSON.stringify(ret));
      if (ret.error) {
        reject(ret);
      } else {
        resolve(ret);
      }
    });
  }

  async open(json) {
    return new Promise(async (resolve, reject) =>  {
      var ret = await SQLiteNative.open(json);
      console.log("CapacitorSQLiteProxy, open ret: ", JSON.stringify(ret));
      if (ret.error) {
        reject(ret);
      } else {
        resolve(ret);
      }
    });
  }

  async close(json) {
    return new Promise(async (resolve, reject) =>  {
      var ret = await SQLiteNative.close(json);
      console.log("CapacitorSQLiteProxy, close ret: ", JSON.stringify(ret));
      if (ret.error) {
        reject(ret);
      } else {
        resolve(ret);
      }
    });
  }

  async initWebStore() {
  }

  async saveToStore(json) {
  }

  async getFromLocalDiskToStore(json) {
  }

  async saveToLocalDisk(json) {
  }

  async isSecretStored(json) {
  }

  async setEncryptionSecret(json) {
  }

  async changeEncryptionSecret(json) {
  }

  async clearEncryptionSecret() {
  }

  async checkEncryptionSecret(json) {
  }

  async beginTransaction(json) {
  }

  async commitTransaction(json) {
  }

  async rollbackTransaction(json) {
  }

  async isTransactionActive(json) {
  }

  async getUrl(json) {
  }

  async execute(json) {
  }

  async executeSet(json) {
  }

  async isDBExists(json) {
  }

  async isDBOpen(json) {
  }

  async isDatabaseEncrypted(json) {
  }

  async isInConfigEncryption(json) {
  }

  async isInConfigBiometricAuth(json) {
  }

  async isDatabase(json) {
  }

  async isTableExists(json) {
  }

  async deleteDatabase(json) {
  }

  async isJsonValid(json) {
  }

  async importFromJson(json) {
  }

  async exportToJson(json) {
  }

  async createSyncTable(json) {
  }

  async setSyncDate(json) {
  }

  async getSyncDate(json) {
  }

  async isJsdeleteExportedRowsonValid(json) {
  }

  async addUpgradeStatement(json) {
  }

  async copyFromAssets(json) {
  }

  async getFromHTTPRequest(json) {
  }

  async getDatabaseList(json) {
  }

  async getTableList(json) {
  }

  async getMigratableDbList(json) {
  }

  async addSQLiteSuffix(json) {
  }

  async deleteOldDatabases(json) {
  }

  async moveDatabasesAndAddSuffix(json) {
  }

  async checkConnectionsConsistency(json) {
  }

  async getNCDatabasePath(json) {
  }

  async createNCConnection(json) {
  }

  async closeNCConnection(json) {
  }

  async isNCDatabase(json) {
  }

}

var CapacitorSQLite = new CapacitorSQLiteProxy();

//export default CapacitorSQLite;