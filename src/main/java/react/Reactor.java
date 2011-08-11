//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

import java.util.Set;

/**
 * A base class for all reactive classes. This is an implementation detail, but is public so that
 * third parties may use it to create their own reactive classes, if desired.
 */
public abstract class Reactor<L extends Reactor.RListener>
{
    /** The base class for all reactor listeners. Handles priority. */
    public abstract static class RListener {
        /** Returns the priority of this listener. Listeners are notified in order of priority. */
        public int priority () {
            return 0;
        }
    }

    protected Cons<L> addConnection (L listener) {
        Cons<L> cons = new Cons<L>(this, listener);
        if (isDispatching()) {
            cons.next = _toAdd;
            _toAdd = cons;
        } else {
            _listeners = Cons.insert(_listeners, cons);
        }
        return cons;
    }

    protected final boolean isDispatching () {
        return _listeners == DISPATCHING;
    }

    protected Cons<L> prepareNotify () {
        Cons<L> lners = _listeners;
        @SuppressWarnings("unchecked") Cons<L> sentinel = (Cons<L>)DISPATCHING;
        _listeners = sentinel;
        return lners;
    }

    protected void finishNotify (Cons<L> lners) {
        // note that we're no longer dispatching
        _listeners = lners;

        // now remove listeners any queued for removing and add any queued for adding
        if (_toRemove != null) {
            _listeners = Cons.removeAll(_listeners, _toRemove);
            _toRemove = null;
        }
        if (_toAdd != null) {
            _listeners = Cons.insertAll(_listeners, _toAdd);
            _toAdd = null;
        }
    }

    protected void disconnect (Cons<L> cons) {
        if (isDispatching()) {
            _toRemove = Cons.queueRemove(_toRemove, cons);
        } else {
            _listeners = Cons.remove(_listeners, cons);
        }
    }

    /**
     * Returns true if both values are null, reference the same instance, or are {@link
     * Object#equals}.
     */
    protected static <T> boolean areEqual (T o1, T o2) {
        return (o1 == o2 || (o1 != null && o1.equals(o2)));
    }

    /**
     * Called prior to mutating any underlying model; allows subclasses to reject mutation.
     */
    protected void checkMutate () {
        // noop
    }

    protected Cons<L> _listeners;
    protected Cons<L> _toAdd;
    protected Set<Cons<L>> _toRemove;

    protected static final Cons<RListener> DISPATCHING = new Cons<RListener>(null, null);
}
