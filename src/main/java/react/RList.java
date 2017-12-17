//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011-present, React Authors
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
public class RList<E> extends RCollection<E> implements List<E> {

  /** Publishes list events to listeners. */
  public interface Listener<E> extends Reactor.RListener {

    /** Notifies listener of an added element. This method will call the index-forgetting
      * version ({@link #onAdd(Object)}) by default. */
    default void onAdd (int index, E elem) { onAdd(elem); }

    /** Notifies listener of an added element. */
    default void onAdd (E elem) {} // noop

    /** Notifies listener of an updated element. This method will call the old-value-forgetting
      * version ({@link #onSet(int,Object)}) by default. */
    default void onSet (int index, E newElem, E oldElem) { onSet(index, newElem); }

    /** Notifies listener of an updated element. */
    default void onSet (int index, E newElem) {} // noop

    /** Notifies listener of a removed element. This method will call the index-forgetting
      * version ({@link #onRemove(Object)}) by default. */
    default void onRemove (int index, E elem) { onRemove(elem); }

    /** Notifies listener of a removed element. */
    default void onRemove (E elem) {} // noop
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
  public Connection connect (Listener<? super E> listener) {
    return addConnection(listener);
  }

  /**
   * Invokes {@code onAdd(int,E)} for all existing list elements, then connects {@code listener}.
   */
  public Connection connectNotify (Listener<? super E> listener) {
    for (int ii = 0, ll = size(); ii < ll; ii++) listener.onAdd(ii, get(ii));
    return connect(listener);
  }

  /**
   * Disconnects the supplied listener from this list if listen was called with it.
   */
  public void disconnect (Listener<? super E> listener) {
    removeConnection(listener);
  }

  /**
   * Removes the supplied element from the list, forcing a notification to the listeners
   * regardless of whether the element was in the list or not.
   * @return true if the element was in the list and was removed, false if it was not.
   */
  public boolean removeForce (E elem) {
    checkMutate();
    int index = _impl.indexOf(elem);
    if (index >= 0) _impl.remove(index);
    emitRemove(index, elem);
    return (index >= 0);
  }

  // List methods that perform reactive functions in addition to calling through
  @Override public boolean add (E element) {
    add(size(), element);
    return true;
  }

  @Override public void add (int index, E element) {
    checkMutate();
    _impl.add(index, element);
    emitAdd(index, element);
  }

  @Override public boolean addAll (Collection<? extends E> collection) {
    return addAll(size(), collection);
  }

  @Override public boolean addAll (int index, Collection<? extends E> elements) {
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

  @Override public ListIterator<E> listIterator () {
    return listIterator(0);
  }

  @Override public ListIterator<E> listIterator (int index) {
    ListIterator<E> iiter = _impl.listIterator();
    return new ListIterator<E> () {
      public void add (E elem) {
        checkMutate();
        int index = iiter.nextIndex();
        iiter.add(elem);
        emitAdd(index, elem);
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
        int index = iiter.previousIndex();
        iiter.remove();
        emitRemove(index, _current);
      }
      public void set (E elem) {
        checkMutate();
        iiter.set(elem);
        emitSet(iiter.previousIndex(), elem, _current);
        _current = elem;
      }
      protected E _current; // the element targeted by remove or set
    };
  }

  @Override public boolean retainAll (Collection<?> collection) {
    boolean modified = false;
    for (Iterator<E> iter = iterator(); iter.hasNext(); ) {
      if (!collection.contains(iter.next())) {
        iter.remove();
        modified = true;
      }
    }
    return modified;
  }

  @Override public boolean removeAll (Collection<?> collection) {
    boolean modified = false;
    for (Object o : collection) modified |= remove(o);
    return modified;
  }

  @Override public boolean remove (Object object) {
    checkMutate();
    int index = _impl.indexOf(object);
    if (index < 0) return false;
    _impl.remove(index);
    // the cast is safe if the element was removed
    @SuppressWarnings("unchecked") E elem = (E)object;
    emitRemove(index, elem);
    return true;
  }

  @Override public E remove (int index) {
    checkMutate();
    E removed = _impl.remove(index);
    emitRemove(index, removed);
    return removed;
  }

  @Override public E set (int index, E element) {
    checkMutate();
    E removed = _impl.set(index, element);
    emitSet(index, element, removed);
    return removed;
  }

  @Override public List<E> subList (int fromIndex, int toIndex) {
    return new RList<E>(_impl.subList(fromIndex, toIndex));
  }

  @Override public boolean equals (Object other) {
    return other == this || _impl.equals(other);
  }

  @Override public String toString () {
    return "RList(" + _impl + ")";
  }

  // List methods that purely pass through to the underlying list
  @Override public int hashCode () {
    return _impl.hashCode();
  }

  @Override public int size () {
    return _impl.size();
  }

  @Override public boolean isEmpty () {
    return _impl.isEmpty();
  }

  @Override public E get (int index) {
    return _impl.get(index);
  }

  @Override public int indexOf (Object element) {
    return _impl.indexOf(element);
  }

  @Override public int lastIndexOf (Object element) {
    return _impl.lastIndexOf(element);
  }

  @Override public boolean contains (Object object) {
    return _impl.contains(object);
  }

  @Override public boolean containsAll (Collection<?> collection) {
    return _impl.containsAll(collection);
  }

  @Override public void clear () {
    // clear in such a way as to emit events
    while (!isEmpty()) remove(0);
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

  // Non-list RList implementation
  protected void emitAdd (int index, E elem) {
    notify(ADD, index, elem, null);
  }

  protected void emitSet (int index, E newElem, E oldElem) {
    notify(SET, index, newElem, oldElem);
  }

  protected void emitRemove (int index, E elem) {
    notify(REMOVE, index, elem, null);
  }

  /** Contains our underlying elements. */
  protected List<E> _impl;

  protected static final Listener<Object> NOOP = new Listener<Object>() {};

  @SuppressWarnings("unchecked") protected static final Notifier ADD = new Notifier() {
    public void notify (Object lner, Object index, Object elem, Object ignored) {
      ((Listener<Object>)lner).onAdd((Integer)index, elem);
    }
  };

  @SuppressWarnings("unchecked") protected static final Notifier SET = new Notifier() {
    public void notify (Object lner, Object index, Object newElem, Object oldElem) {
      ((Listener<Object>)lner).onSet((Integer)index, newElem, oldElem);
    }
  };

  @SuppressWarnings("unchecked") protected static final Notifier REMOVE = new Notifier() {
    public void notify (Object lner, Object index, Object elem, Object ignored) {
      ((Listener<Object>)lner).onRemove((Integer)index, elem);
    }
  };
}
