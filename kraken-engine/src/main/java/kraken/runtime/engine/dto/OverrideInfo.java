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
package kraken.runtime.engine.dto;

/**
 * @author mulevicius
 */
public class OverrideInfo {

    private boolean isOverridable;

    private String overrideGroup;

    private OverridableRuleContextInfo overridableRuleContextInfo;

    public OverrideInfo(boolean isOverridable, String overrideGroup, OverridableRuleContextInfo overridableRuleContextInfo) {
        this.isOverridable = isOverridable;
        this.overrideGroup = overrideGroup;
        this.overridableRuleContextInfo = overridableRuleContextInfo;
    }

    /**
     *
     * @return indicates if the rule evaluation result is overridable
     */
    public boolean isOverridable() {
        return isOverridable;
    }

    /**
     *
     * @return indicates override group; this can be used to categorise evaluation results
     */
    public String getOverrideGroup() {
        return overrideGroup;
    }

    /**
     *
     * @return  additional info to be used with {@link kraken.runtime.engine.result.reducers.validation.RuleOverrideStatusResolver};
     *          this is only included when validation rule is overridable and there is an actual validation result that may be overridden.
     *          otherwise this will ne null.
     */
    public OverridableRuleContextInfo getOverridableRuleContextInfo() {
        return overridableRuleContextInfo;
    }
}
