package paxos;

import java.util.Optional;
import java.util.logging.Logger;

enum ProposerPhase {
    IDLE,
    PROPOSE, // we are sending proposals
    ACCEPT, // we are sending accepts
    CONFIRM, // we have received majority and can confirm
    AWAIT_TIMEOUT, // we lost, we sit and wait for timeout
}

enum MoveState {
    SELF, // we are sending our own move
    PROPAGATE, // we have to propagate someone elses move
}

public class ProposerState {

    protected ProposerPhase phase;
    protected MoveState moveState;
    protected Ballot ballotToPropose; // ballot we want to propose
    protected GameMove move; // the move we are mandated to commit
    protected Optional<GameMove> previousMove; // the move we must propagate if we receive one back
    protected Optional<Ballot> higherBallot; // the ballot we lost to

    private Logger logger;

    public ProposerState(Ballot ballotToPropose, GameMove move, Logger logger) {
        this.phase = ProposerPhase.IDLE;
        this.moveState = MoveState.SELF;
        this.ballotToPropose = ballotToPropose;
        this.move = move;
        this.previousMove = Optional.empty();
        this.higherBallot = Optional.empty();

        this.logger = logger;
    }

    public void transitionToPropose() {
        logger.info("transitioning to propose state");
        this.phase = ProposerPhase.PROPOSE;
    }

    // when we have sent accepts
    public void transitionToAccept() {
        logger.info("transitioning to accept state");

        this.phase = ProposerPhase.ACCEPT;
    }

    public void transitionToConfirm() {
        logger.info("transitioning to confirm state");

        this.phase = ProposerPhase.CONFIRM;
    }

    public void transitionToAwaitTimeout(Ballot higherBallot) {
        logger.info("transitioning to wait for timeout state");

        this.phase = ProposerPhase.AWAIT_TIMEOUT;
        this.higherBallot = Optional.of(higherBallot);

        //todo : start the timeout timer ?!
    }

    public void propagatePreviousMove(PromiseWithPreviousAcceptedValue p) {
        logger.info("modifying state to propagate previous move");

        // if this is the first propagation we see
        if (this.moveState == MoveState.SELF && this.previousMove.isEmpty()) {
            this.moveState = MoveState.PROPAGATE;
            this.higherBallot = Optional.of(p.ballot());
            this.previousMove = Optional.of(p.previousMove());
            return;
        }

        if (this.higherBallot.get().isLessThan(p.previousBallot())) {
            this.higherBallot = Optional.of(p.previousBallot());
            this.previousMove = Optional.of(p.previousMove());
        }
    }

    @Override
    public String toString() {
        // Use Optional.map and orElse for clean handling of Optional fields
        String prevMoveStr = previousMove
            .map(GameMove::toString)
            .orElse("None");
        String higherBallotStr = higherBallot
            .map(Ballot::toString)
            .orElse("None");

        return String.format(
            "ProposerState{phase=%s, moveState=%s, ballotToPropose=%s, move=%s, previousMove=%s, higherBallot=%s}",
            phase,
            moveState,
            ballotToPropose,
            move,
            prevMoveStr,
            higherBallotStr
        );
    }
}
