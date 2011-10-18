//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

/**
 * Reacts to signal emissions. The priority of a slot can be changed from the default by overriding
 * {@link #priority}.
 */
public abstract class Slot<T> extends ValueView.Listener<T>
{
    /**
     * Called when a signal to which this slot is connected has emitted an event.
     * @param event the event emitted by the signal.
     */
    public abstract void onEmit (T event);

    /**
     * Creates a slot with the specified priority, which passes event emission onto this slot. This
     * is useful for situations where one obtains a slot from code they don't control and wish to
     * add it to a signal with a custom priority.
     */
    public Slot<T> atPriority (final int priority) {
        final Slot<T> outer = this;
        return new Slot<T>() {
            @Override public void onEmit (T event) {
                outer.onEmit(event);
            }
            @Override public int priority () {
                return priority;
            }
        };
    }

    /**
     * Allows a slot to be used as a {@link ValueView.Listener} by passing just the new value
     * through to {@link #onEmit}.
     */
    @Override public void onChange (T value, T oldValue) {
        onEmit(value);
    }
}
