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

import com.github.kahalemakai.opencsv.beans.Builder;
import com.github.kahalemakai.opencsv.beans.CsvToBeanMapper;
import com.github.kahalemakai.opencsv.beans.QuotingMode;
import com.github.kahalemakai.opencsv.beans.processing.Decoder;
import com.github.kahalemakai.opencsv.beans.processing.PostProcessor;
import com.github.kahalemakai.opencsv.beans.processing.PostValidator;
import com.github.kahalemakai.opencsv.beans.processing.ResultWrapper;
import com.github.kahalemakai.opencsv.beans.processing.decoders.EnumDecoder;
import com.github.kahalemakai.opencsv.beans.processing.decoders.NullDecoder;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

import static org.w3c.dom.Node.ELEMENT_NODE;

/**
 * Build a {@link CsvToBeanMapper} from an xml config file.
 * <p>
 * This class essentially maps all configuration possibilities
 * of the {@link Builder} class to an xml. It consists of two
 * basic blocks, the {@code <csv:reader>} and the
 * {@code <bean:config>} tag. The csv parsing can be configured
 * via the former tag, whereas the latter configures the
 * mapping to a bean including decoding, post-processing
 * and -validation.
 * <p>
 * For a most detailed overview of the xml configuration,
 * please refer to the javadoc of the individual methods in this
 * class, to the xsd schemas under src/main/resources/schemas/,
 * to the README.md file or to the overview page of
 * com/github/kahalemakai/opencsv/config.
 */
@Log4j
public class ConfigParser {

    /**
     * Xml namespace of opencsv root element.
     */
    public static final String OPENCSV_NAMESPACE = "http://github.com/kaHaleMaKai/opencsv";

    /**
     * Xml namespace of csv reader config.
     */
    public static final String CSV_NAMESPACE = "http://github.com/kaHaleMaKai/opencsv/csv";

    /**
     * Xml namespace of bean mapper config.
     */
    public static final String BEAN_NAMESPACE = "http://github.com/kaHaleMaKai/opencsv/bean";

    /**
     * Xml namespace of sink config.
     */
    public static final String SINK_NAMESPACE = "http://github.com/kaHaleMaKai/opencsv/sink";

    /**
     * The default null string.
     */
    public static final String DEFAULT_NULL_STRING = "null";

    /**
     * The default global null string.
     */
    public String globalNullString = DEFAULT_NULL_STRING;

    /**
     * By default don't enforce trimming globally.
     */
    public String globalTrimmingMode = "false";

    /**
     * The namespaced column element name.
     */
    public static final String CSV_COLUMN = "column";

    /**
     * The ignore column element name.
     */
    public static final String CSV_IGNORE = "ignore";

    /**
     * The namespaced decoder element name.
     */
    public static final String BEAN_DECODER = "decoder";

    /**
     * The namespaced enum element name.
     */
    public static final String BEAN_ENUM = "enum";

    /**
     * The namespaced enum map element name.
     */
    public static final String BEAN_ENUM_MAP = "map";

    /**
     * The namespaced post processor element name.
     */
    public static final String BEAN_POSTPROCESSOR = "postProcessor";

    /**
     * The namespaced post validator element name.
     */
    public static final String BEAN_POSTVALIDATOR = "postValidator";

    /**
     * The default processing package.
     */
    public static final String DEFAULT_PROCESSING_PACKAGE = "com.github.kahalemakai.opencsv.beans.processing";

    /**
     * List of all registered plugins.
     */
    private static final ServiceLoader<SinkPlugin> registeredSinkPlugins = ServiceLoader.load(SinkPlugin.class);
    private final List<SinkPlugin> sinkPlugins;

    // variables passed to the Builder instance
    private final Optional<File> xmlFile;
    private final Optional<byte[]> xmlFileAsArray;
    private final Reader reader;
    private final Iterable<String> unparsedLines;
    private final Iterable<String[]> parsedLines;
    private final InputStream inputStream;
    private final ParameterMap parameters;

    /**
     * Setup the {@code ConfigParser} with the state defined in the calling class.
     * @param xmlInputStream stream of xml config file
     * @param reader a {@code Reader} instance, or {@code null}
     * @param unparsedLines {@code Iterable} of unparsed lines, or {@code null}
     * @param parsedLines {@code Iterable} of parsed lines, or {@code null}
     * @param inputStream input stream of unparsed lines, or {@code null}
     */
    private ConfigParser(@NonNull final InputStream xmlInputStream,
                         final Reader reader,
                         final Iterable<String> unparsedLines,
                         final Iterable<String[]> parsedLines,
                         final InputStream inputStream) throws IOException {
        this.xmlFileAsArray = Optional.ofNullable(asByteArray(xmlInputStream));
        this.xmlFile = Optional.empty();
        this.reader = reader;
        this.unparsedLines = unparsedLines;
        this.parsedLines = parsedLines;
        this.inputStream = inputStream;
        this.parameters = ParameterMap.init();
        try {
            this.sinkPlugins = getSinkPlugins();
        } catch (IllegalAccessException | InstantiationException e) {
            final String msg = "could not setup sink plugins";
            log.error(msg, e);
            // FIXME: you specific exception and think about exception usage
            // in ConfigParser in general
            throw new RuntimeException(msg, e);
        }
    }

