//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package com.threerings.react;

/**
 * Reacts to signal emissions. Users must override either {@link #onEmit(T)} or {@link #onEmit()},
 * depending on whether they care about the emitted event. The priority of a slot can be changed
 * from the default by overriding {@link #priority}.
 */
public abstract class Slot<T>
{
    /**
     * Called when a signal to which this signal is connected has emitted an event. The default
     * implementation calls the event-forgetting {@code onEvent} method.
     * @param event the event emitted by the signal.
     */
    public void onEmit (T event) {
        onEmit();
    }

    /**
     * Called when a signal to which this signal is connected has emitted an event. This is only
     * called if the non-event-forgetting method has not been overridden.
     */
    public void onEmit () {
        // noop
    }

    /**
     * Returns the priority of this slot. Slots will be notified by signals in order of priority.
     */
    public int priority () {
        return 0;
    }

    /**
     * Creates a slot with the specified priority, which passes event emission onto this slot. This
     * is useful for situations where one obtains a slot from code they don't control and wish to
     * add it to a signal with a custom priority.
     */
    public Slot<T> atPriority (final int priority) {
        final Slot<T> outer = this;
        return new Slot<T>() {
            public void onEmit (T event) {
                outer.onEmit(event);
            }
            public int priority () {
                return priority;
            }
        };
    }
}
