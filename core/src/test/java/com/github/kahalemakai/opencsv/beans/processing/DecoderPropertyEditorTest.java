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

        val = editor.decode("1");
        assertEquals(Integer.valueOf(1), val);

        val = editor.decode("11.5");
        assertEquals(Integer.valueOf(23), val);

        val = editor.decode("s");
        assertEquals(Integer.valueOf(23), val);

        val = editor.decode("2349283472342342234234");
        assertEquals(Integer.valueOf(23), val);
    }

    @Test
    public void defaultValueFromString() throws Exception {
        final DecoderPropertyEditor<Integer> editor = DecoderPropertyEditor.forColumn("example");
        Integer val;
        editor.add(new IntDecoder());
        editor.withDefaultFromString("23");

        val = editor.decode("1");
        assertEquals(Integer.valueOf(1), val);

        val = editor.decode("11.5");
        assertEquals(Integer.valueOf(23), val);

        val = editor.decode("s");
        assertEquals(Integer.valueOf(23), val);

        val = editor.decode("2349283472342342234234");
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