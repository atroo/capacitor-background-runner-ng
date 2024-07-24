const fs = require('fs').promises;
const fsnp = require('fs');
const path = require('path');
const { replaceExports } = require('./transpile-util');

const sqliteDefinitionsPath = path.resolve(process.env.INIT_CWD, 'node_modules', '@capacitor-community', 'sqlite', 'src', 'definitions.ts');

console.log('definitionsPath: ', sqliteDefinitionsPath);

if (!fsnp.existsSync(sqliteDefinitionsPath)) {
  console.error('Error: definitions.ts not found...');
  process.exit(1);
}

// const jsCode = replaceExports(inputFilePath);
const pluginDir = path.resolve(__dirname, 'android', 'src', 'main', 'assets');


// const projectAssetsDir = path.resolve(process.env.INIT_CWD, 'src', 'assets');
const projectAssetsPath = path.resolve(process.env.INIT_CWD, 'node_modules', '@atroo', 'background-runner-ng', 'android', 'src', 'main', 'assets', 'sqliteplugin.js');
// const projectAssetsPath = path.resolve(pluginDir, 'tmp.js');
console.log('projectAssetsPath: ', projectAssetsPath);

replaceExports(sqliteDefinitionsPath, projectAssetsPath)
  .then((transpiledCode) => {
    console.log('sqlite transpilation successful!');
    // console.log('The transpiled JS Code:\n', transpiledCode);
  })
  .catch((error) => {
    console.error('Error during sqlite transpilation:', error);
  });
