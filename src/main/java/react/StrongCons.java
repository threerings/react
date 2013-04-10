package react;

public class StrongCons<L extends Reactor.RListener> extends Cons<L> {

    /** Receives signals from the reactor. */
    private final L listener;

    public StrongCons(Reactor<L> owner, L listener) {
        super(owner);
        this.listener = listener;
    }

    @Override
    public L getListener() {
        return listener;
    }
}
