package sms;

/**
 * A generic data type storing a pair of objects
 * @param first The first object to store
 * @param second The second object to store
 * @param <F> The type of the first stored object
 * @param <S> The type of the second stored object
 */
public record Pair<F, S>(F first, S second) {}
