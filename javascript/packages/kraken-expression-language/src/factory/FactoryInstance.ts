import { TypeRegistry } from "../TypeRegistry";
import { ClassName } from "./ClassName";
import { ClassNameMarker } from "./ClassNameMarker";
import { Json } from "./Json";

export class FactoryInstance implements ClassNameMarker {
    // tslint:disable-next-line: variable-name
    constructor(json: Json & ClassNameMarker, _typeRegistry: TypeRegistry) {
        Object.assign(this, json);
    }

    // tslint:disable-next-line: variable-name
    __type!: ClassName;
}
