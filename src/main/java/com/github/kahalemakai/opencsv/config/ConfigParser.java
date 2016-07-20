package com.github.kahalemakai.opencsv.config;

import com.github.kahalemakai.opencsv.beans.CsvToBeanMapper;
import com.github.kahalemakai.opencsv.beans.processing.Decoder;
import com.github.kahalemakai.opencsv.beans.processing.PostProcessor;
import com.github.kahalemakai.opencsv.beans.processing.PostValidator;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.w3c.dom.Node.ELEMENT_NODE;

@Log4j
public class ConfigParser {
    public static final String STRICT_QUOTES = "strict";
    public static final String NONSTRICT_QUOTES = "non-strict";
    public static final String IGNORE_QUOTES = "ignore";
    public static final String CSV_COLUMN = "csv:column";
    public static final String CSV_IGNORE = "csv:ignore";
    public static final String BEAN_DECODER = "bean:decoder";
    public static final String BEAN_POSTPROCESSOR = "bean:postProcessor";
    public static final String BEAN_POSTVALIDATOR = "bean:postValidator";
    public static final String DEFAULT_NAME_SPACE = "com.github.kahalemakai.opencsv.beans.processing";

    private final File xmlFile;
    private final Reader reader;
    private final Iterable<String> unparsedLines;
    private final Iterable<String[]> parsedLines;

    private ConfigParser(final File xmfFile,
                         final Iterable<String> unparsedLines,
                         final Iterable<String[]> parsedLines,
                         final Reader reader) {
        this.xmlFile = xmfFile;
        this.reader = reader;
        this.parsedLines = parsedLines;
        this.unparsedLines = unparsedLines;
    }

    private Optional<String> getValue(final Node node, final String attribute) {
        final Node item = node.getAttributes().getNamedItem(attribute);
        if (item == null)
            return Optional.empty();

        final String value = item.getNodeValue();
        if (value == null)
            return Optional.empty();
        else
            return Optional.of(value);
    }

    public <T> CsvToBeanMapper<T> parse()
            throws ParserConfigurationException, IOException, SAXException, InstantiationException, ClassNotFoundException {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        final Document doc = documentBuilder.parse(this.xmlFile);
        doc.getDocumentElement().normalize();
        final Node reader = doc.getElementsByTagName("csv:reader").item(0);
        final Optional<String> separator = getValue(reader, "separator");
        final Optional<String> skipLines = getValue(reader, "skipLines");

        /*********************
         * get the attributes
         *********************/

        final Optional<String> quoteChar = getValue(reader, "quoteChar");
        final Optional<String> ignoreLeadingWhiteSpace = getValue(reader, "ignoreLeadingWhiteSpace");
        final Optional<String> onErrorSkipLine = getValue(reader, "onErrorSkipLine");
        final Optional<String> quotingBehaviour = getValue(reader, "quotingBehaviour");

        final Node config = doc.getElementsByTagName("bean:config").item(0);
        final Optional<String> className = getValue(config, "class");
//        final Optional<String> data = getValue(config, "data");
//        final Optional<String> value = getValue(config, "value");
//        final Optional<String> valueType = getValue(config, "valueType");

        /***********************
         * get the sub-elements
         ***********************/
        Class<? extends T> type = null;
        try {
            type = (Class<? extends T>) Class.forName(className.get());
        } catch (ClassNotFoundException e) {
            log.error(e);
            throw new IllegalStateException(e);
        }
        final com.github.kahalemakai.opencsv.beans.Builder<T> builder = CsvToBeanMapper.builder(type);

        // set the input sourc
        if (this.reader != null) {
            builder.withReader(this.reader);
        }
        if (this.parsedLines != null) {
            builder.withParsedLines(this.parsedLines);
        }
        else {
            builder.withLines(this.unparsedLines);
        }

        final String[] header = getHeader(reader);
        builder.setHeader(header);

        if (quoteChar.isPresent()) builder.quoteChar(quoteChar.get().charAt(0));
        if (separator.isPresent()) builder.separator(separator.get().charAt(0));
        if (ignoreLeadingWhiteSpace.isPresent()) {
            final boolean b = Boolean.parseBoolean(ignoreLeadingWhiteSpace.get());
            if (!b) builder.dontIgnoreLeadingWhiteSpace();
        }
        if (onErrorSkipLine.isPresent()) {
            final boolean b = Boolean.parseBoolean(onErrorSkipLine.get());
            if (b) builder.onErrorSkipLine();
        }
        if (quotingBehaviour.isPresent()) {
            switch (quotingBehaviour.get()) {
                case STRICT_QUOTES:
                    builder.strictQuotes();
                    break;
                case IGNORE_QUOTES:
                    builder.ignoreQuotes();
                    break;
                default:
                    builder.nonStrictQuotes();
            }
        }
        if (skipLines.isPresent()) {
            final int i = Integer.parseInt(skipLines.get());
            builder.skipLines(i);
        }
        configureFields(config, builder);
        return builder.build();
    }
    
