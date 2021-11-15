var fs = require('fs')
require('colors');
var TypeDoc = require('typedoc');
var configFor = require('./.typedoc/base.typedoc.config')
var t = require('./.typedoc/htmlTemplates')
var folderPath = './packages/'
var dirs = fs.readdirSync(folderPath)

console.log('--------------------------------------------------'.green);
console.log('  Generating documentation for Kraken UI modules  '.brightGreen);
console.log('--------------------------------------------------'.green);

const publicPackages = dirs
    .map(dir => ({
        dir,
        package: require(`.${folderPath}${dir}/package.json`)
    }))
    .filter(d => !d.package.private);

const publicDirs = publicPackages
    .map(p => `./packages/${p.dir}`)

// folders will be resolved from lerna.json
// for explicitly passed folders will be generated docs
// if folders, in lerna packages has readme.md, it will be generated as module
// but will not contain docs, only parsed markdown.
// modules should have readme.md only as a public docs for end user

var config = require('./.typedoc/typedoc.config')('Kraken UI engine', publicPackages[0].package.version)
var out = config.out
delete config['src']
delete config['out']
var app = new TypeDoc.Application(config)
const files = app.expandInputFiles(publicDirs);
var project = app.convert(files);
app.lernaExclude = ['test-app']
if (project) {
    var outputDir = out;
    app.generateDocs(project, outputDir);
}