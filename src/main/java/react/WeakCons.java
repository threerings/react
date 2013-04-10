package react;

import java.lang.ref.WeakReference;

class WeakCons<L extends Reactor.RListener> extends Cons<L> {

    private final WeakReference<L> _weakListener;

    public WeakCons(Reactor<L> owner, L listener) {
        super(owner);
        _weakListener = new WeakReference<L>(listener);
    }

    @Override
    public L getListener() {
        L listener = _weakListener.get();
        if (listener == null) {
            listener = owner.placeholderListener;
            disconnect();
        }
        return listener;
    }
}
