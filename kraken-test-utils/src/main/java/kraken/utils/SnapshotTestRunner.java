/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Injects configured instance of {@link Snapshot} before each test method into test class
 *
 * @author mulevicius
 */
public class SnapshotTestRunner extends BlockJUnit4ClassRunner {

    public SnapshotTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected Statement methodInvoker(FrameworkMethod method, Object test) {
        Arrays.stream(test.getClass().getDeclaredFields())
            .filter(it -> it.getType() == Snapshot.class)
            .forEach(field -> {
                boolean updateSnapshots = System.getProperty("updateSnapshots") != null;
                String testClassName = test.getClass().getSimpleName();
                Path snapshotDirectory = Paths.get("src", "test", "resources", "__snapshots__", testClassName);
                String snapshotFileName = method.getName();
                Logger logger = LoggerFactory.getLogger(test.getClass());
                Snapshot snapshot = new Snapshot(updateSnapshots, snapshotDirectory, snapshotFileName, logger);
                field.setAccessible(true);
                try {
                    field.set(test, snapshot);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });

        return super.methodInvoker(method, test);
    }

}
