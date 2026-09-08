package com.stackroute.helpdesk.util;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Modern Java 17 Sealed Interface representing the outcome of a service-layer operation.
 * Permits exhaustive pattern-matching without unchecked casting.
 *
 * @param <T> Payload data type
 */
public sealed interface ServiceResult<T> permits ServiceResult.Success, ServiceResult.Failure {

    /**
     * Immutable successful result containing the output payload and a success message.
     */
    record Success<T>(T data, String message) implements ServiceResult<T> {}

    /**
     * Immutable failure result containing an error code and human-readable diagnostic message.
     */
    record Failure<T>(String errorCode, String errorMessage) implements ServiceResult<T> {}

    // --- Static Factory Methods ---
    static <T> ServiceResult<T> ok(T data) {
        return new Success<>(data, "Operation completed successfully.");
    }

    static <T> ServiceResult<T> ok(T data, String message) {
        return new Success<>(data, message);
    }

    static <T> ServiceResult<T> fail(String errorMessage) {
        return new Failure<>("GENERIC_FAILURE", errorMessage);
    }

    static <T> ServiceResult<T> fail(String errorCode, String errorMessage) {
        return new Failure<>(errorCode, errorMessage);
    }

    // --- Helper Inspection & Functional Methods ---
    default boolean isSuccess() {
        return this instanceof Success<T>;
    }

    default boolean isFailure() {
        return this instanceof Failure<T>;
    }

    default Optional<T> getData() {
        if (this instanceof Success<T> s) {
            return Optional.ofNullable(s.data());
        }
        return Optional.empty();
    }

    default String getMessage() {
        if (this instanceof Success<T> s) {
            return s.message();
        } else if (this instanceof Failure<T> f) {
            return f.errorMessage();
        }
        return "";
    }

    default String getErrorCode() {
        if (this instanceof Failure<T> f) {
            return f.errorCode();
        }
        return "";
    }

    default String getErrorMessage() {
        return getMessage();
    }

    default void ifSuccess(Consumer<T> consumer) {
        if (this instanceof Success<T> s && s.data() != null) {
            consumer.accept(s.data());
        }
    }

    default void ifFailure(Consumer<String> consumer) {
        if (this instanceof Failure<T> f) {
            consumer.accept(f.errorMessage());
        }
    }

    default <R> ServiceResult<R> map(Function<? super T, ? extends R> mapper) {
        if (this instanceof Success<T> s) {
            return ServiceResult.ok(mapper.apply(s.data()), s.message());
        } else if (this instanceof Failure<T> f) {
            return ServiceResult.fail(f.errorCode(), f.errorMessage());
        }
        return ServiceResult.fail("UNKNOWN", "Unknown result state");
    }
}