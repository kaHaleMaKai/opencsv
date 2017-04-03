package com.github.kahalemakai.opencsv.examples;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PersonWithUninitializedIntList extends Person {
    private List<Integer> list;
}
