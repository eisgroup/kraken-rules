/*
 *  Copyright © 2019 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 *  CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.
 *
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
