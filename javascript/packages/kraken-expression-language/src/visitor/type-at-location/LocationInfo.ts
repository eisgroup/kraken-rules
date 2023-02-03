import { Range } from '../NodeUtils'

type WithRange = { range?: Range }
export type FunctionLocationInfo = { type: 'function'; parametersCount: number; functionName: string } & WithRange
export type ReferenceLocationInfo = { type: 'type'; evaluationType: string } & WithRange
export type LocationInfo = FunctionLocationInfo | ReferenceLocationInfo
