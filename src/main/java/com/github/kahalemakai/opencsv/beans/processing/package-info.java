/**
 * Components for decoding, post-processing and -validation of
 * csv columns into bean fields.
 * <p>
 * The {@link com.github.kahalemakai.opencsv.beans.processing.DecoderManager}
 * does all the bookkeeping. For every bean field, a
 * {@link com.github.kahalemakai.opencsv.beans.processing.DecoderPropertyEditor}
 * can be added, that keeps track of
 * {@link com.github.kahalemakai.opencsv.beans.processing.Decoder}s,
 * {@link com.github.kahalemakai.opencsv.beans.processing.PostProcessor}s and
 * {@link com.github.kahalemakai.opencsv.beans.processing.PostValidator}s.
 */
package com.github.kahalemakai.opencsv.beans.processing;