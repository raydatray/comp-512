package paxos;

import java.util.Optional;
import java.util.logging.Logger;

enum AcceptorPhase {
    PROMISED,
    ACCEPTED,
    CONFIRMED,
}

class AcceptorState {

    protected AcceptorPhase phase;

    protected Ballot highestB;

    protected Optional<Ballot> prevB;
    protected Optional<GameMove> prevM;

    private final Logger log;

    /// init an acceptor state as promised given a ballot
    protected AcceptorState(Ballot ballot, Logger log) {
        this.phase = AcceptorPhase.PROMISED;

        this.highestB = ballot;

        this.prevB = Optional.empty();
        this.prevM = Optional.empty();

        this.log = log;
    }

    /// transition to accepted, given a ballot and move
    /// caller is responsible to verify this transition is valid
    protected void transitionToAccept(Ballot ballot, GameMove move) {
        log.info("transitioning to accepted");

        this.phase = AcceptorPhase.ACCEPTED;

        this.highestB = ballot;

        this.prevB = Optional.of(ballot);
        this.prevM = Optional.of(move);
        return;
    }

    /// transition to confirmed
    protected void transitionToConfirm(Ballot ballot, GameMove move) {
        log.info("transitioning to confirmed");

        this.phase = AcceptorPhase.CONFIRMED;

        if (
            this.prevB.isPresent() &&
            this.prevM.isPresent() &&
            this.prevB.get().equals(ballot) &&
            this.prevM.get().equals(move)
        ) {
            log.info("confirm matches previously accepted value");
            return;
        }

        log.warning(
            "confirm differs from previously accepted value, prev: " +
                this.prevB.map(Object::toString).orElse("none") +
                this.prevM.map(Object::toString).orElse("none") +
                " curr: " +
                ballot +
                move
        );

        // since a confirm is authoritative, we must take the new values
        this.prevB = Optional.of(ballot);
        this.prevM = Optional.of(move);
        return;
    }

    /// update to a higher ballot
    public void updateHighestB(Ballot ballot) {
        log.info(
            "updating highest ballot from " + this.highestB + " to " + ballot
        );
        this.highestB = ballot;
        return;
    }
}
