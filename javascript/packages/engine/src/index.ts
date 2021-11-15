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
import { krakenConfig } from "./config";
krakenConfig.init();

// spi
export * from "./engine/contexts/info/DataObjectInfoResolver";
export * from "./engine/contexts/info/ContextInstanceInfoResolver";
export * from "./engine/contexts/info/ContextInstanceInfo";

// engine
export * from "./engine/executer/SyncEngine";
export * from "./repository/RepoClientCache";
export { registry } from "./engine/runtime/expressions/ExpressionEvaluator";
export { FunctionScope } from "./engine/runtime/expressions/functionLibrary/Registry";

// entry point results
export * from "./dto/EntryPointResult";
export * from "./dto/RuleEvaluationResults";
export { FieldEvaluationResult } from "./dto/FieldEvaluationResult";
export * from "./dto/ConditionEvaluationResult";
export { DataContext } from "./engine/contexts/data/DataContext";
export { DataContextTypes } from "./engine/contexts/data/DataContext.types";
export * from "./engine/results/Events";
export * from "./engine/results/PayloadResult";

export * from "./models/EntryPointBundle";
export * from "./models/ContextModelTree";

// reducer
export * from "./engine/results/Reducer";
export * from "./engine/results/Localization";
export * from "./engine/results/RuleOverride";
export * from "./engine/results/RuleInfo";
export * from "./engine/results/Events";
export * from "./engine/results/RuleOverrideContextExtractor";
export * from "./engine/results/PayloadResult";

// field metadata reducer
export * from "./engine/results/field_metadata_reducer/FieldMetadataReducer";
export * from "./engine/results/field_metadata_reducer/FieldMetadata";
export * from "./engine/results/field_metadata_reducer/FieldErrorMessage";
