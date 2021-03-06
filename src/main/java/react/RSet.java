//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Provides a reactive model of a set. Note that {@link #add} and other default mechanisms for
 * updating the set <em>will not</em> trigger a notification if the updated element is equal to an
 * element already in the set. Use {@link #addForce} to force a notification. Similarly, {@link
 * #remove} will only generate a notification if an element was actually removed, use {@link
 * #removeForce} to force a notification.
 */
public class RSet<E> extends RCollection<E> implements Set<E>
{
    /** An interface for publishing set events to listeners. */
    public static abstract class Listener<E> implements Reactor.RListener
    {
        /** Notifies listener of an added element. */
        public void onAdd (E elem) {
            // noop
        }

        /** Notifies listener of a removed element. */
        public void onRemove (E elem) {
            // noop
        }
    }

    /**
     * Creates a reactive set backed by a @{link HashSet}.
     */
    public static <E> RSet<E> create () {
        return create(new HashSet<E>());
    }

    /**
     * Creates a reactive set with the supplied underlying set implementation.
     */
    public static <E> RSet<E> create (Set<E> impl) {
        return new RSet<E>(impl);
    }

    /**
     * Creates a reactive set with the supplied underlying set implementation.
     */
    public RSet (Set<E> impl) {
        _impl = impl;
    }

    /**
     * Connects the supplied listener to this set, such that it will be notified on adds and
     * removes.
     * @return a connection instance which can be used to cancel the connection.
     */
    public Connection connect (Listener<? super E> listener) {
        return addConnection(listener);
    }

    /**
     * Invokes {@code onAdd} for all existing elements, and then connects {@code listener}.
     */
    public Connection connectNotify (Listener<? super E> listener) {
        for (E elem : this) listener.onAdd(elem);
        return connect(listener);
    }

    /**
     * Disconnects the supplied listener from this set if listen was called with it.
     */
    public void disconnect (Listener<? super E> listener) {
        removeConnection(listener);
    }

    /**
     * Adds the supplied element to the set, forcing a notification to the listeners regardless of
     * whether the element was already in the set or not.
     * @return true if the element was added, false if it was already in the set.
     */
    public boolean addForce (E elem) {
        checkMutate();
        boolean added = _impl.add(elem);
        emitAdd(elem);
        return added;
    }

    /**
     * Removes the supplied element from the set, forcing a notification to the listeners
     * regardless of whether the element was already in the set or not.
     * @return true if the element was in the set and was removed, false if it was not.
     */
    public boolean removeForce (E elem) {
        checkMutate();
        boolean removed = _impl.remove(elem);
        emitRemove(elem);
        return removed;
    }

    /**
     * Returns a value that models whether the specified element is contained in this map. The
     * value will report a change when the specified element is added or removed. Note that {@link
     * #addForce} or {@link #removeForce} will cause this view to trigger and incorrectly report
     * that the element was not or was previously contained in the set. Caveat user.
     */
    public ValueView<Boolean> containsView (final E elem) {
        if (elem == null) throw new NullPointerException("Must supply non-null 'elem'.");
        return new MappedValue<Boolean>() {
            @Override public Boolean get () {
                return contains(elem);
            }
            @Override protected Connection connect () {
                return RSet.this.connect(new RSet.Listener<E>() {
                    @Override public void onAdd (E aelem) {
                        if (elem.equals(aelem)) notifyChange(true, false);
                    }
                    @Override public void onRemove (E relem) {
                        if (elem.equals(relem)) notifyChange(false, true);
                    }
                });
            }
        };
    }

    // from interface Set<E>
    public int size () {
        return _impl.size();
    }

    // from interface Set<E>
    public boolean isEmpty () {
        return _impl.isEmpty();
    }

    // from interface Set<E>
    public boolean contains (Object key) {
        return _impl.contains(key);
    }

    // from interface Set<E>
    public boolean add (E elem) {
        checkMutate();
        if (!_impl.add(elem)) return false;
        emitAdd(elem);
        return true;
    }

    // from interface Set<E>
    public boolean remove (Object rawElem) {
        checkMutate();
        if (!_impl.remove(rawElem)) return false;
        @SuppressWarnings("unchecked") E elem = (E)rawElem;
        emitRemove(elem);
        return true;
    }

    // from interface Set<E>
    public boolean containsAll (Collection<?> coll) {
        return _impl.containsAll(coll);
    }

    // from interface Set<E>
    public boolean addAll (Collection<? extends E> coll) {
        boolean modified = false;
        for (E elem : coll) {
            modified |= add(elem);
        }
        return modified;
    }

    // from interface Set<E>
    public boolean retainAll (Collection<?> coll) {
        boolean modified = false;
        for (Iterator<E> iter = iterator(); iter.hasNext(); ) {
            if (!coll.contains(iter.next())) {
                iter.remove();
                modified = true;
            }
        }
        return modified;
    }

    // from interface Set<E>
    public boolean removeAll (Collection<?> coll) {
        boolean modified = false;
        for (Iterator<?> iter = coll.iterator(); iter.hasNext(); ) {
            modified |= remove(iter.next());
        }
        return modified;
    }

    // from interface Set<E>
    public void clear () {
        checkMutate();
        // generate removed events for our elemens (do so on a copy of our set so that we can clear
        // our underlying set before any of the published events are processed)
        List<E> elems = new ArrayList<E>(_impl);
        _impl.clear();
        for (E elem : elems) emitRemove(elem);
    }

    // from interface Set<E>
    public Iterator<E> iterator () {
        final Iterator<E> iiter = _impl.iterator();
        return new Iterator<E>() {
            public boolean hasNext () {
                return iiter.hasNext();
            }
            public E next () {
                return (_current = iiter.next());
            }
            public void remove () {
                checkMutate();
                iiter.remove();
                emitRemove(_current);
            }
            protected E _current;
        };
    }

    // from interface Set<E>
    public Object[] toArray () {
        return _impl.toArray();
    }

    // from interface Set<E>
    public <T> T[] toArray (T[] array) {
        return _impl.toArray(array);
    }

    @Override public int hashCode () {
        return _impl.hashCode();
    }

    @Override public boolean equals (Object other) {
        return other == this || _impl.equals(other);
    }

    @Override public String toString () {
        return "RSet" + _impl;
    }

    @Override Listener<E> placeholderListener () {
        @SuppressWarnings("unchecked") Listener<E> p = (Listener<E>)NOOP;
        return p;
    }

    protected void emitAdd (E elem) {
        notifyAdd(elem);
    }

    protected void notifyAdd (E elem) {
        notify(ADD, elem, null, null);
    }

    protected void emitRemove (E elem) {
        notifyRemove(elem);
    }

    protected void notifyRemove (E elem) {
        notify(REMOVE, elem, null, null);
    }

    /** Contains our underlying elements. */
    protected Set<E> _impl;

    protected static final Listener<Object> NOOP = new Listener<Object>() {};

    @SuppressWarnings("unchecked") protected static final Notifier ADD = new Notifier() {
        public void notify (Object lner, Object elem, Object _1, Object _2) {
            ((Listener<Object>)lner).onAdd(elem);
        }
    };

    @SuppressWarnings("unchecked") protected static final Notifier REMOVE = new Notifier() {
        public void notify (Object lner, Object elem, Object _1, Object _2) {
            ((Listener<Object>)lner).onRemove(elem);
        }
    };
}
