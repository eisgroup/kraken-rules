/*
 *  Copyright 2020 EIS Ltd and/or one of its affiliates.
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

import { mock } from '../../../mock'
import { PathCardinalityResolver } from '../../../../src/engine/contexts/ccr/PathCardinalityResolver'
import { CommonPathResolver } from '../../../../src/engine/contexts/ccr/CommonPathResolver'
import { ReferencePathResolverImpl } from '../../../../src/engine/contexts/ccr/ReferencePathResolverImpl'

describe('Reference Path Resolver', () => {
    const { Policy, Vehicle, AddressLine, AddressLine2, AddressInfo, Party, PartyRole, RRCoverage } =
        mock.modelTreeJson.contexts
    let resolver: ReferencePathResolverImpl
    beforeEach(() => {
        resolver = new ReferencePathResolverImpl(
            mock.modelTree.pathsToNodes,
            new PathCardinalityResolver(mock.modelTree.contexts),
            new CommonPathResolver(),
        )
    })
    describe('one path', () => {
        it('should resolve self reference on the root', () => {
            const ref = resolver.resolveReferencePath([Policy.name], Policy.name)
            expect(ref.cardinality).toBe('SINGLE')
            expect(ref.path).toMatchObject([Policy.name])
        })
        it('should resolve self reference in the array', () => {
            const ref = resolver.resolveReferencePath([Policy.name, Vehicle.name], Vehicle.name)
            expect(ref.path).toMatchObject([Vehicle.name])
            expect(ref.cardinality).toBe('SINGLE')
        })
        it('should resolve reference to child array', () => {
            const ref = resolver.resolveReferencePath([Policy.name], Vehicle.name)
            expect(ref.path).toMatchObject([Policy.name, Vehicle.name])
            expect(ref.cardinality).toBe('MULTIPLE')
        })
        it('should resolve reference to parent', () => {
            const ref = resolver.resolveReferencePath([Policy.name, Vehicle.name], Policy.name)
            expect(ref.path).toMatchObject([Policy.name])
            expect(ref.cardinality).toBe('SINGLE')
        })
    })
    describe('multiple paths', () => {
        it('should resolve self reference', () => {
            const ref = resolver.resolveReferencePath([Policy.name, Vehicle.name], Vehicle.name)
            expect(ref.cardinality).toBe('SINGLE')
            expect(ref.path).toMatchObject([Vehicle.name])
        })
        it('should resolve reference to the children with single cardinality', () => {
            const ref = resolver.resolveReferencePath([Policy.name, Vehicle.name], AddressLine2.name)
            expect(ref.path).toMatchObject([Vehicle.name, AddressInfo.name, AddressLine2.name])
            expect(ref.cardinality).toBe('SINGLE')
        })
        it('should resolve reference to the children with multiple cardinality', () => {
            const ref = resolver.resolveReferencePath([Policy.name], 'RentalCoverage')
            expect(ref.path).toMatchObject([Policy.name, Vehicle.name, RRCoverage.name])
            expect(ref.cardinality).toBe('MULTIPLE')
        })
    })
    describe('negative cases', () => {
        it('should throw more than one ref in different brach ', () => {
            expect(() =>
                resolver.resolveReferencePath(
                    [
                        Policy.name,
                        Policy.children.Party.targetName,
                        Party.children.PartyRole.targetName,
                        PartyRole.name,
                    ],
                    AddressLine.name,
                ),
            ).toThrow('Cannot determine path to reference, resolved 2')
        })
        it('should throw more than one ref in the same branch', () => {
            expect(() =>
                resolver.resolveReferencePath([Policy.name, Policy.children.Vehicle.targetName], AddressLine.name),
            ).toThrow('Cannot determine path to reference, resolved 2')
        })
    })
})
