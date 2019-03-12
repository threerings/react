//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

import java.lang.ref.WeakReference;

import react.Reactor.RListener;

/**
 * Implements {@link Connection} and a linked-list style listener list for {@link Reactor}s.
 */
class Cons extends Connection
{
    /** The next connection in our chain. */
    public Cons next;

    /** Indicates whether this connection is one-shot or persistent. */
    public final boolean oneShot () { return _oneShot; }

    /** Returns the listener for this cons cell. */
    public RListener listener () {
        return _ref.get(this);
    }

    @Override public void close () {
        // multiple disconnects are OK, we just NOOP after the first one
        if (_owner != null) {
            _ref.defang(_owner.placeholderListener());
            _owner.disconnect(this);
            _owner = null;
        }
    }

    @Override public Connection once () {
        _oneShot = true;
        return this;
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
        if (!_ref.isWeak()) _ref = new WeakRef(_ref.get(this));
        return this;
    }

    @Override public String toString () {
        return "[owner=" + _owner + ", pri=" + _priority + ", lner=" + listener() +
            ", hasNext=" + (next != null) + ", oneShot=" + oneShot() + "]";
    }

    protected Cons (Reactor owner, RListener listener) {
        _owner = owner;
        _ref = new StrongRef(listener);
    }

    private static abstract class ListenerRef {
        abstract boolean isWeak ();
        abstract void defang (RListener noop);
        abstract RListener get (Cons cons);
    }

    private static class StrongRef extends ListenerRef {
        private RListener _lner;
        public StrongRef (RListener lner) { _lner = lner; }
        public boolean isWeak () { return false; }
        public void defang (RListener noop) { _lner = noop; }
        public RListener get (Cons cons) { return _lner; }
    }

    private static class WeakRef extends ListenerRef {
        private WeakReference<RListener> _wref;
        private RListener _noop;
        public WeakRef (RListener lner) { _wref = new WeakReference<RListener>(lner); }
        public boolean isWeak () { return true; }
        public void defang (RListener noop) { _noop = noop; _wref = null; }
        public RListener get (Cons cons) {
            if (_wref != null) {
                RListener listener = _wref.get();
                if (listener != null) return listener;
                cons.close(); // close will defang() us
            }
            return _noop;
        }
    }

    static Cons insert (Cons head, Cons cons) {
        if (head == null) {
            return cons;

        } else if (cons._priority > head._priority) {
            cons.next = head;
            return cons;
        }

        Cons cycleHead = head;
        do {
            if (cycleHead.next == null) {
                cycleHead.next = cons;
                break;

            } else if (cons._priority > cycleHead.next._priority) {
                cons.next = cycleHead.next;
                cycleHead.next = cons;
                break;
            }

            cycleHead = cycleHead.next;

        } while (cycleHead != null);


        return head;
    }

    static Cons remove (Cons head, Cons cons) {
        if (head == null) return head;
        if (head == cons) return head.next;

        Cons cycleHead = head;
        do {
            if (cycleHead.next == cons) {
                cycleHead.next = cons.next;
                break;
            }

            cycleHead = cycleHead.next;

        } while (cycleHead != null);

        return head;
    }

    static Cons removeAll (Cons head, RListener listener) {
        if (head == null) return null;

        Cons cycleHead = head;
        while (cycleHead.next != null) {
            if (cycleHead.next.listener() == listener) {
                cycleHead.next = cycleHead.next.next;
            } else {
                cycleHead = cycleHead.next;
            }
        }

        return head.listener() == listener ? head.next : head;
    }

    private Reactor _owner;
    private ListenerRef _ref;
    private boolean _oneShot; // defaults to false
    private int _priority; // defaults to zero
}
