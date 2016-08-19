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

package com.github.kahalemakai.opencsv.beans.processing;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

import java.beans.PropertyEditorSupport;
import java.util.LinkedList;
import java.util.List;

/**
 *The {@code DecoderPropertyEditor} contains all the logic of processing csv column values into bean field values.
 * <p>
 * The {@code DecoderPropertyEditor} extends the {@code PropertyEditorSupport} and relies on its implementation
 * for {@link #getValue()} and {@link #setAsText(String)}.
 * When processing a split csv line, a the field value is processed as follows:
 * <p>
 * {@code String field -> #setAsText(field) -> #getValue()}
 * @see #getValue()
 *
 * @param <T> class, the csv column should be converted to
 */
@RequiredArgsConstructor(staticName = "forColumn")
@Log4j
public final class DecoderPropertyEditor<T> extends PropertyEditorSupport {
    private static final String ANY_COLUMN = "*";
    private final List<Decoder<? extends T>> decoders = new LinkedList<>();
    private PostProcessor<T> postProcessor = PostProcessor.identity();
    private final List<PostValidator<T>> postValidators = new LinkedList<>();
    private String data;
    @Getter
    private final String columnName;

    /**
     * Define trimming behaviour for csv String values before decoding them.
     * <p>
     * Defaults to {@code false}.
     * @param trimField new value for trimming behaviour
     * @return the trimming behaviour
     */
    @Getter @Setter
    private boolean trimField = false;

    /**
     * The type of the output bean field.
     *
     * @param type class object representing the output bean field
     * @return type of the output bean field
     */
    @Getter @Setter
    private Class<? extends T> type;

    /**
     * Define behaviour for encountering nulls in postprocessing.
     * <p>
     * When setting this field to {@code true}, nulls will not be further processed, but simply returned.
     * A value of {@code false} marks nulls to be processed.
     * <p>
     * Defaults to {@code false}.
     *
     * @param nullFallthroughForPostProcessors mode for postprocessing null values
     * @return mode for postprocessing null values
     */
    @Getter @Setter
    private boolean nullFallthroughForPostProcessors;

    /**
     * Define behaviour for encountering nulls in postvalidation.
     * <p>
     * When setting this field to {@code true}, nulls will not be validated.
     * A value of {@code false} marks nulls to be validated.
     * <p>
     * Defaults to {@code false}.
     *
     * @param nullFallthroughForPostValidators mode for postprocessing null values
     * @return mode for postvalidating null values
     */
    @Getter @Setter
    private boolean nullFallthroughForPostValidators;

    /**
     * Add a new decoder to the decoding chain.
     * @see #decodeValue() decodeValue()
     *
     * @param decoder decoder instance to be added to the decoding chain
     * @return the {@code DecoderPropertyEditor} instance
     */
    public DecoderPropertyEditor<T> add(Decoder<? extends T> decoder) {
        decoders.add(decoder);
        return this;
    }

    /**
     * Add a new postprocessor to the postprocessing chain.
     * @see #postProcess(Object) postProcess()
     *
     * @param postProcessor postprocessor instance to be added to the postprocessing chain
     * @return the {@code DecoderPropertyEditor} instance
     */
    public DecoderPropertyEditor<T> addPostProcessor(final PostProcessor<T> postProcessor) {
        this.postProcessor = PostProcessor.compose(this.postProcessor, postProcessor);
        return this;
    }

    /**
     * Add a new postvalidator to the postvalidation chain.
     * @see #postValidate(Object) postValidate()
     *
     * @param postValidator postvalidator instance to be added to the postvalidation chain
     * @return the {@code DecoderPropertyEditor} instance
     */
    public DecoderPropertyEditor<T> addPostValidator(final PostValidator<T> postValidator) {
        postValidators.add(postValidator);
        return this;
    }


