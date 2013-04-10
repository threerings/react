//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

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

    /**
     * Create the Reactor with the given placeholderListener to be used when a weakly held listener is discovered to
     * have been collected while dispatching. The placeholder should noop when signalled.
     */
    public Reactor(L placeholderListener) {
        this.placeholderListener = placeholderListener;
    }

    /**
     * Returns true if this reactor has at least one connection.
     */
    public boolean hasConnections () {
        return _listeners != null;
    }

    protected synchronized Cons<L> addConnection (L listener) {
        if (listener == null) throw new NullPointerException("Null listener");
        return addCons(new StrongCons<L>(this, listener));
    }

    protected synchronized Cons<L> addConnectionWeak (L listener) {
        if (listener == null) throw new NullPointerException("Null listener");
        return addCons(new WeakCons<L>(this, listener));
    }

    protected synchronized Cons<L> addCons (final Cons<L> cons) {
        if (isDispatching()) {
            _pendingRuns = insert(_pendingRuns, new Runs() {
                public void run () {
                    _listeners = Cons.insert(_listeners, cons);
                    connectionAdded();
                }
            });
        } else {
            _listeners = Cons.insert(_listeners, cons);
            connectionAdded();
        }
        return cons;
    }

    protected synchronized Cons<L> prepareNotify () {
        if (_listeners == DISPATCHING)
            throw new IllegalStateException("Initiated notify while notifying");
        Cons<L> lners = _listeners;
        @SuppressWarnings("unchecked") Cons<L> sentinel = (Cons<L>)DISPATCHING;
        _listeners = sentinel;
        return lners;
    }

    protected synchronized void finishNotify (Cons<L> lners) {
        // note that we're no longer dispatching
        _listeners = lners;

        // now remove listeners any queued for removing and add any queued for adding
        for (; _pendingRuns != null; _pendingRuns = _pendingRuns.next) {
            _pendingRuns.run();
        }
    }

    protected synchronized void disconnect (final Cons<L> cons) {
        if (isDispatching()) {
            _pendingRuns = insert(_pendingRuns, new Runs() {
                public void run () {
                    _listeners = Cons.remove(_listeners, cons);
                    connectionRemoved();
                }
            });
        } else {
            _listeners = Cons.remove(_listeners, cons);
            connectionRemoved();
        }
    }

    protected synchronized void removeConnection (final Object listener) {
        if (isDispatching()) {
            _pendingRuns = insert(_pendingRuns, new Runs() {
                public void run () {
                    _listeners = Cons.removeAll(_listeners, listener);
                    connectionRemoved();
                }
            });
        } else {
            _listeners = Cons.removeAll(_listeners, listener);
            connectionRemoved();
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

    /**
     * Called when a connection has been added to this reactor.
     */
    protected void connectionAdded () {
        // noop
    }

    /**
     * Called when a connection may have been removed from this reactor.
     */
    protected void connectionRemoved () {
        // noop
    }

    protected static Runs insert (Runs head, Runs action) {
        if (head == null) return action;
        head.next = insert(head.next, action);
        return head;
    }

    // always called while lock is held on this reactor
    private final boolean isDispatching () {
        return _listeners == DISPATCHING;
    }

    protected Cons<L> _listeners;
    protected Runs _pendingRuns;
    final L placeholderListener;

    protected static abstract class Runs implements Runnable {
        public Runs next;
    }

    protected static final Cons<RListener> DISPATCHING = new StrongCons<RListener>(null, null);
}
