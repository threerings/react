//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

import react.Reactor.RListener;

import java.lang.ref.WeakReference;

/**
 * Implements {@link Connection} and a linked-list style listener list for {@link Reactor}s.
 */
class Cons<L extends RListener> implements Connection
{
    /** The next connection in our chain. */
    public Cons<L> next;

    /** Indicates whether this connection is one-shot or persistent. */
    public final boolean oneShot () { return _oneShot; }

    /** Returns the listener for this cons cell. */
    public L listener () {
        return _ref.get();
    }

    @Override public void disconnect () {
        // multiple disconnects are OK, we just NOOP after the first one
        if (_owner != null) {
            _owner.disconnect(this);
            _owner = null;
        }
    }

    @Override public Connection once () {
        _oneShot = true;
        return this;
    }

    @Override public Connection atPriority (int priority) {
        if (_owner == null) throw new IllegalStateException(
            "Cannot change priority of disconnected connection.");
        _owner.disconnect(this);
        next = null;
        _priority = priority;
        _owner.addCons(this);
        return this;
    }

    // Synchronize to make sure it's impossible to create a WeakListenerRef to the placeholder listener, as that
    // listener always has a strong reference
    @Override public synchronized Connection holdWeakly() {
        if (_owner == null) throw new IllegalStateException("Cannot change disconnected connection to weak.");
        if (!(_ref instanceof Cons.WeakListenerRef)) {
            _ref = new WeakListenerRef();
        }
        return this;
    }

    @Override public String toString () {
        return "[owner=" + _owner + ", pri=" + _priority + ", lner=" + listener() +
            ", hasNext=" + (next != null) + ", oneShot=" + oneShot() + "]";
    }

    interface ListenerRef<L extends RListener> {
        abstract L get();
    }

    class WeakListenerRef implements ListenerRef<L> {
        private final WeakReference<L> _weak = new WeakReference<L>(_ref.get());
        public L get() {
            L listener = _weak.get();
            if (listener == null) {
                listener = _owner.placeholderListener();
                disconnect();
            }
            return listener;
        }
    }

    protected Cons (Reactor<L> owner, final L listener) {
        _owner = owner;
        _ref = new ListenerRef<L>() {
            @Override public L get() {
                return listener;
            }
        };
    }

    static <L extends RListener> Cons<L> insert (Cons<L> head, Cons<L> cons) {
        if (head == null) {
            return cons;
        } else if (head._priority > cons._priority) {
            cons.next = head;
            return cons;
        } else {
            head.next = insert(head.next, cons);
            return head;
        }
    }

    static <L extends RListener> Cons<L> remove (Cons<L> head, Cons<L> cons) {
        if (head == null) return head;
        if (head == cons) return head.next;
        head.next = remove(head.next, cons);
        return head;
    }

    static <L extends RListener> Cons<L> removeAll (Cons<L> head, L listener) {
        if (head == null) return null;
        if (head.listener() == listener) return removeAll(head.next, listener);
        head.next = removeAll(head.next, listener);
        return head;
    }

    protected Reactor<L> _owner;
    private boolean _oneShot; // defaults to false
    private int _priority; // defaults to zero
    private ListenerRef<L> _ref;
}
