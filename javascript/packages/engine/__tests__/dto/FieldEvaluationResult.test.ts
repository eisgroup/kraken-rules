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

import { FieldEvaluationResultImpl, FieldEvaluationResultUtils } from "../../src/dto/FieldEvaluationResult";
import { mock } from "../mock";
import { ContextFieldInfo } from "../../src/dto/ContextFieldInfo";

describe("FieldEvaluationResult", () => {
    it("should return id", () => {
        const dataContext = mock.dataContextEmpty();
        const fieldEvaluationResult = new FieldEvaluationResultImpl(
            new ContextFieldInfo(dataContext, "state"),
            []
        );
        expect(FieldEvaluationResultUtils.targetId(fieldEvaluationResult)).toBe("Policy:0:state");
    });
});
