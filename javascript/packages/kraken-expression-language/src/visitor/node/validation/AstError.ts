import { ErrorRange } from "../../../ErrorRange";
import { Node } from "../../KelTraversingVisitor";

export interface AstError {
    message: string;
    node: Node;
    /**
     * if value is `undefined`, then it is impossible to
     * determine range of error node in expression.
     */
    range: ErrorRange | undefined;
}
