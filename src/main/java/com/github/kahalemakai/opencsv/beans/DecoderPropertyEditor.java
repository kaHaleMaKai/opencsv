package com.github.kahalemakai.opencsv.beans;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.beans.PropertyEditorSupport;

@RequiredArgsConstructor(staticName = "of")
public class DecoderPropertyEditor<T> extends PropertyEditorSupport {
    private final Decoder<T> decoder;
    private String data;
    @Getter @Setter
    private boolean trimField = false;

    @Override
    public T getValue() {
        System.out.println(data);
        return decoder.decode(data);
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        this.data = isTrimField() ? text.trim() : text;
    }

}
