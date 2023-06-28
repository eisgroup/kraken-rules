/* eslint-disable @typescript-eslint/ban-ts-comment */
import { debug } from '../../src/debugger/Debugger'

describe('debug', () => {
    describe('impl', () => {
        describe('DevToolsDebugger', () => {
            describe('debugRule', () => {
                it('should throw an error if property is incorrect', () => {
                    const d = new debug.impl.DevToolsDebugger()
                    //@ts-ignore
                    expect(() => d.debugRule({ contextIds: '' })).toThrow('contextId')
                    //@ts-ignore
                    expect(() => d.debugRule({ entryPoint: '' })).toThrow('entryPointName')
                    //@ts-ignore
                    expect(() => d.debugRule({ contexName: '' })).toThrow('contextName')
                    //@ts-ignore
                    expect(() => d.debugRule({ rule: '' })).toThrow('ruleName')
                })
            })
            describe('debugEntryPoint', () => {
                it('should throw an error if property is incorrect', () => {
                    const d = new debug.impl.DevToolsDebugger()
                    //@ts-ignore
                    expect(() => d.debugEntryPoint({ entryPoint: '' })).toThrow('entryPointName')
                })
            })
        })
        describe('BreakPointMatcher', () => {
            describe('ruleMatches', () => {
                it('should return false if break is disabled', () => {
                    const d = new debug.impl.DevToolsDebugger()
                    d.debugRule({})
                    const matcher = new debug.impl.BreakPointMatcher(d.breakPoints, { break: false, log: false })
                    expect(
                        matcher.ruleMatches({
                            ruleName: 'rn',
                            contextId: '1',
                            contextName: 'Policy',
                            entryPointName: 'epn',
                        }),
                    ).toBe(false)
                })
                it('should match if rule name is undefined', () => {
                    const d = new debug.impl.DevToolsDebugger()
                    d.debugRule({ ruleName: undefined })
                    const matcher = new debug.impl.BreakPointMatcher(d.breakPoints, { ...d })

                    expect(
                        matcher.ruleMatches({
                            ruleName: 'rn',
                            contextId: '1',
                            contextName: 'Policy',
                            entryPointName: 'epn',
                        }),
                    ).toBe(true)
                })
                it('should match if no filters defined', () => {
                    const d = new debug.impl.DevToolsDebugger()
                    d.debugRule({})
                    const matcher = new debug.impl.BreakPointMatcher(d.breakPoints, { ...d })

                    expect(
                        matcher.ruleMatches({
                            ruleName: 'rn',
                            contextId: '1',
                            contextName: 'Policy',
                            entryPointName: 'epn',
                        }),
                    ).toBe(true)
                })
                it('should match defined entrypoint', () => {
                    const d = new debug.impl.DevToolsDebugger()
                    d.debugRule({ entryPointName: 'epn' })
                    const matcher = new debug.impl.BreakPointMatcher(d.breakPoints, { ...d })

                    expect(
                        matcher.ruleMatches({
                            ruleName: 'rn',
                            contextId: '1',
                            contextName: 'Policy',
                            entryPointName: 'epn',
                        }),
                    ).toBe(true)
                })
                it('should match defined entrypoint and rule', () => {
                    const d = new debug.impl.DevToolsDebugger()
                    d.debugRule({ entryPointName: 'epn', ruleName: 'rn' })
                    const matcher = new debug.impl.BreakPointMatcher(d.breakPoints, { ...d })

                    expect(
                        matcher.ruleMatches({
                            ruleName: 'rn',
                            contextId: '1',
                            contextName: 'Policy',
                            entryPointName: 'epn',
                        }),
                    ).toBe(true)
                })
                it('should match defined entrypoint and rule and context name', () => {
                    const d = new debug.impl.DevToolsDebugger()
                    d.debugRule({ entryPointName: 'epn', ruleName: 'rn', contextName: 'Policy' })
                    const matcher = new debug.impl.BreakPointMatcher(d.breakPoints, { ...d })

                    expect(
                        matcher.ruleMatches({
                            ruleName: 'rn',
                            contextId: '1',
                            contextName: 'Policy',
                            entryPointName: 'epn',
                        }),
                    ).toBe(true)
                })
                it('should match defined entrypoint and rule and context name and context id', () => {
                    const d = new debug.impl.DevToolsDebugger()
                    d.debugRule({ entryPointName: 'epn', ruleName: 'rn', contextName: 'Policy', contextId: '1' })
                    const matcher = new debug.impl.BreakPointMatcher(d.breakPoints, { ...d })

                    expect(
                        matcher.ruleMatches({
                            ruleName: 'rn',
                            contextId: '1',
                            contextName: 'Policy',
                            entryPointName: 'epn',
                        }),
                    ).toBe(true)
                })
                it('should not match defined entrypoint and rule and context name and context id', () => {
                    const d = new debug.impl.DevToolsDebugger()
                    d.debugRule({ entryPointName: 'epn', ruleName: 'rn', contextName: 'Policy', contextId: '1' })
                    const matcher = new debug.impl.BreakPointMatcher(d.breakPoints, { ...d })

                    expect(
                        matcher.ruleMatches({
                            ruleName: 'rn',
                            contextId: '1',
                            contextName: 'Policy',
                            entryPointName: '-',
                        }),
                    ).toBe(false)
                    expect(
                        matcher.ruleMatches({
                            ruleName: 'rn',
                            contextId: '1',
                            contextName: '-',
                            entryPointName: '-',
                        }),
                    ).toBe(false)
                })
            })
            describe('entryPointMatches', () => {
                it('should match defined entryPoint', () => {
                    const d = new debug.impl.DevToolsDebugger()
                    d.debugEntryPoint({ entryPointName: 'test' })
                    const matcher = new debug.impl.BreakPointMatcher(d.breakPoints, { ...d })

                    expect(matcher.entryPointMatches({ entryPointName: 'test' })).toBe(true)
                })
                it('should match with no entryPoint', () => {
                    const d = new debug.impl.DevToolsDebugger()
                    d.debugEntryPoint({})
                    const matcher = new debug.impl.BreakPointMatcher(d.breakPoints, { ...d })

                    expect(matcher.entryPointMatches({ entryPointName: 'test' })).toBe(true)
                })
                it('should not match entryPoint', () => {
                    const d = new debug.impl.DevToolsDebugger()
                    d.debugEntryPoint({ entryPointName: 'test' })
                    const matcher = new debug.impl.BreakPointMatcher(d.breakPoints, { ...d })

                    expect(matcher.entryPointMatches({ entryPointName: '-' })).toBe(false)
                })
            })
            describe('breakpoints', () => {
                it('should generate incremental ids', () => {
                    const d = new debug.impl.DevToolsDebugger()
                    d.debugRule({})
                    const id2 = d.debugRule({})
                    d.debugRule({})
                    d.breakPoints.delete(id2)
                    const id4 = d.debugRule({})
                    expect(id4).toBe('4-rule')
                })
            })
        })
    })
})
