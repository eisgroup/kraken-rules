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
package kraken.runtime.repository.dynamic;

/**
 * @author mulevicius
 */
public class DynamicRuleRepositoryCacheConfig {

    private long cacheMaxSize;

    private long expireAfterWriteInSeconds;

    public DynamicRuleRepositoryCacheConfig(long cacheMaxSize, long expireAfterWriteInSeconds) {
        this.cacheMaxSize = cacheMaxSize;
        this.expireAfterWriteInSeconds = expireAfterWriteInSeconds;
    }

    public long getCacheMaxSize() {
        return cacheMaxSize;
    }

    public long getExpireAfterWriteInSeconds() {
        return expireAfterWriteInSeconds;
    }

    public static DynamicRuleRepositoryCacheConfig noCaching() {
        return new DynamicRuleRepositoryCacheConfig(Long.MAX_VALUE, 0);
    }

    public static DynamicRuleRepositoryCacheConfig eternal() {
        return new DynamicRuleRepositoryCacheConfig(Long.MAX_VALUE, Long.MAX_VALUE);
    }

    public static DynamicRuleRepositoryCacheConfig defaultConfig() {
        // by default holds 10000 rules for 24 hours
        return new DynamicRuleRepositoryCacheConfig(20000, 24*60*60);
    }
}
