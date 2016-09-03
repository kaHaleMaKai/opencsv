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

/**
 * Post-process a (decoded) element.
 * <p>
 * This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #process(Object)}.
 *
 * @param <T> type of object to be post-processed
 */
@FunctionalInterface
public interface PostProcessor<T> {
    /**
     * Post-process an object.
     * <p>
     * Unsuccessful post-processing should throw
     * any kind of runtime exception.
     *
     * @param value object to be post-processed
     * @return the post-processed object
     */
    T process(T value);

    /**
     * The identity transformation.
     */
    PostProcessor<?> IDENTITY = x -> x;

    /**
     * Apply the functional composition of two consecutive {@code PostProcessors}.
     * @param after the {@code PostProcessor} to execute afterwards
     * @return the functional composition of two consecutive {@code PostProcessors}
     */
    default PostProcessor<T> andThen(@NonNull PostProcessor<T> after) {
        return (T t) -> after.process(process(t));
    }

    /**
     * Return the identity transformation, appropriately cast.
     * @param <R> type of object to transform
     * @return the identity transformation
     */
    static <R> PostProcessor<R> identity() {
        @SuppressWarnings("unchecked")
        final PostProcessor<R> identity = (PostProcessor<R>) IDENTITY;
        return identity;
    }

    /**
     * Compose a new {@code PostProcessor} out of two existing ones.
     *
     * @param first first {@code PostProcessor}
     * @param then second {@code PostProcessor}
     * @param <R> type of object to transform
     * @return the functional composition of both {@code PostProcessors}
     */
    static <R> PostProcessor<R> compose(PostProcessor<R> first, @NonNull PostProcessor<R> then) {
        if (first == null) {
            return then;
        }
        else {
            return first.andThen(then);
        }

    }
}
