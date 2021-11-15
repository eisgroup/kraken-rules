const { green, bgRed, bgGreen, bgGray, red, bgBlue } = require('chalk');

function log(event) {
    console.log(`${bgGreen(`[${this.scriptName}][log]`)} ${green(event)}`);
}

function logErr(event) {
    console.log(`${bgRed(`[${this.scriptName}][err]`)} ${red(event)}`);
    return event
}

function logCmd(event) {
    console.log(`${bgGray(`[${this.scriptName}][cmd]`)} ${event}`);
    return event
}

function logHeader() {
    console.log('');
    console.log(`${bgBlue(`---------- ${this.scriptName} ----------`)}`);
    console.log();
}

class Logger {
    constructor(scriptName) {
        this.scriptName = scriptName
        logHeader.bind(this)()
    }
}

Logger.prototype.header = logHeader
Logger.prototype.log = log
Logger.prototype.error = logErr
Logger.prototype.command = logCmd

module.exports = Logger