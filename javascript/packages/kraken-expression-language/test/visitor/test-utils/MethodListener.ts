import { Reducer } from "declarative-js";
import { DecorateAll } from "decorate-all";
import { KelTraversingVisitor } from "../../../src/visitor/KelTraversingVisitor";

export type MethodName = keyof KelTraversingVisitor;
export type Call = { method: MethodName, text: string, customProps?: Record<string, string> };

export type PropListener<T> = {
    name: keyof T, stringify: <K extends keyof T>(value: T[K]) => string
};

export const MethodListener = (events: Call[], propListeners: PropListener<any>[]) => {
    return DecorateAll(Listen((method, text, customProps) =>
        events.push({ method, text, customProps }), ...propListeners), { deep: true });
};

function Listen(
    listen: (
        methodName: MethodName,
        text: string,
        customProps: Record<string, string>
    ) => void,
    ...propListeners: PropListener<any>[]
):
    (target: any, propertyKey: string, descriptor?: PropertyDescriptor) => void {
    return function log(target: any, propertyKey: string, descriptor?: PropertyDescriptor): void {
        const originalMethod = descriptor!.value;
        descriptor!.value = function (this: any, ...args: any[]): any {
            const zis = this;
            const cutomP = propListeners
                .reduce(Reducer.toObject(x => x.name as string, x => x.stringify(zis[x.name])), {});
            if (args.length) {
                    listen(propertyKey as MethodName, args[0].text, cutomP);
                }
            return originalMethod.apply(this, args);
        };
        Object.defineProperty(target.prototype, propertyKey, descriptor!);
    };
}
