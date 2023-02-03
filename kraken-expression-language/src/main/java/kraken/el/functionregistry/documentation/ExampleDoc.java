/*
 *  Copyright © 2019 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 *  CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.
 *
 */

package kraken.el.functionregistry.documentation;

public class ExampleDoc {
    private final String call;
    private final String result;
    private final boolean validCall;

    public ExampleDoc(String call, String result, boolean validCall) {
        this.call = call;
        this.result = result;
        this.validCall = validCall;
    }

    public boolean isValidCall() {
        return validCall;
    }

    public String getResult() {
        return result;
    }

    public String getCall() {
        return call;
    }
}
