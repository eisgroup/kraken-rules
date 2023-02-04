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

package kraken.el.functionregistry.documentation;

public class ExampleDoc {
    private final String call;
    private final String result;
    private final boolean validCall;

    public ExampleDoc(String call, String result, boolean validCall) {
        this.call = call;
        this.result = result;
        this.validCall = validCall;
    }

    public boolean isValidCall() {
        return validCall;
    }

    public String getResult() {
        return result;
    }

    public String getCall() {
        return call;
    }
}
