//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

/**
 * A base class for all reactive classes. This is an implementation detail, but is public so that
 * third parties may use it to create their own reactive classes, if desired.
 */
public abstract class Reactor
{
    /** The base class for all reactor listeners. */
    public abstract static class RListener {}

    /**
     * Returns true if this reactor has at least one connection.
     */
    public boolean hasConnections () {
        return _listeners != null;
    }

    /** Returns the listener to be used when a weakly held listener is discovered to have been
     * collected while dispatching. This listener should NOOP when signaled. */
    abstract RListener placeholderListener ();

    protected synchronized Cons addConnection (RListener listener) {
        if (listener == null) throw new NullPointerException("Null listener");
        return addCons(new Cons(this, listener));
    }

    protected synchronized Cons addCons (final Cons cons) {
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

    protected synchronized void disconnect (final Cons cons) {
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

    protected synchronized void removeConnection (final RListener listener) {
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

    /**
     * Emits the supplied event to all connected slots. We omit a bunch of generic type shenanigans
     * here and force the caller to just cast things, because this is all under the hood where
     * there's zero chance of fucking up and this results in simpler, easier to read code.
     */
    protected void notify (Notifier notifier, Object a1, Object a2, Object a3) {
        Cons lners;
        synchronized (this) {
            if (_listeners == DISPATCHING)
                throw new IllegalStateException("Initiated notify while notifying");
            lners = _listeners;
            Cons sentinel = DISPATCHING;
            _listeners = sentinel;
        }

        RuntimeException exn = null;
        try {
            for (Cons cons = lners; cons != null; cons = cons.next) {
                try {
                    notifier.notify(cons.listener(), a1, a2, a3);
                } catch (RuntimeException ex) {
                    // Java7: if (exn != null) exn.addSuppressed(ex)
                    exn = ex;
                }
                if (cons.oneShot()) cons.disconnect();
            }
            if (exn != null) throw exn;

        } finally {
            synchronized (this) {
                // note that we're no longer dispatching
                _listeners = lners;
                // now remove listeners any queued for removing and add any queued for adding
                for (; _pendingRuns != null; _pendingRuns = _pendingRuns.next) {
                    _pendingRuns.run();
                }
            }
        }
    }

    /**
     * Returns true if both values are null, reference the same instance, or are
     * {@link Object#equals}.
     */
    protected static <T> boolean areEqual (T o1, T o2) {
        return (o1 == o2 || (o1 != null && o1.equals(o2)));
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

    protected Cons _listeners;
    protected Runs _pendingRuns;

    protected static abstract class Runs implements Runnable {
        public Runs next;
    }

    protected static abstract class Notifier {
        public abstract void notify (Object listener, Object a1, Object a2, Object a3);
    }

    protected static final Cons DISPATCHING = new Cons(null, null);
}
