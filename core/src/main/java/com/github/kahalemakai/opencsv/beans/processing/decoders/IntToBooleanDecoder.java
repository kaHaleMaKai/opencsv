package com.github.kahalemakai.opencsv.beans.processing.decoders;

/**
 * Decode {@code {"0", "1"} -> {false, true}}.
 */
public class IntToBooleanDecoder extends BooleanDecoder {

    /**
     * Create a new decoder instance.
     */
    public IntToBooleanDecoder() {
        setTrueValue("1");
        setFalseValue("0");
    }
}
