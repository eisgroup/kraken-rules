/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package kraken.tracer;

/**
 * Operation which is added to trace if an error occurs.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
public final class ErrorOperation implements VoidOperation {

    private final String message;
    private final String firstStackElement;

    public ErrorOperation(Throwable e) {
        this.message = e.getMessage();
        this.firstStackElement = e.getStackTrace().length > 0
            ? e.getStackTrace()[0].toString()
            : null;
    }

    @Override
    public String describe() {
        return String.format("An error occurred during trace. %s %s",
            message != null ? "Error message: " + message + ", " : "",
            firstStackElement != null ? "Error occurred at: " + firstStackElement : "");
    }

}
