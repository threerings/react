//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

import java.util.HashSet;
import java.util.Set;

/**
 * An implementation detail that simplifies life for {@link Connection}-using entities.
 */
abstract class AbstractConnection<T extends AbstractConnection> implements Connection
{
    /** The next connection in our chain. */
    public T next;

    /** Indicates whether this connection is one-shot or persistent. */
    public boolean oneShot;

    @Override public Connection once () {
        oneShot = true;
        return this;
    }

    /** Returns the priority of this connection. */
    public abstract int priority ();

    static <T extends AbstractConnection<T>> T insert (T head, T cons) {
        if (head == null) {
            return cons;
        } else if (head.priority() > cons.priority()) {
            cons.next = head;
            return cons;
        } else {
            head.next = insert(head.next, cons);
            return head;
        }
    }

    static <T extends AbstractConnection<T>> T insertAll (T head, T toAdd) {
        while (toAdd != null) {
            T next = toAdd.next;
            toAdd.next = null;
            head = insert(head, toAdd);
            toAdd = next;
        }
        return head;
    }

    static <T extends AbstractConnection<T>> T remove (T head, T cons) {
        if (head == cons) return head.next;
        T prev = head;
        while (prev.next != cons) {
            prev = prev.next;
        }
        prev.next = prev.next.next;
        return head;
    }

    static <T extends AbstractConnection<T>> T removeAll (T head, Set<T> toRemove) {
        if (head == null) return null;
        if (toRemove.contains(head)) return removeAll(head.next, toRemove);
        head.next = removeAll(head.next, toRemove);
        return head;
    }

    static <T extends AbstractConnection<T>> Set<T> queueRemove (Set<T> toRemove, T cons) {
        if (toRemove == null) {
            toRemove = new HashSet<T>();
        }
        toRemove.add(cons);
        return toRemove;
    }
}
