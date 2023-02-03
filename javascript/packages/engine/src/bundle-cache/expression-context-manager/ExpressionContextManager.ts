export type ExpressionContext = Record<string, unknown>

export interface ExpressionContextManager {
    /**
     * Caches expression context cache.
     * Expression context cache can be cleared using {@link #clearExpressionContext}
     *
     * @since 1.38.0
     */
    setExpressionContext(expressionContext: Record<string, unknown>): void

    /**
     * Invalidates expression context cache.
     * After this, cache must be set with {@link #setExpressionContext} before rules can be evaluated.
     *
     * @since 1.38.0
     */
    clearExpressionContext(): void

    /**
     * @return true if expression context is cached
     * @since 1.38.0
     */
    isExpressionContextPresent(): boolean

    /**
     * @throws {@link KrakenRuntimeError}
     * @since 1.40.0
     */
    getExpressionContext(): ExpressionContext
}
