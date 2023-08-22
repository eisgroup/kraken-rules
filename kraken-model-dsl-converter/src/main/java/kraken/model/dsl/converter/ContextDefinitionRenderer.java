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
package kraken.model.dsl.converter;

import kraken.model.context.Cardinality;
import kraken.model.context.ContextField;
import kraken.model.context.ContextNavigation;
import kraken.model.context.PrimitiveFieldDataType;
import org.apache.commons.lang3.StringUtils;
import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.misc.ObjectModelAdaptor;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

import java.beans.Introspector;
import java.util.Locale;
import java.util.Objects;

/**
 * Used by {@link DSLModelConverter} to render/adapt {@link ContextNavigation}, {@link ContextField} and {@link Cardinality}
 * when converting {@link kraken.model.resource.Resource} to Dsl <code>string</code>
 *
 * @author avasiliauskas
 */
class ContextDefinitionRenderer {

    static String cardinalityRenderer(Object object, String s, Locale locale) {
        Cardinality cardinality = (Cardinality) object;
        switch (cardinality) {
            case SINGLE: return StringUtils.EMPTY;
            case MULTIPLE: return "*";
        }
        return null;
    }

    private static String resolveNavigationExpression(ContextNavigation navigation){
        if(Objects.equals(navigation.getNavigationExpression(), Introspector.decapitalize(navigation.getTargetName()))) {
            return null;
        }
        return navigation.getNavigationExpression();
    }

    private static String resolveFieldPath(ContextField field){
        if(Objects.equals(field.getFieldPath(), Introspector.decapitalize(field.getName()))) {
            return null;
        }
        return field.getFieldPath();
    }

    private static String resolveFieldType(ContextField field){
        String fieldType = field.getFieldType();
        if(PrimitiveFieldDataType.isPrimitiveType(fieldType)){
            return fieldType.substring(0, 1).toUpperCase().concat(fieldType.substring(1).toLowerCase());
        }
        return fieldType;
    }

    static class ContextNavigationAdapter extends ObjectModelAdaptor {

        @Override
        public Object getProperty(Interpreter interp, ST self, Object o, Object property, String propertyName)
                throws STNoSuchPropertyException {
            if("navigationExpression".equals(propertyName)){
                return resolveNavigationExpression(((ContextNavigation)o));
            }
            return super.getProperty(interp, self, o, property, propertyName);
        }
    }

    static class ContextFieldAdapter extends ObjectModelAdaptor {

        @Override
        public Object getProperty(Interpreter interp, ST self, Object o, Object property, String propertyName)
                throws STNoSuchPropertyException {
            if("fieldPath".equals(propertyName)){
                return resolveFieldPath(((ContextField)o));
            }
            if("fieldType".equals(propertyName)){
                return resolveFieldType((ContextField)o);
            }
            return super.getProperty(interp, self, o, property, propertyName);
        }
    }
}
