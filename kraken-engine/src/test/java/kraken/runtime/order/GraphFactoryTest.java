package kraken.runtime.order;

import static kraken.runtime.order.GraphTestUtils.rule;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import kraken.runtime.model.context.RuntimeContextDefinition;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.repository.RuntimeContextRepository;

/**
 * @author psurinin@eisgroup.com
 * @since 1.0.29
 */
public class GraphFactoryTest {

    private GraphFactory graphFactory;

    @Before
    public void setUp() {
        RuntimeContextRepository contextRepository = mock(RuntimeContextRepository.class);

        RuntimeContextDefinition ABC = mock(RuntimeContextDefinition.class);
        when(contextRepository.getContextDefinition("ABC")).thenReturn(ABC);
        when(ABC.getInheritedContexts()).thenReturn(List.of());

        RuntimeContextDefinition A = mock(RuntimeContextDefinition.class);
        when(A.getInheritedContexts()).thenReturn(List.of("ABC"));
        when(contextRepository.getContextDefinition("A")).thenReturn(A);

        RuntimeContextDefinition B = mock(RuntimeContextDefinition.class);
        when(B.getInheritedContexts()).thenReturn(List.of("ABC"));
        when(contextRepository.getContextDefinition("B")).thenReturn(B);

        RuntimeContextDefinition C = mock(RuntimeContextDefinition.class);
        when(C.getInheritedContexts()).thenReturn(List.of("ABC"));
        when(contextRepository.getContextDefinition("C")).thenReturn(C);

        RuntimeContextDefinition AA = mock(RuntimeContextDefinition.class);
        when(AA.getInheritedContexts()).thenReturn(List.of("A"));
        when(contextRepository.getContextDefinition("AA")).thenReturn(AA);

        RuntimeContextDefinition BB = mock(RuntimeContextDefinition.class);
        when(BB.getInheritedContexts()).thenReturn(List.of("B"));
        when(contextRepository.getContextDefinition("BB")).thenReturn(BB);

        RuntimeContextDefinition CC = mock(RuntimeContextDefinition.class);
        when(CC.getInheritedContexts()).thenReturn(List.of("C"));
        when(contextRepository.getContextDefinition("CC")).thenReturn(CC);

        this.graphFactory = new GraphFactory(contextRepository);
    }

    @Test
    public void shouldCalculateSimpleGraph() {
        RuntimeRule r01 = rule("R01", "A.a", "B.b");
        RuntimeRule r02 = rule("R02", "B.b", "C.c");
        RuntimeRule r03 = rule("R03", "C.c", "D.d");

        // list created with order to be sorted topologically
        List<RuntimeRule> ordered = List.of(r01, r02, r03);
        List<RuntimeRule> unordered = List.of(r03, r02, r01);
        assertOrder(ordered, graphFactory.createGraph(unordered));
    }

    @Test
    public void shouldCalculateGraphWithInheritanceDifferentTypes() {
        RuntimeRule r01 = rule("R01", "A.b", "A.a");
        RuntimeRule r02 = rule("R02", "AA.a", null);

        // list created with order to be sorted topologically
        List<RuntimeRule> ordered = List.of(r01, r02);
        List<RuntimeRule> unordered = List.of(r02, r01);
        assertOrder(ordered, graphFactory.createGraph(unordered));
    }

    @Test
    public void shouldCalculateGraphWithInheritance() {
        RuntimeRule r01 = rule("R01", "A.a", "BB.b");
        RuntimeRule r02 = rule("R02", "B.b", "CC.c");
        RuntimeRule r03 = rule("R03", "C.c", "D.d");

        // list created with order to be sorted topologically
        List<RuntimeRule> ordered = List.of(r01, r02, r03);
        List<RuntimeRule> unordered = List.of(r02, r03, r01);
        assertOrder(ordered, graphFactory.createGraph(unordered));
    }

    @Test
    public void shouldCalculateGraphWithInheritanceDifferentFields() {
        RuntimeRule r01 = rule("R01", "A.a", "BB.b");
        RuntimeRule r02 = rule("R02", "B.b", "CC.c");
        RuntimeRule r03 = rule("R03", "C.c", "D.d");

        // list created with order to be sorted topologically
        List<RuntimeRule> ordered = List.of(r01, r02, r03);
        List<RuntimeRule> unordered = List.of(r02, r03, r01);
        assertOrder(ordered, graphFactory.createGraph(unordered));
    }

    @Test
    public void shouldCalculateGraphWhenDependsOnInheritedTypeButSameAttribute() {
        RuntimeRule r01 = rule("R01", "A.a", "ABC.a");

        List<RuntimeRule> ordered = List.of(r01);
        List<RuntimeRule> unordered = List.of(r01);
        assertOrder(ordered, graphFactory.createGraph(unordered));
    }

    @Test
    public void shouldCalculateGraphWhenDependsOnSelf() {
        RuntimeRule r01 = rule("R01", "A.a", "A.a");
        RuntimeRule r02 = rule("R02", "A.b", "A.a");

        List<RuntimeRule> ordered = List.of(r02, r01);
        List<RuntimeRule> unordered = List.of(r01, r02);
        assertOrder(ordered, graphFactory.createGraph(unordered));
    }

    private void assertOrder(List<RuntimeRule> rules, Graph<RuntimeRule, DefaultEdge> graph) {
        Assert.assertThat(
                strings(new TopologicalOrderIterator<>(graph)),
                is(strings(rules.iterator()))
        );
    }

    private List<String> strings(Iterator<RuntimeRule> iterator) {
        List<String> order = new ArrayList<>();
        while (iterator.hasNext()) {
            order.add(iterator.next().getName());
        }
        return order;
    }

}
