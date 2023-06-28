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

import static kraken.el.coercer.KelCoercer.coerce;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.money.MonetaryAmount;

import org.javamoney.moneta.Money;
import org.junit.Test;

import com.google.gson.reflect.TypeToken;

/**
 * @author Mindaugas Ulevicius
 */
public class KelCoercerTest {

    @Test
    public void shouldCoerceNullToAnyType() {
        assertThat(coerce(null, String.class), nullValue());
        assertThat(coerce(null, Entity.class), nullValue());
    }

    @Test
    public void shouldCoerceToObject() {
        assertThat(coerce(null, Object.class), nullValue());
        assertThat(coerce("string", Object.class), is("string"));
        var entity = new Entity();
        assertThat(coerce(entity, Object.class), is(entity));
        var money = Money.of(10, "USD");
        assertThat(coerce(money, Object.class), is(money));
    }

    @Test
    public void shouldCoerceToPrimitive() {
        var date = LocalDate.of(2020, 1, 1);
        var dateTime = LocalDateTime.of(2020, 1, 1, 10, 30, 0);

        assertThat(coerce("string", String.class), is("string"));
        assertThat(coerce(BigDecimal.valueOf(10), BigDecimal.class), is(BigDecimal.valueOf(10)));
        assertThat(coerce(BigDecimal.valueOf(10), Number.class), is(BigDecimal.valueOf(10)));
        assertThat(coerce(10, Integer.class), is(10));
        assertThat(coerce(10, Number.class), is(10));
        assertThat(coerce(10L, Long.class), is(10L));
        assertThat(coerce(10L, Number.class), is(10L));
        assertThat(coerce(true, Boolean.class), is(true));
        assertThat(coerce(date, LocalDate.class), is(date));
        assertThat(coerce(date, Temporal.class), is(date));
        assertThat(coerce(dateTime, LocalDateTime.class), is(dateTime));
        assertThat(coerce(dateTime, Temporal.class), is(dateTime));

        assertThrows(KelCoercionException.class, () -> coerce("true", Boolean.class));
        assertThrows(KelCoercionException.class, () -> coerce("10", Integer.class));
        assertThrows(KelCoercionException.class, () -> coerce(10, String.class));
        assertThrows(KelCoercionException.class, () -> coerce("1", Boolean.class));
        assertThrows(KelCoercionException.class, () -> coerce("2020-01-01", LocalDate.class));
        assertThrows(KelCoercionException.class, () -> coerce(date, String.class));
        assertThrows(KelCoercionException.class, () -> coerce(dateTime, String.class));
    }

    @Test
    public void shouldCoerceBetweenNumbers() {
        assertThat(coerce(10, BigDecimal.class), is(BigDecimal.valueOf(10)));
        assertThat(coerce(10, Long.class), is(10L));
        assertThat(coerce(10L, Integer.class), is(10));
        assertThat(coerce(10L, BigDecimal.class), is(BigDecimal.valueOf(10)));
        assertThat(coerce(BigDecimal.valueOf(10), Integer.class), is(10));
        assertThat(coerce(BigDecimal.valueOf(10), Long.class), is(10L));
        assertThat(coerce(BigDecimal.valueOf(10.5), Long.class), is(10L));
        assertThat(coerce(BigDecimal.valueOf(10.5), Integer.class), is(10));
        assertThat(coerce(Money.of(10, "USD"), Integer.class), is(10));
        assertThat(coerce(Money.of(10, "USD"), Long.class), is(10L));
        assertThat(coerce(Money.of(10, "USD"), BigDecimal.class), is(BigDecimal.valueOf(10)));

        assertThrows(KelCoercionException.class, () -> coerce(10, Float.class));
        assertThrows(KelCoercionException.class, () -> coerce(10, Double.class));
        assertThrows(KelCoercionException.class, () -> coerce(10, BigInteger.class));
        assertThrows(KelCoercionException.class, () -> coerce(10, MonetaryAmount.class));
    }

