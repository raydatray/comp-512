package paxos;

import java.util.Optional;

enum AcceptorPhase {
    PROMISED, // Received Propose, sent Promise
    ACCEPTED, // Received AcceptRequest, sent AcceptAck
}

class AcceptorTurnState {

    protected AcceptorPhase phase;
    protected Ballot highestPromised; // Highest ballot we've promised to
    protected Optional<Ballot> prevAcceptedBallot; // Ballot we accepted (empty if only promised)
    protected Optional<GameMove> prevAcceptedValue; // Value we accepted (empty if only promised)

    // Constructor for initial Promise
    public AcceptorTurnState(Ballot promised) {
        this.phase = AcceptorPhase.PROMISED;
        this.highestPromised = promised;
        this.prevAcceptedBallot = Optional.empty();
        this.prevAcceptedValue = Optional.empty();
    }

    // Update when we accept a value
    public void accept(Ballot ballot, GameMove value) {
        this.phase = AcceptorPhase.ACCEPTED;
        this.updateHighestPromisedBallot(ballot);
        this.prevAcceptedBallot = Optional.of(ballot);
        this.prevAcceptedValue = Optional.of(value);
    }

    // Update promise to higher ballot
    public void updateHighestPromisedBallot(Ballot newPromise) {
        if (
            highestPromised == null || newPromise.isGreaterThan(highestPromised)
        ) {
            this.highestPromised = newPromise;
        }
    }
}
