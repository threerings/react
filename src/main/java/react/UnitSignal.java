//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

/**
 * A signal that emits an event with no associated data. It can be used with {@code UnitSlot}
 * like so:
 * <pre>{@code
 * UnitSignal signal = new UnitSignal();
 * signal.connect(new UnitSlot() {
 *   public void onEmit () {
 *     // ...
 *   }
 * });
 * }</pre>
 */
public class UnitSignal extends AbstractSignal<Void>
{
    /**
     * Causes this signal to emit an event to its connected slots.
     */
    public void emit () {
        notifyEmit(null);
    }

    /**
     * Returns a slot which can be used to wire this signal to the emissions of a {@link Signal} or
     * another value.
     */
    public UnitSlot slot () {
        return new UnitSlot () {
            @Override public void onEmit () { emit(); }
        };
    }
}
