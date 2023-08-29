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

import static kraken.message.SystemMessageBuilder.Message.CONTEXT_BUILD_INVALID_DATA;
import static kraken.utils.MessageUtils.withSpaceBeforeEachLine;

import java.util.stream.Collectors;

import kraken.message.SystemMessageBuilder;
import kraken.runtime.DataContextPathProvider;
import kraken.runtime.engine.context.extraction.ContextChildExtractionInfo;
import kraken.runtime.engine.context.extraction.instance.ContextExtractionResult;
import kraken.runtime.engine.context.info.ContextInstanceInfo;
import kraken.runtime.engine.context.info.ContextInstanceInfoResolver;
import kraken.runtime.engine.context.info.DataErrorDefinition;
import kraken.runtime.repository.RuntimeContextRepository;

/**
 * Creates a {@link DataContext} instance from passed data object.
 *
 * @author rimas
 * @since 1.0
 */
@SuppressWarnings("WeakerAccess")
public class DataContextBuilder {

    private final RuntimeContextRepository contextRepository;
    private final ContextInstanceInfoResolver<?> instanceInfoResolver;
    private final DataContextPathProvider dataContextPathProvider;

    public DataContextBuilder(
            RuntimeContextRepository contextRepository,
            ContextInstanceInfoResolver<?> instanceInfoResolver,
            DataContextPathProvider dataContextPathProvider
    ) {
        this.contextRepository = contextRepository;
        this.instanceInfoResolver = instanceInfoResolver;
        this.dataContextPathProvider = dataContextPathProvider;
    }

    /**
     * Produce {@link DataContext} instance from passed context object instance
     */
    public DataContext buildFromRoot(Object rootContextObject) {
        throwIfContextDataObjectNotValid(rootContextObject);
        return buildContext(rootContextObject, instanceInfoResolver.resolveRootInfo(rootContextObject), null);
    }

    /**
     * Produce ancestor {@link DataContext} instance
     */
    public DataContext buildForAncestorObject(Object contextDataObject,
                                              String ancestorContextName,
                                              DataContext childContext) {
        throwIfContextDataObjectNotValid(contextDataObject);
        ContextInstanceInfo contextInstanceInfo = instanceInfoResolver.resolveAncestorInfo(
            contextDataObject,
            contextRepository.getContextDefinition(ancestorContextName),
            contextRepository.getContextDefinition(childContext.getContextName()),
            childContext.getContextInstanceInfo()
        );
        return buildContext(contextDataObject, contextInstanceInfo, null);
    }

    public DataContext buildFromExtractedObject(ContextExtractionResult instance, ContextChildExtractionInfo info) {
        throwIfContextDataObjectNotValid(instance.getValue());
        ContextInstanceInfo contextInstanceInfo = instanceInfoResolver.resolveExtractedInfo(
            instance.getValue(),
            contextRepository.getContextDefinition(info.getChildContextName()),
            contextRepository.getContextDefinition(info.getParentDataContext().getContextName()),
            info.getParentDataContext().getContextInstanceInfo()
        );
        return buildContext(
            instance.getValue(),
            contextInstanceInfo,
            info.getParentDataContext()
        );
    }

    private DataContext buildContext(Object contextDataObject, ContextInstanceInfo info, DataContext parentDataContext) {
        var dataContext = new DataContext();
        var contextDefinition = contextRepository.getContextDefinition(info.getContextName());

        dataContext.setContextName(info.getContextName());
        dataContext.setContextId(info.getContextInstanceId());
        dataContext.setContextPath(dataContextPathProvider.getPath(info.getContextInstanceId()));
        dataContext.setDataObject(contextDataObject);
        dataContext.setContextDefinition(contextDefinition);
        dataContext.setContextInstanceInfo(info);
        dataContext.setParentDataContext(parentDataContext);

        return dataContext;
    }

    private void throwIfContextDataObjectNotValid(Object contextDataObject) {
        var errors = instanceInfoResolver.validateContextDataObject(contextDataObject);
        if (!errors.isEmpty()) {
            var joinedReasons = errors.stream()
                .map(DataErrorDefinition::getMessage)
                .collect(Collectors.joining(System.lineSeparator()));

            var className = contextDataObject != null ? contextDataObject.getClass().getName() : "null";

            var m = SystemMessageBuilder.create(CONTEXT_BUILD_INVALID_DATA)
                .parameters(className, System.lineSeparator() + withSpaceBeforeEachLine(joinedReasons))
                .build();
            throw new DataContextBuildingException(m);
        }
    }

}
