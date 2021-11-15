package kraken.runtime.order;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kraken.runtime.model.Metadata;
import kraken.runtime.model.rule.Dependency;
import kraken.runtime.model.rule.RuntimeRule;

/**
 * @author psurinin@eisgroup.com
 */
public class GraphTestUtils {

    public static RuntimeRule rule(String name, String appliedOn, String dependsOn) {
        List<Dependency> dependencies = new ArrayList<>();
        if (dependsOn != null) {
            dependencies.add(new Dependency(dependsOn.split("\\.")[0], dependsOn.split("\\.")[1], true));
        }

        return new RuntimeRule(
            name,
            appliedOn.split("\\.")[0],
            appliedOn.split("\\.")[1],
            null,
            null,
            dependencies,
            false,
            new Metadata(Map.of())
        );
    }

}
