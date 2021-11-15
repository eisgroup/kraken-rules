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
package kraken.model.project;

import java.text.MessageFormat;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kraken.model.project.exception.KrakenProjectValidationException;
import kraken.model.project.validator.KrakenProjectValidationService;
import kraken.model.project.validator.ValidationResult;
import kraken.model.resource.Resource;
import kraken.namespace.Namespaced;

/**
 * Default implementation of {@link KrakenProjectFactory} based on {@link Resource} API
 * that provides validated {@link KrakenProject} implementations as created by {@link KrakenProjectBuilder}.
 *
 * @author mulevicius
 */
public class DefaultKrakenProjectFactory implements KrakenProjectFactory {

    private static final Logger logger = LoggerFactory.getLogger(DefaultKrakenProjectFactory.class);

    private final KrakenProjectBuilder krakenProjectBuilder;

    private final KrakenProjectValidationService krakenProjectValidationService;

    public DefaultKrakenProjectFactory(KrakenProjectBuilder krakenProjectBuilder,
                                       KrakenProjectValidationService krakenProjectValidationService) {
        this.krakenProjectBuilder = krakenProjectBuilder;
        this.krakenProjectValidationService = krakenProjectValidationService;
    }

    @Override
    public KrakenProject createKrakenProject(String namespace) {
        logger.trace("Building KrakenProject for: " + namespace);

        KrakenProject krakenProject = krakenProjectBuilder.buildKrakenProject(namespace);
        ValidationResult validationResult = krakenProjectValidationService.validate(krakenProject);
        validationResult.logMessages(logger);
        if(!validationResult.getErrors().isEmpty()) {
            String namespaceName = namespace.equals(Namespaced.GLOBAL) ? "GLOBAL" : namespace;
            String pattern = "KrakenProject for namespace ''{0}'' is not valid. Validation errors:\n{1}";
            String validationErrors = validationResult.getErrors().stream()
                    .map(error -> error.toString())
                    .collect(Collectors.joining(System.lineSeparator()));
            String message = MessageFormat.format(pattern, namespaceName, validationErrors);
            throw new KrakenProjectValidationException(message);
        }

        return krakenProject;
    }

}
