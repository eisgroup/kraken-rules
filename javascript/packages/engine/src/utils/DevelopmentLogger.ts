/*
 *  Copyright 2020 EIS Ltd and/or one of its affiliates.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import { krakenConfig as config, KrakenConfig } from '../config'

/**
 * Kraken logger is configured via environment variable `process.env.NODE_ENV`.
 * If this variable is set to production no logs will be produced.
 * Otherwise it will log Kraken actions.
 *
 * More granular control of the Kraken logs can be adjusted via global variable.
 * Read more about granular configuration in {@link KrakenConfig}
 *
 * @see {@link KrakenConfig}
 * @since 1.1.1
 */
interface Logger {
    group(name: string): void
    groupEnd(): void
    debug(...message: unknown[]): void
    info(...message: unknown[]): void
    warning(...message: unknown[]): void
    error(...message: unknown[]): void
}

const CONSOLE_LOGGER: Logger = {
    group: (name: string) => (console.groupCollapsed ? console.groupCollapsed(name) : console.group(name)),
    groupEnd: console.groupEnd,
    debug: console.debug,
    info: console.info,
    warning: console.warn,
    error: console.error,
}

export class DevelopmentLogger {
    private logger: Logger
    private isLoggerEnabled: boolean
    private currentGroupDepth: number

    constructor(l: Logger) {
        this.logger = l
        this.isLoggerEnabled = process.env.NODE_ENV !== 'production'
        this.currentGroupDepth = 0
    }

    /**
     * Creates a new nesting level in log output. Only logged if debug level is enabled.
     * If debug level is not enabled, then only action function is evaluated without logging or nesting.
     *
     * @param describeBefore description to log at the entry of the log nesting
     * @param f code to be executed between describeBefore and describeAfter parameters
     * @param describeAfter description to log at the exit of the log nesting
     */
    groupDebug<T>(describeBefore: () => string, f: () => T, describeAfter: (r: T) => string): T {
        const isLoggingLevelEnabled = config.getConfig().logger.debug
        const logAfterToConsole = (after: string) => this.debug(() => after, true)
        return this.doGroup(describeBefore, f, describeAfter, logAfterToConsole, isLoggingLevelEnabled)
    }

    /**
     * Creates a new nesting level in log output. Logs output description to console at info level.
     *
     * @param describeBefore description to log at the entry of the log nesting
     * @param f code to be executed between describeBefore and describeAfter parameters
     * @param describeAfter description to log at the exit of the log nesting
     */
    group<T>(describeBefore: () => string, f: () => T, describeAfter: (r: T) => string): T {
        const isLoggingLevelEnabled = true
        const logAfterToConsole = (after: string) => this.info(() => after, true)
        return this.doGroup(describeBefore, f, describeAfter, logAfterToConsole, isLoggingLevelEnabled)
    }

    /**
     * Logs at debug level.
     *
     * @param getLogMessage function that produces log message
     * @param consoleOnly if true, then message is logged to console output only and will not be
     * included in global kraken logs storage.
     */
    debug(getLogMessage: () => unknown, consoleOnly?: boolean): void {
        const isLoggingLevelEnabled = config.getConfig().logger.debug
        const doLog = (...message: unknown[]) => this.logger.debug(...message)
        this.log(getLogMessage, doLog, isLoggingLevelEnabled, consoleOnly)
    }

    /**
     * Logs at info level.
     *
     * @param getLogMessage function that produces log message
     * @param consoleOnly if true, then message is logged to console output only and will not be
     * included in global kraken logs storage.
     */
    info(getLogMessage: () => unknown, consoleOnly?: boolean): void {
        const isLoggingLevelEnabled = config.getConfig().logger.info
        const doLog = (...message: unknown[]) => this.logger.info(...message)
        this.log(getLogMessage, doLog, isLoggingLevelEnabled, consoleOnly)
    }

    /**
     * Logs at warning level.
     *
     * @param getLogMessage function that produces log message
     * @param consoleOnly if true, then message is logged to console output only and will not be
     * included in global kraken logs storage.
     */
    warning(getLogMessage: () => unknown, consoleOnly?: boolean): void {
        const isLoggingLevelEnabled = config.getConfig().logger.warning
        const doLog = (...message: unknown[]) => this.logger.warning(...message)
        this.log(getLogMessage, doLog, isLoggingLevelEnabled, consoleOnly)
    }

