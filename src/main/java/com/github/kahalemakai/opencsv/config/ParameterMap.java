package com.github.kahalemakai.opencsv.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bookkeeping of parameters to inject into xml configuration.
 */
@Log4j
@RequiredArgsConstructor(staticName = "init")
public class ParameterMap {
    /**
     * Backing map.
     */
    private final Map<String, String> parameters = new HashMap<>();
    /**
     * Pattern for valid parameter names (ns:name).
     */
    private final Pattern VALID_NAME = Pattern.compile("^[a-zA-Z]+[a-zA-Z0-9-_]*:[a-zA-Z]+[a-zA-Z0-9-_]*$");

    /**
     * Put a new key=value pair into the parameter map.
     * @param name name of parameter
     * @param value value of parameter
     * @throws IllegalArgumentException if name of the parameter is invalid
     * @throws IllegalStateException if the parameter has already been defined
     */
    public void put(final String name, final String value) throws IllegalArgumentException, IllegalStateException {
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

    /**
     * Get the value of a parameter, if exists.
     * @param name name of the parameter to look up
     * @return value of a parameter, if exists
     */
    public Optional<String> get(final String name) {
        return Optional.ofNullable(parameters.get(name));
    }

}
