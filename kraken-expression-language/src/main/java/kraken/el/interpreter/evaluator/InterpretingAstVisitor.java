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
package kraken.el.interpreter.evaluator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import kraken.el.EvaluationContext;
import kraken.el.ExpressionEvaluationException;
import kraken.el.ExpressionLanguageConfiguration;
import kraken.el.accelerated.ReflectionsCache;
import kraken.el.ast.AccessByIndex;
import kraken.el.ast.Addition;
import kraken.el.ast.And;
import kraken.el.ast.BooleanLiteral;
import kraken.el.ast.Cast;
import kraken.el.ast.CollectionFilter;
import kraken.el.ast.DateLiteral;
import kraken.el.ast.DateTimeLiteral;
import kraken.el.ast.Division;
import kraken.el.ast.Empty;
import kraken.el.ast.Equals;
import kraken.el.ast.Exponent;
import kraken.el.ast.Expression;
import kraken.el.ast.ValueBlock;
import kraken.el.ast.ForEach;
import kraken.el.ast.ForEvery;
import kraken.el.ast.ForSome;
import kraken.el.ast.Function;
import kraken.el.ast.Identifier;
import kraken.el.ast.If;
import kraken.el.ast.In;
import kraken.el.ast.InlineArray;
import kraken.el.ast.InlineMap;
import kraken.el.ast.InstanceOf;
import kraken.el.ast.LessThan;
import kraken.el.ast.LessThanOrEquals;
import kraken.el.ast.MatchesRegExp;
import kraken.el.ast.Modulus;
import kraken.el.ast.MoreThan;
import kraken.el.ast.MoreThanOrEquals;
import kraken.el.ast.Multiplication;
import kraken.el.ast.Negation;
import kraken.el.ast.Negative;
import kraken.el.ast.NotEquals;
import kraken.el.ast.Null;
import kraken.el.ast.NumberLiteral;
import kraken.el.ast.Or;
import kraken.el.ast.Path;
import kraken.el.ast.ReferenceValue;
import kraken.el.ast.StringLiteral;
import kraken.el.ast.Subtraction;
import kraken.el.ast.This;
import kraken.el.ast.TypeOf;
import kraken.el.ast.Variable;
import kraken.el.ast.visitor.QueuedAstVisitor;
import kraken.el.functionregistry.FunctionInvoker;
import kraken.el.scope.Scope;
import kraken.el.scope.ScopeType;

/**
 * @author mulevicius
 */
public class InterpretingAstVisitor extends QueuedAstVisitor<Value> {

    private final Stack stack;

    private final boolean strictTypeMode;

    private final boolean automaticFunctionIteration;

    private final FunctionInvoker functionInvoker;

    public InterpretingAstVisitor(Scope scope,
                                  EvaluationContext evaluationContext,
                                  ExpressionLanguageConfiguration configuration) {
        this.stack = new Stack();
        this.stack.push(evaluationContext.getVariables(), scope, false);
        this.stack.pushChildBlock(evaluationContext.getDataObject(), scope, true);

        this.strictTypeMode = configuration.isStrictTypeMode();
        this.automaticFunctionIteration = configuration.isAllowAutomaticIterationWhenInvokingFunctions();
        this.functionInvoker = evaluationContext.getFunctionInvoker();
    }

    @Override
    public Value visit(Cast cast) {
         return visit(cast.getReference());
    }

    @Override
    public Value visit(InstanceOf e) {
        return visit(e.getLeft()).isInstanceOf(e.getTypeLiteral().getValue());
    }

    @Override
    public Value visit(TypeOf e) {
        return visit(e.getLeft()).isTypeOf(e.getTypeLiteral().getValue());
    }

    @Override
    public Value visit(BooleanLiteral e) {
        return Value.of(e.getValue());
    }

    @Override
    public Value visit(StringLiteral e) {
        return Value.of(e.getValue());
    }

    @Override
    public Value visit(DateLiteral e) {
        return Value.of(e.getValue());
    }

    @Override
    public Value visit(DateTimeLiteral e) {
        return Value.of(e.getValue());
    }

