package com.github.kahalemakai.opencsv.beans.processing;

import com.github.kahalemakai.opencsv.beans.processing.decoders.IntDecoder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DecoderPropertyEditorTest {
    @Test
    public void defaultValue() throws Exception {
        final DecoderPropertyEditor<Integer> editor = DecoderPropertyEditor.forColumn("example");
        Integer val;
        editor.add(new IntDecoder());
        editor.withDefault(23);

        editor.setAsText("1");
        val = editor.getValue();
        assertEquals(Integer.valueOf(1), val);

        editor.setAsText("11.5");
        val = editor.getValue();
        assertEquals(Integer.valueOf(23), val);

        editor.setAsText("s");
        val = editor.getValue();
        assertEquals(Integer.valueOf(23), val);

        editor.setAsText("2349283472342342234234");
        val = editor.getValue();
        assertEquals(Integer.valueOf(23), val);
    }

    @Test
    public void defaultValueFromString() throws Exception {
        final DecoderPropertyEditor<Integer> editor = DecoderPropertyEditor.forColumn("example");
        Integer val;
        editor.add(new IntDecoder());
        editor.withDefaultFromString("23");

        editor.setAsText("1");
        val = editor.getValue();
        assertEquals(Integer.valueOf(1), val);

        editor.setAsText("11.5");
        val = editor.getValue();
        assertEquals(Integer.valueOf(23), val);

        editor.setAsText("s");
        val = editor.getValue();
        assertEquals(Integer.valueOf(23), val);

        editor.setAsText("2349283472342342234234");
        val = editor.getValue();
        assertEquals(Integer.valueOf(23), val);
    }

    @Test(expected = IllegalStateException.class)
    public void defaultValueThrows() throws Exception {
        final DecoderPropertyEditor<Integer> editor = DecoderPropertyEditor.forColumn("example");
        editor.add(new IntDecoder());
        editor.withDefault(23).withDefault(24);
    }

    @Test(expected = IllegalStateException.class)
    public void defaultValueFromStringThrows() throws Exception {
        final DecoderPropertyEditor<Integer> editor = DecoderPropertyEditor.forColumn("example");
        editor.add(new IntDecoder());
        editor.withDefaultFromString("23").withDefaultFromString("24");
    }
}