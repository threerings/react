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
public class AbstractSignal<T> extends Reactor<Slot<T>>
    implements SignalView<T>
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

    @Override public Connection connect (Slot<? super T> slot) {
        // alas, Java does not support higher kinded types; this cast is safe
        @SuppressWarnings("unchecked") Slot<T> casted = (Slot<T>)slot;
        return addConnection(casted);
    }

    @Override public void disconnect (Slot<? super T> slot) {
        // alas, Java does not support higher kinded types; this cast is safe
        @SuppressWarnings("unchecked") Slot<T> casted = (Slot<T>)slot;
        removeConnection(casted);
    }

    /**
     * Emits the supplied event to all connected slots.
     */
    protected void notifyEmit (T event) {
        Cons<Slot<T>> lners = prepareNotify();
        MultiFailureException error = null;
        try {
            for (Cons<Slot<T>> cons = lners; cons != null; cons = cons.next) {
                try {
                    cons.listener.onEmit(event);
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
}