    @Test
    public void shouldCoerceToEntity() {
        var entity = new Entity();
        assertThat(coerce(entity, Entity.class), is(entity));
        assertThat(coerce(entity, BaseEntity.class), is(entity));

        assertThrows(KelCoercionException.class, () -> coerce(entity, OtherEntity.class));
    }

    @Test
    public void shouldCoerceToRawCollection() {
        var arrayListOfValues = List.of(1, 2L, BigDecimal.valueOf(3), Money.of(1, "USD"), "string", new Entity());
        var hashSetOfValues = new HashSet<>(arrayListOfValues);
        var linkedListOfValues = new LinkedList<>(arrayListOfValues);

        assertThat(coerce(arrayListOfValues, Object.class), is(arrayListOfValues));
        assertThat(coerce(arrayListOfValues, Collection.class), is(arrayListOfValues));
        assertThat(coerce(arrayListOfValues, List.class), is(arrayListOfValues));
        assertThat(coerce(arrayListOfValues, ArrayList.class), is(arrayListOfValues));
        assertThat(coerce(arrayListOfValues, Set.class), is(hashSetOfValues));
        assertThat(coerce(arrayListOfValues, HashSet.class), is(hashSetOfValues));
        assertThat(coerce(arrayListOfValues, Queue.class), is(linkedListOfValues));
        assertThat(coerce(arrayListOfValues, LinkedList.class), is(linkedListOfValues));

        assertThat(coerce(hashSetOfValues, Object.class), is(hashSetOfValues));
        assertThat(coerce(hashSetOfValues, Collection.class), is(new ArrayList<>(hashSetOfValues)));
        assertThat(coerce(hashSetOfValues, List.class), is(new ArrayList<>(hashSetOfValues)));
        assertThat(coerce(hashSetOfValues, ArrayList.class), is(new ArrayList<>(hashSetOfValues)));
        assertThat(coerce(hashSetOfValues, Set.class), is(hashSetOfValues));
        assertThat(coerce(hashSetOfValues, HashSet.class), is(hashSetOfValues));
        assertThat(coerce(hashSetOfValues, Queue.class), is(new LinkedList<>(hashSetOfValues)));
        assertThat(coerce(hashSetOfValues, LinkedList.class), is(new LinkedList<>(hashSetOfValues)));

        assertThat(coerce(linkedListOfValues, Object.class), is(linkedListOfValues));
        assertThat(coerce(linkedListOfValues, Collection.class), is(linkedListOfValues));
        assertThat(coerce(linkedListOfValues, List.class), is(arrayListOfValues));
        assertThat(coerce(linkedListOfValues, ArrayList.class), is(arrayListOfValues));
        assertThat(coerce(linkedListOfValues, Set.class), is(hashSetOfValues));
        assertThat(coerce(linkedListOfValues, HashSet.class), is(hashSetOfValues));
        assertThat(coerce(linkedListOfValues, Queue.class), is(linkedListOfValues));
        assertThat(coerce(linkedListOfValues, LinkedList.class), is(linkedListOfValues));

        assertThrows(KelCoercionException.class, () -> coerce(arrayListOfValues, LinkedHashSet.class));
        assertThrows(KelCoercionException.class, () -> coerce(arrayListOfValues, Vector.class));
        assertThrows(KelCoercionException.class, () -> coerce(arrayListOfValues, Map.class));
    }

