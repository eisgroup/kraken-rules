import { TypeRegistry } from '../TypeRegistry'
import { ClassName } from './ClassName'
import { ClassNameMarker } from './ClassNameMarker'
import { Json } from './Json'

export class FactoryInstance implements ClassNameMarker {
    constructor(json: Json & ClassNameMarker, _typeRegistry: TypeRegistry) {
        Object.assign(this, json)
    }

    __type!: ClassName
}
