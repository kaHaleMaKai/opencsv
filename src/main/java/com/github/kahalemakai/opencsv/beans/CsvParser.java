package com.github.kahalemakai.opencsv.beans;

import com.opencsv.CSVParser;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CsvParser extends CSVParser {
    @Getter
    final char separator;
    @Getter
    final char quotechar;
    @Getter
    final char escape;
    @Getter
    final boolean strictQuotes;
    @Getter
    final boolean ignoreLeadingWhiteSpace;
    @Getter
    final boolean ignoreQuotations;
    private final CSVReaderNullFieldIndicator nullFieldIndicator;
    @Getter
    private String pending;
    @Getter
    private boolean inField;

    @Override
    protected String[] parseLine(String nextLine, boolean multi) throws IOException {
        if (!multi && pending != null) {
            pending = null;
        }

        if (nextLine == null) {
            if (pending != null) {
                String s = pending;
                pending = null;
                return new String[]{s};
            }
            return null;
        }

        List<String> tokensOnThisLine = new ArrayList<>();
        StringBuilder sb = new StringBuilder(nextLine.length() + READ_BUFFER_SIZE);
        boolean inQuotes = false;
        boolean fromQuotedField = false;
        if (pending != null) {
            sb.append(pending);
            pending = null;
            inQuotes = !this.isIgnoreQuotations();//true;
        }
        for (int i = 0; i < nextLine.length(); i++) {

            char c = nextLine.charAt(i);
            if (c == this.escape) {
                if (isNextCharacterEscapable(nextLine, inQuotes(inQuotes), i)) {
                    i = appendNextCharacterAndAdvanceLoop(nextLine, sb, i);
                }
            } else if (c == quotechar) {
                if (isNextCharacterEscapedQuote(nextLine, inQuotes(inQuotes), i)) {
                    i = appendNextCharacterAndAdvanceLoop(nextLine, sb, i);
                } else {

                    inQuotes = !inQuotes;
                    if (atStartOfField(sb)) {
                        fromQuotedField = true;
                    }

                    // the tricky case of an embedded quote in the middle: a,bc"d"ef,g
                    if (!strictQuotes
                            && i > 2 // not at the beginning of the line
                            && nextLine.charAt(i - 1) != this.separator //not at the beginning of an escape sequence
                            && nextLine.length() > (i + 1)
                            && nextLine.charAt(i + 1) != this.separator //not at the end of an escape sequence
                            ) {

                        if (ignoreLeadingWhiteSpace && sb.length() > 0 && StringUtils.isWhitespace(sb)) {
                            sb.setLength(0);
                        } else if (inQuotes){
                            sb.append(c);
                        }

                    }
                }
                inField = !inField;
            } else if (c == separator && !(inQuotes && !ignoreQuotations)) {
                tokensOnThisLine.add(convertEmptyToNullIfNeeded(sb.toString(), fromQuotedField));
                fromQuotedField = false;
                sb.setLength(0);
                inField = false;
            } else {
                if (!strictQuotes || (inQuotes && !ignoreQuotations)) {
                    sb.append(c);
                    inField = true;
                    fromQuotedField = true;
                }
            }

        }
        // line is done - check status
        if ((inQuotes && !ignoreQuotations)) {
            if (multi) {
                // continuing a quoted section, re-append newline
                sb.append('\n');
                pending = sb.toString();
                sb = null; // this partial content is not to be added to field list yet
            } else {
                throw new IOException("Un-terminated quoted field at end of CSV line");
            }
            if (inField) {
                fromQuotedField = true;
            }
        } else {
            inField = false;
        }

        if (sb != null) {
            tokensOnThisLine.add(convertEmptyToNullIfNeeded(sb.toString(), fromQuotedField));
        }
        return tokensOnThisLine.toArray(new String[tokensOnThisLine.size()]);
    }

    /**
     * Determines if we can process as if we were in quotes.
     *
     * @param inQuotes Are we currently in quotes.
     * @return True if we should process as if we are inside quotes.
     */
    private boolean inQuotes(boolean inQuotes) {
        return (inQuotes && !ignoreQuotations) || inField;
    }

    private int appendNextCharacterAndAdvanceLoop(String line, StringBuilder sb, int i) {
        sb.append(line.charAt(i + 1));
        i++;
        return i;
    }

    /**
     * Checks to see if the character after the index is a quotation character.
     *
     * Precondition: the current character is a quote or an escape.
     *
     * @param nextLine The current line
     * @param inQuotes True if the current context is quoted
     * @param i        Current index in line
     * @return True if the following character is a quote
     */
    private boolean isNextCharacterEscapedQuote(String nextLine, boolean inQuotes, int i) {
        return inQuotes  // we are in quotes, therefore there can be escaped quotes in here.
                && nextLine.length() > (i + 1)  // there is indeed another character to check.
                && (nextLine.charAt(i + 1)) == quotechar;
    }

    private boolean atStartOfField(StringBuilder sb) {
        return sb.length() == 0;
    }

    private String convertEmptyToNullIfNeeded(String s, boolean fromQuotedField) {
        if (s.isEmpty() && shouldConvertEmptyToNull(fromQuotedField)) {
            return null;
        }
        return s;
    }

    private boolean shouldConvertEmptyToNull(boolean fromQuotedField) {
        switch (nullFieldIndicator) {
            case BOTH:
                return true;
            case EMPTY_SEPARATORS:
                return !fromQuotedField;
            case EMPTY_QUOTES:
                return fromQuotedField;
            default:
                return false;
        }
    }

    CsvParser(char separator, char quotechar, char escape, boolean strictQuotes, boolean ignoreLeadingWhiteSpace, boolean ignoreQuotations) {
        super(separator, quotechar, escape, strictQuotes, ignoreLeadingWhiteSpace, ignoreQuotations);
        this.separator = separator;
        this.quotechar = quotechar;
        this.escape = escape;
        this.strictQuotes = strictQuotes;
        this.ignoreLeadingWhiteSpace = ignoreLeadingWhiteSpace;
        this.ignoreQuotations = ignoreQuotations;
        this.nullFieldIndicator = CSVReaderNullFieldIndicator.NEITHER;
    }

}