    /**
     * Setup the {@code ConfigParser} with the state defined in the calling class.
     * @param xmlFile {@code File} instance of xml config
     * @param reader a {@code Reader} instance, or {@code null}
     * @param unparsedLines {@code Iterable} of unparsed lines, or {@code null}
     * @param parsedLines {@code Iterable} of parsed lines, or {@code null}
     * @param inputStream input stream of unparsed lines, or {@code null}
     */
    private ConfigParser(@NonNull final File xmlFile,
                         final Reader reader,
                         final Iterable<String> unparsedLines,
                         final Iterable<String[]> parsedLines,
                         final InputStream inputStream)
            throws FileNotFoundException {
        this.xmlFileAsArray = Optional.empty();
        this.xmlFile = Optional.of(xmlFile);
        this.reader = reader;
        this.unparsedLines = unparsedLines;
        this.parsedLines = parsedLines;
        this.inputStream = inputStream;
        this.parameters = ParameterMap.init();
        try {
            this.sinkPlugins = getSinkPlugins();
        } catch (IllegalAccessException | InstantiationException e) {
            final String msg = "could not setup sink plugins";
            log.error(msg, e);
            // FIXME: you specific exception and think about exception usage
            // in ConfigParser in general
            throw new RuntimeException(msg, e);
        }
    }

    /**
     * Inject a parameter into the xml parameter table.
     * <p>
     * The {@code ConfigParser} or its extensions can allow
     * for substitution of String values into the xml configuration.
     * A parameter has to be injected by this method prior to
     * reading the xml config file, if used there.
     * @param name a namespaced parameter name (format ns:name)
     * @param value associated value
     * @return the {@code ConfigParser} instance
     * @throws IllegalStateException if the provided {@code name} is not allowed, or a parameter with the same name has already been registered
     */
    public ConfigParser injectParameter(final String name, final String value) throws IllegalStateException {
        parameters.put(name, value);
        return this;
    }

    /**
     * Lookup the string value associated with a named config parameter.
     * @param name the parameter to lookup
     * @return the associated value
     * @throws NoSuchElementException if no mapping for this parameter has been defined
     */
    private String resolveParameter(final String name) throws NoSuchElementException {
        final Optional<String> value = parameters.get(name);
        if (!value.isPresent()) {
            final String msg = String.format("parameter '%s' has not been defined", name);
            log.error(msg);
            throw new NoSuchElementException(msg);
        }
        return value.get();
    }


