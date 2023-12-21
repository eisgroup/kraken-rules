import { logger } from '../utils/DevelopmentLogger'

export namespace debug {
    export namespace api {
        export interface DebugRuleOptions {
            entryPointName?: string
            ruleName?: string
            contextName?: string
            contextId?: string
        }
        export interface DebugEntryPointOptions {
            entryPointName?: string
        }

        export type DebugOptions =
            | { type: 'rule'; options: DebugRuleOptions }
            | { type: 'entryPoint'; options: DebugEntryPointOptions }

        export interface Debugger {
            log: boolean
            break: boolean
            breakPoints: Map<string, debug.api.DebugOptions>
            help(): void
            debugRule(options: DebugRuleOptions): string
            debugEntryPoint(options: DebugEntryPointOptions): string
        }
    }
    export namespace impl {
        const ruleKeyObject: Record<keyof api.DebugRuleOptions, unknown> = {
            contextId: '',
            contextName: '',
            entryPointName: '',
            ruleName: '',
        }
        const ruleKeys = Object.keys(ruleKeyObject)
        const entryPointKeyObject: Record<keyof api.DebugEntryPointOptions, unknown> = {
            entryPointName: '',
        }
        const entryPointKeys = Object.keys(entryPointKeyObject)

        export class DevToolsDebugger implements debug.api.Debugger {
            breakPoints: Map<string, debug.api.DebugOptions>
            #i: number

            log: boolean
            break: boolean

            constructor(previous?: debug.api.Debugger) {
                this.breakPoints = previous ? previous.breakPoints : new Map()
                this.#i = previous ? previous.breakPoints.size + 1 : 1
                this.log = previous ? previous.log : false
                this.break = previous ? previous.break : true
            }

            debugRule(options: debug.api.DebugRuleOptions) {
                this.#validate(ruleKeys, options)
                return this.#addBreakPoint({ type: 'rule', options })
            }
            debugEntryPoint(options: api.DebugEntryPointOptions) {
                this.#validate(entryPointKeys, options)
                return this.#addBreakPoint({ type: 'entryPoint', options })
            }
            help(): void {
                const bps: Record<string, unknown> = {}
                this.breakPoints.forEach((bp, key) => (bps[key] = bp.options))

                console.groupCollapsed('How to use?')
                console.log(`
🐙 Interface

// The debugger interface which allows you to create conditional breakpoints
interface Debugger {
    // enables logging of breakpoint identifier in the Kraken logs
    // logging identifier can help to navigate in the logs
    // default value is false
    log: boolean

    // default value is true
    break: boolean
    
    breakPoints: Map<string, debug.api.DebugOptions>

    help(): void

    // create a conditional breakpoint to debug rule evaluation
    // returns breakpoint identifier
    debugRule(options: {
        entryPointName?: string
        ruleName?: string
        contextName?: string
        contextId?: string
    }): string
    
    // create a conditional breakpoint to debug entryPoint content 
    // returns breakpoint identifier
    debugEntryPoint(options: {entryPointName?: string}): string
}


🐙 Example usage:
Kraken.debugger.help()
Kraken.debugger.debugEntryPoint({entryPointName: 'Policy:DataGather'}) // entryPoint-1
Kraken.debugger.debugRule({ruleName: 'Validate code'}) // rule-2
Kraken.debugger.debugRule({ruleName: 'Required zip code', contextName: 'BillingAddress'}) // rule-3
Kraken.debugger.breakPoints.delete('rule-2') // delete breakpoint by id 'rule-2'
Kraken.debugger.breakPoints.clear() // delete all breakpoints
Kraken.debugger.log = true // enable logging on matching breakpoints. Now you can  search for breakpoint ids in the logs 
Kraken.debugger.break = false // disable stopping on all breakpoints
`)
                console.groupEnd()

                console.groupCollapsed('Breakpoints and toggles')
                console.log(`
                

🐙 Breakpoints`)
                if (this.breakPoints.size) {
                    console.table(bps)
                } else {
                    console.log('No breakpoints found. Forgot to add?')
                }
                console.log(`
                

🐙 Toggles`)
                console.table({ log: this.log, break: this.break })
                console.groupEnd()
            }
            #addBreakPoint = (options: debug.api.DebugOptions) => {
                const id = `${this.#i++}-${options.type}`
                this.breakPoints.set(id, options)
                return id
            }

            #validate = (keys: string[], userOptions: api.DebugRuleOptions | api.DebugEntryPointOptions) => {
                const unknownPropertyNames = []
                for (const propertyName of Object.keys(userOptions)) {
                    if (!keys.includes(propertyName)) {
                        unknownPropertyNames.push(propertyName)
                    }
                }
                if (unknownPropertyNames.length) {
                    throw new Error(
                        `Invalid property names: '${unknownPropertyNames.join(
                            ', ',
                        )}'. Available properties are: '${keys.join(', ')}'`,
                    )
                }
            }
        }

        export class BreakPointMatcher {
            constructor(
                private readonly breakPoints: Map<string, api.DebugOptions>,
                public readonly actions: { break: boolean; log: boolean },
            ) {}

            entryPointMatches(matchAgainst: Required<api.DebugEntryPointOptions>): boolean {
                for (const breakpointEntry of this.breakPoints.entries()) {
                    const [id, breakpoint] = breakpointEntry
                    if (breakpoint.type === 'entryPoint') {
                        if (
                            !breakpoint.options.entryPointName ||
                            breakpoint.options.entryPointName === matchAgainst.entryPointName
                        ) {
                            return this.break(id, breakpoint)
                        }
                    }
                }
                return false
            }

            ruleMatches(matchAgainst: Required<api.DebugRuleOptions>): boolean {
                for (const breakpointEntry of this.breakPoints.entries()) {
                    const [id, breakpoint] = breakpointEntry
                    if (breakpoint.type === 'rule') {
                        const keysInBreakpointOptions = Object.keys(breakpoint.options)
                        const matched = keysInBreakpointOptions.every(againstKey => {
                            const key = againstKey as keyof api.DebugRuleOptions
                            return !breakpoint.options[key] || breakpoint.options[key] === matchAgainst[key]
                        })
                        if (matched) {
                            return this.break(id, breakpoint)
                        }
                    }
                }
                return false
            }

            private break(id: string, breakPoint: api.DebugOptions): boolean {
                if (this.actions.log) {
                    logger.debug(() => `Breakpoint '${id}' matched. (${JSON.stringify(breakPoint)})`)
                }
                if (this.actions.break) {
                    return true
                }
                return false
            }
        }
    }
}
