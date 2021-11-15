import { CharStreams, CommonTokenStream } from "antlr4ts";
import { KrakenLexer } from "./KrakenLexer";
import { Kel } from './antlr/generated/Kel'
import { KrakenParser } from "./KrakenParser";

/**
 * Creates Parses for KEL expression, which arses expression to tree based structure with antlr4 generated context.
 * This tree must be traversed with visitor {@link KelVisitor} implementation.
 * 
 * ```
 * // example.ts
 * export class ValidationVisitor implements KelVisitor<string> { ... }
 * const error = new ValidationVisitor().visit(tree)
 * ```
 *
 * @export
 * @param {string} expression
 * @returns
 */
export function createParser(typeToken: string): KrakenParser {
    const inputStream = CharStreams.fromString(typeToken);
    const lexer = new KrakenLexer(inputStream)
    const tokenStream = new CommonTokenStream(lexer);
    const parser = new KrakenParser(tokenStream)
    return parser;
}