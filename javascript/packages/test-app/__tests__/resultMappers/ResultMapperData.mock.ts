import { Payloads } from 'kraken-model'
import { PayloadBuilder } from 'kraken-model-builder'
import { FieldMetadata, payloadResultCreator, ContextInstanceInfo } from 'kraken-typescript-engine'

export const mockValidationMetadataVisibleApplicable = () => ({
    isVisible: true,
    isApplicable: true,
    errMessage: 'The field is mandatory',
})

export const mockValidationMetadataWithoutMessage = () => ({ isVisible: true, isApplicable: false, errMessage: '' })

const contextName = 'AutoPolicySummaryCtx'
const id = '8'
export const mockFieldMetadataNotVisibleNotApplicable = (): FieldMetadata => ({
    id: id,
    resolvedTargetPath: 'this.createdFromPolicyRev',
    info: {
        contextInstanceId: id,
        contextName: contextName,
        getContextName: () => contextName,
        getContextInstanceId: () => id,
    } as ContextInstanceInfo,
    isHidden: true,
    isDisabled: true,
    ruleResults: [],
    fieldType: 'UNKNOWN',
})

export const mockFieldMetadataWithErrorMessage = (): FieldMetadata => ({
    id: id,
    resolvedTargetPath: 'this.createdFromPolicyRev',
    info: {
        contextInstanceId: id,
        contextName: contextName,
        getContextName: () => contextName,
        getContextInstanceId: () => id,
    } as ContextInstanceInfo,
    isHidden: true,
    isDisabled: true,
    ruleResults: [
        {
            errorCode: 'code',
            isFailed: true,
            isOverridable: false,
            isOverridden: false,
            payloadResult: payloadResultCreator.length(PayloadBuilder.lengthLimit().limit(1), false, []),
            ruleName: 'name',
            severity: Payloads.Validation.ValidationSeverity.critical,
            errorMessage: 'code',
            templateVariables: [],
        },
    ],
    fieldType: 'UNKNOWN',
})
