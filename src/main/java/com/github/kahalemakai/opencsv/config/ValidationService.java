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

package com.github.kahalemakai.opencsv.config;

import lombok.extern.log4j.Log4j;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.IOException;
import java.net.URL;

@Log4j
public final class ValidationService {
    private ValidationService() { }

    public static void validate(final File xmlFile) throws SAXException, IOException {
        URL schemaFile = ValidationService.class.getClassLoader().getResource("schemas/opencsv.xsd");
        Source xmlSource = new StreamSource(xmlFile);
        SchemaFactory schemaFactory = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = null;
        try {
            schema = schemaFactory.newSchema(schemaFile);
        } catch (SAXException e) {
            throw new SAXException(e);
        }
        javax.xml.validation.Validator validator = schema.newValidator();
        try {
            validator.validate(xmlSource);
            log.debug(String.format("xml file '%s' is valid'", xmlFile));
        } catch (SAXException e) {
            final String msg = String.format("xml file '%s' is invalid", xmlFile);
            log.error(msg, e);
            throw new SAXException(msg, e);
        } catch (IOException e) {
            final String msg = String.format("error while trying to open '%s' as StreamSource", xmlFile);
            log.error(msg, e);
            throw new IOException(msg, e);
        }

    }

}
