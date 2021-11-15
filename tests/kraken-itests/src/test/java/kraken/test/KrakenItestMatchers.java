/*
 *  Copyright 2020 EIS Ltd and/or one of its affiliates.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package kraken.test;

import kraken.runtime.engine.EntryPointResult;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import static kraken.test.Result.assertToMatchSnapshot;

/**
 * Utility class for kraken itest specific matchers.
 *
 * @author psurinin
 * @since 1.0.38
 */
public class KrakenItestMatchers {

    /**
     * Checks {@link EntryPointResult} to match snapshot result.
     * how class must be prepared for snapshot testing please read here:
     * <a href="https://github.com/json-snapshot/json-snapshot.github.io">json-snapshot.github.io</a>
     */
    public static Matcher<EntryPointResult> matchesSnapshot() {
        return SnapshotMatchers.matchesSnapshot();
    }

    private static class SnapshotMatchers<T extends EntryPointResult> extends TypeSafeDiagnosingMatcher<T> {

        static Matcher<EntryPointResult> matchesSnapshot() {
            return new SnapshotMatchers<>();
        }

        @Override
        protected boolean matchesSafely(EntryPointResult item, Description mismatchDescription) {
            assertToMatchSnapshot(item);
            return true;
        }

        @Override
        public void describeTo(Description description) {

        }
    }

}
