/**
 * Included standard decoders.
 * <p>
 * This package includes a set of decoders for all primitives.
 * Furthermore, it adds among others
 * an {@link com.github.kahalemakai.opencsv.beans.processing.decoders.EnumDecoder}
 * as well as decoders for {@code null} references, {@code booleans} and
 * {@code decimals}.
 * <p>
 * The decoders for primitives can be referenced as unqualified class names
 * in the xlm configuration. The
 * {@link com.github.kahalemakai.opencsv.beans.processing.decoders.IntDecoder}
 * can be used as ({@code <bean:field name="nameOfField" type="int">} or
 * {@code <bean:decoder type="IntDecoder" />}.
 *
 * @see com.github.kahalemakai.opencsv.beans.processing.Decoder Decoder
 */
package com.github.kahalemakai.opencsv.beans.processing.decoders;