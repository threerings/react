//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

/**
 * Provides utility methods for {@link Slot}s.
 */
public class Slots
{
    /**
     * Returns a slot that logs the supplied message (via {@link System#err}) with the emitted
     * value appended to it before passing the emitted value on to {@code slot}. Useful for
     * debugging.
     */
    public static <T> Slot<T> trace (final String message, final Slot<T> slot) {
        return new Slot<T>() {
            public void onEmit (T value) {
                System.err.println(message + value);
                slot.onEmit(value);
            }
        };
    }

    private Slots () {} // no constructski
}
