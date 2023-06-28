/*
 * Copyright 2023 EIS Ltd and/or one of its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kraken.el.coercer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import javax.money.MonetaryAmount;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.TypeUtils;

import kraken.el.math.Numbers;

/**
 *
 * @author Mindaugas Ulevicius
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class KelCoercer {

    /**
     * Coerces and normalizes java object of some type to cast compatible expected type within the supported limits of KEL.
     * Collection and Map instances will be recreated.
     *
     * @param value java object to coerce
     * @param type is expected type
     * @return coerced java object
     * @throws KelCoercionException if the value cannot be coerced to expected type
     */
    public static Object coerce(Object value, Type type) {
        if(value == null) {
            return null;
        }
        if(type instanceof Class) {
            var classType = (Class) type;
            if(Collection.class.isAssignableFrom(classType) && value instanceof Collection) {
                var collectionType = (Class<Collection>) classType;
                var collectionItemType =  Object.class;
                return coerceCollection((Collection) value, collectionType, collectionItemType);
            }
            if(Map.class.isAssignableFrom(classType) && value instanceof Map) {
                var mapType = (Class<Map>) classType;
                var mapKeyType =  Object.class;
                var mapValueType =  Object.class;
                return coerceMap((Map) value, mapType, mapKeyType, mapValueType);
            }
            if(Number.class.isAssignableFrom(classType)) {
                if(ClassUtils.isAssignable(value.getClass(), classType)) {
                    return value;
                }
                if(value instanceof MonetaryAmount) {
                    var number = ((MonetaryAmount) value).getNumber().numberValue(BigDecimal.class);
                    return coerceNumber(number, classType);
                }
                if(value instanceof Number) {
                    return coerceNumber((Number) value, classType);
                }
            }
        }
        if(type instanceof ParameterizedType) {
            var parameterizedType = (ParameterizedType) type;
            // Collection and Map are the only parameterizable types with coercion support
            if(parameterizedType.getRawType() instanceof Class) {
                var rawClassType = (Class) parameterizedType.getRawType();
                var typeArguments = parameterizedType.getActualTypeArguments();
                if(Collection.class.isAssignableFrom(rawClassType) && value instanceof Collection && typeArguments.length == 1) {
                    var collectionType = (Class<Collection>) rawClassType;
                    var collectionItemType = typeArguments[0];
                    return coerceCollection((Collection) value, collectionType, collectionItemType);
                }
                if(Map.class.isAssignableFrom(rawClassType) && value instanceof Map && typeArguments.length == 2) {
                    var mapType = (Class<Map>) rawClassType;
                    var mapKeyType = typeArguments[0];
                    var mapValueType = typeArguments[1];
                    return coerceMap((Map) value, mapType, mapKeyType, mapValueType);
                }
            }
        }
        if(type instanceof WildcardType) {
            var wildcardType = (WildcardType) type;
            if (wildcardType.getUpperBounds().length == 1) {
                var upperBoundType = wildcardType.getUpperBounds()[0];
                return coerce(value, upperBoundType);
            }
        }
        if(type instanceof TypeVariable) {
            var typeVariable = (TypeVariable) type;
            if (typeVariable.getBounds().length == 1) {
                var upperBoundType = typeVariable.getBounds()[0];
                return coerce(value, upperBoundType);
            }
        }
        if(TypeUtils.isInstance(value, type)) {
            return value;
        }
        String template = "Cannot convert value of type %s to type %s. Value cannot be converted to expected type.";
        String message = String.format(template, value.getClass(), type);
        throw new KelCoercionException(message);
    }

    private static Map coerceMap(Map map, Class<Map> expectedMapType, Type expectedMapKeyType,
                                       Type expectedMapValueType) {
        var coercedMap = createCompatibleEmptyMap(expectedMapType, map.size());
        for (var entry : map.entrySet()) {
            var key = ((Entry) entry).getKey();
            var value = ((Entry) entry).getValue();
            coercedMap.put(coerce(key, expectedMapKeyType), coerce(value, expectedMapValueType));
        }
        return coercedMap;
    }

    private static Collection coerceCollection(Collection collection,
                                               Class<Collection> expectedCollectionType,
                                               Type expectedCollectionItemType) {
        var coercedCollection = createCompatibleEmptyCollection(expectedCollectionType, collection.size());
        for (var item : collection) {
            coercedCollection.add(coerce(item, expectedCollectionItemType));
        }
        return coercedCollection;
    }

    private static Collection createCompatibleEmptyCollection(Class<Collection> expectedCollectionType, int size) {
        if(expectedCollectionType.isAssignableFrom(ArrayList.class)) {
            return new ArrayList<>(size);
        }
        if(expectedCollectionType.isAssignableFrom(HashSet.class)) {
            return new HashSet<>(size);
        }
        if(expectedCollectionType.isAssignableFrom(LinkedList.class)) {
            return new LinkedList<>();
        }
        String template = "Cannot create compatible Collection for type %s. "
            + "Only ArrayList, HashSet and LinkedList are supported.";
        String message = String.format(template, expectedCollectionType);
        throw new KelCoercionException(message);
    }

    private static Map createCompatibleEmptyMap(Class<Map> expectedMapType, int size) {
        if(expectedMapType.isAssignableFrom(HashMap.class)) {
            return new HashMap<>(size);
        }
        String template = "Cannot create compatible Map for type %s. "
            + "Only HashMap is supported.";
        String message = String.format(template, expectedMapType);
        throw new KelCoercionException(message);
    }

    private static Number coerceNumber(Number value, Class<?> expectedType) {
        if (expectedType.isAssignableFrom(BigDecimal.class)) {
            return Numbers.normalized(value);
        }
        if (expectedType.isAssignableFrom(Long.class)) {
            return value.longValue();
        }
        if (expectedType.isAssignableFrom(Integer.class)) {
            return value.intValue();
        }
        String template = "Cannot coerce number value of type %s to type %s. "
            + "Only BigDecimal, Long and Integer are supported.";
        String message = String.format(template, value.getClass(), expectedType);
        throw new KelCoercionException(message);
    }

}
