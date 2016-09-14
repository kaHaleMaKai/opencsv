/**
 * Configure the CsvToBeanMapper using xml.
 * <p>
 * The xml configuration consists of two basic building
 * blocks: the {@code <csv:reader>} and the
 * {@code <bean:config>} tags. While the former sets
 * up the parsing of the csv data into columns,
 * the latter configures the mapping of columns
 * to the bean fields. I.e. both tags and their
 * respective nested elements map to methods
 * of the {@link com.github.kahalemakai.opencsv.beans.Builder}
 * class.
 */
package com.github.kahalemakai.opencsv.config;