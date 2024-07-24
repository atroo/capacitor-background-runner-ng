const path = require('path');
const { replaceExports } = require('./transpile-util');

const assetsDir = path.resolve(process.env.INIT_CWD, 'src', 'assets');

const backgroundPath = path.resolve(assetsDir, 'background.ts');
const backgroundOutputPath = path.resolve(assetsDir, 'background.js');
replaceExports(backgroundPath, backgroundOutputPath)
  .then((transpiledCode) => {
    console.log('Background transpilation successful!');
  })
  .catch((error) => {
    console.error('Error during Background transpilation:', error);
  });