    /**
     * Inject registered parameters into the xml document.
     * <p>
     * A parameter pattern is defined as the sequence
     * <code>$&#123;</code>{@code [a-z][a-z0-9_]*:[a-z][a-z0-9_]*}<code></code>&#125;</code>. If it is
     * found anywhere inside an xml document (wellformedness of xml limits possible locations to
     * attribute values), it will be replaced by the registered parameter value.
     * <p>
     * FIXME: care for different character encodings
     * @param xmlInputStream the xml config file as an {@link InputStream}
     * @return a byte array with parameters substituted by the corresponding values
     * @throws NoSuchElementException if an unregistered parameter is encountered
     * @throws IllegalStateException if a parameter pattern is not correctly closed
     * @throws IOException if en error happens while reading the xml config file
     */
    private byte[] withParameters(final InputStream xmlInputStream)
            throws NoSuchElementException, IllegalStateException, IOException {
        final InputStreamReader reader = new InputStreamReader(xmlInputStream);
        final ByteArrayOutputStream sink = new ByteArrayOutputStream();
        final OutputStreamWriter writer = new OutputStreamWriter(sink);
        boolean dollarFound = false,
                paramFound = false;
        int paramLength = 0,
            currentColumn = 0,
            currentLine = 1,
            nextChar,
            parameterSequenceStartCol = 0;
        final char[] paramBuffer = new char[1024];
        char c;
        final char dollar = '$';
        final char opening = '{';
        final char closing = '}';
        final char eol = '\n';

        while ((nextChar = reader.read()) != -1) {
            c = (char) nextChar;

            // for pretty printing
            if (c == eol) {
                currentColumn = 0;
                currentLine++;
            }
            currentColumn++;

            if (!dollarFound) { // are still in regular text mode
                if (c == dollar) {
                    dollarFound = true; // expect a parameter pattern: ${...},
                    continue;           // continue with the next char
                }
                // else it's just a regular char; write it out
                writer.write(nextChar);
            }
            else { // expect a parameter pattern
                if (!paramFound) { // which has not been found yet
                    if (c == opening) { // but here it comes
                        paramFound = true;
                        parameterSequenceStartCol = currentColumn - 2;
                        continue; // and step to the next char
                    }
                    // previous dollar was just a regular token,
                    // just write it out
                    writer.write(dollar);

                    // if current char were a dollar, a parameter pattern
                    // should again be expected (dollarFound stays true)
                    if (c != dollar) { // but if it's not a dollar,
                        dollarFound = false; // go back to normal text mode
                        writer.write(c); // and write the current character out
                    }
                }
                else { // parameter found
                    if (c != closing) { // still not at the end of the pattern
                        if (c == ':'
                                || c == '_'
                                || (nextChar >= '0' && nextChar <= '9')
                                || (nextChar >= 'A' && nextChar <= 'Z')
                                || (nextChar >= 'a' && nextChar <= 'z')) {
                            paramBuffer[paramLength] = c; // and construct the parameter name
                            paramLength++; // and memorize the length of the parameter name
                        }
                        else {
                            final String msg = new StringBuilder()
                                    .append("found illegal parameter substitution sequence '${")
                                    .append(paramBuffer, 0, paramLength)
                                    .append(c)
                                    .append("' at line: ")
                                    .append(currentLine)
                                    .append(", column: ")
                                    .append(parameterSequenceStartCol)
                                    .toString();
                            log.error(msg);
                            throw new IllegalStateException(msg);
                        }
                    }
                    else {
                        // end of the parameter pattern reached
                        // query the global parameter map for the corresponding value
                        // if no param has been injected, this will throw
                        final String paramName = new String(paramBuffer, 0, paramLength);
                        final String paramValue = resolveParameter(paramName);
                        writer.write(paramValue); // write the value instead of the parameter pattern
                        paramLength = 0; // reset the memorized parameter length
                        paramFound = false; // and reset the book-keeping flags
                        dollarFound = false;
                        parameterSequenceStartCol = 0;
                    }
                }
            }
        }
        // no need to check for un-closed parameter patterns at the end
        // xml is well-formed, so every attribute (only place for ${...} patterns)
        // will be closed until the eof and thus no further checking is required
        writer.flush(); // remaining chars get written to the ByteArrayOutputStream
        return sink.toByteArray();
    }

    /**
     * Get a {@code Schema} instance corresponding to the
     * opencsv.xsd schema file.
     * @return the corresponding schema
     * @throws SAXException if the schema file cannot be parsed
     */
    private Schema getSchema() throws SAXException, FileNotFoundException {
        final URL schemaUrl = getClass()
                .getResource("/schemas/opencsv.xsd");
        assert schemaUrl != null;
        SchemaFactory schemaFactory = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        final StreamSource opencsvSchemaSource = new StreamSource(new File(schemaUrl.getFile()));
        opencsvSchemaSource.setSystemId(schemaUrl.toExternalForm());
        final int numSchemas = sinkPlugins.size() + 1;
        final List<StreamSource> schemas = sinkPlugins
                .stream()
                .map((sinkPlugin) -> {
                    final URL url = sinkPlugin.getSchemaUrl();
                    final StreamSource source = new StreamSource(new File(url.getFile()));
                    source.setSystemId(url.toExternalForm());
                    return source;
                })
                .collect(Collectors.toList());
        schemas.add(0, opencsvSchemaSource);
        return schemaFactory.newSchema(schemas.toArray(new Source[numSchemas]));
    }

