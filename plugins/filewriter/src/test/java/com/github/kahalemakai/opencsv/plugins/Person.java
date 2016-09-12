package com.github.kahalemakai.opencsv.plugins;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class Person {
    private Integer age;
    private String givenName;
    private String surName;
    private String address;
}
