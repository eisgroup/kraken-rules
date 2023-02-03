/*
 *  Copyright 2019 EIS Ltd and/or one of its affiliates.
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
package kraken.model;

import kraken.annotations.API;
import kraken.model.context.ContextDefinition;
import kraken.model.context.ContextField;

/**
 * Main entity in rules model, defines implementation of single business rule.
 * Rule is identified by name and is defined against {{targetPath}} within {{context}}.
 * Rule can be conditional, if condition is null, it is assumed that rule applies unconditionally.
 */
@API
public interface Rule extends KrakenModelItem, MetadataAware {

    /**
     * Rule variation is a specific definition of Rule logic which is applicable for a particular set of dimensions.
     * <p/>
     * Kraken Engine allows to have multiple Rule definitions with the same {@link #getName()} value
     * when each such Rule is applicable for a different set of dimension values.
     * Each such Rule that has the same name but is applicable for different dimensions is a variation of Rule logic.
     * {@link #getRuleVariationId()} then uniquely identifies a Rule logic variation between all Rule definitions of the same name.
     * <p/>
     * It is not required to be unique between Rule definitions with different names.
     * <p/>
     * In case when Rule does not vary by dimensions then {@link #getRuleVariationId()} can be equal to {@link #getName()}.
     * <p/>
     * This is used by Kraken Engine to perform additional optimisations at runtime.
     * <p/>
     * {@link #getRuleVariationId()} value can be null, in which case Rule will not be identified by variation
     * and additional optimisations will not be applied.
     *
     * @return uniquely identifies Rule variation
     */
    String getRuleVariationId();

    /**
     * @see #getRuleVariationId()
     * @param ruleVariationId which uniquely identifies Rule variation.
     *                        It must be unique in scope of all Rule variations that has same {@link #getName()}.
     */
    void setRuleVariationId(String ruleVariationId);

    /**
     * Returns a description of a rule.
     */
    String getDescription();

    void setDescription(String ruleDescription);

    /**
     * Name of context definition against which rule is defined
     */
    String getContext();

    void setContext(String context);

    /**
     * Rule target, pointing to specific field within context.
     * If not {@link ContextDefinition#isStrict()} then this is direct bean path in entity represented by Context Definition,
     * otherwise this is equal to {@link ContextField#getName()}
     */
    String getTargetPath();

    void setTargetPath(String targetPath);

    /**
     * Defines condition expression. If not null, must evaluate to true for rule to be active
     */
    Condition getCondition();

    void setCondition(Condition condition);

    /**
     * Payload defines actual logic to be executed if rule is active
     */
    Payload getPayload();

    void setPayload(Payload payload);

    /**
     *  A dimensional Rule is a Rule that has different variations for the same {@link #getName()} based on dimension values.
     *  In other words, a dimensional Rule varies by dimensions. A Rule which is not dimensional will never vary
     *  by dimensions and Kraken Engine will perform additional optimizations based on that.
     * <p/>
     * Default is {@code false}.
     *
     * @return is rule dimensional
     */
    @Deprecated(since = "1.40.0", forRemoval = true)
    boolean isDimensional();

    /**
     *  A dimensional Rule is a Rule that has different variations for the same {@link #getName()} based on dimension values.
     *  In other words, a dimensional Rule varies by dimensions. A Rule which is not dimensional will never vary
     *  by dimensions and Kraken Engine will perform additional optimizations based on that.
     *  <p/>
     *  Default is {@code false}.
     *
     * @param dimensional
     */
    @Deprecated(since = "1.40.0", forRemoval = true)
    void setDimensional(boolean dimensional);

    /**
     * Priority is a number which affects which rule should be applied on entity field
     * with respect to other rules on the same field. Higher number indicates a higher priority.
     * <p/>
     * If there are more than one rule for the same field, then the result of the applicable rule with the highest
     * priority is applied while rules with lower priority are not evaluated (unused).
     * <p/>
     * Priority can be a negative number. Priority can be null.
     * If priority is null then it is handled as if the priority is 0 by default.
     * In other words, null priority is equal to 0 priority.
     * Also, null priority is a higher priority than a negative priority.
     * <p/>
     * Currently only supported for defaulting rules.
     * If priority is set for other kind of rule then the rule model validation will return error.
     *
     * @return priority of this rule variation.
     */
    Integer getPriority();

    /**
     * @see #getPriority()
     * @param priority of this rule variation.
     */
    void setPriority(Integer priority);
}
