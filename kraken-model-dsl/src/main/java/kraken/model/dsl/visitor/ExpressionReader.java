/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package kraken.model.dsl.visitor;

import kraken.model.dsl.KrakenDSL;
import kraken.model.dsl.model.DSLExpression;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;

/**
 * @author mulevicius
 */
public class ExpressionReader {

    private ExpressionReader() {
    }

    /**
     * Reads {@link DSLExpression} from embedded expression node in parse tree;
     * expression text will be preserved exactly as written in Kraken DSL
     * 
     * @param context
     * @return
     */
    public static DSLExpression read(KrakenDSL.InlineExpressionContext context) {
        Token start = context.getStart();
        Token stop = context.getStop();
        CharStream cs = start.getTokenSource().getInputStream();
        int stopIndex = stop != null ? stop.getStopIndex() : -1;
        return new DSLExpression(cs.getText(new Interval(start.getStartIndex(), stopIndex)));
    }
}
