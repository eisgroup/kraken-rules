import { Common } from './antlr/generated/Common'

export class KelLexer extends Common {
    /** @override */
    popMode(): number {
        return this._modeStack.isEmpty ? Common.DEFAULT_MODE : super.popMode()
    }
}
