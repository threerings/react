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
public class AbstractSignal<T> implements SignalView<T>
{
    @Override public Connection connect (Slot<T> slot) {
        Cons cons = new Cons(slot);
        if (isDispatching()) {
            cons.next = _toAdd;
            _toAdd = cons;
        } else {
            _slots = Cons.insert(_slots, cons);
        }
        return cons;
    }

    /**
     * Emits the supplied event to all connected slots.
     */
    protected void emitToSlots (T event) {
        // note that we're dispatching
        Cons slots = _slots;
        _slots = (Cons)DISPATCHING;

        // now dispatch to all existing slots
        Cons cons = slots;
        while (cons != null) {
            cons.slot.onEmit(event);
            if (cons.oneShot) cons.disconnect();
            cons = cons.next;
        }

        // note that we're no longer dispatching
        _slots = slots;

        // now remove slots any queued for removing and add any queued for adding
        if (_toRemove != null) {
            _slots = Cons.removeAll(_slots, _toRemove);
            _toRemove = null;
        }
        if (_toAdd != null) {
            _slots = Cons.insertAll(_slots, _toAdd);
            _toAdd = null;
        }
    }

    protected final boolean isDispatching () {
        return _slots == DISPATCHING;
    }

    protected class Cons extends AbstractConnection<Cons> {
        public final Slot<T> slot;

        public Cons (Slot<T> slot) {
            this.slot = slot;
        }

        @Override public void disconnect () {
            if (isDispatching()) {
                _toRemove = Cons.queueRemove(_toRemove, this);
            } else {
                _slots = Cons.remove(_slots, this);
            }
        }

        @Override public int priority () {
            return slot.priority();
        }
    }

    protected Cons _slots;
    protected Cons _toAdd;
    protected Set<Cons> _toRemove;

    protected static final Object DISPATCHING = new AbstractSignal<Void>().new Cons(null);
}
