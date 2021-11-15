package kraken.runtime.order;

import kraken.runtime.KrakenRuntimeException;
import kraken.runtime.model.context.RuntimeContextDefinition;
import kraken.runtime.model.rule.Dependency;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.repository.RuntimeContextRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Creates {@link Graph} from {@link RuntimeRule}s
 *
 * @author psurinin@eisgroup.com
 * @since 1.0.29
 */
public class GraphFactory {

    private final static Logger logger = LoggerFactory.getLogger(GraphFactory.class);
    private final RuntimeContextRepository contextRepository;

    public GraphFactory(RuntimeContextRepository contextRepository) {
        this.contextRepository = contextRepository;
    }

    public List<RuntimeRule> getOrderedRules(Collection<RuntimeRule> rules) {
        return createList(createGraph(rules));
    }

    public Graph<RuntimeRule, DefaultEdge> createGraph(Collection<RuntimeRule> rules) {
        DirectedMultigraph<RuntimeRule, DefaultEdge> graph = new DirectedMultigraph<>(DefaultEdge.class);
        rules.forEach(graph::addVertex);

        Map<Attribute, List<RuntimeRule>> rulesByAttribute = rules.stream()
                .flatMap(rule -> inheritedContexts(rule.getContext())
                        .stream()
                        .map(ctxName -> Pair.of(ctxName, rule)))
                .collect(Collectors.groupingBy(pair -> new Attribute(pair.getKey(), pair.getValue().getTargetPath()),
                        Collectors.mapping(pair -> pair.getValue(), Collectors.toList())));
        logger.debug("Generating dependency graph to determine rules order.");
        for (RuntimeRule rule : rules) {
            logger.debug("For rule '{}'", rule.getName());
            for (Dependency dependency : rule.getDependencies()) {
                logger.debug("For dependency '{}'", dependency.getContextName());
                Collection<String> inherited = inheritedContexts(dependency.getContextName());
                for (String contextName : inherited) {
                    List<RuntimeRule> dependant =
                            rulesByAttribute.getOrDefault(new Attribute(contextName, dependency.getTargetPath()), List.of());
                    for (RuntimeRule dep : dependant) {
                        if(!dep.getName().equals(rule.getName())) {
                            graph.addEdge(rule, dep);
                        }
                    }
                }
            }
        }
        checkCycles(graph);
        return graph;
    }

    private void checkCycles(DirectedMultigraph<RuntimeRule, DefaultEdge> graph) {
        var cycled = new CycleDetector<>(graph).findCycles();
        if (!cycled.isEmpty()) {
            var ruleTemplate = "Rule '%s' \n\tdefined on %s \n\twith dependencies to [%s]";
            var targetTemplate = "'%s.%s'";
            var message = cycled.stream().map(r -> String.format(
                    ruleTemplate,
                    r.getName(),
                    String.format(targetTemplate, r.getContext(), r.getTargetPath()),
                    r.getDependencies().stream()
                            .map(dependency -> String.format(
                                    targetTemplate,
                                    dependency.getContextName(),
                                    dependency.getTargetPath())
                            )
                            .distinct()
                            .collect(Collectors.joining(", "))
            ))
                    .collect(Collectors.joining("\n"));

            var messageTemplate = "Cycle detected between rules:\n%s";
            throw new KrakenRuntimeException(String.format(messageTemplate, message));
        }
    }

    private Collection<String> inheritedContexts(String contextName) {
        RuntimeContextDefinition contextDefinition = contextRepository.getContextDefinition(contextName);
        if (contextDefinition == null) {
            return List.of(contextName);
        }
        Collection<String> inherited = new ArrayList<>(contextDefinition.getInheritedContexts());
        inherited.add(contextName);
        return inherited;
    }

    private static List<RuntimeRule> createList(Graph<RuntimeRule, DefaultEdge> graph) {
        TopologicalOrderIterator<RuntimeRule, DefaultEdge> iterator = new TopologicalOrderIterator<>(graph);
        ArrayList<RuntimeRule> rules = new ArrayList<>();
        while (iterator.hasNext()) {
            rules.add(iterator.next());
        }
        Collections.reverse(rules);
        return rules;
    }

    private static class Attribute {
        private final String contextName;
        private final String contextField;

        public Attribute(String name, String path) {
            this.contextName = name;
            this.contextField = path;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Attribute attribute = (Attribute) o;

            return Objects.equals(contextName, attribute.contextName) &&
                    Objects.equals(contextField, attribute.contextField);
        }

        @Override
        public int hashCode() {
            return Objects.hash(contextName, contextField);
        }

        @Override
        public String toString() {
            return contextName + "." + contextField;
        }
    }

}
