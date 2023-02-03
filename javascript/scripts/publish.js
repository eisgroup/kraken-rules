const { execSync } = require("child_process");
const Logger = require("./__internal__/log")
const logger = new Logger("publish.js")
const { VERSION } = require('./version');

function cli(cmd) {
    logger.command(cmd)
    execSync(cmd, { stdio: 'inherit' })
}

function publish(v) {
    const publishArgs = [
        '--yes',
        '--exact',
        '--no-git-tag-version',
        '--no-verify-access',
        // do not try to reset git working changes to previous version
        '--no-git-reset',
        '--graph-type all'
    ]
    logger.log('Publishing')
    cli(`yarn run change-version -- -v ${v}`)
    cli(`lerna publish ${v} ${publishArgs.join(' ')}`)
}

function logEnv() {
    cli('yarn config list')
}

logger.log(`Publishing Kraken UI with version '${VERSION}'`)
logger.log('Logging environment configuration')
logEnv()
publish(VERSION)
logger.log(`Kraken UI is published, version '${VERSION}'`)
