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
class Cons implements Connection
{
    /** The next connection in our chain. */
    public Cons next;

    /** Indicates whether this connection is one-shot or persistent. */
    public final boolean oneShot () { return _oneShot; }

    /** Returns the listener for this cons cell. */
    public RListener listener () {
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

    @Deprecated public Connection atPriority (int priority) {
      return atPrio(priority == Integer.MIN_VALUE ? Integer.MAX_VALUE : -priority);
    }

    @Override public Connection atPrio (int priority) {
        if (_owner == null) throw new IllegalStateException(
            "Cannot change priority of disconnected connection.");
        _owner.disconnect(this);
        next = null;
        _priority = priority;
        _owner.addCons(this);
        return this;
    }

    @Override public Connection holdWeakly () {
        if (_owner == null) throw new IllegalStateException(
            "Cannot change disconnected connection to weak.");
        ListenerRef ref = _ref;
        if (!ref.isWeak()) {
            final WeakReference<RListener> weak = new WeakReference<RListener>(ref.get());
            _ref = new ListenerRef() {
                public boolean isWeak () { return true; }
                public RListener get () {
                    RListener listener = weak.get();
                    if (listener == null) {
                        listener = _owner.placeholderListener();
                        disconnect();
                    }
                    return listener;
                }
            };
        }
        return this;
    }

    @Override public String toString () {
        return "[owner=" + _owner + ", pri=" + _priority + ", lner=" + listener() +
            ", hasNext=" + (next != null) + ", oneShot=" + oneShot() + "]";
    }

    protected Cons (Reactor owner, final RListener listener) {
        _owner = owner;
        _ref = new ListenerRef() {
            public boolean isWeak () { return false; }
            public RListener get() { return listener; }
        };
    }

    private interface ListenerRef {
        boolean isWeak ();
        RListener get ();
    }

    static Cons insert (Cons head, Cons cons) {
        if (head == null) {
            return cons;
        } else if (cons._priority > head._priority) {
            cons.next = head;
            return cons;
        } else {
            head.next = insert(head.next, cons);
            return head;
        }
    }

    static Cons remove (Cons head, Cons cons) {
        if (head == null) return head;
        if (head == cons) return head.next;
        head.next = remove(head.next, cons);
        return head;
    }

    static Cons removeAll (Cons head, RListener listener) {
        if (head == null) return null;
        if (head.listener() == listener) return removeAll(head.next, listener);
        head.next = removeAll(head.next, listener);
        return head;
    }

    private Reactor _owner;
    private ListenerRef _ref;
    private boolean _oneShot; // defaults to false
    private int _priority; // defaults to zero
}
