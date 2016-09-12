package com.github.kahalemakai.opencsv.plugins;

import com.github.kahalemakai.opencsv.beans.Builder;
import com.github.kahalemakai.opencsv.config.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.Iterator;
import java.util.Optional;

import static java.nio.file.StandardOpenOption.*;

@RequiredArgsConstructor
@Log4j
public class FileWriterPlugin implements SinkPlugin {
    private static final String NAMESPACE = "http://github.com/kaHaleMaKai/opencsv/plugins/file-writer";

    @Getter
    Sink sink;

    @Override
    public String getNameSpace() {
        return NAMESPACE;
    }

    @Override
    public File getSchemaFile() {
        return new File(
                getClass()
                        .getResource("/schemas/file-writer.xsd")
                        .getFile());
    }

    @Override
    public <T> void configure(Builder<T> builder, Node sink) {
        final Path path = Paths.get(Plugin.getAttributeValue(sink, "path").get());
        final boolean append = Boolean.valueOf(Plugin.getAttributeValue(sink, "append").get());
        final Charset charset = getCharset(Plugin.getAttributeValue(sink, "encoding"));
        final OutputType type = OutputType.getEnumConstant(Plugin.getAttributeValue(sink, "type").get());
        final int batches = Integer.parseInt(Plugin.getAttributeValue(sink, "batches").get());
        final char separator = Plugin.getAttributeValue(sink, "separator").get().charAt(0);
        // TODO: handle quoteChar instead of quoteString
        final String quoteString = Plugin.getAttributeValue(sink, "quoteChar").get();

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

    private enum OutputType {

        CSV("csv"),
        TO_STRING("toString()"),
        SERIALIZATION("serialization");

        @Getter
        private String value;

        OutputType(String value) {
            this.value = value;
        }

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
