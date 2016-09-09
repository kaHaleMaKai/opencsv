package com.github.kahalemakai.opencsv.config;

import com.github.kahalemakai.opencsv.beans.Builder;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public interface SinkPlugin extends Plugin {
    Logger log = Logger.getLogger(SinkPlugin.class);

    Sink getSink();

    <T> void configure(Builder<T> builder, Node sink);

    @Override
    default <T> void configure(Builder<T> builder, Document doc) {
        final NodeList nodeList = doc.getElementsByTagNameNS(getNameSpace(), "config");
        if (nodeList.getLength() == 0) {
            final String msg = "added sink plugin, but no config-tag found";
            log.warn(msg);
        }
        configure(builder, nodeList.item(0));
    }

}
