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
package kraken.runtime.engine.context.data;

import kraken.runtime.engine.context.extraction.ContextChildExtractionInfo;
import kraken.runtime.engine.context.extraction.instance.ContextExtractionResult;
import kraken.runtime.engine.context.info.ContextInstanceInfo;
import kraken.runtime.engine.context.info.ContextInstanceInfoResolver;
import kraken.runtime.engine.context.info.DataErrorDefinition;
import kraken.runtime.model.context.RuntimeContextDefinition;
import kraken.runtime.repository.RuntimeContextRepository;
import kraken.utils.Assertions;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Creates a {@link DataContext} instance from passed data object.
 *
 * @author rimas
 * @since 1.0
 */
@SuppressWarnings("WeakerAccess")
public class DataContextBuilder {

    private static final String FAILED_INVALID_DATA = "Failed to build data context due to invalid data";
    private static final String FAILED_ROOT = "Failed to build data context from root object";
    private static final String FAILED_INHERITANCE = "Failed to build data context on inheritance step";

    private final RuntimeContextRepository contextRepository;
    private final ContextInstanceInfoResolver instanceInfoResolver;

    public DataContextBuilder(
            RuntimeContextRepository contextRepository,
            ContextInstanceInfoResolver instanceInfoResolver
    ) {
        this.contextRepository = contextRepository;
        this.instanceInfoResolver = instanceInfoResolver;
    }

    /**
     * Produce {@link DataContext} instance from passed context object instance
     */
    public DataContext buildFromRoot(Object rootContextObject) {
        final Object dataObject = requireDataIsValid(rootContextObject, FAILED_ROOT);
        return buildContext(dataObject, instanceInfoResolver.resolveRootInfo(dataObject), null);
    }

    /**
     * Produce ancestor {@link DataContext} instance
     */
    public DataContext buildForAncestorObject(
            Object contextDataObject,
            String ancestorContextName,
            DataContext childContext) {
        ContextInstanceInfo contextInstanceInfo = instanceInfoResolver.resolveAncestorInfo(
                requireDataIsValid(contextDataObject, FAILED_INHERITANCE),
                contextRepository.getContextDefinition(ancestorContextName),
                contextRepository.getContextDefinition(childContext.getContextName()),
                childContext.getContextInstanceInfo());
        return buildContext(contextDataObject, contextInstanceInfo, null);
    }

    public DataContext buildFromExtractedObject(ContextExtractionResult instance, ContextChildExtractionInfo info) {
        final Object contextData = requireDataIsValid(instance.getValue(), FAILED_INVALID_DATA);
        ContextInstanceInfo contextInstanceInfo = instanceInfoResolver.resolveExtractedInfo(
                contextData,
                contextRepository.getContextDefinition(info.getChildContextName()),
                contextRepository.getContextDefinition(info.getParentDataContext().getContextName()),
                info.getParentDataContext().getContextInstanceInfo());
        return buildContext(
                contextData,
                contextInstanceInfo,
                info.getParentDataContext()
        );
    }

    private DataContext buildContext(Object contextDataObject, ContextInstanceInfo info, DataContext parentDataContext) {
        Assertions.assertNotNull(info, "context instance info");
        Assertions.assertNotEmpty(info.getContextName(), "context name");
        Assertions.assertNotEmpty(info.getContextInstanceId(), "instance id");
        final Object data = requireDataIsValid(contextDataObject, info);
        final DataContext dataContext = new DataContext();
        final RuntimeContextDefinition contextDefinition = contextRepository.getContextDefinition(info.getContextName());
        dataContext.setContextName(info.getContextName());
        dataContext.setContextId(info.getContextInstanceId());
        dataContext.setDataObject(data);
        dataContext.setContextDefinition(contextDefinition);
        dataContext.setContextInstanceInfo(info);
        dataContext.setParentDataContext(parentDataContext);

        return dataContext;

    }

    private Object requireDataIsValid(Object contextDataObject, ContextInstanceInfo info) {
        Collection<DataErrorDefinition> errors = instanceInfoResolver.validateContextDataObject(contextDataObject);
        if (!errors.isEmpty()) {
            final String errMessage = String.format(
                    "Failed to build context data due to invalid data for context name: %s, id: %s",
                    info.getContextName(),
                    info.getContextInstanceId());
            var errorCause = errors.stream()
                    .map(DataErrorDefinition::getMessage)
                    .collect(Collectors.joining(System.lineSeparator() + "\t"));
            raiseError(contextDataObject, errMessage + System.lineSeparator() + "\t" + errorCause);
        }
        return contextDataObject;
    }

    private Object requireDataIsValid(Object contextDataObject, String errMessage) {
        Collection<DataErrorDefinition> errors = instanceInfoResolver.validateContextDataObject(contextDataObject);
        if (!errors.isEmpty()) {
            var errorCause = errors.stream()
                    .map(DataErrorDefinition::getMessage)
                    .collect(Collectors.joining(System.lineSeparator() + "\t"));
            raiseError(contextDataObject, errMessage + System.lineSeparator() + "\t" + errorCause);
        }
        return contextDataObject;
    }

    private void raiseError(Object contextDataObject, String errMessage) {
        String type = contextDataObject != null ? contextDataObject.getClass().getName() : "null";
        throw new DataContextBuildingException(errMessage + ". Context data object is invalid: " + type);
    }

}
