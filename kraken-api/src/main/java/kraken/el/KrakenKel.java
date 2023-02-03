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
package kraken.el;

/**
 * @author mulevicius
 */
public class KrakenKel {

    /**
     * Constant for kraken engine expression targets.
     * Only implementations of {@link kraken.el.functionregistry.FunctionLibrary}
     * that does not have {@link kraken.el.functionregistry.ExpressionTarget} or implementations that have annotation
     * and value matches kraken expression target value are allowed to be used in Kraken Rules
     */
    public static final String EXPRESSION_TARGET = "kraken.rule";

    public static final ExpressionLanguageConfiguration CONFIGURATION = ExpressionLanguageConfiguration.builder()
            .strictTypeMode()
            .build();

    public static ExpressionLanguage create(TargetEnvironment targetEnvironment) {
        return ExpressionLanguageFactoryHolder.getExpressionLanguageFactory(targetEnvironment)
                .createExpressionLanguage(KrakenKel.CONFIGURATION);
    }
}
