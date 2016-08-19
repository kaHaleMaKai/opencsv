/*
 * Copyright 2016, Lars Winderling
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.github.kahalemakai.opencsv.config;

import com.github.kahalemakai.opencsv.beans.CsvToBeanMapper;
import com.github.kahalemakai.opencsv.beans.NullFallsThroughType;
import com.github.kahalemakai.opencsv.beans.QuotingMode;
import com.github.kahalemakai.opencsv.beans.processing.Decoder;
import com.github.kahalemakai.opencsv.beans.processing.PostProcessor;
import com.github.kahalemakai.opencsv.beans.processing.PostValidator;
import com.github.kahalemakai.opencsv.beans.processing.decoders.EnumDecoder;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.w3c.dom.Node.ELEMENT_NODE;

@Log4j
public class ConfigParser {
    public static final String STRICT_QUOTES = "strict";
    public static final String CSV_COLUMN = "csv:column";
    public static final String CSV_IGNORE = "csv:ignore";
    public static final String BEAN_DECODER = "bean:decoder";
    public static final String BEAN_ENUM = "bean:enum";
    public static final String BEAN_ENUM_MAP = "bean:map";
    public static final String BEAN_POSTPROCESSOR = "bean:postProcessor";
    public static final String BEAN_POSTVALIDATOR = "bean:postValidator";
    public static final String DEFAULT_NAME_SPACE = "com.github.kahalemakai.opencsv.beans.processing";

    private final InputStream xmlInputStream;
    private final Reader reader;
    private final Iterable<String> unparsedLines;
    private final Iterable<String[]> parsedLines;
    private final InputStream inputStream;

    public ConfigParser(InputStream xmlInputStream, Reader reader, Iterable<String> unparsedLines, Iterable<String[]> parsedLines, InputStream inputStream) {
        this.xmlInputStream = xmlInputStream;
        this.reader = reader;
        this.unparsedLines = unparsedLines;
        this.parsedLines = parsedLines;
        this.inputStream = inputStream;
    }

    public ConfigParser(File xmlFile, Reader reader, Iterable<String> unparsedLines, Iterable<String[]> parsedLines, InputStream inputStream) throws FileNotFoundException {
        this(new FileInputStream(xmlFile), reader, unparsedLines, parsedLines, inputStream);
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

    private Schema getOpencsvSchema() throws SAXException {
        final URL schemaUrl = getClass()
                .getResource("/schemas/opencsv.xsd");
        assert schemaUrl != null;
        SchemaFactory schemaFactory = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            return schemaFactory.newSchema(schemaUrl);
        } catch (SAXException e) {
            throw new SAXException(e);
        }
    }

    public <T> CsvToBeanMapper<T> parse()
            throws ParserConfigurationException, IOException, SAXException, InstantiationException, ClassNotFoundException {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        // don't set validating behaviour to true -> or else a DTD is expected
        documentBuilderFactory.setSchema(getOpencsvSchema());
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        final Document doc = documentBuilder.parse(this.xmlInputStream);
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
        final Optional<String> charset = getValue(reader, "charset");

        final Node config = doc.getElementsByTagName("bean:config").item(0);
        final Optional<String> className = getValue(config, "class");
//        final Optional<String> data = getValue(config, "data");
//        final Optional<String> value = getValue(config, "value");
//        final Optional<String> valueType = getValue(config, "valueType");

        /***********************
         * get the sub-elements
         ***********************/

        Class<? extends T> type;
        try {
            type = (Class<? extends T>) Class.forName(className.get());
        } catch (ClassNotFoundException e) {
            log.error(e);
            throw new IllegalStateException(e);
        }
        final com.github.kahalemakai.opencsv.beans.Builder<T> builder = CsvToBeanMapper.builder(type);

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
            final QuotingMode correspondingMode = QuotingMode.forText(quotingBehaviour.get());
            builder.quotingMode(correspondingMode);
        }
        if (skipLines.isPresent()) {
            final int i = Integer.parseInt(skipLines.get());
            builder.skipLines(i);
        }
        if (charset.isPresent()) builder.charset(Charset.forName(charset.get()));

        final String[] header = getHeader(reader);
        builder.setHeader(header);

        // set the input source
        if (this.reader != null) {
            builder.withReader(this.reader);
        }
        else if (this.parsedLines != null) {
            builder.withParsedLines(this.parsedLines);
        }
        else if (this.unparsedLines != null) {
            builder.withLines(this.unparsedLines);
        }
        else if (this.inputStream != null) {
            builder.withInputStream(this.inputStream);
        }
        else {
            final String msg = "input source must be one of [Reader, Iterable<String>, Iterable<String[]>, InputStream]";
            log.error(msg);
            throw new IllegalStateException(msg);
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
            final Optional<String> nullFallsThrough = getValue(field, "nullFallsThrough");
            if (nullFallsThrough.isPresent()) {
                switch (NullFallsThroughType.forText(nullFallsThrough.get())) {
                    case BOTH:
                        builder.setNullFallthroughForPostProcessors(name);
                        builder.setNullFallthroughForPostValidators(name);
                        break;
                    case POST_PROCESSOR:
                        builder.setNullFallthroughForPostProcessors(name);
                        break;
                    case POST_VALIDATOR:
                        builder.setNullFallthroughForPostValidators(name);
                        break;
                }
            }
            final NodeList processors = field.getChildNodes();
            for (int j = 0; j < processors.getLength(); ++j) {
                final Node processor = processors.item(j);
                if (processor.getNodeType() == ELEMENT_NODE) {
                    final String processorNodeName = processor.getNodeName();
                    final String type = getValue(processor, "type").get();
                    switch (processorNodeName) {
                        case BEAN_DECODER:
                            final Class<? extends Decoder<R>> decoderClass = getProcessorClass(type, BEAN_DECODER);
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
                        case BEAN_ENUM:
                            final EnumDecoder<?> enumDecoder = getEnumDecoder(processor);
                            builder.registerDecoder(name, enumDecoder);
                            break;
                    }
                }
            }
        }
    }

    private <E extends Enum<E>> EnumDecoder<E> getEnumDecoder(final Node processor) throws ClassNotFoundException {
        final NodeList maps = processor.getChildNodes();
        final String typeName = getValue(processor, "type").get();
        final Class<? extends E> enumClass = (Class<? extends E>) Class.forName(typeName);
        final EnumDecoder<E> decoder = new EnumDecoder<>();
        decoder.setType(enumClass);
        for (int i = 0; i < maps.getLength(); ++i) {
            final Node node = maps.item(i);
            if (node.getNodeType() == ELEMENT_NODE && BEAN_ENUM_MAP.equals(node.getNodeName())) {
                final String key = getValue(node, "key").get();
                final String value = getValue(node, "value").get();
                decoder.put(key, value);
            }
        }
        return decoder;
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

    public static ConfigParser ofUnparsedLines(@NonNull final InputStream xmlInputStream,
                                               @NonNull final Iterable<String> unparsedLines)
            throws IOException, SAXException {
        return new ConfigParser(xmlInputStream, null, unparsedLines, null, null);
    }

    public static ConfigParser ofUnparsedLines(@NonNull final File xmlFile,
                                               @NonNull final Iterable<String> unparsedLines)
            throws IOException, SAXException {
        return new ConfigParser(xmlFile, null, unparsedLines, null, null);
    }

    public static ConfigParser ofParsedLines(@NonNull final InputStream xmlInputStream,
                                             @NonNull final Iterable<String[]> parsedLines)
            throws IOException, SAXException {
        return new ConfigParser(xmlInputStream, null, null, parsedLines, null);
    }

    public static ConfigParser ofParsedLines(@NonNull final File xmlFile,
                                             @NonNull final Iterable<String[]> parsedLines)
            throws IOException, SAXException {
        return new ConfigParser(xmlFile, null, null, parsedLines, null);
    }

    public static ConfigParser ofReader(@NonNull final InputStream xmlInputStream,
                                        @NonNull final Reader reader)
            throws IOException, SAXException {
        return new ConfigParser(xmlInputStream, reader, null, null, null);
    }

    public static ConfigParser ofReader(@NonNull final File xmlFile,
                                        @NonNull final Reader reader)
            throws IOException, SAXException {
        return new ConfigParser(xmlFile, reader, null, null, null);
    }

    public static ConfigParser ofInputStream(@NonNull final InputStream xmlInputStream,
                                             @NonNull final InputStream inputStream)
            throws IOException, SAXException {
        return new ConfigParser(xmlInputStream, null, null, null, inputStream);
    }

    public static ConfigParser ofInputStream(@NonNull final File xmlFile,
                                             @NonNull final InputStream inputStream)
            throws IOException, SAXException {
        return new ConfigParser(xmlFile, null, null, null, inputStream);
    }

    public static ConfigParser ofFile(@NonNull final InputStream xmlInputStream,
                                      @NonNull final File inputFile)
            throws IOException, SAXException {
        return new ConfigParser(xmlInputStream, null, null, null, new FileInputStream(inputFile));
    }

    public static ConfigParser ofFile(@NonNull final File xmlFile,
                                      @NonNull final File inputFile)
            throws IOException, SAXException {
        return new ConfigParser(xmlFile, null, null, null, new FileInputStream(inputFile));
    }

}
