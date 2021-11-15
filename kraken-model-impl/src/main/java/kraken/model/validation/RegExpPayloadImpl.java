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
package kraken.model.validation;


public class RegExpPayloadImpl extends ValidationPayloadImpl implements RegExpPayload {

    /**
     * Regular expression to be used for field value validation
     */
    private String regExp;

    @Override
    public void setRegExp(String regExp) {
        this.regExp = regExp;
    }

    @Override
    public String getRegExp() {
        return regExp;
    }

}
