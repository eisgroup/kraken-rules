/*
 * Copyright 2023 EIS Ltd and/or one of its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { SystemMessageBuilder } from '../../src'

describe('KrakenRuntimeError', () => {
    it('should format message from template', () => {
        const message = {
            code: 'code',
            messageTemplate: 'First {0}, second {1}, third {2}',
        }
        const systemMessage = new SystemMessageBuilder(message).parameters('a', 10, true).build()
        expect(systemMessage.code).toStrictEqual('code')
        expect(systemMessage.message).toStrictEqual('First a, second 10, third true')
    })
})
