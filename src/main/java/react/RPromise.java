//
// React - a library for functional-reactive-like programming
// Copyright (c) 2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

/**
 * Provides a concrete implementation {@link RFuture} that can be updated with a success or failure
 * result when it becomes available.
 *
 * <p>This implementation also guarantees a useful behavior, which is that all listeners added
 * prior to the completion of the promise will be cleared when the promise is completed, and no
 * further listeners will be retained. This allows the promise to be retained after is has been
 * completed as a useful "box" for its underlying value, without concern that references to long
 * satisfied listeners will be inadvertently retained.</p>
 */
public class RPromise<T> extends RFuture<T> {

    /** Creates a new, uncompleted, promise. */
    public static <T> RPromise<T> create () {
        return new RPromise<T>();
    }

    /** Causes this promise to be completed successfully with {@code value}. */
    public void succeed (T value) {
        _result.update(Try.success(value));
    }

    /** Causes this promise to be completed with failure caused by {@code cause}. */
    public void fail (Throwable cause) {
        _result.update(Try.<T>failure(cause));
    }

    /** Returns a slot that can be used to complete this promise. */
    public Slot<Try<T>> completer () {
        return _result.slot();
    }

    /** Returns a slot that can be used to {@link #succeed} this promise. */
    public Slot<T> succeeder () {
        return new Slot<T>() {
            public void onEmit (T result) {
                succeed(result);
            }
        };
    }

    /** Returns a slot that can be used to {@link #fail} this promise. */
    public Slot<Throwable> failer () {
        return new Slot<Throwable>() {
            public void onEmit (Throwable cause) {
                fail(cause);
            }
        };
    }

    /** Returns true if there are listeners awaiting the completion of this promise. */
    public boolean hasConnections () {
        return _result.hasConnections();
    }

    protected RPromise () {
        this(new Value<Try<T>>(null) {
            @Override protected synchronized Try<T> updateAndNotify (Try<T> value, boolean force) {
                if (_value != null) throw new IllegalStateException("Already completed");
                try {
                    return super.updateAndNotify(value, force);
                } finally {
                    _listeners = null; // clear out our listeners now that they have been notified
                }
            }
        });
    }

    private RPromise (Value<Try<T>> result) {
        super(result);
        _result = result;
    }

    protected final Value<Try<T>> _result;
}
