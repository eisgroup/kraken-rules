import { mapValidationResults } from '../../src/rule-engine/resultMappers'
import {
    mockFieldMetadataNotVisibleNotApplicable,
    mockFieldMetadataWithErrorMessage,
    mockValidationMetadataVisibleApplicable,
    mockValidationMetadataWithoutMessage,
} from './ResultMapperData.mock'
import { FieldMetadata } from 'kraken-typescript-engine'

describe('Result Mapper test', () => {
    it('mapValidationResult should return metadata results', () => {
        const fieldMetadata: Record<string, FieldMetadata> = {}
        fieldMetadata['metadataOne'] = mockFieldMetadataNotVisibleNotApplicable()

        const result = mapValidationResults({})(fieldMetadata)

        expect(result['metadataOne'].isVisible).toBe(false)
        expect(result['metadataOne'].isApplicable).toBe(false)
    })

    it('mapValidationResult should return updated metadata results', () => {
        const metadata = {
            metadataOne: mockValidationMetadataVisibleApplicable(),
        }
        const fieldMetadata: Record<string, FieldMetadata> = {}
        fieldMetadata['metadataOne'] = mockFieldMetadataNotVisibleNotApplicable()

        const result = mapValidationResults(metadata)(fieldMetadata)
        expect(result['metadataOne'].isVisible).toBe(false)
        expect(result['metadataOne'].isApplicable).toBe(false)
    })

    it('mapValidationResult should return metadata with message', () => {
        const metadata = {
            metadataOne: mockValidationMetadataWithoutMessage(),
        }
        const fieldMetadata: Record<string, FieldMetadata> = {}
        fieldMetadata['metadataOne'] = mockFieldMetadataWithErrorMessage()

        const result = mapValidationResults(metadata)(fieldMetadata)
        expect(result['metadataOne'].errMessage).toBe('code')
    })

    it('mapValidationResult should return metadata without message', () => {
        const metadata = {
            metadataOne: mockValidationMetadataVisibleApplicable(),
        }
        const fieldMetadata: Record<string, FieldMetadata> = {}
        fieldMetadata['metadataOne'] = mockFieldMetadataNotVisibleNotApplicable()

        const result = mapValidationResults(metadata)(fieldMetadata)
        expect(result['metadataOne'].errMessage).toBe(undefined)
    })
})
