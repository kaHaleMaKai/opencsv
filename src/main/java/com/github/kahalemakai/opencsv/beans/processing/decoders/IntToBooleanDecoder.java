package com.github.kahalemakai.opencsv.beans.processing.decoders;

public class IntToBooleanDecoder extends BooleanDecoder {
    public IntToBooleanDecoder() {
        setTrueValue("1");
        setFalseValue("0");
    }
}
