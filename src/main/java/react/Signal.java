//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

/**
 * A signal that emits events of type {@code T}. {@link Slot}s may be connected to a signal to be
 * notified upon event emission.
 */
public class Signal<T> extends AbstractSignal<T>
{
    /**
     * Convenience method for creating a signal without repeating the type parameter.
     */
    public static <T> Signal<T> create () {
        return new Signal<T>();
    }

    /**
     * Causes this signal to emit the supplied event to connected slots.
     */
    public void emit (T event) {
        notifyEmit(event);
    }

    /**
     * Returns a slot which can be used to wire this signal to the emissons of a {@link Signal} or
     * another value.
     */
    public Slot<T> slot () {
        return new Slot<T> () {
            @Override public void onEmit (T value) {
                emit(value);
            }
        };
    }
}
