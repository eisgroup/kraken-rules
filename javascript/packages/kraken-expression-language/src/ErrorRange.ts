import { CursorLocation } from "./visitor/node/CursorLocation";

export type ErrorRange = {
    start: CursorLocation;
    end: CursorLocation;
};
