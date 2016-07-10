package com.github.kahalemakai.opencsv.examples;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Address {
    private String street;
    private String zipCode;
    private String city;
    private String country;
}
