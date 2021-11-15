package kraken.runtime.context.model.tree;

import kraken.context.model.tree.impl.ContextRepository;
import kraken.el.TargetEnvironment;
import kraken.namespace.Namespaced;
import kraken.context.model.tree.repository.CachingContextModelTreeRepository;
import kraken.test.TestResources;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;

import static org.mockito.Mockito.*;

public class CachingContextModelTreeRepositoryTest {

    private static final String NAMESPACE = TestResources.Info.NAMESPACE.namespace;
    private static final TargetEnvironment TARGET_ENVIRONMENT = TargetEnvironment.JAVA;

    @Test
    public void shouldCacheModelTrees() {
        final ContextRepository contextRepository =
                ContextRepository.from(TestResources.create(TestResources.Info.NAMESPACE).getRuntimeKrakenProject());
        final ContextRepository spy =
                spy(contextRepository);
        final SpyRegistry registry =
                new SpyRegistry(spy);
        final CachingContextModelTreeRepository repository =
                new CachingContextModelTreeRepository(registry);
        repository.clearCache();

        repository.get(NAMESPACE, TARGET_ENVIRONMENT);
        verify(spy, times(2)).getKeys();

        repository.get(NAMESPACE, TARGET_ENVIRONMENT);
        verify(spy, times(2)).getKeys();

        repository.clearCache();
        repository.get(NAMESPACE, TARGET_ENVIRONMENT);
        verify(spy, times(4)).getKeys();
    }

    public static class SpyRegistry implements CachingContextModelTreeRepository.ContextRepositoryRegistry {
        private ContextRepository contextRepository;
        public SpyRegistry(ContextRepository contextRepository) {
            this.contextRepository = contextRepository;
        }
        @Override
        public ContextRepository get(String namespace, TargetEnvironment targetEnvironment) {
            return contextRepository;
        }
    }
}
