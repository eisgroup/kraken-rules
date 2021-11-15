/*
 *  Copyright 2017 EIS Ltd and/or one of its affiliates.
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
package kraken.el.scope;

import kraken.el.ast.Identifier;
import kraken.el.ast.builder.AstBuilder;
import kraken.el.scope.type.Type;

/**
 * Indicates a kind of {@link Scope}.
 * <p/>
 * Knowing a {@link ScopeType} of {@link Scope} could be useful during expression translation
 * when implementing {@link kraken.el.ExpressionLanguage}.
 * <p/>
 * For example, certain evaluation language may have different semantics of variable scoping for different language
 * constructs (like filtering, mapping or looping) and this can be addressed
 * during translation by checking what kind of {@link Scope} is the source of the symbol referenced in expression.
 *
 * @author mulevicius
 */
public enum ScopeType {

    /**
     * Indicates a {@link Scope} which is global throughout expression and every symbol available in global scope
     * can be accessed from anywhere in expression unless that symbol is shadowed by child scope
     * of {@link ScopeType#FILTER}, {@link ScopeType#FOR_RETURN_EXPRESSION}, {@link ScopeType#LOCAL}
     * or scope has no parent, like {@link ScopeType#PATH}.
     * <p/>
     * Global scope is never created by {@link AstBuilder}.
     * This type of scope must be created externally and supplied to {@link AstBuilder#from(String, Scope)}
     */
    GLOBAL,

    /**
     * Indicates a generic {@link Scope} that may or may not be nested in {@link ScopeType#GLOBAL} scope.
     * In general, semantics for referring to a variable from local scope are the same as referring to a variable from global scope.
     * <p/>
     * Local scope is never created by {@link AstBuilder}.
     * This type of scope must be created externally and supplied to {@link AstBuilder#from(String, Scope)}.
     * <p/>
     * A general pattern is creating a local scope that wraps global scope as it's parent.
     * This can be useful when there is a need to differentiate between global variables and local variables during translation.
     * For example, certain evaluation languages may have different sources of globally referable variables
     * and during translation we may want to translate references of those variables differently
     * when they are referred to from {@link ScopeType#GLOBAL} or {@link ScopeType#LOCAL} scope.
     */
    LOCAL,

    /**
     * Indicates a {@link Scope} that has accessible properties of particular {@link Type} that are accessed by path expression,
     * like 'path.to.property'.
     * <p/>
     * {@link ScopeType#PATH} is created for each path element by {@link AstBuilder} when path expression is encountered.
     * For example, for 'Coverage.limitAmount' expression a {@link Scope} of {@link ScopeType#PATH} will be created
     * for {@link Identifier} 'Coverage' and the {@link Scope} will have all the properties of object referred to by 'Coverage'.
     * <p/>
     * {@link Scope} of {@link ScopeType#PATH} will have NO parent {@link Scope}s,
     * because only object properties can be referred to by path expression.
     */
    PATH,

    /**
     * Indicates a {@link Scope} that has properties accessible in collection item which is being filtered
     * in filter expression, like 'coverages[limitAmount > 100]'.
     * <p/>
     * {@link ScopeType#FILTER} is created by {@link AstBuilder} when filter expression is encountered.
     * For example, for 'coverages[limitAmount > 100]' expression a {@link Scope} of {@link ScopeType#FILTER} will be created
     * as a scope of filter expression 'limitAmount > 100' and the {@link Scope} will have all the properties available
     * of item in collection that is referred to by 'coverages'.
     * <p/>
     * {@link Scope} will also have the parent scope (which can be any scope of any type except {@link ScopeType#PATH})
     * available that may be referred to if current type of filter scope does not have referred property.
     *
     */
    FILTER,

    /**
     * Indicates a {@link Scope} that has looping variable accessible by name as defined in looping expression,
     * like 'for c in coverages return c.limitAmount'.
     * <p/>
     * {@link ScopeType#FOR_RETURN_EXPRESSION} is created by {@link AstBuilder} when loop expression is encountered.
     * For example, for 'for c in coverages return c.limitAmount' expression a {@link Scope} of
     * {@link ScopeType#FOR_RETURN_EXPRESSION} will be created for expression in 'return' section and the {@link Scope} will
     * have a variable with name 'c' which will have all the properties of item in collection referred to by 'coverages'.
     * <p/>
     * {@link Scope} will also have parent scope (which can be any scope of any type except {@link ScopeType#PATH})
     * available that may be referred to if current type of loop scope does not have referred property.
     */
    FOR_RETURN_EXPRESSION
}
