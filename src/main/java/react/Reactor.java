//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

import java.util.ArrayList;
import java.util.List;

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

    protected synchronized Cons<L> addConnection (L listener) {
        final Cons<L> cons = new Cons<L>(this, listener);
        if (isDispatching()) {
            doAfterDispatch(new Runnable() {
                public void run () {
                    _listeners = Cons.insert(_listeners, cons);
                }
            });
        } else {
            _listeners = Cons.insert(_listeners, cons);
        }
        return cons;
    }

    protected synchronized Cons<L> prepareNotify () {
        Cons<L> lners = _listeners;
        @SuppressWarnings("unchecked") Cons<L> sentinel = (Cons<L>)DISPATCHING;
        _listeners = sentinel;
        return lners;
    }

    protected synchronized void finishNotify (Cons<L> lners) {
        // note that we're no longer dispatching
        _listeners = lners;

        // now remove listeners any queued for removing and add any queued for adding
        if (_pendingActions != null) {
            for (Runnable action : _pendingActions) {
                action.run();
            }
            _pendingActions = null;
        }
    }

    protected synchronized void disconnect (final Cons<L> cons) {
        if (isDispatching()) {
            doAfterDispatch(new Runnable() {
                public void run () {
                    _listeners = Cons.remove(_listeners, cons);
                }
            });
        } else {
            _listeners = Cons.remove(_listeners, cons);
        }
    }

    protected synchronized void removeConnection (final L listener) {
        if (isDispatching()) {
            doAfterDispatch(new Runnable() {
                public void run () {
                    _listeners = Cons.removeAll(_listeners, listener);
                }
            });
        } else {
            _listeners = Cons.removeAll(_listeners, listener);
        }
    }

    /**
     * Returns true if both values are null, reference the same instance, or are
     * {@link Object#equals}.
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

    // always called while lock is held on this reactor
    protected void doAfterDispatch (Runnable action) {
        if (_pendingActions == null) _pendingActions = new ArrayList<Runnable>();
        _pendingActions.add(action);
    }

    // always called while lock is held on this reactor
    private final boolean isDispatching () {
        return _listeners == DISPATCHING;
    }

    protected Cons<L> _listeners;
    protected List<Runnable> _pendingActions;

    protected static final Cons<RListener> DISPATCHING = new Cons<RListener>(null, null);
}
