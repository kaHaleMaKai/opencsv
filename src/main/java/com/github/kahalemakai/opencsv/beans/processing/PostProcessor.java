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

@FunctionalInterface
public interface PostProcessor<T> {
    T process(T value);

    PostProcessor<?> IDENTITY = x -> x;

    default PostProcessor<T> andThen(@NonNull PostProcessor<T> after) {
        return (T t) -> after.process(process(t));
    }

    static <R> PostProcessor<R> identity() {
        @SuppressWarnings("unchecked")
        final PostProcessor<R> identity = (PostProcessor<R>) IDENTITY;
        return identity;
    }

    static <R> PostProcessor<R> compose(PostProcessor<R> first, @NonNull PostProcessor<R> then) {
        if (first == null) {
            return then;
        }
        else {
            return first.andThen(then);
        }

    }
}