    @Test
    public void shouldCoerceToParameterizedStringCollection() {
        var arrayListOfValues = List.of("A", "B", "C");
        var hashSetOfValues = new HashSet<>(arrayListOfValues);
        var linkedListOfValues = new LinkedList<>(arrayListOfValues);

        assertThat(coerce(arrayListOfValues, new TypeToken<Collection<Object>>(){}.getType()), is(arrayListOfValues));
        assertThat(coerce(arrayListOfValues, new TypeToken<Collection<String>>(){}.getType()), is(arrayListOfValues));
        assertThat(coerce(arrayListOfValues, new TypeToken<List<String>>(){}.getType()), is(arrayListOfValues));
        assertThat(coerce(arrayListOfValues, new TypeToken<ArrayList<String>>(){}.getType()), is(arrayListOfValues));
        assertThat(coerce(arrayListOfValues, new TypeToken<Set<String>>(){}.getType()), is(hashSetOfValues));
        assertThat(coerce(arrayListOfValues, new TypeToken<HashSet<String>>(){}.getType()), is(hashSetOfValues));
        assertThat(coerce(arrayListOfValues, new TypeToken<Queue<String>>(){}.getType()), is(linkedListOfValues));
        assertThat(coerce(arrayListOfValues, new TypeToken<LinkedList<String>>(){}.getType()), is(linkedListOfValues));

        assertThrows(KelCoercionException.class,
            () -> coerce(arrayListOfValues, new TypeToken<Collection<BigDecimal>>(){}.getType()));
        assertThrows(KelCoercionException.class,
            () -> coerce(arrayListOfValues, new TypeToken<LinkedHashSet<String>>(){}.getType()));
        assertThrows(KelCoercionException.class,
            () -> coerce(arrayListOfValues, new TypeToken<Vector<String>>(){}.getType()));
        assertThrows(KelCoercionException.class,
            () -> coerce(arrayListOfValues, new TypeToken<Map<String, String>>(){}.getType()));
    }

    @Test
    public void shouldCoerceToParameterizedNumberCollection() {
        var arrayListOfValues = List.of(Money.of(1, "USD"), 2, 3L, BigDecimal.valueOf(4));
        var arrayListOfNumberValues = List.of(BigDecimal.valueOf(1), 2, 3L, BigDecimal.valueOf(4));
        var arrayListOfBigDecimalValues = List.of(BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3),
            BigDecimal.valueOf(4));
        var hashSetOfBigDecimalValues = new HashSet<>(arrayListOfBigDecimalValues);
        var linkedListOfBigDecimalValues = new LinkedList<>(arrayListOfBigDecimalValues);

        assertThat(
            coerce(arrayListOfValues, new TypeToken<Collection<Object>>(){}.getType()),
            is(arrayListOfValues)
        );
        assertThat(
            coerce(arrayListOfValues, new TypeToken<Collection<Number>>(){}.getType()),
            is(arrayListOfNumberValues)
        );
        assertThat(
            coerce(arrayListOfValues, new TypeToken<Collection<BigDecimal>>(){}.getType()),
            is(arrayListOfBigDecimalValues)
        );
        assertThat(
            coerce(arrayListOfValues, new TypeToken<List<BigDecimal>>(){}.getType()),
            is(arrayListOfBigDecimalValues)
        );
        assertThat(
            coerce(arrayListOfValues, new TypeToken<ArrayList<BigDecimal>>(){}.getType()),
            is(arrayListOfBigDecimalValues)
        );
        assertThat(
            coerce(arrayListOfValues, new TypeToken<Set<BigDecimal>>(){}.getType()),
            is(hashSetOfBigDecimalValues)
        );
        assertThat(
            coerce(arrayListOfValues, new TypeToken<HashSet<BigDecimal>>(){}.getType()),
            is(hashSetOfBigDecimalValues)
        );
        assertThat(
            coerce(arrayListOfValues, new TypeToken<Queue<BigDecimal>>(){}.getType()),
            is(linkedListOfBigDecimalValues)
        );
        assertThat(
            coerce(arrayListOfValues, new TypeToken<LinkedList<BigDecimal>>(){}.getType()),
            is(linkedListOfBigDecimalValues)
        );

