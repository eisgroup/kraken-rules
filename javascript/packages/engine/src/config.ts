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

const watchedFields = ['enabled', 'debug', 'info', 'error', 'warning', 'entriesCount', 'log', 'break']
const watchedMethods = ['debugRule', 'debugEntryPoint', 'delete', 'clear']
const nestedObjects = ['logger', 'debugger', 'breakPoints']

const handler: ProxyHandler<object> = {
    get(target: object, key: string | symbol): object {
        let value = Reflect.get(target, key)

        if (typeof value === 'object' && value !== null && nestedObjects.some(nested => nested.includes(String(key)))) {
            return new Proxy(Reflect.get(target, key), handler)
        }

        if (typeof value === 'function') {
            value = value.bind(target)
            return new Proxy(value, handler)
        }

        return value
    },
    set(target: object, key: string | symbol, value: unknown): boolean {
        Reflect.set(target, key, value)

        if (watchedFields.some(watchedField => watchedField.includes(String(key)))) {
            const config = { ...(global as unknown as WithKraken).Kraken }
            config.logger.logs = []

            sessionStorage.setItem('krakenLogger', JSON.stringify(config, replacer))
        }

        return true
    },
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    apply(target: object, thisArg: any, argArray: any[]): any {
        if (typeof target === 'function') {
            const applied = Reflect.apply(target, thisArg, argArray)

            if (watchedMethods.some(watchedMethod => target.name.includes(watchedMethod))) {
                const config = { ...(global as unknown as WithKraken).Kraken }
                config.logger.logs = []

                sessionStorage.setItem('krakenLogger', JSON.stringify(config, replacer))
            }

            return applied
        }

        throw Error('Unable to call a function. Target is not a function.')
    },
}

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
    const savedConfig = getFromSessionStorage()
    const logger: KrakenConfig['logger'] = {
        enabled: process.env.NODE_ENV !== 'production',
        debug: savedConfig ? savedConfig.logger.debug : false,
        info: savedConfig ? savedConfig.logger.info : true,
        error: savedConfig ? savedConfig.logger.error : true,
        warning: savedConfig ? savedConfig.logger.warning : true,
        entriesCount: savedConfig ? savedConfig.logger.entriesCount : 2000,
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
        if (typeof window !== 'undefined') {
            ;(global as unknown as WithKraken).Kraken = new Proxy<KrakenConfig>(
                {
                    logger,
                    debugger: new debug.impl.DevToolsDebugger(savedConfig?.debugger),
                },
                handler,
            )
        } else {
            ;(global as unknown as WithKraken).Kraken = {
                logger,
                debugger: new debug.impl.DevToolsDebugger(),
            }
        }

        return
    }
}

function getConfig(): KrakenConfig {
    if (global) {
        return getFromSessionStorage() ?? (global as unknown as { Kraken: KrakenConfig })['Kraken']
    }
    throw new Error('Kraken configuration was not initialized')
}

function getFromSessionStorage(): KrakenConfig | undefined {
    if (typeof window !== 'undefined') {
        if (sessionStorage.getItem('krakenLogger')) {
            return JSON.parse(sessionStorage.getItem('krakenLogger') ?? '', reviver)
        }
    }

    return undefined
}

type ValueContainer = {
    dataType: string
    value: unknown
}

function replacer(_key: unknown, value: unknown) {
    if (value instanceof Map) {
        return {
            dataType: 'Map',
            value: [...value],
        } as ValueContainer
    } else {
        return value
    }
}

function reviver(_key: unknown, value: unknown) {
    if (isValueContainer(value)) {
        if (value.dataType === 'Map') {
            return new Map(value.value as [unknown, unknown][])
        }
    }

    return value
}

function isValueContainer(value: unknown): value is ValueContainer {
    return typeof value === 'object' && value !== null && 'dataType' in value
}

export const krakenConfig = { init, getConfig }
