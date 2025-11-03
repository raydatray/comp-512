package paxos;

import java.util.Optional;

enum AcceptorPhase {
    PROMISED,    // Received Propose, sent Promise
    ACCEPTED     // Received AcceptRequest, sent AcceptAck
}

class AcceptorTurnState {
    private AcceptorPhase phase;
    private Ballot highestPromised;                // Highest ballot we've promised to
    private Optional<Ballot> acceptedBallot;       // Ballot we accepted (empty if only promised)
    private Optional<GameMove> acceptedValue;      // Value we accepted (empty if only promised)

    // Constructor for initial Promise
    public AcceptorTurnState(Ballot promised) {
        this.phase = AcceptorPhase.PROMISED;
        this.highestPromised = promised;
        this.acceptedBallot = Optional.empty();
        this.acceptedValue = Optional.empty();
    }

    // Update when we accept a value
    public void accept(Ballot ballot, GameMove value) {
        this.phase = AcceptorPhase.ACCEPTED;
        this.acceptedBallot = Optional.of(ballot);
        this.acceptedValue = Optional.of(value);
        // Note: highestPromised stays the same or gets updated separately
    }

    // Update promise to higher ballot
    public void updatePromise(Ballot newPromise) {
        if (highestPromised == null || newPromise.isGreaterThan(highestPromised)) {
            this.highestPromised = newPromise;
        }
    }

    // Getters
    public AcceptorPhase getPhase() { return phase; }
    public Ballot getHighestPromised() { return highestPromised; }
    public Optional<Ballot> getAcceptedBallot() { return acceptedBallot; }
    public Optional<GameMove> getAcceptedValue() { return acceptedValue; }
    public boolean hasAcceptedValue() { return acceptedBallot.isPresent(); }
}
