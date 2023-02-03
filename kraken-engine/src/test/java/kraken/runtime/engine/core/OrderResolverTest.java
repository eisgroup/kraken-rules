package kraken.runtime.engine.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import kraken.dimensions.DimensionSet;
import kraken.model.context.Cardinality;
import kraken.model.context.PrimitiveFieldDataType;
import kraken.runtime.KrakenRuntimeException;
import kraken.runtime.engine.core.OrderResolver.Field;
import kraken.runtime.model.Metadata;
import kraken.runtime.model.context.ContextField;
import kraken.runtime.model.context.ContextNavigation;
import kraken.runtime.model.context.RuntimeContextDefinition;
import kraken.runtime.model.rule.Dependency;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.repository.RuntimeContextRepository;

/**
 * @author mulevicius
 * @since 1.40.0
 */
public class OrderResolverTest {

    private OrderResolver orderResolver;

    private RuntimeContextRepository contextRepository;

    @Before
    public void setUp() {
        this.contextRepository = mock(RuntimeContextRepository.class);

        RuntimeContextDefinition ABC = mock(RuntimeContextDefinition.class);
        when(contextRepository.getContextDefinition("ABC")).thenReturn(ABC);
        when(ABC.getName()).thenReturn("ABC");
        when(ABC.getInheritedContexts()).thenReturn(List.of());
        when(ABC.getChildren()).thenReturn(Map.of());
        when(ABC.getFields()).thenReturn(fields("a", "b", "c"));

        RuntimeContextDefinition A = mock(RuntimeContextDefinition.class);
        when(A.getName()).thenReturn("A");
        when(A.getInheritedContexts()).thenReturn(List.of("ABC"));
        when(A.getChildren()).thenReturn(Map.of());
        when(contextRepository.getContextDefinition("A")).thenReturn(A);
        when(A.getFields()).thenReturn(fields("a", "b", "c"));

        RuntimeContextDefinition B = mock(RuntimeContextDefinition.class);
        when(B.getName()).thenReturn("B");
        when(B.getInheritedContexts()).thenReturn(List.of("ABC"));
        when(B.getChildren()).thenReturn(Map.of());
        when(contextRepository.getContextDefinition("B")).thenReturn(B);
        when(B.getFields()).thenReturn(fields("a", "b", "c"));

        RuntimeContextDefinition C = mock(RuntimeContextDefinition.class);
        when(C.getName()).thenReturn("C");
        when(C.getInheritedContexts()).thenReturn(List.of("ABC"));
        when(C.getChildren()).thenReturn(Map.of());
        when(contextRepository.getContextDefinition("C")).thenReturn(C);
        when(C.getFields()).thenReturn(fields("a", "b", "c"));

        RuntimeContextDefinition AA = mock(RuntimeContextDefinition.class);
        when(AA.getName()).thenReturn("AA");
        when(AA.getInheritedContexts()).thenReturn(List.of("A", "ABC"));
        when(AA.getChildren()).thenReturn(Map.of());
        when(contextRepository.getContextDefinition("AA")).thenReturn(AA);
        when(AA.getFields()).thenReturn(fields("a", "b", "c"));

        RuntimeContextDefinition BB = mock(RuntimeContextDefinition.class);
        when(BB.getName()).thenReturn("BB");
        when(BB.getInheritedContexts()).thenReturn(List.of("B", "ABC"));
        when(BB.getChildren()).thenReturn(Map.of());
        when(contextRepository.getContextDefinition("BB")).thenReturn(BB);
        when(BB.getFields()).thenReturn(fields("a", "b", "c"));

        RuntimeContextDefinition BB2 = mock(RuntimeContextDefinition.class);
        when(BB2.getName()).thenReturn("BB2");
        when(BB2.getInheritedContexts()).thenReturn(List.of("B", "ABC"));
        when(BB2.getChildren()).thenReturn(Map.of());
        when(contextRepository.getContextDefinition("BB2")).thenReturn(BB2);
        when(BB2.getFields()).thenReturn(fields("a", "b", "c"));

        RuntimeContextDefinition CC = mock(RuntimeContextDefinition.class);
        when(CC.getName()).thenReturn("CC");
        when(CC.getInheritedContexts()).thenReturn(List.of("C", "ABC"));
        when(CC.getChildren()).thenReturn(Map.of());
        when(contextRepository.getContextDefinition("CC")).thenReturn(CC);
        when(CC.getFields()).thenReturn(fields("a", "b", "c"));

        RuntimeContextDefinition root = mock(RuntimeContextDefinition.class);
        when(root.getName()).thenReturn("Root");
        when(root.getInheritedContexts()).thenReturn(List.of());
        when(root.getFields()).thenReturn(Map.of());
        when(contextRepository.getContextDefinition("Root")).thenReturn(root);
        when(contextRepository.getRootContextDefinition()).thenReturn(root);
        when(root.getChildren()).thenReturn(Map.of(
            "AA", new ContextNavigation("AA", null, Cardinality.SINGLE),
            "BB", new ContextNavigation("BB", null, Cardinality.SINGLE),
            "BB2", new ContextNavigation("BB2", null, Cardinality.SINGLE),
            "CC", new ContextNavigation("CC", null, Cardinality.SINGLE)
        ));

        when(contextRepository.getAllContextDefinitionNames())
            .thenReturn(Set.of("Root", "ABC", "A", "B", "C", "AA", "BB", "BB2", "CC"));


        this.orderResolver = new OrderResolver(contextRepository);
    }

