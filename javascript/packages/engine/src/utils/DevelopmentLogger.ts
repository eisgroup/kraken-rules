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
export interface Logger {
    groupEnd(name: string): void
    group(name: string): void
    info(...msg: unknown[]): void
    warning(...msg: unknown[]): void
    error(...msg: unknown[]): void
    debug(...msg: unknown[]): void
}
export class DevelopmentLogger implements Logger {
    public static CONSOLE = new DevelopmentLogger({
        groupEnd: console.groupEnd,
        group: (name: string) => (console.groupCollapsed ? console.groupCollapsed(name) : console.group(name)),
        debug: console.debug,
        info: console.log,
        warning: console.warn,
        error: console.error,
    })

    private logger: Logger
    private isLoggerEnabled: boolean

    constructor(l: Logger) {
        this.logger = l
        this.isLoggerEnabled = process.env.NODE_ENV !== 'production'
    }
    groupEnd(name: string): void {
        if (this.isLoggerEnabled) {
            this.addEntry('end  ', name)
            this.logger.groupEnd(name)
        }
    }
    group(name: string): void {
        if (this.isLoggerEnabled) {
            this.addEntry('start', name)
            this.logger.group(name)
        }
    }
    debug(...msg: unknown[]): void {
        if (this.isLoggerEnabled && config.getConfig().logger.debug) {
            this.addEntry('debug', msg.join(' '))
            this.logger.debug(...msg)
        }
    }
    info(...msg: unknown[]): void {
        if (this.isLoggerEnabled && config.getConfig().logger.info) {
            this.addEntry('info ', msg)
            this.logger.info(...msg)
        }
    }
    warning(...msg: unknown[]): void {
        if (this.isLoggerEnabled && config.getConfig().logger.warning) {
            this.addEntry('warn ', msg)
            this.logger.warning(...msg)
        }
    }
    error(...msg: unknown[]): void {
        if (this.isLoggerEnabled && config.getConfig().logger.error) {
            this.addEntry('error', msg)
            this.logger.error(...msg)
        }
    }
    isEnabled(): boolean {
        return this.isLoggerEnabled
    }

    private addEntry(type: string, e: unknown): void {
        if (global) {
            let logs
            const krakenConfig = (global as unknown as { Kraken: KrakenConfig }).Kraken.logger
            if (krakenConfig) {
                if (krakenConfig.logs.length > krakenConfig.entriesCount) {
                    krakenConfig.logs = krakenConfig.logs.slice(
                        krakenConfig.logs.length - krakenConfig.entriesCount,
                        krakenConfig.logs.length,
                    )
                }
                logs = krakenConfig.logs
            } else {
                ;(krakenConfig as KrakenConfig['logger']).logs = []
                logs = (krakenConfig as KrakenConfig['logger']).logs
            }
            let entryString = ''
            if (Array.isArray(e)) {
                entryString = e.map(x => JSON.stringify(x, null, 2)).join('  \n')
            } else {
                entryString = JSON.stringify(e, null, 2)
            }
            logs.push(`${type} : ${entryString}`)
        }
    }
}

/**
 * This logger will log result to the console API
 */
export const logger = DevelopmentLogger.CONSOLE
