package com.github.kahalemakai.opencsv.plugins;

import com.github.kahalemakai.opencsv.beans.Builder;
import com.github.kahalemakai.opencsv.config.ConfigParser;
import com.github.kahalemakai.opencsv.config.PluginException;
import com.github.kahalemakai.opencsv.config.Sink;
import com.github.kahalemakai.opencsv.config.SinkPlugin;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.w3c.dom.Node;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Optional;

import static java.nio.file.StandardOpenOption.*;

/**
 * A sink plugin that writes the target beans to a file.
 * <p>
 * The resulting data can be output to a csv file, serialized or
 * by using their {@code toString()} implementation. Currently, only the
 * later one is implemented. For further details, please take a look at the
 * associated xsd file.
 */
@RequiredArgsConstructor
@Log4j
public class FileWriterPlugin implements SinkPlugin {
    /**
     * The file writer xml namespace.
     */
    private static final String NAMESPACE = "http://github.com/kaHaleMaKai/opencsv/plugins/file-writer";

    /**
     * {@inheritDoc}
     */
    @Getter
    Sink sink;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameSpace() {
        return NAMESPACE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL getSchemaUrl() {
        return getClass().getResource("/schemas/file-writer.xsd");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> void configure(Builder<T> builder, Node sink) {
        final Path path = Paths.get(ConfigParser.getAttributeValue(sink, "path").get());
        final boolean append = Boolean.valueOf(ConfigParser.getAttributeValue(sink, "append").get());
        final Charset charset = getCharset(ConfigParser.getAttributeValue(sink, "encoding"));
        final OutputType type = OutputType.getEnumConstant(ConfigParser.getAttributeValue(sink, "type").get());
        final int batches = Integer.parseInt(ConfigParser.getAttributeValue(sink, "batches").get());
        final char separator = ConfigParser.getAttributeValue(sink, "separator").get().charAt(0);
        // TODO: handle quoteChar instead of quoteString
        final String quoteString = ConfigParser.getAttributeValue(sink, "quoteChar").get();

        boolean addLeadingNewLine = false;
        OpenOption[] options;
        if (append) {
            options = new OpenOption[] {WRITE, APPEND, CREATE};
            addLeadingNewLine = Files.exists(path);
        }
        else {
            options = new OpenOption[] {WRITE, TRUNCATE_EXISTING, CREATE};
        }
        OutputStream outputStream;
        try {
            outputStream = Files.newOutputStream(path, options);
        } catch (IOException e) {
            final String msg = String.format("could not open file %%s for writing%s", path);
            log.error(msg);
            throw new PluginException(msg, e);
        }
        switch (type) {
            case CSV:
                throw new PluginException("not implemented yet");
            case SERIALIZATION:
                throw new PluginException("not implemented yet");
            case TO_STRING:
                this.sink = toStringWriter(outputStream, charset, addLeadingNewLine);
        }
        builder.sink(this.sink);
    }

    /**
     * Return a {@link Sink} that generates output by using
     * the beans' {@code toString()} method.
     * @param outputStream the {@link OutputStream} to use
     * @param encoding the file encoding/{@link Charset} to use
     * @param addLeadingNewLine if a leading new line character should be used
     * @return a {@link Sink} that generates output by using
     * the beans' {@code toString()} method
     */
    private Sink toStringWriter(final OutputStream outputStream,
                                final Charset encoding,
                                final boolean addLeadingNewLine) {
        return new Sink() {
            private final OutputStream os = outputStream;
            private final Charset charset = encoding;

            @Override
            public void close() throws IOException {
                this.os.flush();
                this.os.close();
            }

            @Override
            public void accept(Iterator<?> iter) {
                final byte[] newLine = "\n".getBytes(charset);
                if (!addLeadingNewLine && iter.hasNext()) {
                    try {
                        this.os.write(iter.next().toString().getBytes(this.charset));
                    } catch (IOException e) {
                        log.error(e.getMessage());
                        throw new PluginException(e);
                    }
                }
                while (iter.hasNext()) {
                    try {
                        this.os.write(newLine);
                        this.os.write(iter.next().toString().getBytes(this.charset));
                    } catch (IOException e) {
                        log.error(e.getMessage());
                        throw new PluginException(e);
                    }
                }
            }
        };
    }

    /**
     * Get the file encoding/{@link Charset} as given by the xml config file.
     * <p>
     * If no encoding is given, fallback to the platform's default one.
     * @param encoding the encoding as specified in the xml config, if present
     * @return the file encoding/{@link Charset} as given by the xml config file
     */
    private Charset getCharset(@NonNull final Optional<String> encoding) {
        Charset charset;
        if (encoding.isPresent()) {
            final String s = encoding.get();
            if (Charset.isSupported(s)) {
                charset = Charset.forName(s);
            } else {
                final String msg = String.format("charset '%s' is not available", s);
                log.error(msg);
                throw new PluginException(msg);
            }
        } else {
            charset = Charset.defaultCharset();
        }
        return charset;
    }

    /**
     * Enumeration of possible output types for the file writer plugin.
     */
    private enum OutputType {

        /**
         * Output to csv format.
         */
        CSV("csv"),

        /**
         * Output using the beans' {@code toString()} method
         */
        TO_STRING("toString()"),

        /**
         * Output using standard java serialization.
         */
        SERIALIZATION("serialization");

        /**
         * The xml attribute value corresponding to the respective enum constant.
         * @return the xml attribute value corresponding to the respective enum constant
         */
        @Getter
        private String value;

        /**
         * Constructor.
         * @param value the xml attribute value corresponding to the respective enum constant
         */
        OutputType(String value) {
            this.value = value;
        }

        /**
         * Get the enum constant corresponding to the given xml attribute value.
         * @param value the xml attribute value
         * @return the corresponding enum constant
         */
        public static OutputType getEnumConstant(final String value) {
            for (OutputType type : OutputType.values()) {
                if (type.getValue().equals(value)) {
                    return type;
                }
            }
            final String msg = String.format("cannot find OutputType for value %%s%s", value);
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
    }

}
