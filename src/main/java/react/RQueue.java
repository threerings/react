//
// React - a library for functional-reactive-like programming
// Copyright (c) 2015, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Provides a reactive model of a {@link Queue}. Note: unlike standard Java queues, reactive queues
 * do not support removing arbitrary items from the queue; only the head of the queue may be
 * removed.
 */
public class RQueue<E> extends RCollection<E> implements Queue<E>
{
    /** Publishes queue events to listeners. */
    public interface Listener<E> extends Reactor.RListener
    {
        /** Notifies listener of an offered (added) element. */
        default void onOffer (E elem) {} // noop

        /** Notifies listener of a polled (removed) element. */
        default void onPoll (E elem) {} // noop
    }

    /**
     * Creates a reactive queue backed by an {@link LinkedList}.
     */
    public static <E> RQueue<E> create () {
        return create(new LinkedList<E>());
    }

    /**
     * Creates a reactive queue with the supplied underlying queue implementation.
     */
    public static <E> RQueue<E> create (Queue<E> impl) {
        return new RQueue<E>(impl);
    }

    /**
     * Creates a reactive queue with the supplied underlying queue implementation.
     */
    public RQueue (Queue<E> impl) {
        _impl = impl;
    }

    /**
     * Connects the supplied listener to this queue, such that it will be notified on offers and
     * polls.
     * @return a connection instance which can be used to cancel the connection.
     */
    public Connection connect (Listener<? super E> listener) {
        return addConnection(listener);
    }

    /**
     * Invokes {@code onOffer} for all existing queue elements, then connects {@code listener}.
     */
    public Connection connectNotify (Listener<? super E> listener) {
        for (E elem : _impl) listener.onOffer(elem);
        return connect(listener);
    }

    /**
     * Disconnects the supplied listener from this queue if listen was called with it.
     */
    public void disconnect (Listener<? super E> listener) {
        removeConnection(listener);
    }

    // Queue methods that perform reactive functions in addition to calling through
    @Override public boolean offer (E element) {
        checkMutate();
        if (!_impl.offer(element)) return false;
        emitOffer(element);
        return true;
    }

    @Override public boolean add (E element) {
        checkMutate();
        _impl.add(element); // throws on failure
        emitOffer(element);
        return true;
    }

    @Override public boolean addAll (Collection<? extends E> elements) {
        checkMutate();
        // call add instead of calling _impl.addAll so if a listener throws an exception on
        // emission, we don't have elements added without a corresponding emission
        for (E elem : elements) add(elem);
        return true;
    }

    @Override public E poll () {
        checkMutate();
        E elem = _impl.poll();
        if (elem != null) emitPoll(elem);
        return elem;
    }

    @Override public E remove () {
        checkMutate();
        E elem = _impl.remove(); // throws on empty
        emitPoll(elem);
        return elem;
    }

    @Override public void clear () {
        // clear in such a way as to emit events
        while (!isEmpty()) remove();
    }

    @Override public Iterator<E> iterator () {
        return new Iterator<E> () {
            private final Iterator<E> _iter = _impl.iterator();
            public boolean hasNext () {
                return _iter.hasNext();
            }
            public E next () {
                return _iter.next();
            }
            public void remove () {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override public boolean equals (Object other) {
        return other == this || _impl.equals(other);
    }

    @Override public String toString () {
        return "RQueue(" + _impl + ")";
    }

    // Unsupported Queue methods
    @Override public boolean retainAll (Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override public boolean removeAll (Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override public boolean remove (Object object) {
        throw new UnsupportedOperationException();
    }

    // Queue methods that purely pass through to the underlying queue
    @Override public int hashCode () {
        return _impl.hashCode();
    }

    @Override public int size () {
        return _impl.size();
    }

    @Override public boolean isEmpty () {
        return _impl.isEmpty();
    }

    @Override public E peek () {
        return _impl.peek();
    }

    @Override public E element () {
        return _impl.element();
    }

    @Override public boolean contains (Object object) {
        return _impl.contains(object);
    }

    @Override public boolean containsAll (Collection<?> collection) {
        return _impl.containsAll(collection);
    }

    @Override public Object[] toArray () {
        return _impl.toArray();
    }

    @Override public <T> T[] toArray (T[] array) {
        return _impl.toArray(array);
    }

    @Override Listener<E> placeholderListener () {
        @SuppressWarnings("unchecked") Listener<E> p = (Listener<E>)NOOP;
        return p;
    }

    // Non-list RQueue implementation
    protected void emitOffer (E elem) {
        notify(OFFER, elem, null, null);
    }

    protected void emitPoll (E elem) {
        notify(POLL, elem, null, null);
    }

    /** Contains our underlying elements. */
    protected Queue<E> _impl;

    protected static final Listener<Object> NOOP = new Listener<Object>() {};

    @SuppressWarnings("unchecked") protected static final Notifier OFFER = new Notifier() {
        public void notify (Object lner, Object elem, Object ignored0, Object ignored1) {
            ((Listener<Object>)lner).onOffer(elem);
        }
    };

    @SuppressWarnings("unchecked") protected static final Notifier POLL = new Notifier() {
        public void notify (Object lner, Object elem, Object ignored0, Object ignored1) {
            ((Listener<Object>)lner).onPoll(elem);
        }
    };

}
