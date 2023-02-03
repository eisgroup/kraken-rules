/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
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
package kraken.utils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import junit.framework.AssertionFailedError;

/**
 * Represents tested snapshot and allows comparing snapshot with actual data
 *
 * @author mulevicius
 */
public class Snapshot {

    private final Logger logger;

    private final boolean updateSnapshots;

    private final Path snapshotDirectory;

    private final Path snapshotFile;

    public Snapshot(boolean updateSnapshots, Path snapshotDirectory, String snapshotFileName, Logger logger) {
        this.updateSnapshots = updateSnapshots;
        this.snapshotDirectory = snapshotDirectory;
        this.snapshotFile = snapshotDirectory.resolve(snapshotFileName + ".snap");
        this.logger = logger;
    }

    public void toMatch(String snapshot) {
        if(updateSnapshots) {
            deleteSnapshot();
        }

        if(!Files.exists(snapshotFile)) {
            writeSnapshot(snapshot);
        } else {
            logger.info("Matching snapshot {}", snapshotFile);

            List<String> snapshotLines = snapshot.lines().collect(Collectors.toList());
            List<String> expectedSnapshotLines = readSnapshot().lines().collect(Collectors.toList());

            if(!snapshotLines.equals(expectedSnapshotLines)) {
                reportError(snapshotLines, expectedSnapshotLines);
            }
        }
    }

    private void reportError(List<String> actualSnapshotLines, List<String> expectedSnapshotLines) {
        String linePrefixFormat = "  %4d  ";
        String mismatchedLinePrefixFormat = "x %4d  ";

        StringBuilder message = new StringBuilder();
        message.append("Error: data does not match expected snapshot: ")
            .append(snapshotFile)
            .append(System.lineSeparator());

        message.append("To update snapshots rerun with -DupdateSnapshots option")
            .append(System.lineSeparator())
            .append(System.lineSeparator());

        message.append("Expected:").append(System.lineSeparator()).append(System.lineSeparator());
        for(int i = 0; i < expectedSnapshotLines.size(); i++) {
            String line = expectedSnapshotLines.get(i);
            int lineNr = i + 1;
            String linePrefix = String.format(linePrefixFormat, lineNr);
            message.append(linePrefix).append(line).append(System.lineSeparator());
        }

        message.append(System.lineSeparator());

        message.append("Actual:").append(System.lineSeparator()).append(System.lineSeparator());
        for(int i = 0; i < actualSnapshotLines.size(); i++) {
            String line = actualSnapshotLines.get(i);
            boolean linesDoNotMatch = i >= expectedSnapshotLines.size()
                || !expectedSnapshotLines.get(i).equals(line);
            int lineNr = i + 1;
            String linePrefix = linesDoNotMatch
                ? String.format(mismatchedLinePrefixFormat, lineNr)
                : String.format(linePrefixFormat, lineNr);
            message.append(linePrefix).append(line).append(System.lineSeparator());
        }

        // adding last line if actual snapshot is shorter than expected snapshot
        if(actualSnapshotLines.size() < expectedSnapshotLines.size()) {
            int lineNr = actualSnapshotLines.size();
            String linePrefix = String.format(mismatchedLinePrefixFormat, lineNr);
            message.append(linePrefix).append(System.lineSeparator());
        }

        throw new AssertionFailedError(message.toString());
    }

    private void deleteSnapshot() {
        try {
            if(Files.exists(snapshotFile)) {
                logger.info("Deleting snapshot {}", snapshotFile);
                Files.delete(snapshotFile);
            }
        } catch (IOException e) {
            String template = "Failed to delete snapshot %s";
            String message = String.format(template, snapshotFile);
            throw new UncheckedIOException(message, e);
        }
    }

    private void writeSnapshot(String snapshot) {
        try {
            if (!Files.exists(snapshotFile.getParent())) {
                Files.createDirectories(snapshotFile.getParent());
            }
        } catch (IOException e) {
            String template = "Failed to create directories when writing snapshot to %s";
            String message = String.format(template, snapshotFile);
            throw new UncheckedIOException(message, e);
        }

        try {
            logger.info("Writing snapshot {}", snapshotFile);
            Files.writeString(snapshotFile, snapshot);
        } catch (IOException e) {
            String template = "Failed to write snapshot to %s";
            String message = String.format(template, snapshotFile);
            throw new UncheckedIOException(message, e);
        }
    }

    private String readSnapshot() {
        try {
            return Files.readString(snapshotFile);
        } catch (IOException e) {
            String template = "Failed to read snapshot from %s";
            String message = String.format(template, snapshotFile);
            throw new UncheckedIOException(message, e);
        }
    }
}
