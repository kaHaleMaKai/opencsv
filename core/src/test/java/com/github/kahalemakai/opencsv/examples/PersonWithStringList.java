package com.github.kahalemakai.opencsv.examples;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PersonWithStringList extends Person {
    private List<String> list = new ArrayList<>();
}
