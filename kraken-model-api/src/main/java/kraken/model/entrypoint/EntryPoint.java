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
package kraken.model.entrypoint;

import java.util.List;

import kraken.annotations.API;
import kraken.model.KrakenModelItem;
import kraken.model.MetadataAware;

/**
 * Named rule engine invocation point. Contains list of rule names to executed
 * as part of this invocation.
 *
 * @author rimas
 * @since 1.0
 */
@API
public interface EntryPoint extends KrakenModelItem, MetadataAware {

    /**
     * EntryPoint variation is a specific definition of EntryPoint validation logic which is applicable for a
     * particular set of dimensions.
     * <p/>
     * Kraken Engine allows to have multiple EntryPoint definitions with the same {@link #getName()} value
     * when each such EntryPoint is applicable for a different set of dimension values.
     * Each such EntryPoint that has the same name but is applicable for different dimensions
     * is a variation of EntryPoint validation logic.
     * {@link #getEntryPointVariationId()} then uniquely identifies a EntryPoint validation logic variation between all
     * EntryPoint definitions of the same name.
     * <p/>
     * It is not required to be unique between EntryPoint definitions with different names.
     * <p/>
     * In case when EntryPoint does not vary by dimensions then {@link #getEntryPointVariationId()} can be equal to
     * {@link #getName()} or can be null.
     *
     * @return uniquely identifies EntryPoint variation
     */
    String getEntryPointVariationId();

    /**
     * @see #getEntryPointVariationId()
     * @param entryPointVariationId which uniquely identifies EntryPoint variation.
     *                        It must be unique in scope of all EntryPoint variations that has same {@link #getName()}.
     */
    void setEntryPointVariationId(String entryPointVariationId);

    void setRuleNames(List<String> ruleNames);

    /**
     * @since 1.0.30
     */
    void setIncludedEntryPointNames(List<String> includeEntryPointNames);

    boolean isServerSideOnly();

    void setServerSideOnly(boolean serverSideOnly);

    /**
     * List of names for rules, included in this EntryPoint.
     * Must return not null.
     */
    List<String> getRuleNames();

    /**
     * List of entry point names, included in this EntryPoint.
     * Must return not null.
     * @since 1.0.30
     */
    List<String> getIncludedEntryPointNames();

}
