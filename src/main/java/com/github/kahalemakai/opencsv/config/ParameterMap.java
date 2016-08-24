package com.github.kahalemakai.opencsv.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j
@RequiredArgsConstructor(staticName = "init")
public class ParameterMap {
    private final Map<String, String> parameters = new HashMap<>();
    private final Pattern VALID_NAME = Pattern.compile("^[a-zA-Z]+[a-zA-Z0-9-_]*:[a-zA-Z]+[a-zA-Z0-9-_]*$");

    public void put(final String name, final String value) throws IllegalStateException, IllegalArgumentException {
        final Matcher matcher = VALID_NAME.matcher(name);
        if (!matcher.matches()) {
            final String msg = String.format("parameter '%s' is invalid. must match pattern %s", name, VALID_NAME.pattern());
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        if (parameters.containsKey(name)) {
            final String msg = String.format("ConfigParser already includes mapping for parameter '%s'", name);
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        parameters.put(name, value);

    }

    public Optional<String> get(final String name) {
        return Optional.ofNullable(parameters.get(name));
    }

}
