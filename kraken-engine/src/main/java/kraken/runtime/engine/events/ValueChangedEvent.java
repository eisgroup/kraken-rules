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
package kraken.runtime.engine.events;

import kraken.annotations.API;
import kraken.runtime.engine.context.data.DataContext;

/**
 * Event fired by derive loop when value of attribute is changed by rule engine
 *
 * @author rimas
 * @since 1.0
 */
@API
public class ValueChangedEvent implements RuleEvent {

    /**
     * Target path to changed field attribute
     */
    private String attributeTarget;

    /**
     * Context definition name for context on which event was emitted
     */
    private String contextName;

    /**
     * Context instance identification string for context on which event was emitted
     */
    private String contextId;

    /**
     * New field value
     */
    private Object newValue;

    /**
     * Old field value
     */
    private Object previousValue;

    public ValueChangedEvent(DataContext context, String path, Object oldValue, Object newValue) {
        this.contextName = context.getContextName();
        this.contextId = context.getContextId();
        this.attributeTarget = path;
        this.previousValue = oldValue;
        this.newValue = newValue;
    }

    public String getAttributeTarget() {
        return attributeTarget;
    }

    public String getContextName() {
        return contextName;
    }

    public Object getNewValue() {
        return newValue;
    }

    public Object getPreviousValue() {
        return previousValue;
    }

    public String getContextId() {
        return contextId;
    }

}
