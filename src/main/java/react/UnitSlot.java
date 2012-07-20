//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

/**
 * A {@link Slot} for use when the type of emitted signal is ignored. As slots are contravariant,
 * this slot may be wired to any signal without bothering to determine its type. Instances of this
 * class also implement {@link Runnable} so that they may be invoked by code that expects runnables
 * rather than no-arg slots.
 */
public abstract class UnitSlot extends Slot<Object> implements Runnable
{
    // if you're using unit slot, you're not allow to see the event
    @Override public final void onEmit (Object event) {
        onEmit();
    }

    /**
     * Called when a signal to which this slot is connected has emitted an event.
     */
    public abstract void onEmit ();

    @Override // from Runnable
    public void run () {
        onEmit();
    }

    /**
     * Returns a new slot that invokes this slot and then evokes {@code after}.
     */
    public UnitSlot andThen (final UnitSlot after) {
        final UnitSlot before = this;
        return new UnitSlot() {
            public void onEmit () {
                before.onEmit();
                after.onEmit();
            }
        };
    }
}
