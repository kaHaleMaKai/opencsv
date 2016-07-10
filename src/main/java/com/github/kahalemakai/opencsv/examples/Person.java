package com.github.kahalemakai.opencsv.examples;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class Person {
    private int age;
    private String givenName;
    private String surName;
    private String address;
}
