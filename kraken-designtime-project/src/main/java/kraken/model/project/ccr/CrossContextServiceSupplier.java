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
package kraken.model.project.ccr;

/**
 * Should be implemented by {@link kraken.model.project.KrakenProject} if it provides {@link CrossContextService}
 * @author mulevicius
 */
public interface CrossContextServiceSupplier {

    /**
     * @return instance of {@link CrossContextService} where subsequent calls to this method should return same instance
     */
    CrossContextService getCrossContextService();
}
