const fs = require('fs');
const path = require('path');


console.log('Running setup script...');
function updatePackageJson() {

  console.log('Updating package.json with new scripts...');
  const projectPackageJsonPath = path.resolve(process.env.INIT_CWD, 'package.json');
  
  console.log('projectPackageJsonPath: ', projectPackageJsonPath);
  
  if (!fs.existsSync(projectPackageJsonPath)) {
    console.error('Error: package.json not found in the project root.');
    process.exit(1);
  }

  const packageJson = require(projectPackageJsonPath);

  // Define the new scripts to add
  const newScripts = {
    "sqlite-transpile": "node scripts/sqlite-transpile.js",
    "background-transpile": "node scripts/background-transpile.js"
  };

  // Merge the new scripts with existing scripts
  packageJson.scripts = {
    ...packageJson.scripts,
    ...newScripts
  };

  // Write the updated package.json back to the file system
  fs.writeFileSync(projectPackageJsonPath, JSON.stringify(packageJson, null, 2));
  console.log('package.json updated successfully with new scripts.');
}

function copyTranspileScript() {
  console.log('Copying sqlite-transpile.js to project root...');

  // Define paths
  const projectRoot = process.env.INIT_CWD;
  const pluginDirPath = path.resolve(process.cwd());

  const pluginScriptsDir = path.resolve(pluginDirPath, 'scripts');
  const projectScriptsDir = path.resolve(projectRoot, 'scripts');
  const pluginTranspileScriptPath = path.join(pluginScriptsDir, 'sqlite-transpile.js');
  console.log('setup.js, pluginTranspileScriptPath: ', pluginTranspileScriptPath);
  const projectTranspileScriptPath = path.join(projectScriptsDir, 'sqlite-transpile.js');

  // Ensure the project scripts directory exists
  if (!fs.existsSync(projectScriptsDir)) {
    fs.mkdirSync(projectScriptsDir, { recursive: true });
    console.log('Created scripts directory in project root.');
  }

  fs.copyFileSync(path.join(pluginScriptsDir, 'setup.js'), path.join(projectScriptsDir, 'setup.js'));
  fs.copyFileSync(path.join(pluginScriptsDir, 'sqlite-transpile.js'), path.join(projectScriptsDir, 'sqlite-transpile.js'));
  fs.copyFileSync(path.join(pluginScriptsDir, 'background-transpile.js'), path.join(projectScriptsDir, 'background-transpile.js'));
  fs.copyFileSync(path.join(pluginScriptsDir, 'transpile-util.js'), path.join(projectScriptsDir, 'transpile-util.js'));
  

  console.log('Copied sqlite-transpile.js to project scripts directory.');
}

// Run the update function
updatePackageJson();
copyTranspileScript()
