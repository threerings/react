//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

/**
 * Handles the machinery of connecting slots to a signal and emitting events to them, without
 * exposing a public interface for emitting events. This can be used by entities which wish to
 * expose a signal-like interface for listening, without allowing external callers to emit signals.
 */
public class AbstractSignal<T> extends Reactor implements SignalView<T>
{
    @Override public <M> SignalView<M> map (final Function<? super T, M> func) {
        final AbstractSignal<T> outer = this;
        return new MappedSignal<M>() {
            @Override protected Connection connect () {
                return outer.connect(new Slot<T>() {
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
                return outer.connect(new Slot<T>() {
                    @Override public void onEmit (T value) {
                        if (pred.apply(value)) {
                            notifyEmit(value);
                        }
                    }
                });
            }
        };
    }

    @Override public Connection connect (Slot<? super T> slot) {
        return addConnection(slot);
    }

    @Override public void disconnect (Slot<? super T> slot) {
        removeConnection(slot);
    }

    @Override Slot<T> placeholderListener () {
        @SuppressWarnings("unchecked") Slot<T> p = (Slot<T>)Slots.NOOP;
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
            ((Slot<Object>)slot).onEmit(event);
        }
    };
}
