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

import lombok.NonNull;
import lombok.ToString;
import lombok.extern.log4j.Log4j;

import java.beans.PropertyEditor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Indirection layer around the {@code DecoderPropertyEditor} class.
 * <p>
 * This class manages does the bookkeeping of {@code DecoderPropertyEditor} for
 * all csv columns. In addition, it improves performance by re-using a single instance
 * of decoders, postprocessors or postvalidators to be used in the {@code DecoderPropertyEditor},
 * if possible. I.e. if the same decoder (or postprocessor/postvalidator, respectively) is applied
 * to different columns, only a single instance of it will be created and passed around.
 *
 * @see DecoderPropertyEditor
 */
@Log4j
@ToString
public class DecoderManager {
    private final Map<String, DecoderPropertyEditor<?>> decoderMap;
    private final Map<Class<? extends Decoder<?>>, Decoder<?>> decoderClassMap;
    private final Map<Class<? extends PostProcessor<?>>, PostProcessor<?>> postProcessorClassMap;
    private final Map<Class<? extends PostValidator<?>>, PostValidator<?>> postValidatorClassMap;

    /**
     * Initialize a new and empty {@code DecoderManager}.
     * @return new instance of {@code DecoderManager}
     */
    public static DecoderManager init() {
        return new DecoderManager();
    }

    /**
     * Add a decoder to the decoding chain.
     * <p>
     * If possible, use instead {@link #add(String, Class)} for performance reasons.
     * @param column name of csv column
     * @param decoder {@code Decoder} instance to be added
     * @param <T> target type of decoder conversion
     * @return the {@code DecoderManager} instance
     */
    public <T> DecoderManager add(final String column, Decoder<? extends T> decoder) {
        @SuppressWarnings("unchecked")
        final DecoderPropertyEditor<T> propertyEditor = getPropertyEditor(column);
        propertyEditor.add(decoder);
        return this;
    }

    /**
     * Add a decoder to the decoding chain.
     * <p>
     * The decoderClass is assumed to have a no-args constructor.
     * Upon adding a decoder, the constructor is called and the instance
     * is added to the decoding chain. The same instance is used when adding
     * the decoder class object for any column.
     *
     * @param column name of csv column
     * @param decoderClass class of decoder to add to the chain
     * @return the {@code DecoderManager} instance
     * @throws InstantiationException if trying to call a no-args constructor for {@code decoderClass} fails
     */
    public DecoderManager add(final String column,
                              Class<? extends Decoder<?>> decoderClass)
                              throws InstantiationException {
        if (!decoderClassMap.containsKey(decoderClass)) {
            try {
                final Decoder<?> decoder = decoderClass.newInstance();
                decoderClassMap.put(decoderClass, decoder);
            } catch (InstantiationException | IllegalAccessException e) {
                final String msg = e.getMessage();
                log.error(msg);
                throw new InstantiationException(msg);
            }
        }
        return add(column, decoderClassMap.get(decoderClass));
    }

    /**
     * Add a postprocessor to the postprocessing chain.
     * <p>
     * If possible, use instead {@link #addPostProcessor(String, Class)} for performance reasons.
     * @param column name of csv column
     * @param postProcessor {@code PostProcessor} instance to be added
     * @param <T> type of {@code PostProcessor} input
     * @return the {@code DecoderManager} instance
     */
    public <T> DecoderManager addPostProcessor(final String column, PostProcessor<T> postProcessor) {
        @SuppressWarnings("unchecked")
        final DecoderPropertyEditor<T> propertyEditor = getPropertyEditor(column);
        propertyEditor.addPostProcessor(postProcessor);
        return this;
    }

    /**
     * Add a postprocessor to the postprocessing chain.
     * <p>
     * The postprocessorClass is assumed to have a no-args constructor.
     * Upon adding a postprocessor, the constructor is called and the instance
     * is added to the postprocessing chain. The same instance is used when adding
     * the postprocessor class object for any column.
     *
     * @param column name of csv column
     * @param postProcessorClass class of postprocessor to add to the chain
     * @param <T> type of {@code PostProcessor} input
     * @return the {@code DecoderManager} instance
     * @throws InstantiationException if trying to call a no-args constructor for {@code postProcessorClass} fails
     */
    public <T> DecoderManager addPostProcessor(final String column, Class<? extends PostProcessor<T>> postProcessorClass)
            throws InstantiationException {
        if (!postProcessorClassMap.containsKey(postProcessorClass)) {
            try {
                final PostProcessor postProcessor = postProcessorClass.newInstance();
                postProcessorClassMap.put(postProcessorClass, postProcessor);
            } catch (InstantiationException | IllegalAccessException e) {
                final String msg = e.getMessage();
                log.error(msg);
                throw new InstantiationException(msg);
            }
        }
        return addPostProcessor(column, postProcessorClassMap.get(postProcessorClass));
    }