    @Override
    public Value visit(NumberLiteral e) {
        return Value.of(e.getValue());
    }

    @Override
    public Value visit(If e) {
        Value condition = visit(e.getCondition());
        if(strictTypeMode && condition.asBoolean() || !strictTypeMode && condition.asCoercedBoolean()) {
            return visit(e.getThenExpression());
        }
        return e.getElseExpression().map(this::visit).orElse(Value.nullValue());
    }

    @Override
    public Value visit(MoreThanOrEquals e) {
        if(strictTypeMode) {
            return visit(e.getLeft()).isMoreThanOrEqualsStrict(visit(e.getRight()));
        }
        return visit(e.getLeft()).isMoreThanOrEquals(visit(e.getRight()));
    }

    @Override
    public Value visit(MoreThan e) {
        if(strictTypeMode) {
            return visit(e.getLeft()).isMoreThanStrict(visit(e.getRight()));
        }
        return visit(e.getLeft()).isMoreThan(visit(e.getRight()));
    }

    @Override
    public Value visit(LessThanOrEquals e) {
        if(strictTypeMode) {
            return visit(e.getLeft()).isLessThanOrEqualsStrict(visit(e.getRight()));
        }
        return visit(e.getLeft()).isLessThanOrEquals(visit(e.getRight()));
    }

    @Override
    public Value visit(LessThan e) {
        if(strictTypeMode) {
            return visit(e.getLeft()).isLessThanStrict(visit(e.getRight()));
        }
        return visit(e.getLeft()).isLessThan(visit(e.getRight()));
    }

    @Override
    public Value visit(And e) {
        if(strictTypeMode) {
            return visit(e.getLeft()).asBoolean() && visit(e.getRight()).asBoolean()
                    ? Value.trueValue()
                    : Value.falseValue();
        }
        return visit(e.getLeft()).asCoercedBoolean() && visit(e.getRight()).asCoercedBoolean()
                ? Value.trueValue()
                : Value.falseValue();
    }

    @Override
    public Value visit(Or e) {
        if(strictTypeMode) {
            return visit(e.getLeft()).asBoolean() || visit(e.getRight()).asBoolean()
                    ? Value.trueValue()
                    : Value.falseValue();
        }
        return visit(e.getLeft()).asCoercedBoolean() || visit(e.getRight()).asCoercedBoolean()
                ? Value.trueValue()
                : Value.falseValue();
    }

    @Override
    public Value visit(MatchesRegExp e) {
        if(strictTypeMode) {
            return visit(e.getLeft()).matchesRegexStrict(e.getRegex());
        }
        return visit(e.getLeft()).matchesRegex(e.getRegex());
    }

    @Override
    public Value visit(Negation e) {
        if(strictTypeMode) {
            return visit(e.getExpression()).negateBooleanStrict();
        }
        return visit(e.getExpression()).negateBoolean();
    }

    @Override
    public Value visit(Equals e) {
        return visit(e.getLeft()).isEqualTo(visit(e.getRight()));
    }

    @Override
    public Value visit(NotEquals e) {
        return visit(e.getLeft()).isNotEqualTo(visit(e.getRight()));
    }

    @Override
    public Value visit(Modulus e) {
        return visit(e.getLeft()).modulus(visit(e.getRight()));
    }

    @Override
    public Value visit(Subtraction e) {
        return visit(e.getLeft()).subtract(visit(e.getRight()));
    }

    @Override
    public Value visit(Multiplication e) {
        return visit(e.getLeft()).multiply(visit(e.getRight()));
    }

    @Override
    public Value visit(Exponent e) {
        return visit(e.getLeft()).exponent(visit(e.getRight()));
    }

    @Override
    public Value visit(Division e) {
        return visit(e.getLeft()).divide(visit(e.getRight()));
    }

    @Override
    public Value visit(Addition e) {
        return visit(e.getLeft()).add(visit(e.getRight()));
    }

    @Override
    public Value visit(Negative e) {
        return visit(e.getExpression()).negateNumber();
    }

