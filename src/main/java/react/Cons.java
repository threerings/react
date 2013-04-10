//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

import react.Reactor.RListener;

/**
 * Implements {@link Connection} and a linked-list style listener list for {@link Reactor}s.
 */
abstract class Cons<L extends RListener> implements Connection
{
    /** The reactor that owns this cons cell. */
    public final Reactor<L> owner;

    /** The next connection in our chain. */
    public Cons<L> next;

    /** Indicates whether this connection is one-shot or persistent. */
    public boolean oneShot;

    /** Returns the listener for this cons cell. */
    public abstract L listener ();

    @Override public Connection once () {
        oneShot = true;
        return this;
    }

    @Override public void disconnect () {
        owner.disconnect(this);
    }

    @Override public String toString () {
        return "[owner=" + owner + ", lner=" + listener() + ", hasNext=" + (next != null) +
            ", oneShot=" + oneShot + "]";
    }

    protected Cons (Reactor<L> owner) {
        this.owner = owner;
    }

    static <L extends RListener> Cons<L> insert (Cons<L> head, Cons<L> cons) {
        if (head == null) {
            return cons;
        } else if (head.listener().priority() > cons.listener().priority()) {
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
}
