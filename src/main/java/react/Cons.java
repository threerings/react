//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

import java.util.HashSet;
import java.util.Set;

import react.Reactor.RListener;

/**
 * Implements {@link Connection} and a linked-list style listener list for {@link Reactor}s.
 */
class Cons<L extends RListener> implements Connection
{
    /** The reactor that owns this cons cell. */
    public final Reactor<L> owner;

    /** The listener being tracked by this cons cell. */
    public final L listener;

    /** The next connection in our chain. */
    public Cons<L> next;

    /** Indicates whether this connection is one-shot or persistent. */
    public boolean oneShot;

    public Cons (Reactor<L> owner, L listener) {
        this.owner = owner;
        this.listener = listener;
    }

    @Override public Connection once () {
        oneShot = true;
        return this;
    }

    @Override public void disconnect () {
        owner.disconnect(this);
    }

    static <L extends RListener> Cons<L> insert (Cons<L> head, Cons<L> cons) {
        if (head == null) {
            return cons;
        } else if (head.listener.priority() > cons.listener.priority()) {
            cons.next = head;
            return cons;
        } else {
            head.next = insert(head.next, cons);
            return head;
        }
    }

    static <L extends RListener> Cons<L> insertAll (Cons<L> head, Cons<L> toAdd) {
        while (toAdd != null) {
            Cons<L> next = toAdd.next;
            toAdd.next = null;
            head = insert(head, toAdd);
            toAdd = next;
        }
        return head;
    }

    static <L extends RListener> Cons<L> remove (Cons<L> head, Cons<L> cons) {
        if (head == cons) return head.next;
        Cons<L> prev = head;
        while (prev.next != cons) {
            prev = prev.next;
        }
        prev.next = prev.next.next;
        return head;
    }

    static <L extends RListener> Cons<L> removeAll (Cons<L> head, Set<Cons<L>> toRemove) {
        if (head == null) return null;
        if (toRemove.contains(head)) return removeAll(head.next, toRemove);
        head.next = removeAll(head.next, toRemove);
        return head;
    }

    static <L extends RListener> Set<Cons<L>> queueRemove (Set<Cons<L>> toRemove, Cons<L> cons) {
        if (toRemove == null) {
            toRemove = new HashSet<Cons<L>>();
        }
        toRemove.add(cons);
        return toRemove;
    }
}
