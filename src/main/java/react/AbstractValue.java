//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011-present, React Authors
// http://github.com/threerings/react/blob/master/LICENSE

package react;

import java.util.function.Function;

/**
 * Handles the machinery of connecting listeners to a value and notifying them, without exposing a
 * public interface for updating the value. This can be used by libraries which wish to provide
 * observable values, but must manage the maintenance and distribution of value updates themselves
 * (so that they may send them over the network, for example).
 */
public abstract class AbstractValue<T> extends Reactor implements ValueView<T> {

  @Override public <M> ValueView<M> map (final Function<? super T, M> func) {
    final AbstractValue<T> outer = this;
    return new MappedValue<M>() {
      @Override public M get () {
        return func.apply(outer.get());
      }
      @Override public String toString () {
        return outer + ".map("  + func + ")";
      }
      @Override protected Connection connect () {
        return outer.connect((v, ov) -> notifyChange(func.apply(v), func.apply(ov)));
      }
    };
  }

  @Override public <M> ValueView<M> flatMap (
    final Function<? super T, ? extends ValueView<M>> func) {
    final AbstractValue<T> outer = this;
    final ValueView<? extends ValueView<M>> mapped = map(func);
    return new MappedValue<M>() {
      private Connection conn;

      @Override public M get () {
        return mapped.get().get();
      }
      @Override public String toString () {
        return outer + ".flatMap("  + func + ")";
      }
      @Override protected Connection connect () {
        conn = mapped.connect(v -> reconnect());
        return mapped.get().connect((v, ov) -> notifyChange(v, ov));
      }
      @Override protected void disconnect () {
        super.disconnect();
        if (conn != null) conn.close();
      }
    };
  }

  @Override public SignalView<T> changes () {
    final AbstractValue<T> outer = this;
    return new MappedSignal<T>() {
      @Override protected Connection connect () {
        return outer.connect((v, ov) -> notifyEmit(v));
      }
    };
  }

  @Override public RFuture<T> when (Function<? super T, Boolean> cond) {
    T current = get();
    if (cond.apply(current)) return RFuture.success(current);
    else return changes().filter(cond).next();
  }

  @Override public Connection connect (Listener<? super T> listener) {
    return addConnection(listener);
  }
  @Override public Connection connectNotify (Listener<? super T> listener) {
    // connect before calling emit; if the listener changes the value in the body of onEmit, it
    // will expect to be notified of that change; however if onEmit throws a runtime exception,
    // we need to take care of disconnecting the listener because the returned connection
    // instance will never reach the caller
    Connection conn = connect(listener);
    try {
      listener.onChange(get(), null);
      return conn;
    } catch (RuntimeException re) {
      conn.close();
      throw re;
    } catch (Error e) {
      conn.close();
      throw e;
    }
  }

  @Override public Connection connect (Slot<? super T> listener) {
    return connect((Listener<? super T>)listener);
  }
  @Override public Connection connectNotify (Slot<? super T> listener) {
    return connectNotify((Listener<? super T>)listener);
  }

  @Override public void disconnect (Listener<? super T> listener) {
    removeConnection(listener);
  }

  @Override public int hashCode () {
    T value = get();
    return (value == null) ? 0 : value.hashCode();
  }

  @Override public boolean equals (Object other) {
    if (other == null) return false;
    if (other.getClass() != getClass()) return false;
    T value = get();
    @SuppressWarnings("unchecked") T ovalue = ((AbstractValue<T>)other).get();
    return areEqual(value, ovalue);
  }

  @Override public String toString () {
    String cname = getClass().getName();
    return cname.substring(cname.lastIndexOf(".")+1) + "(" + get() + ")";
  }

  @Override Listener<T> placeholderListener () {
    @SuppressWarnings("unchecked") Listener<T> p = (Listener<T>)Slot.NOOP;
    return p;
  }

  /**
   * Updates the value contained in this instance and notifies registered listeners iff said
   * value is not equal to the value already contained in this instance (per {@link #areEqual}).
   */
  protected T updateAndNotifyIf (T value) {
    return updateAndNotify(value, false);
  }

  /**
   * Updates the value contained in this instance and notifies registered listeners.
   * @return the previously contained value.
   */
  protected T updateAndNotify (T value) {
    return updateAndNotify(value, true);
  }

  /**
   * Updates the value contained in this instance and notifies registered listeners.
   * @param force if true, the listeners will always be notified, if false the will be notified
   * only if the new value is not equal to the old value (per {@link #areEqual}).
   * @return the previously contained value.
   */
  protected T updateAndNotify (T value, boolean force) {
    checkMutate();
    T ovalue = updateLocal(value);
    if (force || !areEqual(value, ovalue)) {
      emitChange(value, ovalue);
    }
    return ovalue;
  }

  /**
   * Emits a change notification. Default implementation immediately notifies listeners.
   */
  protected void emitChange (T value, T oldValue) {
    notifyChange(value, oldValue);
  }

  /**
   * Notifies our listeners of a value change.
   */
  protected void notifyChange (T value, T oldValue) {
    notify(CHANGE, value, oldValue, null);
  }

  /**
   * Updates our locally stored value. Default implementation throws unsupported operation.
   * @return the previously stored value.
   */
  protected T updateLocal (T value) {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("unchecked") protected static final Notifier CHANGE = new Notifier() {
    public void notify (Object lner, Object value, Object oldValue, Object ignored) {
      ((Listener<Object>)lner).onChange(value, oldValue);
    }
  };
}
