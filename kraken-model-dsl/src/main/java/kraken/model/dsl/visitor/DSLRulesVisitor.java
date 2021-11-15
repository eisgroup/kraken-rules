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

import kraken.model.dsl.KrakenDSLBaseVisitor;
import kraken.model.dsl.KrakenDSL;
import kraken.model.dsl.model.DSLAnnotation;
import kraken.model.dsl.model.DSLRule;
import kraken.model.dsl.model.DSLRules;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author mulevicius
 */
public class DSLRulesVisitor extends KrakenDSLBaseVisitor<DSLRules> {

    private DSLRuleVisitor ruleVisitor = new DSLRuleVisitor();

    private DSLAnnotationVisitor annotationVisitor = new DSLAnnotationVisitor();

    @Override
    public DSLRules visitRules(KrakenDSL.RulesContext ctx) {
        DSLAnnotation annotation = ctx.annotations() != null ?
                annotationVisitor.visit(ctx.annotations()) : DSLAnnotation.EMPTY;

        Collection<DSLRule> rules = ctx.aRule().stream()
                .map(ruleContext -> ruleVisitor.visit(ruleContext))
                .collect(Collectors.toList());

        Collection<DSLRules> ruleBlocks = ctx.rules().stream()
                .map(this::visitRules)
                .collect(Collectors.toList());

        return new DSLRules(rules, annotation.getMetadata(), ruleBlocks);
    }

}
