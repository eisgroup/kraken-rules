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

import org.stringtemplate.v4.STErrorListener;
import org.stringtemplate.v4.misc.STMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Default error listener used to listen for errors when converting models to Dsl <code>string</code>.
 *
 * @author avasiliauskas
 * @since 1.0.28
 */
class DefaultErrorListener implements STErrorListener {

    private final List<String> errors = new ArrayList<>();

    @Override
    public void compileTimeError(STMessage msg) {
        handleError(msg);
    }

    @Override
    public void runTimeError(STMessage msg) {
        handleError(msg);
    }

    @Override
    public void IOError(STMessage msg) {
        handleError(msg);
    }

    @Override
    public void internalError(STMessage msg) {
        handleError(msg);
    }

    private void handleError(STMessage stMessage) {
        errors.add(stMessage.toString());
    }

    public List<String> getErrors() {
        return errors;
    }
}