    /**
     * Convert a String value obtained from a parsed csv into a Java object.
     * <p>
     * The conversion is carried out in three phases:
     * <ul>
     *     <li><em>decode</em>: convert the String into an object of type {@code T}</li>
     *     <li><em>postprocess</em>: process the decoded object further without changing its type</li>
     *     <li><em>post-validate</em>: validate the results of the previous steps</li>
     * </ul>
     * <br>
     *
     * <h1>Overview of the individual phases</h1>
     *
     * <h2>decode</h2>
     * The {@code DecoderPropertyEditor} allows for adding an arbitrary amount of
     * decoders. The will be invoked sequentially.
     * <p>
     * If a decoder throws any kind of {@code Throwable}, the next decoder
     * will be invoked, until a decoder <em>returns</em> an object, or no further
     * decoder is available.
     * If even the last decoder <em>throws</em>, a {@code DataDecodingException} will
     * be thrown.
     *
     * <h2>postprocess</h2>
     * If the string value could be successfully decoded into an object, the value will undergo a postprocessing.
     * All postprocessors will be executed sequentially (in contrast to the decoders, cf. above).
     * If any kind of {@code Throwable} is encountered, the postprocessing stops and a
     * {@code PostProcessingException} is thrown.
     *
     * <h2>post-validate</h2>
     * Finally, the decoded and postprocessed object may be validated.
     * All post-validators will be executed sequentially. If a post-validator
     * returns {@code false}, a {@code PostValidationException} will get thrown.
     *
     * <h1>Treatment of {@code null}s</h1>
     * If the decoding step procuces a {@code null} reference, the postprocessing
     * or post-validation, respectively, may pass it on without processing or validating it.
     * This behaviour can be activated manually by {@link #setNullFallthroughForPostProcessors(boolean)},
     * or {@link #setNullFallthroughForPostValidators(boolean)}.
     * <p>
     * <b>Type parameters:</b><br>
     * {@code <T>} - type of field the String value should be converted into
     *
     * @throws DataDecodingException if the String value could not be decoded to the bean field type
     * @throws PostProcessingException if a postprocessing step fails
     * @throws PostValidationException if a post-validation step fails
     *
     * @return the processed object
     *
     * @see Decoder Decoder
     * @see PostProcessor PostProcessor
     * @see PostValidator PostValidator
     */
    @Override
    public T getValue() throws DataDecodingException, PostProcessingException, PostValidationException {
        final T decodedValue = decodeValue();
        final T postProcessedValue = postProcess(decodedValue);
        postValidate(postProcessedValue);
        return postProcessedValue;
    }

    /**
     * Set the String-valued csv field as text for further processing.
     * <p>
     * If {@link #isTrimField()} evaluates to true, the text will get trimmed before setting it.
     *
     * @param text the text to set for further processing
     * @throws IllegalArgumentException only declared for matching the interface
     */
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        this.data = isTrimField() ? text.trim() : text;
    }

    private T decodeValue() throws DataDecodingException {
        if (log.isDebugEnabled()) {
            log.debug(String.format("decoding value '%s' using decoder chain of length %d", data, decoders.size()));
        }
        for (int i = 0; i < decoders.size(); ++i) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("trying decoder nr. %d", i + 1));
            }
            final Decoder<? extends T> decoder = decoders.get(i);
            try {
                final T decodedValue = decoder.decode(data);
                if (Decoder.DECODING_FAILED == decodedValue) {
                    continue;
                }
                if (log.isDebugEnabled()) {
                    log.debug(String.format("successfully decoded value %s -> %s : <%s>",
                            data,
                            decodedValue,
                            decodedValue == null ? "null" : decodedValue.getClass().getCanonicalName()));
                }
                return decodedValue;
            } catch (Throwable e) {
                final String msg = String.format("[col: %s] could not decode value '%s'", getColumnName(), data);
                log.error(msg);
                throw new DataDecodingException(msg, e);
            }
        }
        final String msg = String.format("[col: %s] could not decode value '%s'", getColumnName(), data);
        log.error(msg);
        throw new DataDecodingException(msg);
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
                final String msg = String.format("[col: %s] error while trying to postprocess value %s", getColumnName(), value);
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
                    final String msg = String.format("[col: %s] could not validate data\ninput: %s\nvalidation step: %d", getColumnName(), value, counter);
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

    static DecoderPropertyEditor<String> IDENTITY =
            DecoderPropertyEditor
                    .<String>forColumn(ANY_COLUMN)
                    .add(Decoder.IDENTITY);

}