    private <T, R> void configureFields(final Node config, com.github.kahalemakai.opencsv.beans.Builder<T> builder) throws ClassNotFoundException, InstantiationException {
        final NodeList fields = config.getChildNodes();
        final Class<? extends T> builderType = builder.getStrategy().getType();
        for (int i = 0; i < fields.getLength(); ++i) {
            final Node field = fields.item(i);
            if (field.getNodeType() != ELEMENT_NODE)
                continue;
            final String name = getValue(field, "name").get();
            final NodeList processors = field.getChildNodes();
            for (int j = 0; j < processors.getLength(); ++j) {
                final Node processor = processors.item(j);
                if (processor.getNodeType() == ELEMENT_NODE) {
                    final String processorNodeName = processor.getNodeName();
                    final String type = getValue(processor, "type").get();
                    switch (processorNodeName) {
                        case BEAN_DECODER:
                            final Class<? extends Decoder<R, ? extends Throwable>> decoderClass = getProcessorClass(type, BEAN_DECODER);
                            builder.registerDecoder(name, decoderClass);
                            break;
                        case BEAN_POSTPROCESSOR:
                            final Class<? extends PostProcessor<R>> postProcessorClass = getProcessorClass(type, BEAN_POSTPROCESSOR);
                            builder.registerPostProcessor(name, postProcessorClass);
                            break;
                        case BEAN_POSTVALIDATOR:
                            final Class<? extends PostValidator<R>> postValidatorClass = getProcessorClass(type, BEAN_POSTVALIDATOR);
                            builder.registerPostValidator(name, postValidatorClass);
                            break;
                    }
                }
            }
        }
    }

    private <T> Class<T> getProcessorClass(final String type, final String branch) throws ClassNotFoundException {
        String subPackage = null;
        switch (branch) {
            case BEAN_DECODER:
                subPackage = "decoders";
                break;
            case BEAN_POSTPROCESSOR:
                subPackage = "postprocessors";
                break;
            case BEAN_POSTVALIDATOR:
                subPackage = "postvalidators";
                break;
        }
        Class<T> processorClass = null;
        try {
            processorClass = (Class<T>) Class.forName(type);
        } catch (ClassNotFoundException e) {
            final String qName = String.format("%s.%s.%s", DEFAULT_NAME_SPACE, subPackage, type);
            try {
                processorClass = (Class<T>) Class.forName(qName);
            } catch (ClassNotFoundException e1) {
                final String msg = String.format("neither found class '%s', nor '%s'", type, qName);
                log.error(msg, e);
                throw new ClassNotFoundException(msg, e);
            }
        }
        return processorClass;
    }

    private String[] getHeader(final Node reader) {
        final NodeList csvFields = reader.getChildNodes();
        final List<String> fieldList = new LinkedList<>();

        for (int i = 0; i < csvFields.getLength(); ++i) {
            final Node item = csvFields.item(i);
            if (item.getNodeType() == ELEMENT_NODE) {
                switch (item.getNodeName()) {
                    case CSV_COLUMN:
                        fieldList.add(getValue(item, "name").get());
                        break;
                    case CSV_IGNORE:
                        final String count = getValue(item, "count").orElse("1");
                        fieldList.add(String.format("$ignore%s$", count));
                        break;
                }
            }
        }
        return fieldList.toArray(new String[fieldList.size()]);
    }

    public static ConfigParser ofUnparsedLines(@NonNull final File xmlFile,
                                               @NonNull final Iterable<String> unparsedLines)
            throws IOException, SAXException {
        ValidationService.validate(xmlFile);
        return new ConfigParser(xmlFile, unparsedLines, null, null);
    }

    public static ConfigParser ofParsedLines(@NonNull final File xmlFile,
                                             @NonNull final Iterable<String[]> parsedLines)
            throws IOException, SAXException {
        ValidationService.validate(xmlFile);
        return new ConfigParser(xmlFile, null, parsedLines, null);
    }

    public static ConfigParser ofReader(@NonNull final File xmlFile,
                                        @NonNull final Reader reader)
            throws IOException, SAXException {
        ValidationService.validate(xmlFile);
        return new ConfigParser(xmlFile, null, null, reader);
    }


}
