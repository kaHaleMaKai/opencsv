package com.github.kahalemakai.opencsv.beans.processing;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

import java.beans.PropertyEditorSupport;
import java.util.LinkedList;
import java.util.List;

@NoArgsConstructor(staticName = "init")
@Log4j
public class DecoderPropertyEditor<T> extends PropertyEditorSupport {
    private final List<Decoder<? extends T, ? extends Throwable>> decoders = new LinkedList<>();
    private PostProcessor<T> postProcessor = PostProcessor.identity();
    private final List<PostValidator<T>> postValidators = new LinkedList<>();
    private String data;
    // TODO: pass that field down on initialization or somehow else
    @Getter @Setter
    private boolean trimField = false;
    @Getter @Setter
    private Class<? extends T> type;
    @Getter @Setter
    private boolean nullFallthroughForPostProcessors;
    @Getter @Setter
    private boolean nullFallthroughForPostValidators;

    public DecoderPropertyEditor<T> add(Decoder<? extends T, ? extends Throwable> decoder) {
        decoders.add(decoder);
        return this;
    }

    public DecoderPropertyEditor<T> addPostProcessor(final PostProcessor<T> postProcessor) {
        this.postProcessor = PostProcessor.compose(this.postProcessor, postProcessor);
        return this;
    }

    public DecoderPropertyEditor<T> addPostValidator(final PostValidator<T> postValidator) {
        postValidators.add(postValidator);
        return this;

    }
    private T decodeValue() throws DataDecodingException {
        if (log.isDebugEnabled()) {
            log.debug(String.format("decoding value '%s' using decoder chain of length %d", data, decoders.size()));
        }
        for (int i = 0; i < decoders.size(); ++i) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("trying decoder nr. %d", i + 1));
            }
            final Decoder<? extends T, ? extends Throwable> decoder = decoders.get(i);
            try {
                final T decodedValue = decoder.decode(data);
                if (log.isDebugEnabled()) {
                    log.debug(String.format("successfully decoded value %s -> %s : <%s>",
                            data,
                            decodedValue,
                            decodedValue == null ? "null" : decodedValue.getClass().getCanonicalName()));
                }
                return decodedValue;
            } catch (Throwable e) {
                if (i == decoders.size() - 1) {
                    final String msg = String.format("could not decode value '%s'", data);
                    log.error(msg);
                    throw new DataDecodingException(msg, e);
                }
            }
        }
        return null;
    }

    private T postProcess(final T value) throws PostProcessingException {
        if (postProcessor == PostProcessor.IDENTITY) {
            if (log.isDebugEnabled()) {
                log.debug("no postprocessor setup, returning identical value");
            }
            return value;
        }
        if (!isNullFallthroughForPostProcessors() || value != null) {
            try {
                final T postProcessedValue = postProcessor.process(value);
                if (log.isDebugEnabled()) {
                    log.debug(String.format("postprocessing value %s -> %s",
                            value,
                            postProcessedValue));
                }
                return postProcessedValue;
            } catch (Exception e) {
                final String msg = String.format("error while trying to postprocess value %s", value);
                log.error(msg);
                throw new PostProcessingException(msg, e);
            }
        }
        else if (log.isDebugEnabled()) {
            log.debug("null falls through on postprocessing");
        }
        return null;
    }

    private void postValidate(final T value) throws PostValidationException {
        if (!isNullFallthroughForPostValidators() || value != null) {
            int counter = 1;
            for (PostValidator<T> postValidator : postValidators) {
                if (!postValidator.validate(value)) {
                   final String msg = String.format("could not validate data\ninput: %s\nvalidation step: %d", value, counter);
                    log.error(msg);
                    throw new PostValidationException(msg);
                }
                counter++;
            }
        }
        else if (log.isDebugEnabled()) {
            log.debug("null falls through on validation");
        }
    }

    @Override
    public T getValue() throws DataDecodingException, PostProcessingException, PostValidationException {
        final T decodedValue = decodeValue();
        final T postProcessedValue = postProcess(decodedValue);
        postValidate(postProcessedValue);
        return postProcessedValue;
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        this.data = isTrimField() ? text.trim() : text;
    }

}
