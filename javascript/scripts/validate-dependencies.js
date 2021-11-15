const semver = require("semver");
const Logger = require('./__internal__/log')
const logger = new Logger('validate-dependencies.js')
const fs = require('fs')
const path = require('path')
const { toBe, Reducer } = require("declarative-js");

const root = 'packages'

class Dependency {
    constructor(name, type, version, packageName) {
        this.name = name
        this.type = type
        this.version = version
        this.package = packageName
    }
    verbose() {
        return `${this.package}: ${this.name}@${this.version}`;
    }
}

const krakenModuleNames = fs.readdirSync(root)
    .map(dir => {
        const filePath = path.join(root, dir, 'package.json');
        const file = fs.readFileSync(filePath, { encoding: 'UTF-8' });
        return file
    })
    .map(p => JSON.parse(p).name)

logger.log('Kraken modules to validate dependency versions: ')
logger.log(krakenModuleNames.join(', '))

const r = fs.readdirSync(root)
    .map(dir => {
        const filePath = path.join(root, dir, 'package.json');
        const file = fs.readFileSync(filePath, { encoding: 'UTF-8' });
        const json = JSON.parse(file)

        const deps = json.dependencies
            ? Object.entries(json.dependencies).map(v => new Dependency(
                v[0],
                'dep',
                semver.coerce(v[1]).raw,
                json.name
            ))
            : []
        const devDeps = json.devDependencies
            ? Object.entries(json.devDependencies).map(v => new Dependency(
                v[0],
                'devDep',
                semver.coerce(v[1]).raw,
                json.name
            ))
            : []
        return deps.concat(devDeps)
    })
    .reduce(Reducer.flat, [])
    .reduce(Reducer.groupBy('name'), Reducer.Map())
    .entries()
    .filter(e => e.value.filter(toBe.uniqueBy(d => d.version)).length > 1)

if (r.length) {
    r.forEach(entry => {
        const versions = entry.value
            .filter(toBe.uniqueBy(d => `${d.name}${d.version}`))
            .map(d => d.verbose())
            .join('\n')
        logger.error(`Dependency '${entry.key}' has multiple versions:\n${versions}`)
    })
    throw new Error('Multiple versions for the same dependency is not allowed')
}

logger.log('All dependencies have the same version')
