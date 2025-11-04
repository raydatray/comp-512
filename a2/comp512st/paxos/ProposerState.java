package paxos;

import java.util.Optional;
import java.util.logging.Logger;

enum ProposerPhase {
    PROPOSE,
    ACCEPT,
    CONFIRM,
    AWAIT,
}

enum MoveState {
    SELF,
    PROPAGATE,
}

public class ProposerState {

    protected ProposerPhase phase;
    protected MoveState mState;

    protected Ballot b;
    protected GameMove m;

    protected Optional<GameMove> prevM;
    protected Optional<Ballot> prevB;

    protected Optional<Ballot> higherB;

    private Logger log;

    public ProposerState(Ballot ballot, GameMove move, Logger log) {
        this.phase = ProposerPhase.PROPOSE;
        this.mState = MoveState.SELF;

        this.b = ballot;
        this.m = move;

        this.prevM = Optional.empty();
        this.prevB = Optional.empty();

        this.higherB = Optional.empty();

        this.log = log;
    }

    public void transitionToAccept() {
        log.info("transitioning to accept state");

        this.phase = ProposerPhase.ACCEPT;
        return;
    }

    public void transitionToConfirm() {
        log.info("transitioning to confirm state");

        this.phase = ProposerPhase.CONFIRM;
        return;
    }

    public void transitionToAwait(Ballot ballot) {
        log.info("transitioning to wait for timeout state");

        this.phase = ProposerPhase.AWAIT;
        this.higherB = Optional.of(ballot);
    }

    public void propagatePreviousMove(PromiseWithPreviousAcceptedValue p) {
        log.info("modifying state to propagate previous move");

        if (this.mState == MoveState.SELF) {
            this.mState = MoveState.PROPAGATE;

            this.prevB = Optional.of(p.previousBallot());
            this.prevM = Optional.of(p.previousMove());
            return;
        }

        if (this.prevB.get().isLessThan(p.previousBallot())) {
            this.prevB = Optional.of(p.previousBallot());
            this.prevM = Optional.of(p.previousMove());
        }
    }

    public GameMove getMoveToPropose() {
        if (this.mState == MoveState.PROPAGATE) {
            return this.prevM.get();
        }

        return this.m;
    }

    @Override
    public String toString() {
        String prevMStr = prevM.map(GameMove::toString).orElse("None");
        String prevBStr = prevB.map(Ballot::toString).orElse("None");
        String higherBStr = higherB.map(Ballot::toString).orElse("None");

        return String.format(
            "ProposerState{phase=%s, mState=%s, b=%s, m=%s, prevM=%s, prevB=%s, higherB=%s}",
            phase,
            mState,
            b,
            m,
            prevMStr,
            prevBStr,
            higherBStr
        );
    }
}
