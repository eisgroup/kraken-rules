import {
    ANTLRErrorListener, RecognitionException, Recognizer, RuleContext, Token,
    TokenStream,
} from 'antlr4ts';

import { Kel } from './antlr/generated/Kel';

export class KrakenParser extends Kel {

    private el: KrakenErrorListener

    constructor(input: TokenStream) {
        super(input);
        this.removeErrorListeners();
        const errorListener = new KrakenErrorListener();
        this.addErrorListener(errorListener);
        this.el = errorListener;
    }
    getSyntaxErrors(): SyntaxError[] {
        return this.el.getSyntaxErrors();
    }
}

export class KrakenErrorListener implements ANTLRErrorListener<Token>{

    private errors: SyntaxError[] = [];

    syntaxError(
        _recognizer: Recognizer<Token, any>,
        offendingSymbol: Token | undefined,
        _line: number,
        _charPositionInLine: number,
        msg: string,
        e: RecognitionException | undefined
    ) {
        this.errors.push({ context: e?.context, message: msg, offendingSymbol: offendingSymbol });
    }

    getSyntaxErrors(): SyntaxError[] {
        return this.errors;
    }
}

export interface SyntaxError {
    context?: RuleContext,
    message: string,
    offendingSymbol?: Token
}