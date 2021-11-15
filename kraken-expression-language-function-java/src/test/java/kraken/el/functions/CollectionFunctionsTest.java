package kraken.el.functions;

import org.junit.Test;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

public class CollectionFunctionsTest {

    @Test
    public void shouldJoinTwoArrays() {
        assertThat(CollectionFunctions.join(null, null), empty());
        assertThat(CollectionFunctions.join(asList(), asList()), empty());
        assertThat(CollectionFunctions.join(null, asList("b")), contains("b"));
        assertThat(CollectionFunctions.join(asList("a"), asList("b")), contains("a", "b"));
        assertThat(CollectionFunctions.join(asList("a"), asList("b", "b", null, null, "a")), contains("a", "b", "b", null, null, "a"));
    }

}
