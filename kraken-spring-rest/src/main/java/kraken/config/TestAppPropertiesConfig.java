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
package kraken.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource(value = "resources/application.properties", ignoreResourceNotFound = true)
public class TestAppPropertiesConfig {

    @Autowired
    private Environment environment;

    public String getBuildDate(){
        return environment.getProperty("build.date");
    }
    public String getBranch(){
        return environment.getProperty("build.branch");
    }
    public String getBuildNumber(){
        return environment.getProperty("build.number");
    }
    public String getBuildStartTime(){
        return environment.getProperty("build.startTime");
    }
    public String getBuildRevision(){
        return environment.getProperty("build.revision");
    }
    public String getProjectVersion(){
        return environment.getProperty("project.version");
    }
}
