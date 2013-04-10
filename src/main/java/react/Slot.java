//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

/**
 * Reacts to signal emissions.
 */
public abstract class Slot<T> extends ValueView.Listener<T>
{
    /**
     * Called when a signal to which this slot is connected has emitted an event.
     * @param event the event emitted by the signal.
     */
    public abstract void onEmit (T event);

    /**
     * Returns a new slot that invokes this slot and then evokes {@code after}.
     */
    public <S extends T> Slot<S> andThen (final Slot<? super S> after) {
        final Slot<T> before = this;
        return new Slot<S>() {
            public void onEmit (S event) {
                before.onEmit(event);
                after.onEmit(event);
            }
        };
    }

    /**
     * Allows a slot to be used as a {@link ValueView.Listener} by passing just the new value
     * through to {@link #onEmit}.
     */
    @Override public final void onChange (T value, T oldValue) {
        onEmit(value);
    }
}
