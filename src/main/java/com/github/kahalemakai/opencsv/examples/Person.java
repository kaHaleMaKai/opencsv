package com.github.kahalemakai.opencsv.examples;

import lombok.Data;

@Data
public class Person {
    private Integer age;
    private String givenName;
    private String surName;
    private String address;

    public void setAge(Integer age) {
        this.age = age;
    }
}
