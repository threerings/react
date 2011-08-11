//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

/**
 * A signal that emits an event with no associated data. It can be used with {@code Slot<Void>}
 * like so:
 * <pre>{@code
 * UnitSignal signal = new UnitSignal();
 * signal.connect(new Slot<Void>() {
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
}
