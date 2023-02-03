const Logger = require('./__internal__/log')
const logger = new Logger('change-versions.js')
const semver = require("semver");

function readVersionFromLernaJson() {
    const cv = require('../lerna.json').version
    const coerced = semver.coerce(cv)
    return semver.inc(coerced, 'prerelease', 'stage')
}

/**
 * Function converts maven snapshot version to semver compatible version.
 * Function tries to detect if version is actually a maven snapshot version before converting.
 *
 * @param v is a version string
 * @return converted maven snapshot version to semver
 */
function convertMvnVersionToSemVerIfNeeded(v) {
    // if version includes semver specific symbol '+' then it cannot be maven snapshot version
    if(v.includes('+')) {
        return v
    }

    const versionParts = v.split('-')

    // maven snapshot version must consists of exactly a core version and a snapshot timestamp separated by '-' symbol
    if(versionParts.length !== 2) {
        return v
    }

    const snapshotParts = versionParts[1].split('.')

    // maven snapshot part must be a day (in format YYYYMMDD) and time (in format hhmm) components separated by a '.'
    if(snapshotParts.length !== 2 || snapshotParts[0].length !== 8 || snapshotParts[1].length !== 4) {
        return v
    }

    const coreVersion = versionParts[0]
    const day = snapshotParts[0]
    const time = snapshotParts[1]

    // remove maven dot separator so that version is a valid semver
    return `${coreVersion}-${day}${time}`
}

function resolveVersion() {
    let version = process.argv.indexOf('-v') !== -1
        ? process.argv[process.argv.indexOf('-v') + 1]
        : readVersionFromLernaJson()

    return convertMvnVersionToSemVerIfNeeded(version);
}

function validateVersion(v) {
    if (!v) {
        throw new Error(logger.error("Version must be defined"))
    }
    if (!semver.valid(v, { loose: true })) {
        throw new Error(logger.error(`'${v}' is not a valid SemVer version`))
    }
}

const VERSION = resolveVersion();
validateVersion(VERSION);

logger.log(`Version: '${VERSION}'`)

module.exports = {
    VERSION
}
