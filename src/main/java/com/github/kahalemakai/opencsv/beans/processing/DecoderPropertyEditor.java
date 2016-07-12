package com.github.kahalemakai.opencsv.beans.processing;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.beans.PropertyEditorSupport;
import java.util.LinkedList;
import java.util.List;

@NoArgsConstructor(staticName = "init")
public class DecoderPropertyEditor<T> extends PropertyEditorSupport {
    private final List<Decoder<? extends T, ? extends Throwable>> decoders = new LinkedList<>();
    private PostProcessor<T> postProcessor = PostProcessor.identity();
    private final List<PostValidator> postValidators = new LinkedList<>();
    private String data;
    @Getter @Setter
    private boolean trimField = false;
    @Getter @Setter
    private Class<? extends T> type;

    public DecoderPropertyEditor<T> add(Decoder<? extends T, ? extends Throwable> decoder) {
        decoders.add(decoder);
        return this;
    }

    public DecoderPropertyEditor<T> setPostProcessor(final PostProcessor<T> postProcessor) {
        if (this.postProcessor != PostProcessor.IDENTITY) {
            throw new UnsupportedOperationException("trying to re-set postprocessor");
        }
        this.postProcessor = postProcessor;
        return this;
    }

    public DecoderPropertyEditor<T> addPostValidator(final PostValidator postValidator) {
        postValidators.add(postValidator);
        return this;

    }
    private T decodeValue() throws DataDecodingException {
        final int almostNumDecoders = decoders.size() - 1;
        for (int i = 0; i <= almostNumDecoders; ++i) {
            final Decoder<? extends T, ? extends Throwable> decoder = decoders.get(i);
            try {
                return decoder.decode(data);
            } catch (Throwable e) {
                if (i == almostNumDecoders) {
                    throw new DataDecodingException(String.format("could not decode value '%s'", data), e);
                }
            }
        }
        return null;
    }

    private T postProcess(final T value) throws PostProcessingException {
        try {
            return postProcessor.process(value);
        } catch (Exception e) {
            throw new PostProcessingException(String.format("error while trying to postprocess value %s", value), e);
        }
    }

    private void postValidate(final Object value) throws PostValidationException {
        int counter = 1;
        for (PostValidator postValidator : postValidators) {
            if (!postValidator.validate(value)) {
                throw new PostValidationException(String.format("could not validate data\ninput: %s\nvalidation step: %d",
                        value, counter));
            }
            counter++;
        }
    }

    @Override
    public Object getValue() throws DataDecodingException, PostProcessingException, PostValidationException {
        final T decodedValue = decodeValue();
        final Object postProcessedValue = postProcess(decodedValue);
        postValidate(postProcessedValue);
        return postProcessedValue;
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        this.data = isTrimField() ? text.trim() : text;
    }

}