    /**
     * Add a postvalidator to the postvalidating chain.
     * <p>
     * If possible, use instead {@link #addPostValidator(String, Class)} for performance reasons.
     * @param column name of csv column
     * @param postValidator {@code PostValidator} instance to be added
     * @param <T> type of {@code PostValidator} input
     * @return the {@code DecoderManager} instance
     */
    public <T> DecoderManager addPostValidator(final String column, PostValidator<T> postValidator) {
        final DecoderPropertyEditor<T> propertyEditor = getPropertyEditor(column);
        propertyEditor.addPostValidator(postValidator);
        return this;
    }

    /**
     * Add a postvalidator to the postvalidating chain.
     * <p>
     * The postvalidatorClass is assumed to have a no-args constructor.
     * Upon adding a postvalidator, the constructor is called and the instance
     * is added to the postvalidating chain. The same instance is used when adding
     * the postvalidator class object for any column.
     *
     * @param column name of csv column
     * @param postValidatorClass class of postvalidator to add to the chain
     * @return the {@code DecoderManager} instance
     * @throws InstantiationException if trying to call a no-args constructor for {@code postValidatorClass} fails
     */
    public DecoderManager addPostValidator(final String column, Class<? extends PostValidator<?>> postValidatorClass)
            throws InstantiationException {
        if (!postValidatorClassMap.containsKey(postValidatorClass)) {
            try {
                final PostValidator postValidator = postValidatorClass.newInstance();
                postValidatorClassMap.put(postValidatorClass, postValidator);
            } catch (InstantiationException | IllegalAccessException e) {
                final String msg = e.getMessage();
                log.error(msg);
                throw new InstantiationException(msg);
            }
        }
        return addPostValidator(column, postValidatorClassMap.get(postValidatorClass));
    }

    /**
     * Return the defined {@code DecoderPropertyEditor} instance as {@code Optional}.
     * @see DecoderPropertyEditor DecoderPropertyEditor
     * @param column name of column to be looked up
     * @return {@code Optional} of {@code DecoderPropertyEditor}
     */
    public PropertyEditor get(@NonNull final String column) {
        final DecoderPropertyEditor<?> editor = decoderMap.get(column.toLowerCase());
        return editor != null ? editor : DecoderPropertyEditor.IDENTITY;
    }

    /**
     * Provide an immutable copy of the {@code DecoderManager} instance.
     * <p>
     * This method relies on the {@code unmodifiable} modifiers of the {@code java.lang.Collections} framework.
     * @return immutable copy of the {@code DecoderManager} instance
     */
    public DecoderManager immutableCopy() {
        return new DecoderManager(Collections.unmodifiableMap(decoderMap),
                                  Collections.unmodifiableMap(decoderClassMap),
                                  Collections.unmodifiableMap(postProcessorClassMap),
                                  Collections.unmodifiableMap(postValidatorClassMap));
    }

    /**
     * Define behaviour when encountering nulls in postprocessing.
     *
     * @param column name of column
     * @param value true: return null immediately, false: process null entries
     * @return the {@code DecoderManager} instance
     */
    public DecoderManager setNullFallthroughForPostProcessors(String column, boolean value) {
        getPropertyEditor(column).setNullFallthroughForPostProcessors(value);
        return this;
    }

    /**
     * Define behaviour when encountering nulls in postprocessing.
     *
     * @param column name of column
     * @param value true: return successfull validation for nulls, false: validate null entries
     * @return the {@code DecoderManager} instance
     */
    public DecoderManager setNullFallthroughForPostValidators(String column, boolean value) {
        getPropertyEditor(column).setNullFallthroughForPostValidators(value);
        return this;
    }

    private <R> DecoderPropertyEditor<R> getPropertyEditor(final String column) {
        final String columnToLower = column.toLowerCase();
        if (!decoderMap.containsKey(columnToLower)) {
            decoderMap.put(columnToLower, DecoderPropertyEditor.forColumn(column));
        }
        final DecoderPropertyEditor<R> propertyEditor = (DecoderPropertyEditor<R>) decoderMap.get(columnToLower);
        return propertyEditor;
    }

    private DecoderManager(Map<String, DecoderPropertyEditor<?>> decoderMap,
                           Map<Class<? extends Decoder<?>>, Decoder<?>> decoderClassMap,
                           Map<Class<? extends PostProcessor<?>>, PostProcessor<?>> postProcessorClassMap,
                           Map<Class<? extends PostValidator<?>>, PostValidator<?>> postValidatorClassMap) {
        this.decoderMap = decoderMap;
        this.decoderClassMap = decoderClassMap;
        this.postProcessorClassMap = postProcessorClassMap;
        this.postValidatorClassMap = postValidatorClassMap;
    }

    private DecoderManager() {
        this.decoderMap = new HashMap<>();
        this.decoderClassMap = new HashMap<>();
        this.postProcessorClassMap = new HashMap<>();
        this.postValidatorClassMap = new HashMap<>();
    }

}
