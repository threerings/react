//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

/**
 * Provides utility methods for {@link Signal}s.
 */
public class Signals
{
    /**
     * Converts the supplied signal into a signal that simply toggles a boolean value every time
     * the underlying signal fires.
     *
     * @param signal the signal that will trigger the toggling.
     * @param initial the initial value of the toggler (the first emission will be the opposite of
     * this value as it will be toggled).
     */
    public static <T> SignalView<Boolean> toggler (SignalView<T> signal, boolean initial) {
        return Values.toggler(signal, initial);
    }

    private Signals () {} // no constructski
}
