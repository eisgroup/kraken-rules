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


const packageJsons = dirs
    .map(dir => ({
        dir,
        package: require(`.${folderPath}${dir}/package.json`)
    }))
    .filter(d => !d.package.private)
    .map(d => {
        const { dir, package } = d
        console.log();
        console.log(` ---> Generating for [${package.name.bold}]`.green);
        var config = configFor(dir);
        var { src, out } = config
        delete config['src']
        delete config['out']
        const readme = fs.existsSync(config.readme)
            ? config.readme
            : 'none'
        var app = new TypeDoc.Application({ ...config, readme })
        var project = app.convert(app.expandInputFiles([src]));
        if (project) {
            var outputDir = out;
            app.generateDocs(project, outputDir);
        }
        return { package, outputDir }
    })

const version = packageJsons[0].package.version;

fs.writeFile(
    `./target/docs/${version}/index.html`,
    t(
        'Kraken UI documentation',
        version,
        packageJsons.map(p => ({
            href: `./${p.package.name}/index.html`,
            label: p.package.name
        }))
    ),
    (err) => err && console.log(err)
)