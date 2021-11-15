/*
 *  Copyright 2017 EIS Ltd and/or one of its affiliates.
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
package kraken.model.project.scope;

import kraken.model.project.KrakenProject;

/**
 * @author mulevicius
 */
public class ScopeBuilderProvider {

    /**
     *
     * @param krakenProject
     * @return instance of ScopeBuilder for {@link KrakenProject}.
     *          Will try to resolve ScopeBuilder from KrakenProject if it implements {@link ScopeBuilderSupplier}.
     *          Otherwise, a new instance will be created.
     */
    public static ScopeBuilder forProject(KrakenProject krakenProject) {
        if(krakenProject instanceof ScopeBuilderSupplier) {
            return ((ScopeBuilderSupplier) krakenProject).getScopeBuilder();
        }
        return new ScopeBuilder(krakenProject);
    }

}
