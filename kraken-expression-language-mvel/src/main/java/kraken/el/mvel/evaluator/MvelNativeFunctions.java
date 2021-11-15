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
package kraken.el.mvel.evaluator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.money.MonetaryAmount;

import kraken.el.ExpressionEvaluationException;
import kraken.el.InvocationContextHolder;
import kraken.el.InvocationContextHolder.InvocationContext;
import kraken.el.functions.MoneyFunctions;
import kraken.el.functions.TypeProvider;
import kraken.el.math.Numbers;
import org.mvel2.Operator;
import org.mvel2.math.MathProcessor;

import static kraken.el.functions.TypeProvider.TYPE_PROVIDER_PROPERTY;

/**
 * @author mulevicius
 */
public class MvelNativeFunctions {

    private static MvelExpressionEvaluator expressionEvaluator = new MvelExpressionEvaluator();

    public static Boolean _i(Object value, String type) {
        if(value == null) {
            return false;
        }
        return getTypeAdapter().getInheritedTypesOf(value).contains(type);
    }

    public static Boolean _t(Object value, String type) {
        if(value == null) {
            return false;
        }
        return type.equals(getTypeAdapter().getTypeOf(value));
    }

    private static TypeProvider getTypeAdapter() {
        InvocationContext invocationContext = InvocationContextHolder.getInvocationContext();
        return (TypeProvider) invocationContext.getVariables().get(TYPE_PROVIDER_PROPERTY);
    }

    public static String _s(Object value) {
        if(value instanceof String) {
            return (String) value;
        }
        throw new ExpressionEvaluationException("object is not string: " + value);
    }

    public static Boolean _b(Object value) {
        if(value instanceof Boolean) {
            return (Boolean) value;
        }
        throw new ExpressionEvaluationException("object is not boolean: " + value);
    }

    public static Number _n(Object value) {
        if(value instanceof Number) {
            return (Number) value;
        }
        if(value instanceof MonetaryAmount) {
            return MoneyFunctions.fromMoney((MonetaryAmount) value);
        }
        throw new ExpressionEvaluationException("object is not number or money: " + value);
    }

    public static Object _nd(Object value) {
        if(value instanceof Number) {
            return value;
        }
        if(value instanceof MonetaryAmount) {
            return MoneyFunctions.fromMoney((MonetaryAmount) value);
        }
        if(value instanceof LocalDate || value instanceof LocalDateTime) {
            return value;
        }
        throw new ExpressionEvaluationException("object is not comparable: " + value);
    }

    public static Boolean _neq(Object v1, Object v2) {
        return !_eq(v1, v2);
    }

    public static Boolean _eq(Object v1, Object v2) {
        if(v1 instanceof Number && v2 instanceof Number) {
            return (Boolean) MathProcessor.doOperations(v1, Operator.EQUAL, v2);
        }
        return Objects.equals(v1, v2);
    }

    public static Boolean _in(Object c, Object v) {
        if(c == null) {
            return false;
        }
        if(c instanceof Collection) {
            for(Object item : (Collection) c) {
                if(_eq(item, v)) {
                    return true;
                }
            }
            return false;
        }
        throw new IllegalStateException("object is not array: " + c);
    }

    public static Number _mod(Object first, Object second) {
        return Numbers.modulus(_n(first), _n(second));
    }

    public static Number _sub(Object first, Object second) {
        return Numbers.subtract(_n(first), _n(second));
    }

    public static Number _mult(Object first, Object second) {
        return Numbers.multiply(_n(first), _n(second));
    }

    public static Number _pow(Object first, Object second) {
        return Numbers.power(_n(first), _n(second));
    }

    public static Number _div(Object first, Object second) {
        return Numbers.divide(_n(first), throwIfZero(_n(second)));
    }

    public static Number _add(Object first, Object second) {
        return Numbers.add(_n(first), _n(second));
    }

