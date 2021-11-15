/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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
package kraken.model.dsl.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * Represents entry point scope in Kraken DSL which may contain list of entry points
 *
 * @author mulevicius
 */
public class DSLEntryPoints {

    /**
     * Common metadata for all entry points; if entry point has metadata specified then it will take precedence over common metadata
     */
    private DSLMetadata metadata;

    private Collection<DSLEntryPoint> entryPoints;

    private Collection<DSLEntryPoints> entryPointBlocks;

    private boolean serverSideOnly;

    public DSLEntryPoints(Collection<DSLEntryPoint> entryPoints, DSLMetadata metadata,
                          Collection<DSLEntryPoints> entryPointBlocks, boolean serverSideOnly) {
        this.metadata = metadata;
        this.entryPoints = Objects.requireNonNull(entryPoints);
        this.entryPointBlocks = Objects.requireNonNull(entryPointBlocks);
        this.serverSideOnly = serverSideOnly;
    }

    public Collection<DSLEntryPoint> getEntryPoints() {
        return Collections.unmodifiableCollection(entryPoints);
    }

    public DSLMetadata getMetadata() {
        return metadata;
    }

    public Collection<DSLEntryPoints> getEntryPointBlocks() {
        return Collections.unmodifiableCollection(entryPointBlocks);
    }

    public boolean isServerSideOnly() {
        return serverSideOnly;
    }
}
