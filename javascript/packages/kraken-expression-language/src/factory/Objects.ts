export namespace Objects {
    export function withPrototype<SUP extends object, SUB extends SUP>(data: SUP, Sub: SUB): SUB {
        Object.setPrototypeOf(data, Sub);
        return data as SUB;
    }

    /**
     * Checks a property to exist in the object, and this property to be defined.
     * The object can be instance of Proxy
     *
     * @export
     * @template KEY        string
     * @param {unknown} o   js object, class or Proxy
     * @param {KEY} key     property name
     * @returns {o is Record<KEY, unknown>}
     */
    export function propertyExists<KEY extends string>(o: unknown, key: KEY): o is Record<KEY, unknown> {
        return o !== null
            && o !== undefined
            && typeof o === "object"
            && (o as Record<string, unknown>)[key] !== null
            && (o as Record<string, unknown>)[key] !== undefined;
    }

    export function hasOwnProp<KEY extends string>(o: unknown, key: KEY): o is Record<KEY, unknown> {
        return o !== null
            && o !== undefined
            && typeof o === "object"
            && Object.prototype.hasOwnProperty.call(o, key);
    }
}
