package kraken.runtime.context.model.tree;

import java.util.Collection;

import kraken.context.model.tree.ContextModelTree;
import kraken.context.model.tree.impl.ContextModelTrees;
import kraken.context.model.tree.impl.ContextRepository;
import kraken.context.path.ContextPath;
import kraken.converter.KrakenProjectConverter;
import kraken.el.TargetEnvironment;
import kraken.model.project.KrakenProject;
import kraken.runtime.model.context.RuntimeContextDefinition;
import kraken.runtime.model.expression.CompiledExpression;
import kraken.runtime.model.expression.ExpressionType;
import kraken.runtime.model.project.RuntimeKrakenProject;
import kraken.test.TestResources;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.MatcherAssert.assertThat;

public class ContextModelTreesTest {

    private final static ContextModelTree modelTree = getContextModelTree();

    private static ContextModelTree getContextModelTree() {
        final KrakenProject krakenProject = TestResources.create(TestResources.Info.POLICY_GENESIS).getKrakenProject();
        final RuntimeKrakenProject runtimeKrakenProject = new KrakenProjectConverter(krakenProject, TargetEnvironment.JAVA).convert();
        final ContextRepository contextRepository = ContextRepository.from(runtimeKrakenProject);
        return ContextModelTrees.create(
                contextRepository,
                TestResources.Info.POLICY_GENESIS.namespace,
                TargetEnvironment.JAVASCRIPT
        );
    }

    @Test
    public void shouldFindPathsFromTo() {
        final Collection<ContextPath> paths = modelTree.getPathsToNode("AutoPolicyParty");
        assertThat(paths, hasSize(1));
        assertThat(modelTree.getPathsToNode("PersonalAutoPolicyParty"), hasSize(1));
    }

    @Test
    public void shouldFindContextDefinition() {
        final RuntimeContextDefinition context = modelTree.getContext("PolicyOrganization");
        assertThat(context, notNullValue());
        assertThat(context.getName(), is("PolicyOrganization"));
        // translated
        final CompiledExpression navigationExpression =
                context.getChildren().values().iterator().next().getNavigationExpression();
        assertThat(navigationExpression, notNullValue());
        assertThat(navigationExpression.getExpressionType(), is(ExpressionType.PATH));
    }

    @Test
    public void shouldReturnModelTreeMeta() {
        assertThat(modelTree.getMetadata().getNamespace(), is(TestResources.Info.POLICY_GENESIS.namespace));
        assertThat(modelTree.getMetadata().getTargetEnvironment(), is(TargetEnvironment.JAVASCRIPT));
    }



}
