//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Provides a reactive model of a list. Note that {@link #remove} <em>will not</em> trigger a
 * notification if the removed element is not present in the list. Use {@link #removeForce} to
 * force a notification.
 */
public class RList<E> extends Reactor<RList.Listener<E>>
    implements List<E>
{
    /** Publishes list events to listeners. */
    public static abstract class Listener<E> extends Reactor.RListener
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
     * Creates a reactive list backed by an {@link ArrayList}.
     */
    public static <E> RList<E> create () {
        return create(new ArrayList<E>());
    }

    /**
     * Creates a reactive list with the supplied underlying list implementation.
     */
    public static <E> RList<E> create (List<E> impl) {
        return new RList<E>(impl);
    }

    /**
     * Creates a reactive list with the supplied underlying list implementation.
     */
    public RList (List<E> impl) {
        _impl = impl;
    }

    /**
     * Connects the supplied listener to this list, such that it will be notified on adds and
     * removes.
     * @return a connection instance which can be used to cancel the connection.
     */
    public Connection listen (Listener<? super E> listener) {
        // alas, Java does not support higher kinded types; this cast is safe
        @SuppressWarnings("unchecked") Listener<E> casted = (Listener<E>)listener;
        return addConnection(casted);
    }

    /**
     * Removes the supplied element from the list, forcing a notification to the listeners
     * regardless of whether the element was in the list or not.
     * @return true if the element was in the list and was removed, false if it was not.
     */
    public boolean removeForce (E elem) {
        checkMutate();
        boolean removed = _impl.remove(elem);
        emitRemove(elem);
        return removed;
    }

    // List methods that perform reactive functions in addition to calling through
    @Override public boolean add(E element) {
        add(size(), element);
        return true;
    }

    @Override public void add(int index, E element) {
        checkMutate();
        _impl.add(index, element);
        emitAdd(element);
    }

    @Override public boolean addAll(Collection<? extends E> collection) {
        return addAll(size(), collection);
    }

    @Override public boolean addAll(int index, Collection<? extends E> elements) {
        checkMutate();
        // Call add instead of calling _impl.addAll so if a listener throws an exception on
        // emission, we don't have elements added without a corresponding emission
        for (E elem : elements) {
            add(index++, elem);
        }
        return true;
    }

    @Override public Iterator<E> iterator () {
        return listIterator();
    }

    @Override public ListIterator<E> listIterator() {
        return listIterator(0);
    }

    @Override public ListIterator<E> listIterator(int index) {
        final ListIterator<E> iiter = _impl.listIterator();
        return new ListIterator<E> () {
            public void add (E elem) {
                iiter.add(elem);
                emitAdd(elem);
            }
            public boolean hasNext () {
                return iiter.hasNext();
            }
            public boolean hasPrevious () {
                return iiter.hasPrevious();
            }
            public E next () {
                return (_current = iiter.next());
            }
            public int nextIndex () {
                return iiter.nextIndex();
            }
            public E previous () {
                return (_current = iiter.previous());
            }
            public int previousIndex () {
                return iiter.previousIndex();
            }
            public void remove () {
                checkMutate();
                iiter.remove();
                emitRemove(_current);
            }
            public void set (E elem) {
                iiter.set(elem);
                emitRemove(_current);
                _current = elem;
                emitAdd(_current);
            }
            protected E _current;// The element targetted by remove or set
        };
    }

    @Override public boolean retainAll(Collection<?> collection) {
        boolean modified = false;
        for (Iterator<E> iter = iterator(); iter.hasNext(); ) {
            if (!collection.contains(iter.next())) {
                iter.remove();
                modified = true;
            }
        }
        return modified;
    }

    @Override public boolean removeAll(Collection<?> collection) {
        boolean modified = false;
        for (Object o : collection) {
            modified |= remove(o);
        }
        return modified;
    }

    @Override public boolean remove(Object object) {
        checkMutate();
        boolean removed = _impl.remove(object);
        if (removed) {
            // The cast is safe if the element was removed
            @SuppressWarnings("unchecked") E elem = (E)object;
            emitRemove(elem);
        }
        return removed;
    }

    @Override public E remove(int index) {
        E removed = _impl.remove(index);
        emitRemove(removed);
        return removed;
    }

    @Override public E set(int index, E element) {
        E removed = _impl.set(index, element);
        emitAdd(element);
        emitRemove(element);
        return removed;
    }

    @Override public List<E> subList(int fromIndex, int toIndex) {
        return new RList<E>(_impl.subList(fromIndex, toIndex));
    }

    @Override public boolean equals(Object object) {
        return object == this || _impl.equals(object);
    }

    // List methods that purely pass through to the underlying list
    @Override public int hashCode() {
        return _impl.hashCode();
    }

    @Override public int size() {
        return _impl.size();
    }

    @Override public boolean isEmpty() {
        return _impl.isEmpty();
    }

    @Override public E get(int index) {
        return _impl.get(index);
    }

    @Override public int indexOf(Object element) {
        return _impl.indexOf(element);
    }

    @Override public int lastIndexOf(Object element) {
        return _impl.lastIndexOf(element);
    }

    @Override public boolean contains(Object object) {
        return _impl.contains(object);
    }

    @Override public boolean containsAll(Collection<?> collection) {
        return _impl.containsAll(collection);
    }

    @Override public void clear() {
        _impl.clear();
    }

    @Override public Object[] toArray() {
        return _impl.toArray();
    }

    @Override public <T> T[] toArray(T[] array) {
        return _impl.toArray(array);
    }

    // Non-list RList implementation
    protected void emitAdd (E elem) {
        Cons<Listener<E>> lners = prepareNotify();
        MultiFailureException error = null;
        try {
            for (Cons<Listener<E>> cons = lners; cons != null; cons = cons.next) {
                try {
                    cons.listener.onAdd(elem);
                } catch (Throwable t) {
                    if (error == null) error = new MultiFailureException();
                    error.addFailure(t);
                }
                if (cons.oneShot) cons.disconnect();
            }
        } finally {
            finishNotify(lners);
        }
        if (error != null) error.trigger();
    }

    protected void emitRemove (E elem) {
        Cons<Listener<E>> lners = prepareNotify();
        MultiFailureException error = null;
        try {
            for (Cons<Listener<E>> cons = lners; cons != null; cons = cons.next) {
                try {
                    cons.listener.onRemove(elem);
                } catch (Throwable t) {
                    if (error == null) error = new MultiFailureException();
                    error.addFailure(t);
                }
                if (cons.oneShot) cons.disconnect();
            }
        } finally {
            finishNotify(lners);
        }
        if (error != null) error.trigger();
    }

    /** Contains our underlying elements. */
    protected List<E> _impl;
}
