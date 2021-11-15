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
import { toBe } from "declarative-js";
import { Contexts } from "kraken-model";
import ContextDefinition = Contexts.ContextDefinition;
import { DataContextBuilder } from "./DataContextBuilder";
import { DataContext } from "./DataContext";
import { requireDefinedValue } from "../../../utils/Utils";
import { ExpressionEvaluator } from "../../runtime/expressions/ExpressionEvaluator";
import { expressionFactory } from "../../runtime/expressions/ExpressionFactory";
import { ExpressionEvaluationResult } from "../../runtime/expressions/ExpressionEvaluationResult";
import { ErrorCode, KrakenRuntimeError } from "../../../error/KrakenRuntimeError";

export interface ContextChildExtractionInfo {
    parentContextDefinition: ContextDefinition;
    parentDataContext: DataContext;
    childContextName: string;
}

export class ExtractedChildDataContextBuilder {

    constructor(
        private readonly dataContextBuilder: DataContextBuilder,
        private readonly expressionEvaluator: ExpressionEvaluator
    ) {
        this.resolveImmediateChildren = this.resolveImmediateChildren.bind(this);
        this.extract = this.extract.bind(this);
    }

    resolveImmediateChildren(info: ContextChildExtractionInfo): DataContext[] {
        const { childContextName, parentDataContext } = info;
        const result = this.extract(info);
        const toDataContext = this.createDataContextFactory(childContextName, parentDataContext);
        if (Array.isArray(result)) {
            return result.filter(toBe.present).map(toDataContext);
        }
        return [toDataContext(result)];
    }

    private createDataContextFactory(
        childContextName: string,
        parentDataContext: DataContext
    ): (dataObject: object, index?: number) => DataContext {
        const { dataContextBuilder } = this;
        return function _createDataContextFactory(dataObject: object, index?: number): DataContext {
            return dataContextBuilder.buildFromExtractedObject(
                dataObject, childContextName, parentDataContext, index
            );
        };
    }
    private extract(info: ContextChildExtractionInfo): object | object[] {
        const children = requireDefinedValue(info.parentContextDefinition.children, "Children cannot be null");
        const navigation = children[info.childContextName];
        const expressionResult = this.expressionEvaluator.evaluate(
            expressionFactory.fromNavigation(navigation),
            info.parentDataContext
        );
        if (ExpressionEvaluationResult.isError(expressionResult)) {
            throw new KrakenRuntimeError(
                ErrorCode.EXTRACT_EXPRESSION_FAILED,
                `Extraction of an attribute '${navigation}' failed`
            );
        }
        const extracted = expressionResult.success;

        if (extracted === undefined) {
            return [];
        }
        if (Array.isArray(extracted)) {
            return flat(extracted);
        }
        return extracted;
    }
}

function flat(array: any[]): any[] {
    return array.reduce((pv: any[], cv) => {
        if (Array.isArray(cv)) {
            return pv.concat(flat(cv));
        }
        if (cv) {
            return pv.concat(cv);
        }
        return pv;
    }, []);
}
