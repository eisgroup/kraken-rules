/* eslint-disable @typescript-eslint/no-explicit-any */
import { Reducer } from 'declarative-js'
import { DecorateAll } from 'decorate-all'
import { AstGeneratingVisitor } from '../../../src/visitor/AstGeneratingVisitor'

export type MethodName = keyof AstGeneratingVisitor
export type Call = { method: MethodName; text: string; customProps?: Record<string, string> }

export type PropListener<T> = {
    name: keyof T
    stringify: <K extends keyof T>(value: T[K]) => string
}

export const MethodListener = (events: Call[], propListeners: PropListener<unknown>[]) => {
    return DecorateAll(
        Listen((method, text, customProps) => events.push({ method, text, customProps }), ...propListeners),
        { deep: true },
    )
}

function Listen(
    listen: (methodName: MethodName, text: string, customProps: Record<string, string>) => void,
    ...propListeners: PropListener<unknown>[]
): (target: unknown, propertyKey: string, descriptor?: PropertyDescriptor) => void {
    return function log(target: any, propertyKey: string, descriptor?: PropertyDescriptor): void {
        const originalMethod = descriptor?.value
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        descriptor!.value = function (this: any, ...args: any[]): unknown {
            // eslint-disable-next-line @typescript-eslint/no-this-alias
            const zis = this
            const cutomP = propListeners.reduce(
                Reducer.toObject(
                    x => x.name as string,
                    x => x.stringify(zis[x.name]),
                ),
                {},
            )
            if (args.length && args.length === 1 && propertyKey.startsWith('visit')) {
                listen(propertyKey as MethodName, args[0].text, cutomP)
            }
            return originalMethod.apply(this, args)
        }
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        Object.defineProperty(target.prototype, propertyKey, descriptor!)
    }
}
