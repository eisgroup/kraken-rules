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

import { debug } from './debugger/Debugger'

/**
 * Configures Kraken evaluation. It can be configured from the console.
 * Open the console and  modify the config options
 * @example
 * global.Kraken.debug = false;
 * global.Kraken.info = false;
 * global.Kraken.debug = false;
 * @see Logger
 * @since 1.1.1
 */
export interface KrakenConfig {
    logger: {
        enabled: boolean
        debug: boolean
        info: boolean
        error: boolean
        warning: boolean
        entriesCount: number
        logs: string[]
        getLogs(): void
        clear(): void
    }
    debugger: debug.api.Debugger
}
export type WithKraken = {
    Kraken: KrakenConfig
}

function init(): void {
    const logger: KrakenConfig['logger'] = {
        enabled: process.env.NODE_ENV !== 'production',
        debug: false,
        info: true,
        error: true,
        warning: true,
        entriesCount: 2000,
        logs: [],
        clear(): void {
            const config = (global as unknown as WithKraken).Kraken
            if (config.logger.logs) {
                config.logger.logs = []
            }
        },
        getLogs(): void {
            const config = (global as unknown as WithKraken).Kraken
            // applicable only for the browser
            if (config.logger.logs && document && window) {
                const blob = new Blob([config.logger.logs.join('\n')], { type: 'text/plain' })
                const url = window.URL.createObjectURL(blob)
                const a = document.createElement('a')
                a.style.display = 'none'
                a.href = url
                a.download = `kraken-logs-${new Date().getTime()}.txt`
                document.body.appendChild(a)
                a.click()
                window.URL.revokeObjectURL(url)
                document.body.removeChild(a)
            }
        },
    }
    if (global) {
        ;(global as unknown as WithKraken).Kraken = {
            logger,
            debugger: new debug.impl.DevToolsDebugger(),
        }
        return
    }
}

function getConfig(): KrakenConfig {
    if (global) {
        return (global as unknown as { Kraken: KrakenConfig })['Kraken']
    }
    throw new Error('Kraken configuration was not initialized')
}

export const krakenConfig = { init, getConfig }
