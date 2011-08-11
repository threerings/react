//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

import java.util.Set;

/**
 * Handles the machinery of connecting slots to a signal and emitting events to them, without
 * exposing a public interface for emitting events. This can be used by entities which wish to
 * expose a signal-like interface for listening, without allowing external callers to emit signals.
 */
public class AbstractSignal<T> extends Reactor<Slot<T>>
    implements SignalView<T>
{
    @Override public Connection connect (Slot<? super T> slot) {
        // alas, Java does not support higher kinded types; this cast is safe
        @SuppressWarnings("unchecked") Slot<T> casted = (Slot<T>)slot;
        return addConnection(casted);
    }

    /**
     * Emits the supplied event to all connected slots.
     */
    protected void notifyEmit (T event) {
        Cons<Slot<T>> lners = prepareNotify();
        try {
            for (Cons<Slot<T>> cons = lners; cons != null; cons = cons.next) {
                cons.listener.onEmit(event);
                if (cons.oneShot) cons.disconnect();
            }
        } finally {
            finishNotify(lners);
        }
    }
}
