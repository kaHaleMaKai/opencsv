package com.github.kahalemakai.opencsv.beans;

import com.opencsv.CSVParser;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Partially tuned copy of opencsv.CSVParser.
 * <p>
 * Remove the last quote character from a field, even if followed by whitespace.
 */
@Log4j
final class CsvParser extends CSVParser {
    @Getter
    private final char separator;
    @Getter
    private final char quotechar;
    @Getter
    private final char escape;
    @Getter
    private final boolean strictQuotes;
    @Getter
    private final boolean ignoreLeadingWhiteSpace;
    @Getter
    private final boolean ignoreQuotations;
    @Getter
    private final boolean multiLine;
    private final CSVReaderNullFieldIndicator nullFieldIndicator;
    @Getter
    private String pendingLine;
    @Getter
    private boolean inField;
    @Getter
    private long currentLineNr = 1;
    @Getter
    private long currentRecordNr = 1;

    public Iterator<String[]> wrapIterator(@NonNull final Iterator<String> iterator) {
        return new Iterator<String[]>() {
            private Iterator<String> lineIterator = iterator;

            @Override
            public boolean hasNext() {
                return lineIterator.hasNext();
            }

            /**
             * Reads the next line from the buffer and converts to a string array.
             *
             * @return A string array with each comma-separated element as a separate
             * entry.
             */
            public String[] next() {
                String[] result = null;
                do {
                    String[] r;
                    final String nextLine = lineIterator.next();
                    try {
                        r = parseLine(nextLine, isMultiLine());
                        currentLineNr++;
                    } catch (IOException e) {
                        final String msg = String.format("could not parse line %d, record %d ['%s']",
                                getCurrentLineNr(), getCurrentRecordNr(), nextLine);
                        log.error(msg);
                        throw new CsvToBeanException(e);
                    }
                    if (r.length > 0) {
                        if (result == null) {
                            result = r;
                        } else {
                            result = combineResults(result, r);
                        }
                    }
                } while (lineIterator.hasNext() && isPending());
                currentRecordNr++;
                return result;
            }
        };
    }

    protected String[] parseLine(String nextLine, boolean multi) throws IOException {
        if (!multi && isPending()) {
            popPendingLine();
        }

        if (nextLine == null) {
            if (isPending()) {
                String s = popPendingLine();
                return new String[]{s};
            }
            return null;
        }

        List<String> tokensOnThisLine = new ArrayList<>();
        StringBuilder sb = new StringBuilder(nextLine.length() + READ_BUFFER_SIZE);
        boolean inQuotes = false;
        boolean fromQuotedField = false;
        if (isPending()) {
            sb.append(popPendingLine());
            inQuotes = !this.isIgnoreQuotations(); //true;
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
                setPendingLine(sb.toString());
                sb = null; // this partial content is not to be added to field list yet
            } else {
                throw new IOException("un-terminated quoted field at end of csv line");
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

    static CsvParser of(char separator,
              char quoteChar,
              char escapeChar,
              boolean strictQuotes,
              boolean ignoreLeadingWhiteSpace,
              boolean ignoreQuotations,
              boolean multiLine) {
        return new CsvParser(
                separator,
                quoteChar,
                escapeChar,
                strictQuotes,
                ignoreLeadingWhiteSpace,
                ignoreQuotations,
                multiLine);
    }

    private CsvParser(char separator,
              char quotechar,
              char escape,
              boolean strictQuotes,
              boolean ignoreLeadingWhiteSpace,
              boolean ignoreQuotations,
              boolean multiLine) {
        super(separator, quotechar, escape, strictQuotes, ignoreLeadingWhiteSpace, ignoreQuotations);
        this.separator = separator;
        this.quotechar = quotechar;
        this.escape = escape;
        this.strictQuotes = strictQuotes;
        this.ignoreLeadingWhiteSpace = ignoreLeadingWhiteSpace;
        this.ignoreQuotations = ignoreQuotations;
        this.nullFieldIndicator = CSVReaderNullFieldIndicator.NEITHER;
        this.multiLine = multiLine;
    }

    public boolean isPending() {
        return this.pendingLine != null;
    }

    /**
     * For multi-line records this method combines the current result with the result from previous read(s).
     * @param buffer Previous data read for this record
     * @param lastRead Latest data read for this record.
     * @return String array with union of the buffer and lastRead arrays.
     */
    private String[] combineResults(String[] buffer, String[] lastRead) {
        String[] combinedLine = new String[buffer.length + lastRead.length];
        System.arraycopy(buffer, 0, combinedLine, 0, buffer.length);
        System.arraycopy(lastRead, 0, combinedLine, buffer.length, lastRead.length);
        return combinedLine;
    }

    private String popPendingLine() {
        final String tmp = this.pendingLine;
        this.pendingLine = null;
        return tmp;
    }

    private void setPendingLine(final String pendingLine) {
        this.pendingLine = pendingLine;
    }
}
