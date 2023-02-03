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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import javax.money.MonetaryAmount;

import kraken.el.ExpressionEvaluationException;
import kraken.el.InvocationContextHolder;
import kraken.el.TypeProvider;
import kraken.el.math.Numbers;

/**
 * @author mulevicius
 */
public class Value {

    private static final Value nullValue = new Value(null);
    private static final Value trueValue = new Value(true);
    private static final Value falseValue = new Value(false);

    private final Object value;

    private Value(Object value) {
        this.value = value;
    }

    public static Value of(Object value) {
        if(value == null) {
            return nullValue();
        }
        if(value instanceof Boolean) {
            return (Boolean) value ? trueValue() : falseValue();
        }
        return new Value(value);
    }

    public Boolean asBoolean() {
        if(value instanceof Boolean) {
            return (Boolean) value;
        }
        throw new ExpressionEvaluationException("object is not boolean: " + value);
    }

    public Boolean asCoercedBoolean() {
        return Boolean.TRUE.equals(value);
    }

    public boolean isBoolean() {
        return value instanceof Boolean;
    }

    public String asString() {
        if(value instanceof String) {
            return (String) value;
        }
        throw new ExpressionEvaluationException("object is not string: " + value);
    }

    public boolean isString() {
        return value instanceof String;
    }

    public Number asNumber() {
        if(value instanceof Number) {
            return (Number) value;
        }
        if(value instanceof MonetaryAmount) {
            return Numbers.fromMoney((MonetaryAmount) value);
        }

        throw new ExpressionEvaluationException("object is not number: " + value);
    }

    public boolean isNumber() {
        return value instanceof Number || value instanceof MonetaryAmount;
    }

    public Collection asCollection() {
        if(value instanceof Collection) {
            return (Collection) value;
        }
        if(value == null) {
            return Collections.emptyList();
        }
        throw new ExpressionEvaluationException("object is not array: " + value);
    }

    public boolean isCollection() {
        return value instanceof Collection;
    }

    public Object getValue() {
        return value;
    }

    public Value getValueAtIndex(Value index) {
        int i = index.asNumber().intValue();
        Collection collection = asCollection();
        return Value.of(getElementInCollection(collection, i));
    }

    public Value hasItemInCollection(Value item) {
        Collection collection = asCollection();
        for(Object element : collection) {
            Value elementValue = Value.of(element);
            if(elementValue.isEqualTo(item).asBoolean()) {
                return Value.trueValue();
            }
        }
        return Value.falseValue();
    }

    private static Object getElementInCollection(Collection collection, int i) {
        if(collection == null || collection.size() <= i) {
            throw new ExpressionEvaluationException("Unable to retrieve element from collection. " +
                    "Collection has no element at index: " + i);
        }

        if(collection instanceof List) {
            return ((List) collection).get(i);
        }

        int index = 0;
        for (Object item : collection) {
            if (index == i) {
                return item;
            }
            index++;
        }
        return null;
    }

    public Value negateBoolean() {
        return !asCoercedBoolean() ? trueValue : falseValue;
    }

    public Value negateBooleanStrict() {
        return !asBoolean() ? trueValue : falseValue;
    }

    private static final Predicate<Integer> MORE_THAN = i -> i > 0;
    private static final Predicate<Integer> MORE_THAN_OR_EQUALS = i -> i >= 0;
    private static final Predicate<Integer> LESS_THAN = i -> i < 0;
    private static final Predicate<Integer> LESS_THAN_OR_EQUALS = i -> i <= 0;

    public Value isMoreThanStrict(Value v) {
        return compared(v, MORE_THAN, true);
    }

    public Value isMoreThanOrEqualsStrict(Value v) {
        return compared(v, MORE_THAN_OR_EQUALS, true);
    }

    public Value isLessThanStrict(Value v) {
        return compared(v, LESS_THAN, true);
    }

    public Value isLessThanOrEqualsStrict(Value v) {
        return compared(v, LESS_THAN_OR_EQUALS, true);
    }

    public Value isMoreThan(Value v) {
        return compared(v, MORE_THAN, false);
    }

    public Value isMoreThanOrEquals(Value v) {
        return compared(v, MORE_THAN_OR_EQUALS, false);
    }

    public Value isLessThan(Value v) {
        return compared(v, LESS_THAN, false);
    }

    public Value isLessThanOrEquals(Value v) {
        return compared(v, LESS_THAN_OR_EQUALS, false);
    }

    private Value compared(Value v, Predicate<Integer> comparator, boolean strict) {
        if((value == null || v.value == null) && !strict) {
            return Value.falseValue();
        }
        if(isNumber() && v.isNumber()) {
            return comparator.test(Numbers.compareTo(asNumber(), v.asNumber())) ? trueValue : falseValue;
        }
        if(value instanceof LocalDate && v.value instanceof LocalDate) {
            return comparator.test(((LocalDate) value).compareTo((LocalDate) v.value)) ? trueValue : falseValue;
        }
        if(value instanceof LocalDateTime && v.value instanceof LocalDateTime) {
            return comparator.test(((LocalDateTime) value).compareTo((LocalDateTime) v.value)) ? trueValue : falseValue;
        }
        throw new ExpressionEvaluationException("object is not comparable: " + value);
    }

    public Value matchesRegexStrict(String regex) {
        return asString().matches(regex) ? trueValue : falseValue;
    }

    public Value matchesRegex(String regex) {
        if(value == null) {
            return Value.falseValue();
        }
        return asString().matches(regex) ? trueValue : falseValue;
    }

    public Value isEqualTo(Value v) {
        return isValueEqualTo(v) ? trueValue : falseValue;
    }

    public Value isNotEqualTo(Value v) {
        return !isValueEqualTo(v) ? trueValue : falseValue;
    }

    private boolean isValueEqualTo(Value v) {
        if(isNumber() && v.isNumber()) {
            return Objects.equals(Numbers.normalized(asNumber()).stripTrailingZeros(), Numbers.normalized(v.asNumber()).stripTrailingZeros());
        }
        return Objects.equals(getValue(), v.getValue());
    }

    public Value negateNumber() {
        return Value.of(Numbers.normalized(asNumber()).negate());
    }

    public Value add(Value v) {
        return Value.of(Numbers.add(asNumber(), v.asNumber()));
    }

    public Value subtract(Value v) {
        return Value.of(Numbers.subtract(asNumber(), v.asNumber()));
    }

    public Value multiply(Value v) {
        return Value.of(Numbers.multiply(asNumber(), v.asNumber()));
    }

    public Value divide(Value v) {
        return Value.of(Numbers.divide(asNumber(), v.asNumber()));
    }

    public Value modulus(Value v) {
        return Value.of(Numbers.modulus(asNumber(), v.asNumber()));
    }

    public Value exponent(Value v) {
        return Value.of(Numbers.power(asNumber(), v.asNumber()));
    }

    public Value isTypeOf(String typeName) {
        return value != null && typeName.equals(getTypeAdapter().getTypeOf(value)) ? trueValue : falseValue;
    }

    public Value isInstanceOf(String typeName) {
        return value != null && getTypeAdapter().getInheritedTypesOf(value).contains(typeName) ? trueValue : falseValue;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    private static TypeProvider getTypeAdapter() {
        return InvocationContextHolder.getInvocationContext().getEvaluationContext().getTypeProvider();
    }

    public static Value nullValue() {
        return nullValue;
    }

    public static Value trueValue() {
        return trueValue;
    }

    public static Value falseValue() {
        return falseValue;
    }
}