    /**
     * Logs at error level.
     *
     * @param getLogMessage function that produces log message
     * @param consoleOnly if true, then message is logged to console output only and will not be
     * included in global kraken logs storage.
     */
    error(getLogMessage: () => unknown, consoleOnly?: boolean): void {
        const isLoggingLevelEnabled = config.getConfig().logger.error
        const doLog = (...message: unknown[]) => this.logger.error(...message)
        this.log(getLogMessage, doLog, isLoggingLevelEnabled, consoleOnly)
    }

    private doGroup<T>(
        describeBefore: () => string,
        f: () => T,
        describeAfter: (r: T) => string,
        logAfterToConsole: (after: string) => void,
        isLoggingLevelEnabled: boolean,
    ): T {
        if (!this.isLoggerEnabled || !isLoggingLevelEnabled) {
            return f()
        }

        const beforeMessage = describeBefore()
        this.addEntry(beforeMessage, this.currentGroupDepth, '->')
        this.logger.group(beforeMessage)

        this.currentGroupDepth++

        let result: T
        let resultAssigned = false
        try {
            result = f()
            resultAssigned = true
            return result
        } catch (error) {
            this.error(() => [`An error occurred. Error message:`, error])
            throw error
        } finally {
            this.currentGroupDepth--
            if (this.currentGroupDepth < 0) {
                this.currentGroupDepth = 0
            }

            let afterMessage: string | undefined = undefined
            if (resultAssigned) {
                // Explicit check if result was actually assigned. Typing system cannot help here
                // because it is impossible to distinguish between unassigned result and assigned undefined result
                afterMessage = describeAfter(result!)
                logAfterToConsole(afterMessage)
            }
            this.addEntry(afterMessage ?? '', this.currentGroupDepth, '<-')
            this.logger.groupEnd()
        }
    }

    private log(
        getLogMessage: () => unknown,
        doLog: (...message: unknown[]) => void,
        isLoggingLevelEnabled: boolean,
        consoleOnly?: boolean,
    ): void {
        if (this.isLoggerEnabled && isLoggingLevelEnabled) {
            const message = getLogMessage()
            if (!consoleOnly) {
                this.addEntry(message, this.currentGroupDepth, '--')
            }
            if (Array.isArray(message)) {
                doLog(...message)
            } else {
                doLog(message)
            }
        }
    }

    private addEntry(message: unknown, depth: number, prefix: string): void {
        if (global) {
            const logLines = this.render(message).split('\n')
            const logs = this.prepareAndGetKrakenLogs()
            const alignmentPadding = '   '.repeat(depth)
            const firstLogLinePrefix = `${alignmentPadding}${prefix} `
            const firstLogLine = `${firstLogLinePrefix}${logLines[0]}`
            logs.push(firstLogLine)
            for (let i = 1; i < logLines.length; i++) {
                const nextLogLinePrefix = ' '.repeat(firstLogLinePrefix.length)
                const nextLogLine = `${nextLogLinePrefix}${logLines[i]}`
                logs.push(nextLogLine)
            }
        }
    }

    private prepareAndGetKrakenLogs(): string[] {
        const krakenConfig = (global as unknown as { Kraken: KrakenConfig }).Kraken.logger
        if (krakenConfig) {
            if (krakenConfig.logs.length > krakenConfig.entriesCount) {
                krakenConfig.logs = krakenConfig.logs.slice(
                    krakenConfig.logs.length - krakenConfig.entriesCount,
                    krakenConfig.logs.length,
                )
            }
            return krakenConfig.logs
        } else {
            ;(krakenConfig as KrakenConfig['logger']).logs = []
            return (krakenConfig as KrakenConfig['logger']).logs
        }
    }

    private render(message: unknown): string {
        if (Array.isArray(message)) {
            return message.map(item => this.render(item)).join('\n')
        }
        if (typeof message === 'string') {
            return message
        }
        if (message instanceof Error) {
            return `Error message: ${message.message}`
        }
        return JSON.stringify(message, null, 2)
    }
}

/**
 * This logger will log result to the console API
 */
export const logger = new DevelopmentLogger(CONSOLE_LOGGER)
