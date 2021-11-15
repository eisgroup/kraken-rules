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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import kraken.el.ExpressionEvaluationException;
import kraken.el.ExpressionLanguageConfiguration;
import kraken.el.accelerated.ReflectionsCache;
import kraken.el.ast.*;
import kraken.el.ast.visitor.QueuedAstVisitor;
import kraken.el.function.FunctionInvoker;

/**
 * @author mulevicius
 */
public class InterpretingAstVisitor extends QueuedAstVisitor<Value> {

    private final Stack stack;

    private final boolean strictTypeMode;

    private final boolean automaticFunctionIteration;

    public InterpretingAstVisitor(Object dataObject, Map<String, Object> globalVars, ExpressionLanguageConfiguration configuration) {
        this.stack = new Stack();
        this.stack.pushChildBlock(null, globalVars);
        this.stack.pushChildBlock(dataObject);

        this.strictTypeMode = configuration.isStrictTypeMode();
        this.automaticFunctionIteration = configuration.isAllowAutomaticIterationWhenInvokingFunctions();
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
                ? FunctionInvoker.invokeWithIteration(e.getFunctionName(), parameters)
                : FunctionInvoker.invoke(e.getFunctionName(), parameters);

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
                stack.push(item);
                Value propertyResult = visit(e.getProperty());
                stack.pop();
                flatMapIfCollectionAndThenAdd(projection, propertyResult.getValue());
            }
            return Value.of(projection);
        } else {
            stack.push(object.getValue());
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

            Object thisObject = stack.peek().getContext().getThisObject();
            Deque<StackElement> tail = stack.rewindToBlock();
            stack.pushChildBlock(thisObject, withVar(e.getVar(), item));
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

            Object thisObject = stack.peek().getContext().getThisObject();
            Deque<StackElement> tail = stack.rewindToBlock();
            stack.pushChildBlock(thisObject, withVar(e.getVar(), item));
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

            Object thisObject = stack.peek().getContext().getThisObject();
            Deque<StackElement> tail = stack.rewindToBlock();
            stack.pushChildBlock(thisObject, withVar(e.getVar(), item));
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
            stack.pushChildBlock(item);
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
        String property = e.getIdentifier();
        return Value.of(stack.peek().getContext().getProperty(property, strictTypeMode));
    }

    @Override
    public Value visit(Null e) {
        return Value.nullValue();
    }

    @Override
    public Value visit(This e) {
        return Value.of(stack.peek().getContext().getThisObject());
    }

    private void flatMapIfCollectionAndThenAdd(Collection foldedCollection, Object result) {
        if (result instanceof Collection) {
            // support for proxy collection used in Genesis, which does not work with ArrayList#addAll
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

        StackElement pushChildBlock(Object object, Map<String, Object> variables) {
            StackElement parent = stackElements.peek();
            StackElement stackElement = new StackElement(
                    new ScopeContext(parent != null ? parent.getContext() : null, variables, object),
                    true
            );
            stackElements.push(stackElement);
            return stackElement;
        }

        StackElement pushChildBlock(Object object) {
            return pushChildBlock(object, null);
        }

        StackElement push(Object object) {
            StackElement stackElement = new StackElement(
                    new ScopeContext(null, null, object),
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

        private final ScopeContext context;

        StackElement(ScopeContext context, boolean block) {
            this.context = context;
            this.block = block;
        }

        public ScopeContext getContext() {
            return context;
        }

        public boolean isBlock() {
            return block;
        }

        @Override
        public String toString() {
            return context.toString();
        }
    }

    private static class ScopeContext {

        private final ScopeContext parent;

        private final Map<String, Object> variables;

        private final Object thisObject;

        ScopeContext(ScopeContext parent,
                     Map<String, Object> variables,
                     Object thisObject) {
            this.parent = parent;
            this.variables = variables;
            this.thisObject = thisObject;
        }

        Object getThisObject() {
            return thisObject;
        }

        Object getProperty(String property, boolean strict) {
            if(variables != null && variables.containsKey(property)) {
                return variables.get(property);
            }
            if (thisObject != null) {
                if (thisObject instanceof Map) {
                    return ((Map) thisObject).get(property);
                } else {
                    Map<String, Method> properties = ReflectionsCache.getGettersOrCompute(thisObject.getClass());
                    if (properties.containsKey(property)) {
                        try {
                            return properties.get(property).invoke(thisObject);
                        } catch (ReflectiveOperationException e) {
                            if (e instanceof InvocationTargetException && e.getCause() instanceof RuntimeException) {
                                throw (RuntimeException) e.getCause();
                            }
                            throw new ExpressionEvaluationException(
                                    "Cannot evaluate get '" + property + "' : " + thisObject.getClass(), e
                            );
                        }
                    }
                }
            }

            if(parent != null) {
                return parent.getProperty(property, strict);
            }

            if(!strict) {
                return null;
            }

            throw new ExpressionEvaluationException("Object is null or it does not have property: " + property);
        }

        @Override
        public String toString() {
            return parent != null
                    ? parent + "->" + String.valueOf(thisObject)
                    : String.valueOf(thisObject);
        }
    }
}
