//
// React - a library for functional-reactive-like programming
// Copyright (c) 2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

/**
 * Represents a computation that either provided a result, or failed with an exception. Monadic
 * methods are provided that allow one to map and compose tries in ways that propagate failure.
 * This class is not itself "reactive", but it facilitates a more straightforward interface and
 * implementation for {@link RFuture} and {@link RPromise}.
 */
public abstract class Try<T> {

    /** Represents a successful try. Contains the successful result. */
    public static final class Success<T> extends Try<T> {
        public final T value;
        public Success (T value) {
            this.value = value;
        }

        @Override public T get () { return value; }
        @Override public Throwable getFailure () { throw new IllegalStateException(); }
        @Override public boolean isSuccess () { return true; }
        @Override public <R> Try<R> map (Function<? super T, R> func) {
            return success(func.apply(value));
        }
        @Override public <R> Try<R> flatMap (Function<? super T, Try<R>> func) {
            return func.apply(value);
        }

        @Override public String toString () { return "Success(" + value + ")"; }
    }

    /** Represents a failed try. Contains the cause of failure. */
    public static final class Failure<T> extends Try<T> {
        public final Throwable cause;
        public Failure (Throwable cause) {
            this.cause = cause;
        }

        @Override public T get () {
            if (cause instanceof RuntimeException) {
                throw (RuntimeException)cause;
            } else if (cause instanceof Error) {
                throw (Error)cause;
            } else {
                throw (RuntimeException)new RuntimeException().initCause(cause);
            }
        }
        @Override public Throwable getFailure () { return cause; }
        @Override public boolean isSuccess () { return false; }
        @Override public <R> Try<R> map (Function<? super T, R> func) {
            return this.<R>casted();
        }
        @Override public <R> Try<R> flatMap (Function<? super T, Try<R>> func) {
            return this.<R>casted();
        }

        @Override public String toString () { return "Failure(" + cause + ")"; }

        @SuppressWarnings("unchecked") private <R> Try<R> casted () { return (Try<R>)this; }
    }

    /** Creates a successful try. */
    public static <T> Try<T> success (T value) { return new Success<T>(value); }

    /** Creates a failed try. */
    public static <T> Try<T> failure (Throwable cause) { return new Failure<T>(cause); }

    /** Lifts {@code func}, a function on values, to a function on tries. */
    public static <T,R> Function<Try<T>,Try<R>> lift (final Function<? super T, R> func) {
        return new Function<Try<T>,Try<R>>() {
            public Try<R> apply (Try<T> result) { return result.map(func); }
        };
    }

    /** Returns the value associated with a successful try, or rethrows the exception if the try
     * failed. If the exception is a checked exception, it will be thrown as a the {@code cause} of
     * a newly constructed {@link RuntimeException}. */
    public abstract T get ();

    /** Returns the cause of failure for a failed try. Throws {@link IllegalStateException} if
     * called on a successful try. */
    public abstract Throwable getFailure ();

    /** Returns try if this is a successful try, false if it is a failed try. */
    public abstract boolean isSuccess ();

    /** Returns try if this is a failed try, false if it is a successful try. */
    public boolean isFailure () { return !isSuccess(); }

    /** Maps successful tries through {@code func}, passees failure through as is. */
    public abstract <R> Try<R> map (Function<? super T, R> func);

    /** Maps successful tries through {@code func}, passes failure through as is. */
    public abstract <R> Try<R> flatMap (Function<? super T, Try<R>> func);

    private Try () {}
}
