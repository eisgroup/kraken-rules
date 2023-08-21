import { KrakenRuntimeError, NO_EXPRESSION_CONTEXT, SystemMessageBuilder } from '../../error/KrakenRuntimeError'
import { ExpressionContext, ExpressionContextManager } from './ExpressionContextManager'

export class ExpressionContextManagerImpl implements ExpressionContextManager {
    #expressionContext: Record<string, unknown> | undefined = undefined

    getExpressionContext(): ExpressionContext {
        if (!this.#expressionContext) {
            const m = new SystemMessageBuilder(NO_EXPRESSION_CONTEXT).build()
            throw new KrakenRuntimeError(m)
        }
        return this.#expressionContext
    }
    setExpressionContext(expressionContext: Record<string, unknown>): void {
        this.#expressionContext = expressionContext
    }
    clearExpressionContext(): void {
        this.#expressionContext = undefined
    }
    isExpressionContextPresent(): boolean {
        return this.#expressionContext !== undefined
    }
}