    @Override
    public Value visit(Function e) {
        Object[] parameters = new Object[e.getParameters().size()];
        for(int i = 0; i < e.getParameters().size(); i++) {
            parameters[i] = visit(e.getParameters().get(i)).getValue();
        }
        Object value = automaticFunctionIteration
                ? functionInvoker.invokeWithIteration(e.getFunctionName(), parameters)
                : functionInvoker.invoke(e.getFunctionName(), parameters);

        return Value.of(value);
    }

    @Override
    public Value visit(InlineArray e) {
        List<Object> inlineCollection = new ArrayList<>();
        for(Expression item : e.getItems()) {
            inlineCollection.add(visit(item).getValue());
        }
        return Value.of(inlineCollection);
    }

    @Override
    public Value visit(InlineMap e) {
        Map<String, Object> inlineMap = new HashMap<>();
        for(InlineMap.KeyValuePair keyValuePair : e.getKeyValuePairs()) {
            inlineMap.put(keyValuePair.getKey(), visit(keyValuePair.getValue()).getValue());
        }
        return Value.of(inlineMap);
    }

    @Override
    public Value visit(Path e) {
        Value object = visit(e.getObject());
        if(object.isCollection()) {
            List<Object> projection = new ArrayList<>();
            for(Object item : object.asCollection()) {
                stack.push(item, e.getProperty().getScope(), e.isNullSafe());
                Value propertyResult = visit(e.getProperty());
                stack.pop();
                flatMapIfCollectionAndThenAdd(projection, propertyResult.getValue());
            }
            return Value.of(projection);
        } else {
            stack.push(object.getValue(), e.getProperty().getScope(), e.isNullSafe());
            Value propertyResult = visit(e.getProperty());
            stack.pop();
            return propertyResult;
        }
    }

    @Override
    public Value visit(AccessByIndex e) {
        Value collection = visit(e.getCollection());

        Deque<StackElement> tail = stack.rewindToBlock();
        Value index = visit(e.getIndexExpression());
        stack.resetToBlock(tail);

        return collection.getValueAtIndex(index);
    }

    @Override
    public Value visit(ForEach e) {
        List<Object> collection = new ArrayList<>();
        for(Object item : visit(e.getCollection()).asCollection()) {

            Deque<StackElement> tail = stack.rewindToBlock();
            stack.pushChildBlock(withVar(e.getVar(), item), e.getReturnExpression().getScope(), false);
            Value result = visit(e.getReturnExpression());
            flatMapIfCollectionAndThenAdd(collection, result.getValue());
            stack.pop();
            stack.resetToBlock(tail);
        }
        return Value.of(collection);
    }

    @Override
    public Value visit(ForSome e) {
        for(Object item : visit(e.getCollection()).asCollection()) {

            Deque<StackElement> tail = stack.rewindToBlock();
            stack.pushChildBlock(withVar(e.getVar(), item), e.getReturnExpression().getScope(), false);
            Value result = visit(e.getReturnExpression());
            stack.pop();
            stack.resetToBlock(tail);

            if(result.asCoercedBoolean()) {
                return Value.trueValue();
            }

        }
        return Value.falseValue();
    }

    @Override
    public Value visit(ForEvery e) {
        for(Object item : visit(e.getCollection()).asCollection()) {

            Deque<StackElement> tail = stack.rewindToBlock();
            stack.pushChildBlock(withVar(e.getVar(), item), e.getReturnExpression().getScope(), false);
            Value result = visit(e.getReturnExpression());
            stack.pop();
            stack.resetToBlock(tail);

            if(!result.asCoercedBoolean()) {
                return Value.falseValue();
            }
        }
        return Value.trueValue();
    }

    private Map<String, Object> withVar(String var, Object item) {
        Map<String, Object> variables = new HashMap<>();
        variables.put(var, item);
        return variables;
    }

    @Override
    public Value visit(CollectionFilter e) {
        if(e.getPredicate() == null) {
            return visit(e.getCollection());
        }
        List<Object> collection = new ArrayList<>();
        for(Object item : visit(e.getCollection()).asCollection()) {

            Deque<StackElement> tail = stack.rewindToBlock();
            stack.pushChildBlock(item, e.getPredicate().getScope(), true);
            Value result = visit(e.getPredicate());
            stack.pop();
            stack.resetToBlock(tail);

            if(result.asCoercedBoolean()) {
                collection.add(item);
            }
        }
        return Value.of(collection);
    }

