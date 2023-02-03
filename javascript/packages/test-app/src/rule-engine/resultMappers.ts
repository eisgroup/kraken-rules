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

import { ValidationMetadata } from '../components/core/field/SingleField'
import { FieldMetadata } from 'kraken-typescript-engine'

export const mapValidationResults =
    (metadata: { [key: string]: ValidationMetadata }) => (res: Record<string, FieldMetadata>) => {
        const updatedMetadata = Object.keys(metadata).reduce((metamap, key) => {
            metamap[key] = {
                isVisible: res[key] ? !res[key].isHidden : metadata[key].isVisible,
                isApplicable: res[key] ? !res[key].isDisabled : metadata[key].isVisible,
                errMessage:
                    res[key] && res[key].ruleResults && res[key].ruleResults.length
                        ? res[key].ruleResults
                              .filter(m => m.isFailed)
                              .map(m => m.errorMessage || m.errorCode)
                              .join('\n')
                        : undefined,
            }
            return metamap
        }, {} as { [key: string]: ValidationMetadata })
        const newMetadata = Object.keys(res)
            .filter(key => !Object.keys(metadata).some(mk => mk === key))
            .reduce((pv, cv) => {
                pv[cv] = {
                    isApplicable: !res[cv].isDisabled,
                    isVisible: !res[cv].isHidden,
                    errMessage:
                        res[cv] &&
                        res[cv].ruleResults &&
                        res[cv].ruleResults.length &&
                        res[cv].ruleResults
                            .filter(m => m.isFailed)
                            .map(m => m.errorMessage || m.errorCode)
                            .join('\n'),
                }
                return pv
            }, {} as { [key: string]: ValidationMetadata })
        return { ...updatedMetadata, ...newMetadata }
    }
