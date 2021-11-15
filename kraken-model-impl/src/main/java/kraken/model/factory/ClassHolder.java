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
package kraken.model.factory;

/**
 * Class to hold interface and it implementation.
 * psurinin@eisgroup.com
 */
public class ClassHolder<T> {
    /**
     * Interface name
     */
    private final Class<T> interfaceName;

    /**
     * Class that implements that interface
     */
    private final Class<? extends  T> implementationName;

    public ClassHolder(Class<T> interfaceName, Class<? extends T> implementationName){
        this.implementationName = implementationName;
        this.interfaceName = interfaceName;
    }

    public Class<T> getInterfaceName() {
        return interfaceName;
    }

    public Class<? extends T> getImplementationName() {
        return implementationName;
    }
}
