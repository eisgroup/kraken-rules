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

import { ExecutionSession } from "../src/engine/ExecutionSession";
import { ContextModelTree } from "../src/models/ContextModelTree";
import { KRAKEN_MODEL_TREE_POLICY as modelTreeJson } from "kraken-test-product-model-tree";
import { KRAKEN_MODEL_TREE_POLICYEXTENDED as extendedModelTreeJson } from "kraken-test-product-model-tree";
import { ContextInstanceInfo } from "../src/engine/contexts/info/ContextInstanceInfo";
import { DataObjectInfoResolver } from "../src/engine/contexts/info/DataObjectInfoResolver";
import { ContextInstanceInfoResolver } from "../src/engine/contexts/info/ContextInstanceInfoResolver";
import Identifiable = TestProduct.kraken.testproduct.domain.meta.Identifiable;
import { ContextDataExtractorImpl } from "../src/engine/contexts/data/extraction/ContextDataExtractorImpl";
import { DataContextBuilder } from "../src/engine/contexts/data/DataContextBuilder";
import { DataContext } from "../src/engine/contexts/data/DataContext";
import { TestProduct } from "kraken-test-product";
import { ExtractedChildDataContextBuilder } from "../src/engine/contexts/data/ExtractedChildDataContextBuilder";
import { ExpressionEvaluator } from "../src/engine/runtime/expressions/ExpressionEvaluator";

const modelTree = Object.freeze(modelTreeJson as unknown as ContextModelTree.ContextModelTree);
const extendedModelTree = Object.freeze(extendedModelTreeJson as unknown as ContextModelTree.ContextModelTree);
const evaluationConfig = Object.freeze({ context: {}, currencyCd: "USD" });
const session = Object.freeze(new ExecutionSession(evaluationConfig, {}));
const toMoney = (amount: number) => ({ amount: amount, currency: "USD" });
const contextInstanceInfo: ContextInstanceInfo = Object.freeze({
    getContextInstanceId: () => "1",
    getContextName: () => "mock"
});
const empty = (): TestProduct.kraken.testproduct.domain.Policy => ({
    id: "0",
    cd: "Policy",
    insured: {
        cd: "Insured",
        id: "insured-1-id",
        addressInfo: {
            id: "iai1",
            cd: "AddressInfo"
        }
    },
    billingInfo: {
        id: "1",
        cd: "BillingInfo",
        creditCardInfo: {
            id: "2",
            cd: "CreditCardInfo"
        }
    },
    parties: [
        {
            id: "3",
            cd: "Party",
            personInfo: {
                id: "4",
                cd: "PersonInfo"
            },
            roles: [
                {
                    id: "5",
                    cd: "PartyRole"
                }
            ]
        }
    ],
    riskItems: [
        {
            id: "6",
            cd: "Vehicle",
            addressInfo: {
                id: "7",
                cd: "AddressInfo"
            }
        }
    ],
    transactionDetails: {
        id: "8",
        cd: "TransactionDetails"
    },
    accessTrackInfo: {
        id: "9",
        cd: "AccessTrackInfo"
    },
    termDetails: {
    },
    policyDetail: {
        id: "11",
        cd: "PolicyDetail"
    }
});
const emptyExtended: () => TestProduct.kraken.testproduct.domain.Policy = () => ({
    id: "0",
    cd: "PolicyExtended",
    billingInfo: {
        id: "1",
        cd: "BillingInfoExtended",
        creditCardInfo: {
            id: "2",
            cd: "CreditCardInfoExtended",
            billingAddress: {
                id: "99",
                cd: "BillingAddressExtended"
            }
        }
    },
    parties: [
        {
            id: "3",
            cd: "PartyExtended",
            driverInfo: {
                cd: "DriverInfoExtended",
                id: "88"
            },
            personInfo: {
                id: "4",
                cd: "PersonInfoExtended"
            },
            roles: [
                {
                    id: "5",
                    cd: "PartyRoleExtended"
                }
            ]
        }
    ],
    riskItems: [
        {
            id: "6",
            cd: "VehicleExtended",
            addressInfo: {
                id: "7",
                cd: "AddressInfoExtended"
            },
            info: {
                id: "61",
                cd: "VehicleInfoExtended"
            }
        }
    ],
    transactionDetails: {
        id: "8",
        cd: "TransactionDetailsExtended"
    },
    accessTrackInfo: {
        id: "9",
        cd: "AccessTrackInfoExtended"
    },
    termDetails: {},
    policyDetail: {
        id: "11",
        cd: "PolicyDetailExtended"
    }
});
const dataContextEmpty: () => DataContext = () => {
    const emptyPolicy = empty();
    const { Policy } = modelTreeJson.contexts;
    const info: ContextInstanceInfo = {
        getContextInstanceId: () => emptyPolicy.id!,
        getContextName: () => emptyPolicy.cd!
    };
    return new DataContext(
        emptyPolicy.id!, Policy.name, emptyPolicy, info, modelTree.contexts[Policy.name].fields
    );
};
const dataContextEmptyExtended: () => DataContext = () => {
    const emptyPolicy = emptyExtended();
    const { PolicyExtended } = extendedModelTree.contexts;
    const info: ContextInstanceInfo = {
        getContextInstanceId: () => emptyPolicy.id!,
        getContextName: () => emptyPolicy.cd!
    };
    return new DataContext(
        emptyPolicy.id!,
        PolicyExtended.name,
        emptyPolicy,
        info,
        extendedModelTree.contexts[PolicyExtended.name].fields,
        undefined
    );
};

const dataContextCustom = (policy: Partial<TestProduct.kraken.testproduct.domain.Policy>) => {
    const dataContext = dataContextEmpty();
    (dataContext as { dataObject: {} }).dataObject = {
        ...dataContext.dataObject,
        ...policy
    };
    return dataContext;
};

const data = {
    empty,
    emptyExtended,
    dataContextEmpty,
    dataContextEmptyExtended,
    dataContextCustom
};
const dataResolver = ({
    validate: (identifiable: Identifiable) => {
        const errors = [];
        if (identifiable.cd === undefined) {
            errors.push({ message: "'cd' field is not present in data object" });
        }
        if (identifiable.id === undefined) {
            errors.push({ message: "'id' field is not present in data object" });
        }
        return errors;
    },
    resolveId: (identifiable: Identifiable) => identifiable["id"],
    resolveName: (identifiable: Identifiable) => identifiable["cd"]
} as DataObjectInfoResolver);
function resolveInfo(root: Identifiable): ContextInstanceInfo {
    return ({
        getContextInstanceId: () => root["id"]!,
        getContextName: () => root["cd"]!
    });
}
const spi = {
    dataResolver,
    instance: {
        validateContextDataObject: dataResolver.validate,
        processContextInstanceInfo: info => info,
        resolveRootInfo: resolveInfo,
        resolveAncestorInfo: resolveInfo,
        resolveExtractedInfo: resolveInfo
    } as ContextInstanceInfoResolver<Identifiable>
};
const contextBuilder = new DataContextBuilder(modelTree, spi.instance);
const evaluator = ExpressionEvaluator.DEFAULT;
const contextDataExtractor = new ContextDataExtractorImpl(
    modelTree,
    new ExtractedChildDataContextBuilder(
        contextBuilder,
        evaluator
    )
);
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
    contextBuilder
};
