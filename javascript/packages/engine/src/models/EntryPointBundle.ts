/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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

import { Rule } from "kraken-model";

export namespace EntryPointBundle {

    /**
     * Represents rule data for specific entry point and preprocessed statically
     * to be used for evaluation on specific data context. Evaluation contains rules in exact
     * order to be evaluated. First must be default value rules topologically sorted,
     * next all other rules.
     */
    export interface EntryPointEvaluation {
        rules: Rule[];
        entryPointName: string;
        /**
         * Order of the rules to be evaluated.
         * Key is rule name, value is its order.
         * The less number, the earlier it needs to be evaluated.
         *
         * @see {@link Rule}
         * @since 1.0.41
         */
        rulesOrder: Record<string, number>;
        /**
         * If delta is *true*, then only dimensional rules are
         * present in the bundle. If *false*, all rules are in the bundle
         */
        delta: boolean;
    }

    // tslint:disable-next-line: no-shadowed-variable
    export interface EntryPointBundle {
        engineVersion?: string;
        evaluation: EntryPointEvaluation;
        expressionContext: Record<string, unknown>;
    }
}
