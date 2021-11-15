const Logger = require('./__internal__/log')
const { execSync } = require('child_process')
const logger = new Logger('stage.js')

require('./change-version')
logger.command('npx lerna run release')
execSync('npx lerna run release', { stdio: 'inherit' })