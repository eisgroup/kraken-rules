import { InstanceFactory } from '../factory/InstanceFactory'
import { Scope } from '../scope/Scope'

export class ScopeDeserializer {
    private readonly factory: InstanceFactory

    constructor(typeRegistryJson: unknown) {
        const factory = new InstanceFactory(InstanceFactory.registry)
        factory.initTypeRegistry(typeRegistryJson)
        this.factory = factory
    }

    provideScope(json: unknown): Scope {
        return this.factory.create(json)
    }
}
