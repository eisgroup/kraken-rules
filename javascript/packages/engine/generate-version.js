const fs = require('fs')
const path = require('path')
const xmlParser = require('fast-xml-parser')

console.log('----------------------------------');
console.log('generating engine version constant');
console.log('----------------------------------');


const pomPath = path.join(
    __dirname,
    '../',
    '../',
    '../',
    'pom.xml'
)

console.log(`  reading '${pomPath}'`);

const pom = xmlParser.parse(
    fs.readFileSync(
        pomPath,
        { encoding: 'utf8' }
    )
)

console.log(`  version is: '${pom.project.version}'`);

const genFolderPath = path.join(
    __dirname,
    'src',
    'engine',
    'generated'
)

if (!fs.existsSync(genFolderPath)) {
    fs.mkdirSync(genFolderPath)
}

const genPath = path.join(
    __dirname,
    'src',
    'engine',
    'generated',
    'engineVersion.ts'
);

console.log(`  writing to path: '${genPath}'`);

fs.writeFileSync(
    genPath,
    `
// generated code    
// do not change it manually    
export const ENGINE_VERSION = "${pom.project.version}";

`, { encoding: 'utf8' }
)

console.log('  backend version is written');

process.exit(0)