    @Override
    public Value visit(In e) {
        Value item = visit(e.getLeft());
        Value collection = visit(e.getRight());
        return collection.hasItemInCollection(item);
    }

    @Override
    public Value visit(ReferenceValue e) {
        return visit(e.getReference());
    }

    @Override
    public Value visit(Identifier e) {
        // evaluating field path with the same null safety operator that was used in path
        boolean nullSafe = stack.peek().getStackObject().nullSafe;
        Object o = evaluateProperty(e.getIdentifierParts()[0]);
        for(int i = 1; i < e.getIdentifierParts().length; i++) {
            StackObject stackObject = new StackObject(null, o, e.getScope(), false, nullSafe);
            o = stackObject.getStaticProperty(e.getIdentifierParts()[i], strictTypeMode);
        }
        return Value.of(o);
    }

    private Object evaluateProperty(String property) {
        StackObject stackObject = stack.peek().getStackObject();
        StackObject staticStackObject = stackObject.findStackObjectWithProperty(property);
        if(staticStackObject != null) {
            return staticStackObject.getStaticProperty(property, strictTypeMode);
        }
        return stackObject.getDynamicProperty(property, strictTypeMode);
    }

    @Override
    public Value visit(Null e) {
        return Value.nullValue();
    }

    @Override
    public Value visit(Empty e) {
        return Value.nullValue();
    }

    @Override
    public Value visit(This e) {
        return Value.of(stack.peek().getStackObject().getThisObject());
    }

    @Override
    public Value visit(ValueBlock valueBlock) {
        if(valueBlock.getVariables().isEmpty()) {
            return visit(valueBlock.getValue());
        }

        Map<String, Object> variables = new HashMap<>();

        Variable firstVariable = valueBlock.getVariables().get(0);
        Value firstVariableValue = visit(firstVariable);
        variables.put(firstVariable.getVariableName(), firstVariableValue.getValue());

        for(int i = 1; i < valueBlock.getVariables().size(); i++) {
            Variable nextVariable = valueBlock.getVariables().get(i);

            stack.pushChildBlock(variables, nextVariable.getScope(), false);
            Value nextVariableValue = visit(nextVariable);
            stack.pop();

            variables.put(nextVariable.getVariableName(), nextVariableValue.getValue());
        }

        stack.pushChildBlock(variables, valueBlock.getValue().getScope(), false);
        Value value = visit(valueBlock.getValue());
        stack.pop();

        return value;
    }

    @Override
    public Value visit(Variable variable) {
        return visit(variable.getValue());
    }

    private void flatMapIfCollectionAndThenAdd(Collection foldedCollection, Object result) {
        if (result instanceof Collection) {
            // Implemented to support proxy collection object, which do not work with ArrayList#addAll
            // because it must be invoked using iterator
            for(Object item : (Collection)result) {
                foldedCollection.add(item);
            }
        } else {
            foldedCollection.add(result);
        }
    }

    private static class Stack {

        private final Deque<StackElement> stackElements;

        Stack() {
            this.stackElements = new LinkedList<>();
        }

        StackElement pushChildBlock(Object object, Scope scope, boolean candidateForThisReference) {
            StackElement parent = stackElements.peek();
            StackElement stackElement = new StackElement(
                new StackObject(parent != null ? parent.getStackObject() : null, object, scope, candidateForThisReference, false),
                true
            );
            stackElements.push(stackElement);
            return stackElement;
        }

        StackElement push(Object object, Scope scope, boolean nullSafe) {
            StackElement stackElement = new StackElement(
                new StackObject(null, object, scope, false, nullSafe),
                false
            );
            stackElements.push(stackElement);
            return stackElement;
        }

        StackElement pop() {
            return stackElements.pop();
        }

        Deque<StackElement> rewindToBlock() {
            Deque<StackElement> stackSequenceToBlock = new LinkedList<>();
            StackElement currentElement = stackElements.peek();
            while(currentElement != null && !currentElement.isBlock()) {
                stackSequenceToBlock.push(stackElements.pop());
                currentElement = stackElements.peek();
            }
            return stackSequenceToBlock;
        }

