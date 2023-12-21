/* eslint-disable @typescript-eslint/no-non-null-assertion */
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

import { ExecutionSession } from '../src/engine/ExecutionSession'
import { ContextModelTree } from '../src/models/ContextModelTree'
import { KRAKEN_MODEL_TREE_POLICY as modelTreeJson } from 'kraken-test-product-model-tree'
import { KRAKEN_MODEL_TREE_POLICYEXTENDED as extendedModelTreeJson } from 'kraken-test-product-model-tree'
import { KRAKEN_FUNCTIONS_POLICY as policyFunctionsJson } from 'kraken-test-product-model-tree'
import { KRAKEN_FUNCTIONS_POLICYEXTENDED as policyExtendedFunctionsJson } from 'kraken-test-product-model-tree'
import { DataObjectInfoResolver } from '../src/engine/contexts/info/DataObjectInfoResolver'
import { ContextInstanceInfoResolver } from '../src/engine/contexts/info/ContextInstanceInfoResolver'
import Identifiable = TestProduct.kraken.testproduct.domain.meta.Identifiable
import { ContextDataExtractorImpl } from '../src/engine/contexts/data/extraction/ContextDataExtractorImpl'
import { DataContextBuilder } from '../src/engine/contexts/data/DataContextBuilder'
import { TestProduct } from 'kraken-test-product'
import { ExtractedChildDataContextBuilder } from '../src/engine/contexts/data/ExtractedChildDataContextBuilder'
import { ExpressionEvaluator, KelFunction } from '../src/engine/runtime/expressions/ExpressionEvaluator'
import { FunctionRegistry } from '../src/engine/runtime/expressions/functionLibrary/Registry'
import { DataContext } from '../src/engine/contexts/data/DataContext'
import { ContextInstanceInfo } from 'kraken-engine-api'
import { DataContextPathProvider, DEFAULT_PATH_PROVIDER } from '../src/engine/runtime/DataContextPathProvider'

const modelTree = Object.freeze(modelTreeJson as unknown as ContextModelTree.ContextModelTree)
const extendedModelTree = Object.freeze(extendedModelTreeJson as unknown as ContextModelTree.ContextModelTree)
const policyFunctions = policyFunctionsJson as unknown as KelFunction[]
const policyExtendedFunctions = policyExtendedFunctionsJson as unknown as KelFunction[]
const evaluationConfig = Object.freeze({ context: {}, currencyCd: 'USD' })
const session = Object.freeze(new ExecutionSession(evaluationConfig, {}, 'ep'))
const toMoney = (amount: number) => ({ amount: amount, currency: 'USD' })
const contextInstanceInfo: ContextInstanceInfo = Object.freeze({
    getContextInstanceId: () => '1',
    getContextName: () => 'mock',
})
const empty = (): TestProduct.kraken.testproduct.domain.Policy => ({
    id: '0',
    cd: 'Policy',
    insured: {
        cd: 'Insured',
        id: 'insured-1-id',
        addressInfo: {
            id: 'iai1',
            cd: 'AddressInfo',
        },
    },
    billingInfo: {
        id: '1',
        cd: 'BillingInfo',
        creditCardInfo: {
            id: '2',
            cd: 'CreditCardInfo',
        },
    },
    parties: [
        {
            id: '3',
            cd: 'Party',
            personInfo: {
                id: '4',
                cd: 'PersonInfo',
            },
            roles: [
                {
                    id: '5',
                    cd: 'PartyRole',
                },
            ],
        },
    ],
    riskItems: [
        {
            id: '6',
            cd: 'Vehicle',
            addressInfo: {
                id: '7',
                cd: 'AddressInfo',
            },
        },
    ],
    transactionDetails: {
        id: '8',
        cd: 'TransactionDetails',
    },
    accessTrackInfo: {
        id: '9',
        cd: 'AccessTrackInfo',
    },
    termDetails: {},
    policyDetail: {
        id: '11',
        cd: 'PolicyDetail',
    },
})
const emptyExtended: () => TestProduct.kraken.testproduct.domain.Policy = () => ({
    id: '0',
    cd: 'PolicyExtended',
    billingInfo: {
        id: '1',
        cd: 'BillingInfoExtended',
        creditCardInfo: {
            id: '2',
            cd: 'CreditCardInfoExtended',
            billingAddress: {
                id: '99',
                cd: 'BillingAddressExtended',
            },
        },
    },
    parties: [
        {
            id: '3',
            cd: 'PartyExtended',
            driverInfo: {
                cd: 'DriverInfoExtended',
                id: '88',
            },
            personInfo: {
                id: '4',
                cd: 'PersonInfoExtended',
            },
            roles: [
                {
                    id: '5',
                    cd: 'PartyRoleExtended',
                },
            ],
        },
    ],
    riskItems: [
        {
            id: '6',
            cd: 'VehicleExtended',
            addressInfo: {
                id: '7',
                cd: 'AddressInfoExtended',
            },
            info: {
                id: '61',
                cd: 'VehicleInfoExtended',
            },
        },
    ],
    transactionDetails: {
        id: '8',
        cd: 'TransactionDetailsExtended',
    },
    accessTrackInfo: {
        id: '9',
        cd: 'AccessTrackInfoExtended',
    },
    termDetails: {},
    policyDetail: {
        id: '11',
        cd: 'PolicyDetailExtended',
    },
})
const dataContextEmpty: () => DataContext = () => {
    const emptyPolicy = empty()
    const { Policy } = modelTreeJson.contexts
    const info: ContextInstanceInfo = {
        getContextInstanceId: () => emptyPolicy.id!,
        getContextName: () => emptyPolicy.cd!,
    }
    return new DataContext(
        emptyPolicy.id!,
        Policy.name,
        '',
        emptyPolicy as Record<string, unknown>,
        info,
        modelTree.contexts[Policy.name],
    )
}
const dataContextEmptyExtended: () => DataContext = () => {
    const emptyPolicy = emptyExtended()
    const { PolicyExtended } = extendedModelTree.contexts
    const info: ContextInstanceInfo = {
        getContextInstanceId: () => emptyPolicy.id!,
        getContextName: () => emptyPolicy.cd!,
    }
    return new DataContext(
        emptyPolicy.id!,
        PolicyExtended.name,
        '',
        emptyPolicy as Record<string, unknown>,
        info,
        extendedModelTree.contexts[PolicyExtended.name],
        undefined,
    )
}

