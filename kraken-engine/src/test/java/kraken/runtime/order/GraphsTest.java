package kraken.runtime.order;

import kraken.runtime.model.context.RuntimeContextDefinition;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.repository.RuntimeContextRepository;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;

import static kraken.runtime.order.GraphTestUtils.rule;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GraphsTest {

    @Test
    public void shouldCreateSortedListFromGraph() {
        RuntimeContextDefinition contextDefinition = mock(RuntimeContextDefinition.class);
        when(contextDefinition.getInheritedContexts()).thenReturn(List.of());
        RuntimeContextRepository contextRepository = mock(RuntimeContextRepository.class);
        when(contextRepository.getContextDefinition(anyString())).thenReturn(contextDefinition);
        GraphFactory graphFactory = new GraphFactory(contextRepository);

        RuntimeRule r01 = rule("R01", "A.a", "B.b");
        RuntimeRule r02 = rule("R02", "B.b", "C.c");
        RuntimeRule r03 = rule("R03", "C.c", "D.d");

        HashSet<RuntimeRule> rules = new HashSet<>();
        rules.add(r01);
        rules.add(r02);
        rules.add(r03);
        List<RuntimeRule> list = graphFactory.getOrderedRules(rules);
        assertEquals(r03, list.get(0));
        assertEquals(r01, list.get(2));
        assertEquals(r02, list.get(1));
    }

}