package com.github.kahalemakai.opencsv.examples;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.ByteBuffer;

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class DecoderArgsTester {
    private Integer number;
    private Boolean flag;
    private ByteBuffer decimal;
}
