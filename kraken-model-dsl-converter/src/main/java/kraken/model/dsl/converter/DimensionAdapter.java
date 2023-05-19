/*
 * Copyright 2023 EIS Ltd and/or one of its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kraken.model.dsl.converter;

import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.misc.ObjectModelAdaptor;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

import kraken.model.Dimension;

/**
 * @author Tomas Dapkunas
 * @since 1.48.0
 */
class DimensionAdapter extends ObjectModelAdaptor {

    @Override
    public Object getProperty(Interpreter interp, ST self, Object o, Object property, String propertyName)
        throws STNoSuchPropertyException {
        Dimension dimension = ((Dimension) o);

        if ("dataType".equals(propertyName)) {
            return dimension.getDataType().getRenderedRepresentation();
        }

        return super.getProperty(interp, self, o, property, propertyName);
    }
}
