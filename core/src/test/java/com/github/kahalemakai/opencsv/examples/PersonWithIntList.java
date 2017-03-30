package com.github.kahalemakai.opencsv.examples;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PersonWithIntList extends Person {
    private List<Integer> list = new ArrayList<>();
}