    private Map<String, ContextField> fields(String ...fieldNames) {
        return Arrays.stream(fieldNames).collect(Collectors.toMap(
            f -> f,
            f -> new ContextField(f, PrimitiveFieldDataType.STRING.toString(), f, Cardinality.SINGLE)
        ));
    }

    @Test
    public void shouldCalculateOrderWhenDependsOnBaseTypes() {
        RuntimeRule r01 = rule("R01", "A.a", "B.b");
        RuntimeRule r02 = rule("R02", "B.b", "C.c");
        RuntimeRule r03 = rule("R03", "C.c", null);

        List<Field> expected = List.of(
            new Field("CC", "c"),
            new Field("BB", "b"),
            new Field("BB2", "b"),
            new Field("AA", "a")
        );

        List<Field> actual = orderResolver.resolveOrderedFields(List.of(r01, r02, r03));

        assertThat(actual, equalTo(expected));
    }

    @Test
    public void shouldCalculateOrderWhenDependsOnEntityTypes() {
        RuntimeRule r01 = rule("R01", "A.a", "BB.b");
        RuntimeRule r02 = rule("R02", "B.b", "CC.c");
        RuntimeRule r03 = rule("R03", "C.c", null);

        List<Field> expected = List.of(
            new Field("CC", "c"),
            new Field("BB", "b"),
            new Field("BB2", "b"),
            new Field("AA", "a")
        );

        List<Field> actual = orderResolver.resolveOrderedFields(List.of(r01, r02, r03));

        assertThat(actual, equalTo(expected));
    }

    @Test
    public void shouldCalculateOrderWithInheritanceDifferentTypes() {
        RuntimeRule r01 = rule("R01", "A.b", "A.a");
        RuntimeRule r02 = rule("R02", "AA.a", null);

        // list created with order to be sorted topologically
        List<Field> expected = List.of(
            new Field("AA", "a"),
            new Field("AA", "b")
        );

        List<Field> actual = orderResolver.resolveOrderedFields(List.of(r01, r02));

        assertThat(actual, equalTo(expected));
    }

    @Test
    public void shouldCalculateOrderWhenDependsOnInheritedTypeButSameField() {
        RuntimeRule r01 = rule("R01", "A.a", "ABC.a");

        List<Field> expected = List.of(
            new Field("AA", "a")
        );

        List<Field> actual = orderResolver.resolveOrderedFields(List.of(r01));

        assertThat(actual, equalTo(expected));
    }

    @Test
    public void shouldCalculateOrderWhenDependsOnSelf() {
        RuntimeRule r01 = rule("R01", "A.a", "A.a");
        RuntimeRule r02 = rule("R02", "A.b", "A.a");

        List<Field> expected = List.of(
            new Field("AA", "a"),
            new Field("AA", "b")
        );

        List<Field> actual = orderResolver.resolveOrderedFields(List.of(r01, r02));

        assertThat(actual, equalTo(expected));
    }

    @Test
    public void shouldCalculateOrderWhenCrossDepends() {
        RuntimeRule r01 = rule("R01", "A.a", "BB.b");
        RuntimeRule r02 = rule("R02", "BB2.b", "A.a");

        List<Field> expected = List.of(
            new Field("AA", "a"),
            new Field("BB2", "b")
        );

        List<Field> actual = orderResolver.resolveOrderedFields(List.of(r01, r02));

        assertThat(actual, equalTo(expected));
    }

    @Test(expected = KrakenRuntimeException.class)
    public void shouldCalculateOrderWithCycleLowerInheritance() {
        RuntimeRule r01 = rule("R01", "ABC.a", "BB.b");
        RuntimeRule r02 = rule("R02", "B.b", "ABC.a");

        orderResolver.resolveOrderedFields(List.of(r01, r02));
    }

    @Test(expected = KrakenRuntimeException.class)
    public void shouldCalculateOrderWithCycleUpperInheritance() {
        RuntimeRule r01 = rule("R01", "ABC.a", "ABC.b");
        RuntimeRule r02 = rule("R02", "B.b", "ABC.a");

        orderResolver.resolveOrderedFields(List.of(r01, r02));
    }

    @Test(expected = KrakenRuntimeException.class)
    public void shouldCalculateOrderWithCycleOnSelf() {
        RuntimeRule r01 = rule("R01", "A.a", "A.b");
        RuntimeRule r02 = rule("R02", "A.b", "A.a");

        orderResolver.resolveOrderedFields(List.of(r01, r02));
    }

    public RuntimeRule rule(String name, String appliedOn, String dependsOn) {
        var targetContext = appliedOn.split("\\.")[0];
        var targetField = appliedOn.split("\\.")[1];

        List<Dependency> dependencies = new ArrayList<>();
        if (dependsOn != null) {
            var context = dependsOn.split("\\.")[0];
            var field = dependsOn.split("\\.")[1];
            var self = contextRepository.getContextDefinition(targetContext).getInheritedContexts().contains(context);
            dependencies.add(new Dependency(context, field, true, self));
        }

        return new RuntimeRule(
            name,
            targetContext,
            targetField,
            null,
            null,
            dependencies,
            DimensionSet.createStatic(),
            new Metadata(Map.of()),
            null
        );
    }
}
