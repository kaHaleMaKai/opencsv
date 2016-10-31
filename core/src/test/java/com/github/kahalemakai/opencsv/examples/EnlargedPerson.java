package com.github.kahalemakai.opencsv.examples;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString(callSuper = true)
@Getter @Setter
public class EnlargedPerson extends Person {
    private String drink;
    private Integer favoriteNumber;

    public static EnlargedPerson of(final Person person) {
        final EnlargedPerson e = new EnlargedPerson();
        e.setAge(person.getAge());
        e.setAddress(person.getAddress());
        e.setSurName(person.getSurName());
        e.setGivenName(person.getGivenName());
        return e;
    }
}
