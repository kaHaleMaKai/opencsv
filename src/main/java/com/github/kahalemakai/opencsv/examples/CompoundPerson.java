package com.github.kahalemakai.opencsv.examples;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CompoundPerson {
    private int age;
    private String givenName;
    private String surName;
    private Address address;
}