        void resetToBlock(Deque<StackElement> stackSequenceToBlock) {
            while(!stackSequenceToBlock.isEmpty()) {
                stackElements.push(stackSequenceToBlock.pop());
            }
        }

        StackElement peek() {
            return stackElements.peek();
        }

        @Override
        public String toString() {
            return Objects.toString(stackElements.peek());
        }
    }

    private static class StackElement {

        private final boolean block;

        private final StackObject stackObject;

        StackElement(StackObject stackObject, boolean block) {
            this.stackObject = stackObject;
            this.block = block;
        }

        public StackObject getStackObject() {
            return stackObject;
        }

        public boolean isBlock() {
            return block;
        }

        @Override
        public String toString() {
            return stackObject.toString();
        }
    }

    private static class StackObject {

        private final StackObject parent;

        private final Object object;

        private final Scope scope;

        private final boolean candidateForThisReference;

        private final boolean nullSafe;

        StackObject(StackObject parent, Object object, Scope scope, boolean candidateForThisReference, boolean nullSafe) {
            this.parent = parent;
            this.object = object;
            this.scope = scope;
            this.candidateForThisReference = candidateForThisReference;
            this.nullSafe = nullSafe;
        }

        Object getThisObject() {
            if(candidateForThisReference) {
                return object;
            }
            if(parent != null) {
                return parent.getThisObject();
            }
            throw new ExpressionEvaluationException("Error while resolving 'this'.");
        }

        /**
         * Resolves property from current stack object using static resolution which means that the property
         * is forcefully resolved from the current stack object without considering full stack.
         *
         * @param property to resolve from the current stack object
         * @param strict controls what to do in case the object does not have the property.
         *               If true, then exception is thrown.
         *               If false, then null is returned.
         * @return value of the property
         */
        Object getStaticProperty(String property, boolean strict) {
            return getProperty(property, strict, false);
        }

        /**
         * Resolves property from current stack object using dynamic resolution which means that the property is
         * resolved from the closest stack object that has required property dynamically.
         *
         * @param property to resolve from the stack
         * @param strict controls what to do in case the stack does not have the property.
         *               If true, then exception is thrown.
         *               If false, then null is returned.
         * @return value of the property
         */
        Object getDynamicProperty(String property, boolean strict) {
            return getProperty(property, strict, true);
        }

        private Object getProperty(String property, boolean strict, boolean dynamicResolution) {
            if (object != null) {
                if (object instanceof Map) {
                    if(((Map) object).containsKey(property) || parent == null) {
                        return ((Map) object).get(property);
                    }
                } else {
                    Map<String, Method> properties = ReflectionsCache.getGettersOrCompute(object.getClass());

                    if (properties.containsKey(property)) {
                        try {
                            return properties.get(property).invoke(object);
                        } catch (ReflectiveOperationException e) {
                            if (e instanceof InvocationTargetException && e.getCause() instanceof RuntimeException) {
                                throw (RuntimeException) e.getCause();
                            }
                            throw new ExpressionEvaluationException(
                                "Cannot evaluate get '" + property + "' : " + object.getClass(), e
                            );
                        }
                    }
                }
            }

            if(parent != null && dynamicResolution) {
                return parent.getDynamicProperty(property, strict);
            }

            if(!strict || nullSafe) {
                return null;
            }

            throw new ExpressionEvaluationException("Object is null or it does not have property: " + property);
        }

        /**
         *
         * @param property property to find for
         * @return stack object which is statically typed and its type has required property
         */
        StackObject findStackObjectWithProperty(String property) {
            if (scope.isReferenceStrictlyInImmediateScope(property)) {
                return scope.getScopeType() == ScopeType.FILTER ? null : this;
            }
            if(parent != null) {
                return parent.findStackObjectWithProperty(property);
            }
            return null;
        }

        @Override
        public String toString() {
            return parent != null
                    ? parent + "->" + String.valueOf(object)
                    : String.valueOf(object);
        }
    }

}
