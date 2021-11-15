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
package kraken.runtime.engine.context.info.navpath;

import kraken.runtime.model.context.RuntimeContextDefinition;
import kraken.runtime.engine.context.info.*;

import java.util.Collection;

/**
 * Implementation of {@link ContextInstanceInfoResolver} SPI,
 * to store information about each context instance, as well as all navigation steps executed during context
 * extraction to obtain this data object instance.
 * Navigation steps can be replayed by invoking client application on root object to obtain same instance to
 * apply rule results
 *
 * @author rimas
 * @since 1.0
 */
@SuppressWarnings("unchecked")
public class DataNavigationContextInstanceInfoResolver implements ContextInstanceInfoResolver<DataNavigationPath> {

    // temporary
    private DataObjectInfoResolver infoResolver = new SimpleDataObjectInfoResolver();

    @Override
    public ContextInstanceInfo resolveRootInfo(Object dataObject) {
        return new DataNavigationContextInstanceInfo(
                infoResolver.resolveContextNameForObject(dataObject),
                infoResolver.resolveContextIdForObject(dataObject),
                "this"
        );
    }

    @Override
    public ContextInstanceInfo resolveExtractedInfo(
            Object dataObject,
            RuntimeContextDefinition target,
            RuntimeContextDefinition source,
            ContextInstanceInfo parentInfo) {
        final DataNavigationContextInstanceInfo instanceInfo = new DataNavigationContextInstanceInfo(
                infoResolver.resolveContextNameForObject(dataObject),
                infoResolver.resolveContextIdForObject(dataObject),
                source.getChildren().get(target.getName()).getNavigationExpression().getExpressionString()
        );
        return DataNavigationContextInstanceInfo.fromParent(parentInfo).append(instanceInfo);
    }

    @Override
    public ContextInstanceInfo resolveAncestorInfo(
            Object dataObject,
            RuntimeContextDefinition ancestor,
            RuntimeContextDefinition child,
            ContextInstanceInfo childInfo) {
        return DataNavigationContextInstanceInfo.fromParent(childInfo);
    }

    @Override
    public DataNavigationPath processContextInstanceInfo(
            ContextInstanceInfo contextInstanceInfo,
            Object dataObject) {
        return ((DataNavigationContextInstanceInfo) contextInstanceInfo).getNavigation();
    }

    @Override
    public Collection<DataErrorDefinition> validateContextDataObject(Object data) {
        return infoResolver.validateContextDataObject(data);
    }

    @Override
    public String resolveContextNameForObject(Object data) {
        return infoResolver.resolveContextNameForObject(data);
    }

    public void setInfoResolver(DataObjectInfoResolver infoResolver) {
        this.infoResolver = infoResolver;
    }


}
