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
package kraken.utils;

import java.net.URI;
import java.util.UUID;

/**
 * @author mulevicius
 */
public class ResourceUtils {

    /**
     *
     * @return a random rules resource URI. URI will have 'rules' schema and a randomly generated UUID v4 path.
     */
    public static URI randomResourceUri() {
        return URI.create("rules://" + UUID.randomUUID() + ".rules");
    }
}
