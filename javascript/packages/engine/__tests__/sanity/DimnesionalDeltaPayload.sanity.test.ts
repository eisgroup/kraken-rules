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

import { sanityMocks } from "./_AutoPolicyObject.mocks";
import { sanityEngine } from "./_SanityEngine";

describe("Engine Sanity Dimensional Delta Caching test", () => {
    const { empty } = sanityMocks;
    it("should execute 'Dimensional' entrypoint with state AZ", () => {
        const data = empty();
        data.parties![0].personInfo!.addressInfo = {
            cd: "AddressInfo",
            id: "4-addressInfo",
            addressLine1: {
                cd: "AddressLine1",
                id: "4-addressInfo-addressLine1"
            }
        };
        const results = sanityEngine.evaluate(data, "Dimensional", "Dimensional-state-AZ");
        expect(data.parties![0].personInfo?.addressInfo?.postalCode).toBeDefined();
        expect(data.parties![0].personInfo?.addressInfo?.countryCd).toBeDefined();
        expect(data.parties![0].personInfo?.addressInfo?.addressLine1?.addressLine).toBeDefined();
        expect(results).k_toMatchResultsStats({ total: 9, critical: 4 });
    });
    it("should execute 'Dimensional' entrypoint with no dimensions", () => {
        const data = empty();
        data.parties![0].personInfo!.addressInfo = {
            cd: "AddressInfo",
            id: "4-addressInfo",
            addressLine1: {
                cd: "AddressLine1",
                id: "4-addressInfo-addressLine1"
            }
        };
        const results = sanityEngine.evaluate(data, "Dimensional");
        expect(data.parties![0].personInfo?.addressInfo?.postalCode).not.toBeDefined();
        expect(data.parties![0].personInfo?.addressInfo?.countryCd).toBeDefined();
        expect(data.parties![0].personInfo?.addressInfo?.addressLine1?.addressLine).not.toBeDefined();
        expect(results).k_toMatchResultsStats({ total: 7, critical: 4 });
    });
});