    /**
     * Parse the xml config file.
     * <p>
     * The config is validated against the opencsv.xsd schema file.
     * @param <T> type of desired output bean
     * @return a correctly configured {@code CsvToBeanMapper} instance
     * @throws ParserConfigurationException if the parser configuration is skewed
     * @throws IOException if the xml config cannot be read
     * @throws SAXException if the xsd schema or the xml config file cannot be parsed
     * @throws InstantiationException if the DOM builder or the decoders/processors/validators cannot be instantiated
     * @throws ClassNotFoundException if the bean output class defined in the config file cannot be found
     * @throws IllegalAccessException if instance creation of decoder/processor/validator is forbidden
     */
    public <T> CsvToBeanMapper<T> parse()
            throws ParserConfigurationException, IOException, SAXException, InstantiationException, ClassNotFoundException, IllegalAccessException {

        // check wellformedness
        final boolean wellFormed = isWellFormed(getXmlInputStream());
        if (!wellFormed) {
            //FIXME
            throw new RuntimeException("not well-formed");
        }
        final byte[] bytes = withParameters(getXmlInputStream());
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        // don't set validating behaviour to true -> or else a DTD is expected
        documentBuilderFactory.setSchema(this.getSchema());
        documentBuilderFactory.setNamespaceAware(true);
        final Schema schema = documentBuilderFactory.getSchema();
        final Validator validator = schema.newValidator();
        validator.validate(new StreamSource(new ByteArrayInputStream(bytes)));
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        final Document doc = documentBuilder.parse(new ByteArrayInputStream(bytes));
        doc.getDocumentElement().normalize();
        final Node reader = doc.getElementsByTagNameNS(CSV_NAMESPACE, "reader").item(0);
        final Optional<String> separator = getAttributeValue(reader, "separator");
        final Optional<String> skipLines = getAttributeValue(reader, "skipLines");

        /* ********************
         * get the attributes
         * ********************/

        final Optional<String> quoteChar = getAttributeValue(reader, "quoteChar");
        final Optional<String> escapeChar = getAttributeValue(reader, "escapeChar");
        final Optional<String> multiLine = getAttributeValue(reader, "mutliLine");
        final Optional<String> ignoreLeadingWhiteSpace = getAttributeValue(reader, "ignoreLeadingWhiteSpace");
        final Optional<String> onErrorSkipLine = getAttributeValue(reader, "onErrorSkipLine");
        final Optional<String> quotingBehaviour = getAttributeValue(reader, "quotingBehaviour");
        final Optional<String> charset = getAttributeValue(reader, "charset");

        final Node config = doc.getElementsByTagNameNS(BEAN_NAMESPACE, "config").item(0);
        final Optional<String> className = getAttributeValue(config, "class");
        final Optional<String> nullString = getAttributeValue(config, "nullString");
        this.globalNullString = nullString.orElse(DEFAULT_NULL_STRING);
        final Optional<String> globalTrimming = getAttributeValue(config, "trim");
        if (globalTrimming.isPresent()) {
            this.globalTrimmingMode = globalTrimming.get();
        }

        /* **********************
         * get the sub-elements
         ************************/

        Class<? extends T> type;
        try {
            type = (Class<? extends T>) Class.forName(className.get());
        } catch (ClassNotFoundException e) {
            log.error(e);
            throw new IllegalStateException(e);
        }
        final Builder<T> builder = CsvToBeanMapper.builder(type);

        if (quoteChar.isPresent()) builder.quoteChar(quoteChar.get().charAt(0));
        if (escapeChar.isPresent()) builder.escapeChar(escapeChar.get().charAt(0));
        if (multiLine.isPresent()) builder.multiLine(Boolean.valueOf(multiLine.get()));
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
        configureSinkIfRequired(builder, doc);

        return builder.build();
    }

    /**
     * Configure a {@code <sink:configure>} tag.
     * <p>
     * A plugin {@link Plugin#configure(Builder, Document)} method
     * is only invoked, if a corresponding tag is found. At most, one
     * sink plugin will be configured and the remaining registered
     * ones will be silently ignored.
     * <p>
     * The {@link ConfigParser} does currently not check if a
     * tag cannot be mapped to any plugin.
     * @param builder the {@link Builder} instance to be configured
     * @param doc the {@link Document} root of the xml configuration file
     * @param <T> target bean type for the {@link Builder}
     */
    private <T> void configureSinkIfRequired(final Builder<T> builder, final Document doc) {
        for (SinkPlugin plugin : sinkPlugins) {
            try {
                plugin.configure(builder, doc);
                return;
            } catch (PluginConfigurationException ignored) {}
        }
    }

