package com.github.kahalemakai.opencsv.beans.processing.decoders;


import com.github.kahalemakai.opencsv.beans.processing.Decoder;
import com.github.kahalemakai.opencsv.beans.processing.ObjectWrapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse a decimal number of given precision and scale, or null.
 * <p>
 * Subclasses need to set {@code precision} and {@code scale} on instance creation.
 */
@Log4j
abstract public class AbstractDecimalDecoder implements Decoder<ByteBuffer> {
    @Getter(value = AccessLevel.PRIVATE, lazy = true)
    private final DecimalFormat format = determineFormat();
    private final static Pattern pattern = Pattern.compile("[-.]");

    // subclasses have to set these values by defining the respective methods
    @Setter(AccessLevel.PROTECTED)
    private int precision;
    @Setter(AccessLevel.PROTECTED)
    private int scale;

    // count number of occurrence of minus sign or floating point
    private static int getNumMatches(String data) {
        int counter = 0;
        final Matcher matcher = pattern.matcher(data);
        while (matcher.find())
            counter++;
        return counter;
    }

    // assemble a decimal format with the given precision and scale
    private DecimalFormat determineFormat() {
        final int capacity = precision + scale + 1;
        final StringBuilder sb = new StringBuilder(capacity);
        for (int i = 0; i < precision - 1; ++i)
            sb.append("#");
        sb.append("0.");
        for (int i = 0; i < scale; ++i)
            sb.append("#");
        final DecimalFormat decimalFormat = new DecimalFormat(sb.toString());
        decimalFormat.setParseBigDecimal(true);
        return decimalFormat;
    }

    /**
     * Write a BigDecimal instance as byte array.
     * <p>
     * This conversion is required for interoperability with avro.
     *
     * @param value decimal instance to be converted
     * @return decimal instance as byte array
     */
    private static ByteBuffer toBytes(BigDecimal value) {
        return ByteBuffer.wrap(value.unscaledValue().toByteArray());
    }

    @Override
    public ObjectWrapper<? extends ByteBuffer> decode(String data) {
        // remove a superfluous positive prefix (simplifies parsing with DecimalFormat.parse()
        if (data.startsWith("+") && data.length() > 1)
            data = data.substring(1);
        final int numDigits = data.length() - getNumMatches(data);
        // throw on too many digits (aka precision)
        if (numDigits > precision) {
            if (log.isDebugEnabled()) {
                final String msg = String.format("too small precision for decimal input. expected: precision = %d, got input: '%s'", precision, data);
            }
            return decodingFailed();
        }

        try {
            final BigDecimal number = (BigDecimal) getFormat().parse(data);
            final BigDecimal scaledDecimal = number.setScale(scale, BigDecimal.ROUND_UNNECESSARY);
            final ByteBuffer bytes = toBytes(scaledDecimal);
            return Decoder.success(bytes);
        } catch (ParseException e) {
            if (log.isDebugEnabled()) {
                final String msg = String.format("cannot decode input '%s' as decimal", data);
                log.debug(msg);
            }
            return decodingFailed();
        } catch (ArithmeticException e) {
            // rounding mode set to NOT_NECESSARY
            // those DecimalFormat.parse() throws on too large scale for input (this is what we want)
            if (log.isDebugEnabled()) {
                final String msg = String.format("too small scale for decimal input. expected: scale = %d, got input: '%s'", scale, data);
                log.debug(msg);
            }
            return decodingFailed();
        }
    }

}
