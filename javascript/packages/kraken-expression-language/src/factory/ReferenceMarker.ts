/**
 * Marks data as a reference.
 * Full data must be resolved.
 */
export interface ReferenceMarker {
    __proxy: true;
    name: string;
}
