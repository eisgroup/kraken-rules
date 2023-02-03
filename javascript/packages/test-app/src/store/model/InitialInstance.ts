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

// import { v4 as uuid } from "uuid";
import * as faker from 'faker'
import { TestProduct } from 'kraken-test-product'
import TP = TestProduct.kraken.testproduct.domain

let id = 0
function uuid(): string {
    const i = id + 1
    id = id + 1
    return i.toString()
}

function randomNumber(): number {
    return Math.round(Math.random() * 10)
}
function randomArray(): unknown[] {
    return Array.from(Array(randomNumber()))
}

const getInitialReferer = (): TP.Referer => ({
    id: uuid(),
    cd: 'Referer',
    addressInfo: getInitialAddressInfo(),
    name: faker.name.firstName(),
})

const getInitialCollCoverage = (): TP.COLLCoverage => ({
    code: 'COLLCoverage',
    id: uuid(),
    cd: 'COLLCoverage',
})

const getInitialAnubisCoverage = (code = 'AnubisCoverage'): TP.AnubisCoverage => ({
    code,
    id: uuid(),
    cd: 'AnubisCoverage',
    cult: {},
})

const getInitialRentalCoverage = (): TP.RRCoverage => ({
    code: 'RRCoverage',
    id: uuid(),
    cd: 'RRCoverage',
})

const getInitialFullCoverage = (): TP.FullCoverage => ({
    code: 'FullCoverage',
    id: uuid(),
    cd: 'FullCoverage',
})

const getInitialAddressInfo = (): TP.AddressInfo => ({
    cd: 'AddressInfo',
    id: uuid(),
    doNotSolicit: faker.random.boolean(),
    city: faker.address.city(),
    countryCd: faker.address.countryCode(),
    postalCode: faker.address.zipCode(),
    street: faker.address.streetName(),
})

const initialBillingAddress: () => TP.BillingAddress = () => ({
    cd: 'BillingAddress',
    id: uuid(),
    doNotSolicit: faker.random.boolean(),
    countryCd: faker.address.countryCode(),
    postalCode: faker.address.zipCode(),
    street: faker.address.streetName(),
    city: faker.address.city(),
})

const initInsured: () => TP.Insured = () => ({
    cd: 'Insured',
    id: uuid(),
    addressInfo: initialBillingAddress(),
    haveChildren: true,
    name: faker.name.firstName(),
    childrenAges: randomArray().map(randomNumber),
})

const getInitialVehicle = (): TP.Vehicle => ({
    cd: 'Vehicle',
    id: uuid(),
    included: false,
    costNew: faker.random.number(100000),
    declaredAnnualMiles: Math.pow(randomNumber(), randomNumber()),
    model: faker.commerce.productName(),
    modelYear: faker.date.past().getFullYear(),
    newValue: faker.random.number(),
    numDaysDrivenPerWeek: 3,
    odometerReading: Math.pow(randomNumber(), randomNumber()),
    purchasedDate: faker.date.past(),
    serviceHistory: randomArray().map(() => faker.date.past()),
    collCoverages: [getInitialCollCoverage()],
    anubisCoverages: [
        getInitialAnubisCoverage('Anubis First Coverage'),
        getInitialAnubisCoverage('Anubis Second Coverage'),
    ],
    rentalCoverage: getInitialRentalCoverage(),
    fullCoverages: [getInitialFullCoverage()],
    addressInfo: getInitialAddressInfo(),
})

const initialPartyRole: () => TP.PartyRole = () => ({
    cd: 'PartyRole',
    id: uuid(),
    limit: randomNumber(),
    role: faker.company.bsNoun(),
})

const initialParty: () => TP.Party = () => ({
    cd: 'Party',
    id: uuid(),
    roles: [initialPartyRole()],
    relationToPrimaryInsured: faker.company.bsBuzz(),
    personInfo: {
        cd: 'PersonInfo',
        id: uuid(),
        sameHomeAddress: faker.random.boolean(),
        age: faker.random.number(144),
        firstName: faker.name.firstName(),
        lastName: faker.name.lastName(),
        occupation: faker.hacker.phrase(),
        additionalInfo: faker.random.words(),
        addressInfo: getInitialAddressInfo(),
    },
    driverInfo: {
        cd: 'DriverInfo',
        id: uuid(),
        convicted: faker.random.boolean(),
        driverType: faker.random.word(),
        trainingCompletionDate: faker.date.future(),
    },
})

export const initialModel = (): TP.Policy => ({
    id: uuid(),
    cd: 'Policy',
    referer: getInitialReferer(),
    policyCurrency: 'USD',
    policyNumber: faker.helpers.slugify('policy') + faker.random.number(),
    billingInfo: {
        id: uuid(),
        accountName: faker.finance.accountName(),
        creditCardInfo: {
            additionalInfo: faker.random.word('card'),
            cardCreditLimitAmount: { amount: Math.pow(randomNumber(), randomNumber()), currency: 'USD' },
            cardHolderName: faker.name.firstName(),
            cardNumber: faker.finance.iban(),
            cardType: faker.finance.accountName(),
            cvv: faker.random.number(999),
            expirationDate: faker.date.future(),
            id: uuid(),
            cd: 'CreditCardInfo',
            billingAddress: initialBillingAddress(),
            refsToBank: [],
        },
    },
    insured: initInsured(),
    accessTrackInfo: {
        createdBy: faker.name.firstName(),
        createdOn: faker.date.past(),
        updatedBy: faker.name.firstName(),
        updatedOn: faker.date.recent(),
        id: uuid(),
    },
    transactionDetails: {
        changePremium: faker.random.number(123),
        totalLimit: faker.random.number(9999),
        txCreateDate: faker.date.past(123),
        totalPremium: faker.random.number(9899),
        txEffectiveDate: faker.date.future(123),
        txType: faker.finance.transactionType(),
        txReason: faker.random.word(),
        id: uuid(),
    },
    parties: [initialParty()],
    policyDetail: {
        id: uuid(),
        currentQuoteInd: faker.random.boolean(),
        versionDescription: faker.random.words(),
        oosProcessingStage: faker.random.word(),
    },
    riskItems: Array.from(Array(10)).map(getInitialVehicle),
    termDetails: {
        contractTermTypeCd: faker.random.word(),
        termCd: faker.random.word(),
        termEffectiveDate: faker.date.past(),
        termExpirationDate: faker.date.future(),
        termNo: faker.random.number(9999999),
        id: uuid(),
    },
})
