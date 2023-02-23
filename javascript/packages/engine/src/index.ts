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
import { krakenConfig } from './config'
krakenConfig.init()

// spi
export * from './engine/contexts/info/DataObjectInfoResolver'
export * from './engine/contexts/info/ContextInstanceInfoResolver'

// engine
export * from './engine/executer/SyncEngine'

export * from './bundle-cache/EntryPointBundleCache'
export * from './bundle-cache/delta-cache/RepoClientCache'
export * from './bundle-cache/delta-cache/DeltaBundleCache'
export * from './bundle-cache/dimension-set-cache/DimensionSetBundleCache'
export * from './bundle-cache/dimension-set-cache/DimensionSetCacheLogger'
export * from './bundle-cache/expression-context-manager/ExpressionContextManager'
export * from './bundle-cache/expression-context-manager/ExpressionContextManagerImpl'

export { registry, KelFunction, FunctionParameter } from './engine/runtime/expressions/ExpressionEvaluator'
export { FunctionScope } from './engine/runtime/expressions/functionLibrary/Registry'
export * from './engine/runtime/EvaluationMode'

// Payload result utils
export * from './engine/results/PayloadResultCreator'
export * from './engine/results/PayloadResultTypeChecker'
export { ValueChangedEvent } from './engine/results/ValueChangedEvent'

// Condition result utils
export { conditionEvaluationTypeChecker } from './dto/DefaultConditionEvaluationResult'

export { DataContext } from './engine/contexts/data/DataContext'

// field metadata reducer
export * from './engine/results/field_metadata_reducer/FieldErrorMessage'

export * from './models/EntryPointBundle'
export * from './models/ContextModelTree'

// reducer
export * from './engine/results/RuleOverrideContextExtractor'
export * from './engine/results/Reducer'
export * from './engine/results/Localization'

// field metadata reducer
export * from './engine/results/field_metadata_reducer/FieldMetadataReducer'

// error
export * from './error/KrakenRuntimeError'

// re-export
export {
    ContextInstanceInfo,
    RuleOverride,
    RuleInfo,
    ContextFieldInfo,
    EntryPointResult,
    ConditionEvaluationResult,
    FieldEvaluationResult,
    RuleEvaluationResults,
    PayloadResultType,
    PayloadResult,
    ErrorAwarePayloadResult,
    ErrorMessage,
    ValidationPayloadResult,
    AccessibilityPayloadResult,
    VisibilityPayloadResult,
    DefaultValuePayloadResult,
    AssertionPayloadResult,
    SizePayloadResult,
    SizeRangePayloadResult,
    LengthPayloadResult,
    RegExpPayloadResult,
    UsagePayloadResult,
    ValueListPayloadResult,
    RuleEvent,
    FieldMetadata,
    FieldMetadataResult,
    ConditionEvaluation,
} from 'kraken-engine-api'
