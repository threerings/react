//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011-present, React Authors
// http://github.com/threerings/react/blob/master/LICENSE

package react;

/**
 * Handles the machinery of connecting slots to a signal and emitting events to them, without
 * exposing a public interface for emitting events. This can be used by entities which wish to
 * expose a signal-like interface for listening, without allowing external callers to emit signals.
 */
public class AbstractSignal<T> extends Reactor implements SignalView<T> {

  @Override public <M> SignalView<M> map (final Function<? super T, M> func) {
    final AbstractSignal<T> outer = this;
    return new MappedSignal<M>() {
      @Override protected Connection connect () {
        return outer.connect(new Listener<T>() {
          @Override public void onEmit (T value) {
            notifyEmit(func.apply(value));
          }
        });
      }
    };
  }

  @Override public SignalView<T> filter (final Function<? super T, Boolean> pred) {
    final AbstractSignal<T> outer = this;
    return new MappedSignal<T>() {
      @Override protected Connection connect () {
        return outer.connect(new Listener<T>() {
          @Override public void onEmit (T value) {
            if (pred.apply(value)) {
              notifyEmit(value);
            }
          }
        });
      }
    };
  }

  @Override public <M> SignalView<M> collect (final Function<? super T, M> collector) {
    final AbstractSignal<T> outer = this;
    return new MappedSignal<M>() {
      @Override protected Connection connect () {
        return outer.connect(new Listener<T>() {
          @Override public void onEmit (T value) {
            M mapped = collector.apply(value);
            if (mapped != null) {
              notifyEmit(mapped);
            }
          }
        });
      }
    };
  }

  @Override public RFuture<T> next () {
    final RPromise<T> result = RPromise.create();
    connect(result::succeed).once();
    return result;
  }

  @Override public Connection connect (Listener<? super T> slot) {
    return addConnection(slot);
  }

  @Override public void disconnect (Listener<? super T> slot) {
    removeConnection(slot);
  }

  @Override Listener<T> placeholderListener () {
    @SuppressWarnings("unchecked") Listener<T> p = (Listener<T>)Slot.NOOP;
    return p;
  }

  /**
   * Emits the supplied event to all connected slots.
   */
  protected void notifyEmit (T event) {
    notify(EMIT, event, null, null);
  }

  @SuppressWarnings("unchecked") protected static final Notifier EMIT = new Notifier() {
    public void notify (Object slot, Object event, Object _1, Object _2) {
      ((Listener<Object>)slot).onEmit(event);
    }
  };
}
