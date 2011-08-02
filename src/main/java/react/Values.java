//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

/**
 * Provides utility methods for {@link Value}s.
 */
public class Values
{
    /**
     * Creates a boolean value that is toggled every time the supplied signal fires.
     *
     * @param signal the signal that will trigger the toggling.
     * @param initial the initial value of the to be toggled value.
     */
    public static <T> ValueView<Boolean> toggler (SignalView<T> signal, boolean initial) {
        final Value<Boolean> value = Value.create(initial);
        signal.connect(new Slot<T>() {
            @Override public void onEmit () {
                value.update(!value.get());
            }
        });
        return value;
    }

    private Values () {} // no constructski
}
