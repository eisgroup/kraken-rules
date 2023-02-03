const Logger = require('./__internal__/log')
const logger = new Logger('change-versions.js')
const fs = require('fs')
const path = require('path')
const { VERSION } = require('./version');

function setVersion(v) {
    logger.log(`Setting version '${v}'`);
    const root = 'packages'

    const krakenModuleNames = fs.readdirSync(root)
        .map(dir => {
            const filePath = path.join(root, dir, 'package.json');
            const file = fs.readFileSync(filePath, { encoding: 'UTF-8' });
            return file
        })
        .map(p => JSON.parse(p).name)
    logger.log('Kraken modules to change version: ')
    logger.log(krakenModuleNames.join(', '))

    fs.readdirSync(root)
        .forEach(dir => {
            const filePath = path.join(root, dir, 'package.json');
            const file = fs.readFileSync(filePath, { encoding: 'UTF-8' });
            const json = JSON.parse(file)
            logger.log(`Updating module '${json.name}'`)
            // update dev dependencies
            const devDeps = Object.keys(json.devDependencies || {});
            devDeps.length && logger.log('  Updating dev dependencies')
            devDeps
                .filter(name => krakenModuleNames.includes(name))
                .filter(name => ~logger.log(`    Changing version of '${name}'`))
                .forEach(name => json.devDependencies[name] = v)

            // update dependencies
            const deps = Object.keys(json.dependencies || {});
            deps.length && logger.log('  Updating dependencies')
            deps
                .filter(name => krakenModuleNames.includes(name))
                .filter(name => ~logger.log(`    Changing version of '${name}'`))
                .forEach(name => json.dependencies[name] = v)
            logger.log(`  Updating module '${json.name}' version`)
            json.version = v

            // write updated package.json
            fs.writeFileSync(filePath, JSON.stringify(json, null, 4))
        })

    const filePath = path.join('lerna.json');
    const file = fs.readFileSync(filePath, { encoding: 'UTF-8' });
    const lerna = JSON.parse(file)
    lerna.version = v

    logger.log('Updating lerna.json')
    fs.writeFileSync(filePath, JSON.stringify(lerna, null, 2))

    const rootFilePath = path.join('package.json');
    const rootFile = fs.readFileSync(rootFilePath, { encoding: 'UTF-8' });
    const rootPackageJson = JSON.parse(rootFile)
    rootPackageJson.version = v

    logger.log('Updating root package.json')
    fs.writeFileSync(rootFilePath, JSON.stringify(rootPackageJson, null, 2))
}

setVersion(VERSION)
