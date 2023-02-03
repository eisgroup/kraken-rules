import { ErrorCode, KrakenRuntimeError } from '../../error/KrakenRuntimeError'
import { ExpressionContext, ExpressionContextManager } from './ExpressionContextManager'

export class ExpressionContextManagerImpl implements ExpressionContextManager {
    #expressionContext: Record<string, unknown> | undefined = undefined

    getExpressionContext(): ExpressionContext {
        if (!this.#expressionContext) {
            throw new KrakenRuntimeError(ErrorCode.NO_EXPRESSION_CONTEXT, `Expression context is missing`)
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
