package com.github.kahalemakai.opencsv.examples;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.val;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PersonWithGender extends Person {

    private Gender gender;

    public static PersonWithGender ofPerson(Person person) {
        val p = new PersonWithGender();
        p.setAge(person.getAge());
        p.setAddress(person.getAddress());
        p.setGivenName(person.getGivenName());
        p.setSurName(person.getSurName());
        p.setGender(Gender.UNKNOWN);
        return p;
    }

    public static enum Gender {
        MALE,
        FEMALE,
        UNKNOWN
    }

}