const dataContextCustom = (policy: Partial<TestProduct.kraken.testproduct.domain.Policy>) => {
    const dataContext = dataContextEmpty()
    ;(dataContext as { dataObject: Record<string, unknown> }).dataObject = {
        ...dataContext.dataObject,
        ...policy,
    }
    return dataContext
}

const dataContextExplicit = (policy: Partial<TestProduct.kraken.testproduct.domain.Policy>) => {
    const { Policy } = modelTreeJson.contexts
    return new DataContext(
        policy.id!,
        Policy.name,
        '',
        policy as Record<string, unknown>,
        {
            getContextInstanceId: () => policy.id!,
            getContextName: () => policy.cd!,
        },
        modelTree.contexts[Policy.name],
    )
}

const data = {
    empty,
    emptyExtended,
    dataContextEmpty,
    dataContextEmptyExtended,
    dataContextCustom,
    dataContextExplicit,
}
const dataResolver = {
    validate: (identifiable: Identifiable) => {
        const errors = []
        if (identifiable.cd === undefined) {
            errors.push({ message: "'cd' field is not present in data object" })
        }
        if (identifiable.id === undefined) {
            errors.push({ message: "'id' field is not present in data object" })
        }
        return errors
    },
    resolveId: (identifiable: Identifiable) => identifiable['id'],
    resolveName: (identifiable: Identifiable) => identifiable['cd'],
} as DataObjectInfoResolver

const pathProvider = {
    getPath(dataContextId: string): string | undefined {
        return `path.to.${dataContextId}`
    },
} as DataContextPathProvider
function resolveInfo(root: Identifiable): ContextInstanceInfo {
    return {
        getContextInstanceId: () => root['id']!,
        getContextName: () => root['cd']!,
    }
}
const spi = {
    dataResolver,
    instance: {
        validateContextDataObject: dataResolver.validate,
        processContextInstanceInfo: info => info,
        resolveRootInfo: resolveInfo,
        resolveAncestorInfo: resolveInfo,
        resolveExtractedInfo: resolveInfo,
    } as ContextInstanceInfoResolver<Identifiable>,
}
const contextBuilder = new DataContextBuilder(modelTree, spi.instance, () => DEFAULT_PATH_PROVIDER)
const evaluator = new ExpressionEvaluator(
    FunctionRegistry.createInstanceFunctions(dataResolver, name => modelTree.contexts[name].inheritedContexts),
    [],
)
evaluator.rebuildFunctions(
    FunctionRegistry.INSTANCE.bindRegisteredFunctions({
        zoneId: session.ruleTimeZoneId,
        dateCalculator: session.dateCalculator,
    }),
)
const contextDataExtractor = new ContextDataExtractorImpl(
    modelTree,
    new ExtractedChildDataContextBuilder(contextBuilder, evaluator),
)
const policyEvaluator = new ExpressionEvaluator(
    FunctionRegistry.createInstanceFunctions(dataResolver, name => modelTree.contexts[name].inheritedContexts),
    policyFunctions,
)
policyEvaluator.rebuildFunctions(
    FunctionRegistry.INSTANCE.bindRegisteredFunctions({
        zoneId: session.ruleTimeZoneId,
        dateCalculator: session.dateCalculator,
    }),
)
export const mock = {
    dataContextEmpty,
    evaluator,
    modelTree,
    modelTreeJson,
    extendedModelTree,
    extendedModelTreeJson,
    session,
    evaluationConfig,
    toMoney,
    contextInstanceInfo,
    data,
    spi,
    contextDataExtractor,
    contextBuilder,
    policyFunctions,
    policyExtendedFunctions,
    policyEvaluator,
    pathProvider,
}
