package com.github.kahalemakai.opencsv.beans.processing;

import java.beans.PropertyEditor;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DecoderManager {
    private final Map<String, DecoderPropertyEditor<?>> decoderMap;
    private final Map<Class<? extends Decoder<?, ? extends Throwable>>, Decoder<?, ? extends Throwable>> decoderClassMap;
    private final Map<Class<? extends PostProcessor<?>>, PostProcessor<?>> postProcessorClassMap;
    private final Map<Class<? extends PostValidator>, PostValidator> postValidatorClassMap;

    public static DecoderManager init() {
        return new DecoderManager();
    }

    public <T> DecoderManager add(final String column, Decoder<? extends T, ? extends Throwable> decoder) {
        if (!decoderMap.containsKey(column)) {
            decoderMap.put(column, DecoderPropertyEditor.init());
        }
        @SuppressWarnings("unchecked")
        final DecoderPropertyEditor<T> propertyEditor = (DecoderPropertyEditor<T>) decoderMap.get(column);
        propertyEditor.add(decoder);
        return this;
    }

    public DecoderManager add(final String column,
                                  Class<? extends Decoder<?, ? extends Throwable>> decoderClass)
                              throws InstantiationException {
        if (!decoderClassMap.containsKey(decoderClass)) {
            try {
                final Decoder<?, ? extends Throwable> decoder = decoderClass.newInstance();
                decoderClassMap.put(decoderClass, decoder);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new InstantiationException(e.getMessage());
            }
        }
        return add(column, decoderClassMap.get(decoderClass));
    }

    public <T> DecoderManager setPostProcessor(final String column, PostProcessor<T> postProcessor) {
        if (!decoderMap.containsKey(column)) {
            decoderMap.put(column, DecoderPropertyEditor.init());
        }
        @SuppressWarnings("unchecked")
        final DecoderPropertyEditor<T> propertyEditor = (DecoderPropertyEditor<T>) decoderMap.get(column);
        propertyEditor.setPostProcessor(postProcessor);
        return this;
    }

    public <T> DecoderManager setPostProcessor(final String column, Class<? extends PostProcessor<T>> postProcessorClass)
            throws InstantiationException {
        if (!postProcessorClassMap.containsKey(postProcessorClass)) {
            try {
                final PostProcessor postProcessor = postProcessorClass.newInstance();
                postProcessorClassMap.put(postProcessorClass, postProcessor);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new InstantiationException(e.getMessage());
            }
        }
        return setPostProcessor(column, postProcessorClassMap.get(postProcessorClass));
    }

    public DecoderManager addPostValidator(final String column, PostValidator postValidator) {
        if (!decoderMap.containsKey(column)) {
            decoderMap.put(column, DecoderPropertyEditor.init());
        }
        final DecoderPropertyEditor<?> propertyEditor = decoderMap.get(column);
        propertyEditor.addPostValidator(postValidator);
        return this;
    }

    public DecoderManager addPostValidator(final String column, Class<? extends PostValidator> postValidatorClass)
            throws InstantiationException {
        if (!postValidatorClassMap.containsKey(postValidatorClass)) {
            try {
                final PostValidator postValidator = postValidatorClass.newInstance();
                postValidatorClassMap.put(postValidatorClass, postValidator);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new InstantiationException(e.getMessage());
            }
        }
        return addPostValidator(column, postValidatorClassMap.get(postValidatorClass));
    }

    public Optional<PropertyEditor> get(final String column) {
        return Optional.ofNullable(decoderMap.get(column));
    }

    public DecoderManager immutableCopy() {
        return new DecoderManager(decoderMap, decoderClassMap, postProcessorClassMap, postValidatorClassMap);
    }

    private DecoderManager(Map<String, DecoderPropertyEditor<?>> decoderMap,
                           Map<Class<? extends Decoder<?, ? extends Throwable>>,
                               Decoder<?, ? extends Throwable>> decoderClassMap,
                           Map<Class<? extends PostProcessor<?>>, PostProcessor<?>> postProcessorClassMap,
                           Map<Class<? extends PostValidator>, PostValidator> postValidatorClassMap) {
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
