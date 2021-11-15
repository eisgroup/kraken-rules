package kraken.utils;

import kraken.utils.cache.Memoizer;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;
import java.util.function.Function;

import static org.mockito.Mockito.*;

/**
 * @author psurinin
 */
public class MemoizerTest {

    private static class Lengther {
        int length(String s) {
            return s.length();
        }
    }

    private Lengther spy;

    @Before
    public void setUp() {
        final Lengther lengther = new Lengther();
        spy = spy(lengther);
    }
    @Test
    public void shouldCacheFunction() {
        final Function<String, Integer> memoize = Memoizer.memoize(spy::length);
        memoize.apply("demo");
        memoize.apply("demo");
        memoize.apply("demo");
        verify(spy, times(1)).length(anyString());
    }

}
