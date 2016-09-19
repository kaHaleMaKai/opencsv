package com.github.kahalemakai.opencsv.beans;

/**
 * Execute a parameter-less, void method that is allowed
 * to throw checked exceptions.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #act()}.
 *
 * @param <E> type of checked exception to throw
 */
@FunctionalInterface
interface ExceptionalAction<E extends Exception> {
    /**
     * Carry out the action.
     * @throws E the exception that is allowed to be thrown
     */
    void act() throws E;
}
