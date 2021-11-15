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

import { EvaluationConfig } from "../../src";
import { mock } from "../mock";
import { TestProduct } from "kraken-test-product";

export interface Created {
    policyCurrency?: string;
    bilAdr_countryCd?: string;
    creditCardInfo_cardType?: string;
    vehModel?: string;
    vehAdrCountryCd?: string;
    partyDriverType?: string;
    personInfo_name?: string;
    vehInfo?: string;
    policyState?: string;
}

export const sanityMocks = {
    empty: (): TestProduct.kraken.testproduct.domain.Policy => ({
        id: "0",
        cd: "Policy",
        insured: {
            cd: "Insured",
            id: "insured-1-id"
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
    }),
    emptyExtended: (): TestProduct.kraken.testproduct.domain.Policy => ({
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
    }),
    valid: (): TestProduct.kraken.testproduct.domain.Policy => ({
        id: "12",
        cd: "Policy",
        policyNumber: "Q0006",
        createdFromPolicyRev: 1,
        state: "State",
        billingInfo: {
            creditCardInfo: {
                id: "13",
                cd: "CreditCardInfo",
                cardType: "MasterCard",
                cardNumber: "5105105105105100",
                cardCreditLimitAmount: mock.toMoney(1555.35),
                cvv: 123,
                billingAddress: {
                    id: "14",
                    cd: "BillingAddress",
                    doNotSolicit: true,
                    countryCd: "CD",
                    postalCode: "00000",
                    city: "CITY"
                }
            }
        },
        parties: [
            {
                id: "15",
                cd: "Party",
                relationToPrimaryInsured: "Not Related",
                personInfo: {
                    id: "16",
                    cd: "PersonInfo",
                    firstName: "Jonas",
                    lastName: "Jomantas",
                    addressInfo: {
                        id: "17",
                        cd: "AddressInfo",
                        doNotSolicit: true,
                        countryCd: "CD",
                        postalCode: "00000",
                        city: "CITY"
                    }
                },
                roles: []
            },
            {
                id: "18",
                cd: "Party",
                relationToPrimaryInsured: "Related",
                roles: [
                    {
                        id: "19",
                        cd: "PartyRole",
                        role: "Admin"
                    }
                ]
            }
        ],
        riskItems: [
            {
                id: "20",
                cd: "Vehicle",
                included: true,
                model: "BMW",
                modelYear: 2000,
                purchasedDate: new Date("2000-01-01"),
                addressInfo: {
                    id: "21",
                    cd: "AddressInfo",
                    doNotSolicit: true,
                    countryCd: "LA",
                    postalCode: "12345",
                    city: "San Francisco"
                }
            }
        ],
        transactionDetails: {
            id: "22",
            cd: "TransactionDetails",
            txType: "CashBack",
            txReason: "Loan",
            txEffectiveDate: new Date("2018-01-01T00:00:00Z"),
            txCreateDate: new Date("2018-01-01")
        },
        accessTrackInfo: {
            id: "23",
            cd: "AccessTrackInfo",
            createdOn: new Date("2000-01-01"),
            createdBy: "Manager",
            updatedOn: new Date("2018-01-01"),
            updatedBy: "qaqa"
        },
        termDetails: {
            id: "24",
            cd: "TermDetails",
            contractTermTypeCd: "ContractTermTypeCd",
            termNo: 11,
            termEffectiveDate: new Date("2000-01-01"),
            termExpirationDate: new Date("3000-01-01"),
            termCd: "TemrCd"
        },
        policyDetail: {}
    }),
    inValid: (): TestProduct.kraken.testproduct.domain.Policy => ({
        id: "0",
        cd: "Policy",
        policyNumber: "Not Valid",
        state: "654321",
        billingInfo: {
            id: "1",
            cd: "BillingInfo",
            creditCardInfo: {
                id: "2",
                cd: "CreditCardInfo",
                cardType: "MasterCard",
                cardNumber: "1545464464454545",
                cvv: 132654
            }
        },
        parties: [
            {
                id: "3",
                cd: "Party",
                personInfo: {
                    id: "4",
                    cd: "PersonInfo",
                    firstName: "Antanas",
                    lastName: "Antanas",
                    addressInfo: {
                        id: "5",
                        cd: "AddressInfo",
                        postalCode: "123456",
                        city: "654321"
                    }
                },
                roles: []
            }
        ],
        riskItems: [
            {
                id: "6",
                cd: "Vehicle"
            }
        ],
        termDetails: {
            termNo: 101
        }
    }),
    model: ({
        policyCurrency,
        bilAdr_countryCd,
        creditCardInfo_cardType,
        vehModel,
        vehAdrCountryCd,
        partyDriverType,
        personInfo_name,
        policyState
    }: Created): TestProduct.kraken.testproduct.domain.Policy => {
        const data = sanityMocks.empty();
        data.billingInfo!.creditCardInfo!["cardType"] = creditCardInfo_cardType;
        data["policyCurrency"] = policyCurrency;
        data["state"] = policyState;
        data.billingInfo!.creditCardInfo!["billingAddress"] = {
            id: "99",
            cd: "BillingAddress",
            countryCd: bilAdr_countryCd
        };
        data.riskItems![0]["model"] = vehModel;
        data.riskItems![0].addressInfo!["countryCd"] = vehAdrCountryCd;
        data.parties![0]["driverInfo"] = {
            cd: "DriverInfo",
            id: "88",
            driverType: partyDriverType
        };
        data.parties![0].personInfo!["firstName"] = personInfo_name;
        return data;
    },
    extendedModel: ({
        policyCurrency,
        bilAdr_countryCd,
        creditCardInfo_cardType,
        vehModel,
        vehAdrCountryCd,
        partyDriverType,
        personInfo_name,
        policyState
    }: Created): TestProduct.kraken.testproduct.domain.Policy => {
        const data = sanityMocks.emptyExtended();
        data.billingInfo!.creditCardInfo!["cardType"] = creditCardInfo_cardType;
        data["policyCurrency"] = policyCurrency;
        data["state"] = policyState;
        data.billingInfo!.creditCardInfo!.billingAddress!["countryCd"] = bilAdr_countryCd;
        data.riskItems![0]["model"] = vehModel;
        data.riskItems![0].addressInfo!["countryCd"] = vehAdrCountryCd;
        data.parties![0].driverInfo!["driverType"] = partyDriverType;
        data.parties![0].personInfo!["firstName"] = personInfo_name;
        return data;
    },
    evalConf: (dimensions?: Record<string, {}>, other?: Record<string, {}>): EvaluationConfig => ({
        currencyCd: "USD",
        context: { dimensions: { ...dimensions }, ...other }
    })
};