        assertThrows(KelCoercionException.class,
            () -> coerce(arrayListOfValues, new TypeToken<LinkedHashSet<BigDecimal>>(){}.getType()));
        assertThrows(KelCoercionException.class,
            () -> coerce(arrayListOfValues, new TypeToken<Vector<BigDecimal>>(){}.getType()));
        assertThrows(KelCoercionException.class,
            () -> coerce(arrayListOfValues, new TypeToken<Map<String, BigDecimal>>(){}.getType()));
    }

    @Test
    public void shouldCoerceToParameterizedEntityCollection() {
        var arrayListOfValues = List.of(new Entity(), new Entity());
        var hashSetOfValues = new HashSet<>(arrayListOfValues);
        var linkedListOfValues = new LinkedList<>(arrayListOfValues);

        assertThat(coerce(arrayListOfValues, new TypeToken<Collection<Object>>(){}.getType()), is(arrayListOfValues));
        assertThat(coerce(arrayListOfValues, new TypeToken<Collection<Entity>>(){}.getType()), is(arrayListOfValues));
        assertThat(coerce(arrayListOfValues, new TypeToken<Collection<BaseEntity>>(){}.getType()), is(arrayListOfValues));
        assertThat(coerce(arrayListOfValues, new TypeToken<List<Entity>>(){}.getType()), is(arrayListOfValues));
        assertThat(coerce(arrayListOfValues, new TypeToken<List<BaseEntity>>(){}.getType()), is(arrayListOfValues));
        assertThat(coerce(arrayListOfValues, new TypeToken<ArrayList<Entity>>(){}.getType()), is(arrayListOfValues));
        assertThat(coerce(arrayListOfValues, new TypeToken<ArrayList<BaseEntity>>(){}.getType()), is(arrayListOfValues));
        assertThat(coerce(arrayListOfValues, new TypeToken<Set<Entity>>(){}.getType()), is(hashSetOfValues));
        assertThat(coerce(arrayListOfValues, new TypeToken<Set<BaseEntity>>(){}.getType()), is(hashSetOfValues));
        assertThat(coerce(arrayListOfValues, new TypeToken<HashSet<Entity>>(){}.getType()), is(hashSetOfValues));
        assertThat(coerce(arrayListOfValues, new TypeToken<HashSet<BaseEntity>>(){}.getType()), is(hashSetOfValues));
        assertThat(coerce(arrayListOfValues, new TypeToken<Queue<Entity>>(){}.getType()), is(linkedListOfValues));
        assertThat(coerce(arrayListOfValues, new TypeToken<Queue<BaseEntity>>(){}.getType()), is(linkedListOfValues));
        assertThat(coerce(arrayListOfValues, new TypeToken<LinkedList<Entity>>(){}.getType()), is(linkedListOfValues));
        assertThat(coerce(arrayListOfValues, new TypeToken<LinkedList<BaseEntity>>(){}.getType()), is(linkedListOfValues));

        assertThrows(KelCoercionException.class,
            () -> coerce(arrayListOfValues, new TypeToken<Collection<OtherEntity>>(){}.getType()));
        assertThrows(KelCoercionException.class,
            () -> coerce(arrayListOfValues, new TypeToken<LinkedHashSet<Entity>>(){}.getType()));
        assertThrows(KelCoercionException.class,
            () -> coerce(arrayListOfValues, new TypeToken<Vector<Entity>>(){}.getType()));
        assertThrows(KelCoercionException.class,
            () -> coerce(arrayListOfValues, new TypeToken<Map<String, Entity>>(){}.getType()));
    }

    @Test
    public void shouldCoerceToWildcardNumberCollection() {
        var arrayListOfValues = List.of(Money.of(1, "USD"), 2, 3L, BigDecimal.valueOf(4));
        var arrayListOfNumberValues = List.of(BigDecimal.valueOf(1), 2, 3L, BigDecimal.valueOf(4));
        var arrayListOfBigDecimalValues = List.of(BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3),
            BigDecimal.valueOf(4));
        var hashSetOfBigDecimalValues = new HashSet<>(arrayListOfBigDecimalValues);
        var linkedListOfBigDecimalValues = new LinkedList<>(arrayListOfBigDecimalValues);

        assertThat(
            coerce(arrayListOfValues, new TypeToken<Collection<? extends Number>>(){}.getType()),
            is(arrayListOfNumberValues)
        );
        assertThat(
            coerce(arrayListOfValues, new TypeToken<List<? extends BigDecimal>>(){}.getType()),
            is(arrayListOfBigDecimalValues)
        );
        assertThat(
            coerce(arrayListOfValues, new TypeToken<HashSet<? extends BigDecimal>>(){}.getType()),
            is(hashSetOfBigDecimalValues)
        );
        assertThat(
            coerce(arrayListOfValues, new TypeToken<LinkedList<? extends BigDecimal>>(){}.getType()),
            is(linkedListOfBigDecimalValues)
        );

        assertThrows(KelCoercionException.class,
            () -> coerce(arrayListOfValues, new TypeToken<LinkedHashSet<? extends BigDecimal>>(){}.getType()));
        assertThrows(KelCoercionException.class,
            () -> coerce(arrayListOfValues, new TypeToken<Vector<? extends BigDecimal>>(){}.getType()));
        assertThrows(KelCoercionException.class,
            () -> coerce(arrayListOfValues, new TypeToken<Map<String, ? extends BigDecimal>>(){}.getType()));
    }

    @Test
    public void shouldCoerceToWildcardEntityCollection() {
        var arrayListOfValues = List.of(new Entity(), new Entity());
        var hashSetOfValues = new HashSet<>(arrayListOfValues);
        var linkedListOfValues = new LinkedList<>(arrayListOfValues);

        assertThat(coerce(arrayListOfValues, new TypeToken<Collection<? extends Entity>>(){}.getType()), is(arrayListOfValues));
        assertThat(coerce(arrayListOfValues, new TypeToken<Collection<? extends BaseEntity>>(){}.getType()), is(arrayListOfValues));
        assertThat(coerce(arrayListOfValues, new TypeToken<Collection<? super Entity>>(){}.getType()), is(arrayListOfValues));
        assertThat(coerce(arrayListOfValues, new TypeToken<Collection<? super BaseEntity>>(){}.getType()), is(arrayListOfValues));
        assertThat(coerce(arrayListOfValues, new TypeToken<List<? extends Entity>>(){}.getType()), is(arrayListOfValues));
        assertThat(coerce(arrayListOfValues, new TypeToken<Set<? extends Entity>>(){}.getType()), is(hashSetOfValues));
        assertThat(coerce(arrayListOfValues, new TypeToken<HashSet<? extends Entity>>(){}.getType()), is(hashSetOfValues));
        assertThat(coerce(arrayListOfValues, new TypeToken<Queue<? extends Entity>>(){}.getType()), is(linkedListOfValues));
        assertThat(coerce(arrayListOfValues, new TypeToken<LinkedList<? extends Entity>>(){}.getType()), is(linkedListOfValues));

        assertThrows(KelCoercionException.class,
            () -> coerce(arrayListOfValues, new TypeToken<Collection<? extends OtherEntity>>(){}.getType()));
        assertThrows(KelCoercionException.class,
            () -> coerce(arrayListOfValues, new TypeToken<LinkedHashSet<? extends Entity>>(){}.getType()));
        assertThrows(KelCoercionException.class,
            () -> coerce(arrayListOfValues, new TypeToken<Vector<? extends Entity>>(){}.getType()));
        assertThrows(KelCoercionException.class,
            () -> coerce(arrayListOfValues, new TypeToken<Map<String, ? extends Entity>>(){}.getType()));
    }

    @Test
    public void shouldCoerceToRawMap() {
        var mapOfValues = Map.of("entity", new Entity(), "value", 10);

        assertThat(coerce(mapOfValues, Object.class), is(mapOfValues));
        assertThat(coerce(mapOfValues, Map.class), is(mapOfValues));
        assertThat(coerce(mapOfValues, HashMap.class), is(mapOfValues));

        assertThrows(KelCoercionException.class, () -> coerce(mapOfValues, Collection.class));
        assertThrows(KelCoercionException.class, () -> coerce(mapOfValues, TreeMap.class));
    }

    @Test
    public void shouldCoerceToParameterizedNumberMap() {
        var mapOfValues = Map.of("1", Money.of(1, "USD"), "2", 2);
        var mapOfNumberValues = Map.of("1", BigDecimal.valueOf(1), "2", 2);
        var mapOfBigDecimalValues = Map.of("1", BigDecimal.valueOf(1), "2", BigDecimal.valueOf(2));

        assertThat(
            coerce(mapOfValues, new TypeToken<Map<String, Object>>(){}.getType()),
            is(mapOfValues)
        );
        assertThat(
            coerce(mapOfValues, new TypeToken<Map<String, Number>>(){}.getType()),
            is(mapOfNumberValues)
        );
        assertThat(
            coerce(mapOfValues, new TypeToken<Map<String, BigDecimal>>(){}.getType()),
            is(mapOfBigDecimalValues)
        );
    }

    @Test
    public void shouldCoerceToParameterizedEntityMap() {
        var mapOfValues = Map.of("entity", new Entity());

        assertThat(
            coerce(mapOfValues, new TypeToken<Map<String, Object>>(){}.getType()),
            is(mapOfValues)
        );
        assertThat(
            coerce(mapOfValues, new TypeToken<Map<String, BaseEntity>>(){}.getType()),
            is(mapOfValues)
        );
        assertThat(
            coerce(mapOfValues, new TypeToken<Map<String, Entity>>(){}.getType()),
            is(mapOfValues)
        );

        assertThrows(KelCoercionException.class,
            () -> coerce(mapOfValues, new TypeToken<Map<String, OtherEntity>>(){}.getType()));
    }

    @Test
    public void shouldCoerceToWildcardNumberMap() {
        var mapOfValues = Map.of("1", Money.of(1, "USD"), "2", 2);
        var mapOfNumberValues = Map.of("1", BigDecimal.valueOf(1), "2", 2);
        var mapOfBigDecimalValues = Map.of("1", BigDecimal.valueOf(1), "2", BigDecimal.valueOf(2));

        assertThat(
            coerce(mapOfValues, new TypeToken<Map<String, ? extends Number>>(){}.getType()),
            is(mapOfNumberValues)
        );
        assertThat(
            coerce(mapOfValues, new TypeToken<Map<String, ? extends BigDecimal>>(){}.getType()),
            is(mapOfBigDecimalValues)
        );
    }

    @Test
    public void shouldCoerceToWildcardEntityMap() {
        var mapOfValues = Map.of("entity", new Entity());

        assertThat(
            coerce(mapOfValues, new TypeToken<Map<String, ? extends BaseEntity>>(){}.getType()),
            is(mapOfValues)
        );
        assertThat(
            coerce(mapOfValues, new TypeToken<Map<String, ? extends Entity>>(){}.getType()),
            is(mapOfValues)
        );
        assertThat(
            coerce(mapOfValues, new TypeToken<Map<String, ? super Entity>>(){}.getType()),
            is(mapOfValues)
        );

        assertThrows(KelCoercionException.class,
            () -> coerce(mapOfValues, new TypeToken<Map<String, ? extends OtherEntity>>(){}.getType()));
    }

    @Test
    public void shouldPassThroughParameterizedEntity() {
        var mapOfValues = Map.of("entity", new ParameterizedEntity<>());
        assertThat(
            coerce(mapOfValues, new TypeToken<Map<String, ? extends ParameterizedEntity<BigDecimal, List<BigDecimal>>>>(){}.getType()),
            is(mapOfValues)
        );
        assertThat(
            coerce(mapOfValues, new TypeToken<Map<String, ? extends BaseEntity>>(){}.getType()),
            is(mapOfValues)
        );
    }

    @Test
    public void shouldPassThroughWildcardEntity() {
        var mapOfValues = Map.of("entity", new WildcardEntity<>());
        assertThat(
            coerce(mapOfValues, new TypeToken<Map<String, ? extends WildcardEntity<? extends BigDecimal, List<BigDecimal>>>>(){}.getType()),
            is(mapOfValues)
        );
        assertThat(
            coerce(mapOfValues, new TypeToken<Map<String, ? extends BaseEntity>>(){}.getType()),
            is(mapOfValues)
        );
    }

    @Test
    public void shouldCoercePrimitivesByTypedVariables() throws NoSuchMethodException {
        var setNumberParameterType = KelCoercerTest.class.getMethod("setNumber", Number.class).getGenericParameterTypes()[0];
        var setBigDecimalParameterType = KelCoercerTest.class.getMethod("setBigDecimal", BigDecimal.class).getGenericParameterTypes()[0];

        assertThat(coerce(10, setNumberParameterType), is(10));
        assertThat(coerce(Money.of(10, "USD"), setNumberParameterType), is(BigDecimal.valueOf(10)));
        assertThat(coerce(10, setBigDecimalParameterType), is(BigDecimal.valueOf(10)));
        assertThat(coerce(Money.of(10, "USD"), setBigDecimalParameterType), is(BigDecimal.valueOf(10)));

        assertThrows(KelCoercionException.class, () -> coerce("10", setNumberParameterType));
    }

    @Test
    public void shouldCoerceCollectionByTypedVariables() throws NoSuchMethodException {
        var setNumbersParameterType = KelCoercerTest.class.getMethod("setNumbersParameterType", Collection.class)
            .getGenericParameterTypes()[0];
        var setNumbersCollectionType = KelCoercerTest.class.getMethod("setNumbersCollectionType", Collection.class)
            .getGenericParameterTypes()[0];
        var setNumbersHashSetCollectionType = KelCoercerTest.class.getMethod("setNumbersHashSetParameterType", HashSet.class)
            .getGenericParameterTypes()[0];
        var setBigDecimalsParameterType = KelCoercerTest.class.getMethod("setBigDecimalsParameterType", Collection.class)
            .getGenericParameterTypes()[0];
        var setBigDecimalsCollectionType = KelCoercerTest.class.getMethod("setBigDecimalsCollectionType", Collection.class)
            .getGenericParameterTypes()[0];
        var setBigDecimalsHashSetCollectionType = KelCoercerTest.class.getMethod("setBigDecimalsHashSetCollectionType", HashSet.class)
            .getGenericParameterTypes()[0];

        var arrayListOfValues = List.of(Money.of(1, "USD"), 2, 3L, BigDecimal.valueOf(4));
        var arrayListOfNumberValues = List.of(BigDecimal.valueOf(1), 2, 3L, BigDecimal.valueOf(4));
        var arrayListOfBigDecimalValues = List.of(BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3),
            BigDecimal.valueOf(4));

        assertThat(
            coerce(arrayListOfValues, setNumbersParameterType),
            is(arrayListOfNumberValues)
        );
        assertThat(
            coerce(arrayListOfValues, setNumbersCollectionType),
            is(arrayListOfNumberValues)
        );
        assertThat(
            coerce(arrayListOfValues, setNumbersHashSetCollectionType),
            is(new HashSet<>(arrayListOfNumberValues))
        );
        assertThat(
            coerce(arrayListOfValues, setBigDecimalsParameterType),
            is(arrayListOfBigDecimalValues)
        );
        assertThat(
            coerce(arrayListOfValues, setBigDecimalsCollectionType),
            is(arrayListOfBigDecimalValues)
        );
        assertThat(
            coerce(arrayListOfValues, setBigDecimalsHashSetCollectionType),
            is(new HashSet<>(arrayListOfBigDecimalValues))
        );
    }

    @Test
    public void shouldCoerceEntitiesByTypedVariables() throws NoSuchMethodException {
        var setEntity = KelCoercerTest.class.getMethod("setEntity", BaseEntity.class).getGenericParameterTypes()[0];
        var setEntityCollection = KelCoercerTest.class.getMethod("setEntityCollection", Collection.class).getGenericParameterTypes()[0];
        var setEntitySet = KelCoercerTest.class.getMethod("setEntitySet", Set.class).getGenericParameterTypes()[0];
        var setEntityMap = KelCoercerTest.class.getMethod("setEntityMap", Map.class).getGenericParameterTypes()[0];
        var setParameterizedEntity = KelCoercerTest.class.getMethod("setParameterizedEntity", ParameterizedEntity.class).getGenericParameterTypes()[0];

        var entity = new Entity();
        var listOfEntities = List.of(entity);
        var mapOfEntities = Map.of("entity", entity);
        var parameterizedEntity = new ParameterizedEntity<>();

        assertThat(coerce(entity, setEntity), is(entity));
        assertThat(coerce(listOfEntities, setEntityCollection), is(new ArrayList<>(listOfEntities)));
        assertThat(coerce(listOfEntities, setEntitySet), is(new HashSet<>(listOfEntities)));
        assertThat(coerce(mapOfEntities, setEntityMap), is(mapOfEntities));
        assertThat(coerce(parameterizedEntity, setEntity), is(parameterizedEntity));
        assertThat(coerce(parameterizedEntity, setParameterizedEntity), is(parameterizedEntity));

        assertThrows(KelCoercionException.class, () -> coerce(entity, setParameterizedEntity));
        assertThrows(KelCoercionException.class, () -> coerce(new Object(), setEntity));
        assertThrows(KelCoercionException.class, () -> coerce(List.of(new Object()), setEntityCollection));
        assertThrows(KelCoercionException.class, () -> coerce(List.of(new Object()), setEntitySet));
        assertThrows(KelCoercionException.class, () -> coerce(Map.of("entity", new Object()), setEntityMap));
    }

    interface BaseEntity {

    }

    static class Entity implements BaseEntity {

    }

    static class ParameterizedEntity<V extends Number, T extends Collection<V>> implements BaseEntity {

    }

    static class WildcardEntity<V extends Number, T extends Collection<? extends V>> implements BaseEntity {

    }

    static class OtherEntity implements BaseEntity {

    }

    public static <T extends BaseEntity> void setEntity(T entity) {

    }

    public static <T extends Collection<BaseEntity>> void setEntityCollection(T entities) {

    }

    public static <T extends Set<BaseEntity>> void setEntitySet(T entities) {

    }

    public static <KEY extends String, T extends Map<KEY, ? extends BaseEntity>> void setEntityMap(T entity) {

    }

    public static <T extends ParameterizedEntity<BigDecimal, HashSet<BigDecimal>>> void setParameterizedEntity(T entity) {

    }

    public static <T extends Number> void setNumber(T number) {

    }

    public static <T extends Number> void setNumbersCollectionType(Collection<T> numbers) {

    }

    public static <T extends Collection<? extends Number>> void setNumbersParameterType(T numbers) {

    }

    public static <T extends HashSet<? extends Number>> void setNumbersHashSetParameterType(T numbers) {

    }

    public static <T extends BigDecimal> void setBigDecimal(T bigDecimal) {

    }

    public static <T extends BigDecimal> void setBigDecimalsCollectionType(Collection<T> bigDecimals) {

    }

    public static <T extends HashSet<? extends BigDecimal>> void setBigDecimalsHashSetCollectionType(T bigDecimals) {

    }

    public static <T extends Collection<? extends BigDecimal>> void setBigDecimalsParameterType(T numbers) {

    }
}
