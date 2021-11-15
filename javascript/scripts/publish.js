const { execSync } = require("child_process");
const semver = require("semver");
const Logger = require("./__internal__/log")
const logger = new Logger("publish.js")

const v = prepareVersion();

function prepareVersion() {
    const version = process.argv.indexOf('-v') !== -1
        ? process.argv[process.argv.indexOf('-v') + 1]
        : undefined
    if (!version) {
        const cv = require('../lerna.json').version
        const coerced = semver.coerce(cv)
        const stagingVersion = semver.inc(coerced, 'prerelease', 'stage')
        return stagingVersion;
    }
    return version
}

function cli(cmd) {
    logger.command(cmd)
    execSync(cmd, { stdio: 'inherit' })
}

function validate(version) {
    if (!version) {
        throw new Error(logger.error('Version must be defined'))
    }
    if (!semver.valid(version)) {
        throw new Error(logger.error(`Version '${version}' is not a valid sematic versioning version`))
    }
}

function publish(version) {
    const publishArgs = [
        '--yes',
        '--exact',
        '--no-git-tag-version'
    ]
    logger.log('Publishing')
    cli(`npm run change-version -- -v ${version}`)
    cli(`lerna publish ${version} ${publishArgs.join(' ')}`)
}

function logEnv() {
    cli('npm config list')
}

logger.log(`Publishing Kraken UI with version '${v}'`)
logger.log('Logging environment configuration')
logEnv()
logger.log(`Validating provided version '${v}'`)
validate(v)
logger.log(`Version '${v}' is valid `)
publish(v)
logger.log(`Kraken UI is published, version '${v}'`)