    /**
     * Configure the {@code bean:field} xml objects.
     * @param config the parent {@code bean:config} node
     * @param builder the {@code Builder} instance
     * @param <T> type of bean to be eventually emitted
     * @throws ClassNotFoundException if class of decoders/... cannot be found
     * @throws InstantiationException if decoder/... cannot be instantiated, or column ref data cannot be decoded
     * @throws IllegalAccessException if decoder/... default constructor is inaccessible
     */
    private <T> void configureFields(final Node config, Builder<T> builder) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        final NodeList fields = config.getChildNodes();
//        final Class<? extends T> builderType = builder.getStrategy().getType();
        for (int i = 0; i < fields.getLength(); ++i) {
            final Node field = fields.item(i);
            final String fieldNs = field.getNamespaceURI();
            if (field.getNodeType() != ELEMENT_NODE)
                continue;
            // presence of "name" attribute is enforced by xsd
            final String column = getAttributeValue(field, "name").get();
            final Optional<String> nullFallsThrough = getAttributeValue(field, "nullFallsThrough");
            if (nullFallsThrough.isPresent()) {
                switch (NullFallsThroughType.forText(nullFallsThrough.get())) {
                    case BOTH:
                        builder.setNullFallthroughForPostProcessors(column);
                        builder.setNullFallthroughForPostValidators(column);
                        break;
                    case POST_PROCESSOR:
                        builder.setNullFallthroughForPostProcessors(column);
                        break;
                    case POST_VALIDATOR:
                        builder.setNullFallthroughForPostValidators(column);
                        break;
                }
            }
            final Optional<String> trim = getAttributeValue(field, "trim");
            final boolean doTrim = Boolean.valueOf(trim.orElse(globalTrimmingMode));
            builder.trim(column, doTrim);

            final Optional<String> nullable = getAttributeValue(field, "nullable");
            final Optional<String> maybeNullString = getAttributeValue(field, "nullString");
            final boolean isNullable = Boolean.valueOf(nullable.orElse("false"))
                    || maybeNullString.isPresent();
            if (isNullable) {
                final String nullString = maybeNullString.orElse(this.globalNullString);
                builder.registerDecoder(
                       column,
                        () -> new NullDecoder(nullString),
                        String.format("%s#%s", NullDecoder.class.getCanonicalName(), nullString));
            }
            final Optional<String> type = getAttributeValue(field, "type");
            if (type.isPresent() && !type.get().equals("String")) {
                defineType(builder, column, fieldNs, type.get());
            }
            final Optional<String> ref = getAttributeValue(field, "ref");
            if (ref.isPresent()) {
                final String refType = ref.get();
                final Optional<String> refData = getAttributeValue(field, "refData");
                if (refData.isPresent()) {
                    String data = refData.get();
                    switch (refType) {
                        case "column":
                            builder.setColumnRef(data, column);
                            break;
                        case "value":
                            Object decodedRefData = data;
                            if (type.isPresent() && !type.get().equals("String")) {
                                final String decoderType = String.format("%s%sDecoder",
                                        type.get().substring(0, 1).toUpperCase(), type.get().substring(1));
                                final Class<? extends Decoder<?>> decoderClass =
                                        getProcessorClass(decoderType, fieldNs, BEAN_DECODER);
                                final Decoder<?> decoder = decoderClass.newInstance();
                                final ResultWrapper<?> wrapper = decoder.decode(data);
                                if (wrapper.success()) {
                                    decodedRefData = wrapper.get();
                                }
                                else {
                                    final String msg = String.format("could not decode refData '%s' as type %s",
                                            data, type);
                                    log.error(msg);
                                    throw new InstantiationError(msg);
                                }
                            }
                            builder.setColumnValue(column, decodedRefData);
                            break;
                    }
                }

            }

            boolean anyDecoder = false;
            final NodeList processors = field.getChildNodes();
            for (int j = 0; j < processors.getLength(); ++j) {
                final Node processor = processors.item(j);
                if (processor.getNodeType() == ELEMENT_NODE) {
                    registerProcessor(column, builder, processor);
                    if (processor.getLocalName().equals(BEAN_DECODER)) {
                        anyDecoder = true;
                    }
                }
            }
            // if isNullable, then a null decoder has been registered
            // but the null decoder only decodes null-valued Strings to null.
            // if no additional decoder has been registered, we have to assume the
            // target type to be string and thus add the identity decoder t -> t
            if (isNullable && !anyDecoder) {
                builder.registerDecoder(column, Decoder.IDENTITY);
            }
        }
    }

    /**
     * Map a primitive type name to the corresponding decoder.
     * @param builder the {@code Builder} instance
     * @param columnName name of bean field to be configured
     * @param type name of primitive type
     * @param <T> type of bean to be eventually emitted
     * @throws ClassNotFoundException if the decoder class cannot be found
     * @throws InstantiationException if the decoder cannot be instantiated
     */
    private <T> void defineType(final Builder<T> builder,
                                final String columnName,
                                final String ns,
                                final String type) throws ClassNotFoundException, InstantiationException {
        final String decoderType = String.format("%s%sDecoder", type.substring(0, 1).toUpperCase(), type.substring(1));
        builder.registerDecoder(columnName, getProcessorClass(decoderType, ns, BEAN_DECODER));
    }

    /**
     * Register a processor of type decoder/postProcessor/postValidator/enumDecoder.
     * <p>
     * The processor class is first looked up as-is, and if not found, the
     * config parser tries to find it in an opencsv package.
     * @param column name of bean field to be configured
     * @param builder the {@code Builder} instance
     * @param processor the corresponding xml node
     * @param <T> type of bean to be eventually emitted
     * @param <R> type of corresponding bean field
     * @throws InstantiationException if the processor cannot be instantiated
     * @throws ClassNotFoundException if the processor class cannot be found
     */
    private <T, R> void registerProcessor(final String column,
                                       final Builder<T> builder,
                                       final Node processor) throws InstantiationException, ClassNotFoundException {
        final String processorNodeName = processor.getLocalName();
        // present of attribute "type" is enforced by xsd
        final String type = getAttributeValue(processor, "type").get();
        final String processorNs = processor.getNamespaceURI();
        switch (processorNodeName) {
            case BEAN_DECODER:
                final Class<? extends Decoder<R>> decoderClass = getProcessorClass(type, processorNs, BEAN_DECODER);
                builder.registerDecoder(column, decoderClass);
                break;
            case BEAN_POSTPROCESSOR:
                final Class<? extends PostProcessor<R>> postProcessorClass = getProcessorClass(type, processorNs, BEAN_POSTPROCESSOR);
                builder.registerPostProcessor(column, postProcessorClass);
                break;
            case BEAN_POSTVALIDATOR:
                final Class<? extends PostValidator<R>> postValidatorClass = getProcessorClass(type, processorNs, BEAN_POSTVALIDATOR);
                builder.registerPostValidator(column, postValidatorClass);
                break;
            case BEAN_ENUM:
                final EnumDecoder<?> enumDecoder = getEnumDecoder(processor);
                builder.registerDecoder(column, enumDecoder);
                break;
        }

    }

    /**
     * Setup an enum decoder.
     * @param processor the corresponding xml node
     * @param <E> type of target enumeration
     * @return the enum decoder
     * @throws ClassNotFoundException if the corresponding enum decoder class cannot be found
     */
    private <E extends Enum<E>> EnumDecoder<E> getEnumDecoder(final Node processor) throws ClassNotFoundException {
        final NodeList maps = processor.getChildNodes();
        // presence of attribute "type" is enforced by xsd
        final String typeName = getAttributeValue(processor, "type").get();
        final Class<? extends E> enumClass = (Class<? extends E>) Class.forName(typeName);
        final EnumDecoder<E> decoder = new EnumDecoder<>();
        decoder.setType(enumClass);
        for (int i = 0; i < maps.getLength(); ++i) {
            final Node node = maps.item(i);
            final String ns = node.getNamespaceURI();
            final String localName = node.getLocalName();
            if (node.getNodeType() == ELEMENT_NODE
                    && BEAN_NAMESPACE.equals(ns)
                    && BEAN_ENUM_MAP.equals(localName)) {
                // presence of attributes "key", "value" is enforced by xsd
                final String key = getAttributeValue(node, "key").get();
                final String value = getAttributeValue(node, "value").get();
                decoder.put(key, value);
            }
        }
        return decoder;
    }

    /**
     * Retrieve the class of the desired processor.
     * <p>
     * The class is looked up first as an absolute name, and if not found,
     * it will be searched for in the appropriate opencsv packages.
     * @param className name of processor class as found in the xml config file
     * @param processorType type of processor (decoder/postProcessor/postValidator)
     * @param <T> type of corresponding bean field
     * @return the processor class
     * @throws ClassNotFoundException if no corresponding class can be found
     */
    private <T> Class<T> getProcessorClass(final String className,
                                           final String ns,
                                           final String processorType)
            throws ClassNotFoundException {
        String subPackage = null;
        if (BEAN_NAMESPACE.equals(ns)) {
            switch (processorType) {
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
        }
        Class<T> processorClass;
        try {
            processorClass = (Class<T>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            final String qName = String.format("%s.%s.%s", DEFAULT_PROCESSING_PACKAGE, subPackage, className);
            try {
                processorClass = (Class<T>) Class.forName(qName);
            } catch (ClassNotFoundException e1) {
                final String msg = String.format("neither found class '%s', nor '%s'", className, qName);
                log.error(msg, e);
                throw new ClassNotFoundException(msg, e);
            }
        }
        return processorClass;
    }

    /**
     * Parse out the header information.
     * @param reader xml node for csv:reader element
     * @return the parsed header
     */
    private String[] getHeader(final Node reader) {
        final NodeList csvFields = reader.getChildNodes();
        final List<String> fieldList = new LinkedList<>();

        for (int i = 0; i < csvFields.getLength(); ++i) {
            final Node item = csvFields.item(i);
            if (item.getNodeType() == ELEMENT_NODE) {
                final String localName = item.getLocalName();
                final String ns = item.getNamespaceURI();
                if (CSV_NAMESPACE.equals(ns)) {
                    if (CSV_COLUMN.equals(localName)) {
                        fieldList.add(getAttributeValue(item, "name").get());
                    }
                    if (CSV_IGNORE.equals(localName)) {
                        final String count = getAttributeValue(item, "count").orElse("1");
                        fieldList.add(String.format("$ignore%s$", count));

                    }
                }
            }
        }
        return fieldList.toArray(new String[fieldList.size()]);
    }

    /**
     * Get an {@link InputStream} of the xml config file.
     * <p>
     * Repeated invocations always return a fresh input stream.
     * @return an {@link InputStream} of the xml config file
     * @throws FileNotFoundException if the underlying {@link File} object
     * cannot be found
     */
    private InputStream getXmlInputStream() throws FileNotFoundException {
        InputStream xmlInputStream;
        if (xmlFileAsArray.isPresent()) {
            xmlInputStream = new ByteArrayInputStream(this.xmlFileAsArray.get());
        }
        else {
            xmlInputStream = new FileInputStream(this.xmlFile.get());
        }
        return xmlInputStream;
    }

    /**
     * Turn an {@link InputStream} into a byte array.
     * <p>
     * This method is used to save a copy of the xml config passed in as
     * {@link InputStream} that can be queries multiple times. This is
     * essential for validation against an xml schema.
     * @param xmlInputStream the {@link InputStream} to be saved
     * @return the {@link InputStream} converted into an array of {@code byte}s
     * @throws IOException if the {@link InputStream} cannot be read
     */
    private byte[] asByteArray(final InputStream xmlInputStream) throws IOException {
        final ByteArrayOutputStream sink = new ByteArrayOutputStream();
        int nRead;
        byte[] buffer = new byte[1024];
        while ((nRead = xmlInputStream.read(buffer, 0, buffer.length)) != -1) {
            sink.write(buffer, 0, nRead);
        }
        return sink.toByteArray();
    }

    /**
     * Get a copy of the registered sink plugins.
     * @return a copy of the registered sink plugins
     * @throws IllegalAccessException if the {@code SinkPlugins} constructor cannot be accessed
     * @throws InstantiationException if the creation of a new {@link SinkPlugin} fails
     */
    private static List<SinkPlugin> getSinkPlugins() throws IllegalAccessException, InstantiationException {
        registeredSinkPlugins.reload();
        final List<SinkPlugin> plugins = new ArrayList<>();
        for (SinkPlugin plugin : registeredSinkPlugins) {
            final SinkPlugin sinkPlugin = plugin.getClass().newInstance();
            plugins.add(sinkPlugin);
        }
        return Collections.unmodifiableList(plugins);
    }

    /**
     * Obtain an xml attribute's value for a specific node.
     * @param node the xml node
     * @param attribute the attribute to lookup
     * @return the attribute's value, if present
     */
    public static Optional<String> getAttributeValue(final Node node, final String attribute) {
        final Node item = node.getAttributes().getNamedItem(attribute);
        if (item == null)
            return Optional.empty();

        final String value = item.getNodeValue();
        if (value == null)
            return Optional.empty();
        else
            return Optional.of(value);
    }

    /**
     * Assert that an xml document is well-formed.
     * <p>
     * The xml document is fed into a sax parser and checked for errors.
     * @param xmlInputStream the xml document as input stream
     * @return true if the document is well-formed, else false
     * @throws IOException if the document cannot be read
     * @throws ParserConfigurationException if the sax parser cannot be configured
     * @throws SAXException if the sax parser cannot be created
     */
    private static boolean isWellFormed(final InputStream xmlInputStream) throws IOException, ParserConfigurationException, SAXException {
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        final SAXParser parser = factory.newSAXParser();
        try {
            parser.parse(xmlInputStream, new DefaultHandler());
        } catch (SAXException e) {
            return false;
        }
        return true;
    }

    /**
     * Obtain a new {@code ConfigParser} instance for an xml input stream and an
     * iterable of raw csv data.
     * @param xmlInputStream stream of the xml config file
     * @param unparsedLines raw csv data, split into lines
     * @return the {@code ConfigParser} instance
     * @throws IOException if the {@code xmlInputStream} cannot be read
     */
    public static ConfigParser ofUnparsedLines(@NonNull final InputStream xmlInputStream,
                                               @NonNull final Iterable<String> unparsedLines)
            throws IOException {
        return new ConfigParser(xmlInputStream, null, unparsedLines, null, null);
    }

    /**
     * Obtain a new {@code ConfigParser} instance for an xml input stream and an
     * iterable of raw csv data.
     * @param xmlFile {@code File} instance for the xml config file
     * @param unparsedLines raw csv data, split into lines
     * @return the {@code ConfigParser} instance
     * @throws FileNotFoundException if the xml file cannot be found
     */
    public static ConfigParser ofUnparsedLines(@NonNull final File xmlFile,
                                               @NonNull final Iterable<String> unparsedLines)
            throws FileNotFoundException {
        return new ConfigParser(xmlFile, null, unparsedLines, null, null);
    }

    /**
     * Obtain a new {@code ConfigParser} instance for an xml input stream and an
     * iterable of already parsed lines.
     * @param xmlInputStream stream of the xml config file
     * @param parsedLines already parsed lines
     * @return the {@code ConfigParser} instance
     * @throws IOException if the {@code xmlInputStream} cannot be read
     */
    public static ConfigParser ofParsedLines(@NonNull final InputStream xmlInputStream,
                                             @NonNull final Iterable<String[]> parsedLines)
            throws IOException {
        return new ConfigParser(xmlInputStream, null, null, parsedLines, null);
    }

    /**
     * Obtain a new {@code ConfigParser} instance for an xml input stream and an
     * iterable of already parsed lines.
     * @param xmlFile {@code File} instance for the xml config file
     * @param parsedLines already parsed lines
     * @return the {@code ConfigParser} instance
     * @throws FileNotFoundException if the xml file cannot be found
     */
    public static ConfigParser ofParsedLines(@NonNull final File xmlFile,
                                             @NonNull final Iterable<String[]> parsedLines)
            throws FileNotFoundException {
        return new ConfigParser(xmlFile, null, null, parsedLines, null);
    }

    /**
     * Obtain a new {@code ConfigParser} instance for an xml input stream and a
     * reader of the csv data.
     * @param xmlInputStream stream of the xml config file
     * @param reader reader of csv data
     * @return the {@code ConfigParser} instance
     * @throws IOException if the {@code xmlInputStream} cannot be read
     */
    public static ConfigParser ofReader(@NonNull final InputStream xmlInputStream,
                                        @NonNull final Reader reader)
            throws IOException {
        return new ConfigParser(xmlInputStream, reader, null, null, null);
    }

    /**
     * Obtain a new {@code ConfigParser} instance for an xml input stream and a
     * reader of the csv data.
     * @param xmlFile {@code File} instance for the xml config file
     * @param reader reader of csv data
     * @return the {@code ConfigParser} instance
     * @throws FileNotFoundException if the xml file cannot be found
     */
    public static ConfigParser ofReader(@NonNull final File xmlFile,
                                        @NonNull final Reader reader)
            throws FileNotFoundException {
        return new ConfigParser(xmlFile, reader, null, null, null);
    }

    /**
     * Obtain a new {@code ConfigParser} instance for an xml input stream and an
     * input stream of the csv source.
     * @param xmlInputStream stream of the xml config file
     * @param inputStream stream of csv data
     * @return the {@code ConfigParser} instance
     * @throws IOException if the {@code xmlInputStream} cannot be read
     */
    public static ConfigParser ofInputStream(@NonNull final InputStream xmlInputStream,
                                             @NonNull final InputStream inputStream)
            throws IOException {
        return new ConfigParser(xmlInputStream, null, null, null, inputStream);
    }

    /**
     * Obtain a new {@code ConfigParser} instance for an xml input stream and an
     * input stream of the csv source.
     * @param xmlFile {@code File} instance for the xml config file
     * @param inputStream stream of csv data
     * @return the {@code ConfigParser} instance
     * @throws FileNotFoundException if the xml file cannot be found
     */
    public static ConfigParser ofInputStream(@NonNull final File xmlFile,
                                             @NonNull final InputStream inputStream)
            throws FileNotFoundException {
        return new ConfigParser(xmlFile, null, null, null, inputStream);
    }

    /**
     * Obtain a new {@code ConfigParser} instance for an xml input stream and a
     * reference to the csv file.
     * @param xmlInputStream stream of the xml config file
     * @param inputFile file referring to csv data
     * @return the {@code ConfigParser} instance
     * @throws IOException if the {@code xmlInputStream} cannot be read
     */
    public static ConfigParser ofFile(@NonNull final InputStream xmlInputStream,
                                      @NonNull final File inputFile)
            throws IOException {
        return new ConfigParser(xmlInputStream, null, null, null, new FileInputStream(inputFile));
    }

    /**
     * Obtain a new {@code ConfigParser} instance for an xml input stream and an
     * reference to the csv file.
     * @param xmlFile {@code File} instance for the xml config file
     * @param inputFile file referring to csv data
     * @return the {@code ConfigParser} instance
     * @throws FileNotFoundException if the xml or the csv cannot be found
     */
    public static ConfigParser ofFile(@NonNull final File xmlFile,
                                      @NonNull final File inputFile)
            throws FileNotFoundException {
        return new ConfigParser(xmlFile, null, null, null, new FileInputStream(inputFile));
    }

}
