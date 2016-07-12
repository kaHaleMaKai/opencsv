package com.github.kahalemakai.opencsv.beans;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.beans.PropertyEditorSupport;
import java.util.LinkedList;
import java.util.List;

@NoArgsConstructor(staticName = "init")
public class DecoderPropertyEditor<T> extends PropertyEditorSupport {
    private final List<Decoder<? extends T, ? extends Throwable>> decoders = new LinkedList<>();
    private PostProcessor<T, T> postProcessor = PostProcessor.identity();
    private String data;
    @Getter @Setter
    private boolean trimField = false;
    @Getter @Setter
    private Class<? extends T> type;

    public DecoderPropertyEditor<T> add(Decoder<? extends T, ? extends Throwable> decoder) {
        decoders.add(decoder);
        return this;
    }

    public DecoderPropertyEditor<T> setPostProcessor(final PostProcessor<T, T> postProcessor) throws UnsupportedOperationException {
        if (postProcessor != PostProcessor.IDENTIFY) {
            throw new UnsupportedOperationException("cannot set postProcessor more than once");
        }
        this.postProcessor = postProcessor;
        return this;
    }

    @Override
    public T getValue() throws DataDecodingException, PostProcessingException {
        final int almostNumDecoders = decoders.size() - 1;
        for (int i = 0; i <= almostNumDecoders; ++i) {
            final Decoder<? extends T, ? extends Throwable> decoder = decoders.get(i);
            try {
                final T result = decoder.decode(data);
                return postProcessor.process(result);
            } catch (Throwable e) {
                if (i == almostNumDecoders) {
                    throw new DataDecodingException(String.format("could not decode value '%s'", data), e);
                }
            }
        }
        return null;
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        this.data = isTrimField() ? text.trim() : text;
    }

}