    public static Object getElement(Collection collection, Object elementIndex) {
        if (collection == null) {
            throw new ExpressionEvaluationException("Unable to retrieve element from collection. " +
                    "Collection is not available." );
        }

        return getElementInCollection(collection, _n(elementIndex));
    }

    public static Collection filter(Collection collection, String predicate) {
        Collection filteredCollection = new ArrayList();
        if(collection == null) {
            return filteredCollection;
        }
        for(Object item : collection) {
            if(isPredicateTrue(item, predicate)) {
                filteredCollection.add(item);
            }
        }
        return filteredCollection;
    }

    public static BigDecimal fromMoney(MonetaryAmount monetaryAmount) {
        return MoneyFunctions.fromMoney(monetaryAmount);
    }

    public static Boolean forSome(String var, Collection collection, String predicate) {
        if(collection != null) {
            for (Object item : collection) {
                if (isPredicateTrueForIteratedContext(var, item, predicate)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Boolean forEvery(String var, Collection collection, String predicate) {
        if(collection != null) {
            for (Object item : collection) {
                if (!isPredicateTrueForIteratedContext(var, item, predicate)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static Collection forEach(String var, Collection collection, String function) {
        Collection foldedCollection = new ArrayList();
        if(collection != null) {
            for (Object item : collection) {
                Object result = evalForIteratedContext(var, item, function);
                flatMapAddIfCollection(foldedCollection, result);
            }
        }
        return foldedCollection;
    }

    public static Collection flatMap(Collection collection, String function) {
        Collection foldedCollection = new ArrayList();
        if(collection != null) {
            for (Object item : collection) {
                Object result = eval(item, function);
                flatMapAddIfCollection(foldedCollection, result);
            }
        }
        return foldedCollection;
    }

    private static void flatMapAddIfCollection(Collection foldedCollection, Object result) {
        if (result instanceof Collection) {
            // add support for proxy collection used in Genesis, which does not work with ArrayList#addAll because it must be invoked using iterator
            for(Object item : (Collection)result) {
                foldedCollection.add(item);
            }
        } else {
            foldedCollection.add(result);
        }
    }

    private static Object eval(Object item, String expression) {
        InvocationContext c = InvocationContextHolder.getInvocationContext();
        Map<String, Object> expressionContext = merge(c.getContext(), c.getVariables());
        expressionContext.putIfAbsent("__dataObject__", c.getContext());
        return expressionEvaluator.evaluate(expression, item, expressionContext);
    }

    private static Object evalForIteratedContext(String var, Object item, String expression) {
        InvocationContext c = InvocationContextHolder.getInvocationContext();
        Map<String, Object> expressionContext = merge(c.getContext(), c.getVariables());
        expressionContext.put(var, item);
        return expressionEvaluator.evaluate(expression, c.getContext(), expressionContext);
    }

    private static boolean isPredicateTrue(Object item, String predicate) {
        return Boolean.TRUE.equals(eval(item, predicate));
    }

    private static boolean isPredicateTrueForIteratedContext(String var, Object item, String predicate) {
        return Boolean.TRUE.equals(evalForIteratedContext(var, item, predicate));
    }

    private static Map<String, Object> merge(Object context, Map<String, Object> variables) {
        Map<String, Object> mergedMap = new HashMap<>(variables);
        if(context instanceof Map) {
            mergedMap.putAll((Map) context);
        }
        return mergedMap;
    }

    private static Object getElementInCollection(Collection collection, Number elementIndex) {
        int index = 0;

        for (Object item : collection) {
            if (index == elementIndex.intValue()) {
                return item;
            }

            index++;
        }

        throw new ExpressionEvaluationException("Unable to retrieve element from collection. " +
                "Collection has no element at index: " + elementIndex);
    }

    private static Number throwIfZero(Number number) {
        if(_eq(number, BigDecimal.ZERO)) {
            throw new ExpressionEvaluationException("Division by zero. Rule will be ignored due to missing data.");
        }
        return number;
    }
}
