const fs = require('fs').promises;
const fsnp = require('fs');
const ts = require('typescript');
const path = require('path');

/**
 * Transpile a TypeScript string to JavaScript
 * @param {string} tsString - The TypeScript code as a string
 * @returns {string} - The transpiled JavaScript code
 */
function transpileTypeScriptString(tsString) {
  const result = ts.transpileModule(tsString, {
    compilerOptions: {
      // module: ts.ModuleKind.CommonJS,
      target: ts.ScriptTarget.ES2016,
      downlevelIteration: true,
      removeComments: true,
    },
  });

  return result.outputText;
}

/**
 * Replace exports in a TypeScript file and transpile to JavaScript
 * @param {string} filePath - The path to the TypeScript file
 * @param {string} outputFilePath - The path to the output JavaScript file
 * @returns {Promise<string>} - The transpiled JavaScript code
 */
async function replaceExports(filePath, outputFilePath) {
  try {
    // Read the content of the TypeScript file
    const data = await fs.readFile(filePath, 'utf8');

    let result = data;
    // // Replace "export class" with "class"
    // let result = data.replace(/export class/g, 'class');
    // // Replace "export interface" with "interface"
    // result = result.replace(/export interface/g, 'interface');

    // console.log('Modified TypeScript Code:\n', result);
    let transpiledCode = transpileTypeScriptString(result);

    // Write the transpiled content to a new file
    await fs.writeFile(outputFilePath, transpiledCode, 'utf8');

    console.log(`File has been saved as ${outputFilePath}`);

    return transpiledCode;
  } catch (err) {
    console.error(`Error processing file ${filePath}:`, err);
    throw err;
  }
}


module.exports = {
    replaceExports
};