import { createParser } from "kraken-expression-language-visitor";
import { KelTraversingVisitor } from "../../../src/visitor/KelTraversingVisitor";
import { Call, MethodListener, PropListener } from "./MethodListener";

export type Constructor<T> = abstract new (...args: any[]) => T;

type TestVisitorType = {
    visit: <R = any>(s: string, opts?: { log?: boolean }) => { result: R, events: Call[] };
};

export function createTestVisitor<T extends KelTraversingVisitor>(
    Visitor: Constructor<T>,
    createOpts?: {
        constructorParams?: unknown[]
        listenClassProps?: PropListener<T>[]
    }
): TestVisitorType {
    return {
        visit(expression: string, opts?: { log?: boolean }): { result: any, events: Call[] } {
            const events: Call[] = [];
            @MethodListener(events, createOpts?.listenClassProps ?? [])
            class TestVisitor extends (Visitor as any) {
                constructor(...p: any[]) {
                    super(...p);
                }
            }
            const testVisitor = new TestVisitor(...createOpts?.constructorParams ?? []);
            const parse = (ex: string) => createParser(ex).expression();
            const tree = parse(expression);
            const result = testVisitor.visit(tree);
            if (opts?.log) {
                console.log({ result });
                console.log(
                    events.map(
                        e => {
                            const add = e.customProps && Object.keys(e.customProps).length;
                            return `${e.method} ( ${e.text} )${add ? ` [${JSON.stringify(e.customProps)}]` : ""}`;
                        }
                    )
                );
            }
            return { result, events };
        }
    };
}
