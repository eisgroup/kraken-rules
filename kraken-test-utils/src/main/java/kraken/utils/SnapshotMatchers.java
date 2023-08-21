/* Copyright © 2023 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.*/
package kraken.utils;

import java.util.Optional;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 * Hamcrest matcher for {@link Snapshot}
 * @author psurinin
 */
public class SnapshotMatchers {

    public static Matcher<String> matches(Snapshot snapshot) {
        return new SnapshotMatcher(snapshot);
    }

    private static class SnapshotMatcher extends TypeSafeDiagnosingMatcher<String> {

        private final Snapshot snapshotToMatch;

        public SnapshotMatcher(Snapshot snapshot) {
            this.snapshotToMatch = snapshot;
        }

        @Override
        protected boolean matchesSafely(String snapshot, Description description) {
            Optional<String> mismatch = snapshotToMatch.findMismatch(snapshot);
            mismatch.ifPresent(description::appendText);
            return mismatch.isEmpty();
        }


        @Override
        public void describeTo(Description description) {
            description.appendText("Snapshot to match string");
        }
    }

}