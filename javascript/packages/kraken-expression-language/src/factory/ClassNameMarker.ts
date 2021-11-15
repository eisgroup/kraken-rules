import { ClassName } from "./ClassName";
import { Objects } from "./Objects";

/**
 * Instances of implementing classes are deserialized from a JSON.
 * JSON serialized from java classes. Java class full name is {@link ClassName}.
 *
 * @export
 * @interface ClassNameMarker
 */
export interface ClassNameMarker {
    __type: ClassName;
}

export function isClassNameMarker(o: unknown): o is ClassNameMarker {
    return Objects.propertyExists(o, "__type");
}
