/*
 *  Copyright 2019 EIS Ltd and/or one of its affiliates.
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

package kraken.el.functionregistry.documentation;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LibraryDoc {

    private final String name;
    private final String description;
    private final String since;
    private final List<FunctionDoc> functions;

    public LibraryDoc(String name, String description, String since, List<FunctionDoc> functions) {
        this.name = name;
        this.description = description;
        this.since = since;
        this.functions = functions.stream()
            .sorted(Comparator.<FunctionDoc, String>comparing(f -> f.getFunctionHeader().getName())
                .thenComparingInt(f -> f.getFunctionHeader().getParameterCount()))
            .collect(Collectors.toList());
    }

    public String getName() {
        return name;
    }

    public List<FunctionDoc> getFunctions() {
        return functions;
    }

    public String getDescription() {
        return description;
    }

    public String getSince() {
        return since;
    }

    @Override
    public String toString() {
        return name;
    }
}
