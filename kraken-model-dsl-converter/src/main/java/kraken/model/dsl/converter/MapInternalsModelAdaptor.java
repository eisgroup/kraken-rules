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

import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ModelAdaptor;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

import java.util.Map;

/**
 * This adaptor acts almost the same as {@link ModelAdaptor}.
 * Tth difference is, that {@link Map} in template have different properties.
 * Instead of "keys" -> "__keys__"
 * Instead of "values" -> "__values__".
 * The purpose of switching property names is to support keys ("keys", "values") in {@link Map}
 *
 * @see org.stringtemplate.v4.misc.MapModelAdaptor
 * @author psurinin@eisgroup.com
 * @since 1.3.0
 */
public class MapInternalsModelAdaptor implements ModelAdaptor {
	public Object getProperty(Interpreter interp, ST self, Object o, Object property, String propertyName)
		throws STNoSuchPropertyException
	{
		Object value;
		Map map = (Map)o;
		if ( property==null ) value = map.get(STGroup.DEFAULT_KEY);
		else if ( property.equals("__keys__") ) value = map.keySet();
		else if ( property.equals("__values__") ) value = map.values();
		else if ( map.containsKey(property) ) value = map.get(property);
		else if ( map.containsKey(propertyName) ) { // if can't find the key, try toString version
			value = map.get(propertyName);
		}
		else value = map.get(STGroup.DEFAULT_KEY); // not found, use default
		if ( value == STGroup.DICT_KEY ) {
			value = property;
		}
		return value;
	}
}