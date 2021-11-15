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

import kraken.model.dsl.KrakenDSL.RuleImportContext;
import kraken.model.dsl.KrakenDSLBaseVisitor;
import kraken.model.dsl.KrakenDSL;
import kraken.model.dsl.model.DSLImportReference;
import org.antlr.v4.runtime.RuleContext;

import java.util.Set;
import java.util.stream.Collectors;

import static kraken.el.ast.builder.Literals.escape;
import static kraken.el.ast.builder.Literals.stripQuotes;

/**
 * A {@link KrakenDSLBaseVisitor< DSLImportReference >} extension that parses the information
 * about imports from namespaces for the current DSL model.
 *
 * @author avasiliauskas
 */
public final class DSLImportReferenceVisitor extends KrakenDSLBaseVisitor<DSLImportReference> {

    @Override
    public DSLImportReference visitRuleImport(RuleImportContext ctx) {
        Set<String> importNames = ctx.ruleNames().ruleName().stream()
                .map(RuleContext::getText)
                .map(ruleName -> escape(stripQuotes(ruleName)))
                .collect(Collectors.toSet());
        return new DSLImportReference(importNames, ctx.namespaceName().getText());
    }
}
