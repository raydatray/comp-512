// ProposerInstance.java - Modified version
package paxos;

import java.util.logging.Logger;

public class ProposerInstance {

    private GCLReader reader;
    private GCLWriter writer;
    private Logger log;

    private Integer majority;

    private ProposerState state;

    private Acceptor acceptor;

    ProposerInstance(
        GCLReader reader,
        GCLWriter writer,
        Logger log,
        Integer majority,
        Ballot ballot,
        GameMove move,
        Acceptor acceptor
    ) {
        this.reader = reader;
        this.writer = writer;
        this.log = log;

        this.majority = majority;

        this.state = new ProposerState(ballot, move, log);
        this.acceptor = acceptor;
    }

    public ProposerState runInstance() {
        Boolean majorityPromised = this.sendProposes();
        if (!majorityPromised) {
            return this.state;
        }

        Boolean majorityAccepted = this.sendAccepts();
        if (!majorityAccepted) {
            return this.state;
        }

        this.sendConfirms();
        return this.state;
    }

    public Boolean sendProposes() {
        log.info("sending proposes for " + this.state.toString());

        Integer promises = 0;
        Integer refuses = 0;

        Propose propose = new Propose(this.state.b);
        this.writer.broadcast(propose);

        while (true) {
            try {
                PaxosEnvelope<AcceptorMessage> envelope =
                    this.reader.consumeProposerQ();

                AcceptorMessage msg = envelope.message();

                Long currTurn = this.state.b.turn();

                switch (msg) {
                    case Promise p -> {
                        if (currTurn != p.ballot().turn()) {
                            log.fine(
                                "Ignoring Promise from different turn. " +
                                    "curr turn: " +
                                    currTurn +
                                    " received: " +
                                    p.ballot().turn()
                            );
                            continue;
                        }

                        if (!this.state.b.equalTo(p.ballot())) {
                            log.fine(
                                "Ignoring Promise for different ballot. " +
                                    "curr ballot: " +
                                    state.b +
                                    " received: " +
                                    p.ballot()
                            );
                            continue;
                        }
                        promises++;
                    }
                    case PromiseWithPreviousAcceptedValue p -> {
                        if (currTurn != p.ballot().turn()) {
                            log.fine(
                                "Ignoring PromiseWithPrev from different turn. " +
                                    "curr turn: " +
                                    currTurn +
                                    " received: " +
                                    p.ballot().turn()
                            );
                            continue;
                        }

                        if (!this.state.b.equalTo(p.ballot())) {
                            log.fine(
                                "Ignoring PromiseWithPrev for different ballot. " +
                                    "curr ballot: " +
                                    state.b +
                                    " received: " +
                                    p.ballot()
                            );
                            continue;
                        }
                        promises++;
                        state.propagatePreviousMove(p);
                    }
                    case Refuse r -> {
                        // Only count refuses for our turn
                        if (currTurn != r.ballot().turn()) {
                            log.fine(
                                "Ignoring Refuse from different turn. " +
                                    "curr turn: " +
                                    currTurn +
                                    " received: " +
                                    r.ballot().turn()
                            );
                            continue;
                        }

                        refuses++;

                        // If we got a majority of refuses, we definitely lost
                        if (refuses >= this.majority) {
                            log.info(
                                "Received majority refuses for turn " +
                                    currTurn +
                                    ", waiting for turn to be confirmed"
                            );
                            state.transitionToAwait(r.ballot());
                            return false;
                        }

                        // Store the higher ballot we saw
                        if (
                            state.higherB.isEmpty() ||
                            state.higherB.get().isLessThan(r.ballot())
                        ) {
                            state.higherB = java.util.Optional.of(r.ballot());
                        }
                    }
                    case AcceptAck a -> {
                        // From a previous instance's accept phase - ignore
                        log.fine(
                            "Ignoring AcceptAck in propose phase. " +
                                "curr turn: " +
                                currTurn +
                                " received turn: " +
                                a.ballot().turn()
                        );
                        continue;
                    }
                    case Deny d -> {
                        // From a previous instance's accept phase
                        log.fine(
                            "Ignoring Deny in propose phase. " +
                                "curr turn: " +
                                currTurn +
                                " received turn: " +
                                d.ballot().turn()
                        );
                        continue;
                    }
                    default -> {
                        log.warning(
                            "Unknown message type: " + msg.getClass().getName()
                        );
                    }
                }

                if (majorityReached(promises)) {
                    return true;
                }

                // If we have both refuses and promises, but neither reached majority,
                // check if we still have a chance
                if (
                    promises + refuses >= this.majority &&
                    promises < this.majority
                ) {
                    // We can't possibly get a majority of promises
                    log.info(
                        "Cannot achieve majority promises for turn " +
                            currTurn +
                            ", waiting for turn to be confirmed"
                    );
                    state.transitionToAwait(state.higherB.orElse(this.state.b));
                    return false;
                }
            } catch (InterruptedException e) {
                log.severe(
                    "Uncaught exception in promises loop: " + e.getMessage()
                );
                System.exit(1);
            }
        }
    }

    public Boolean sendAccepts() {
        log.info("sending accepts for " + this.state.toString());
        state.transitionToAccept();

        Integer acceptAcks = 0;
        Integer denies = 0;

        AcceptRequest acceptRequest = new AcceptRequest(
            this.state.b,
            this.state.getMoveToPropose()
        );

        this.writer.broadcast(acceptRequest);

        while (true) {
            try {
                PaxosEnvelope<AcceptorMessage> envelope =
                    this.reader.consumeProposerQ();

                AcceptorMessage msg = envelope.message();

                Long currTurn = this.state.b.turn();

                switch (msg) {
                    case AcceptAck a -> {
                        if (currTurn != a.ballot().turn()) {
                            log.fine(
                                "Ignoring AcceptAck from different turn. " +
                                    "curr turn: " +
                                    currTurn +
                                    " received: " +
                                    a.ballot().turn()
                            );
                            continue;
                        }

                        if (!this.state.b.equalTo(a.ballot())) {
                            log.fine(
                                "Ignoring AcceptAck for different ballot. " +
                                    "curr ballot: " +
                                    state.b +
                                    " received: " +
                                    a.ballot()
                            );
                            continue;
                        }
                        acceptAcks++;
                    }
                    case Deny d -> {
                        // Only count denies for our turn
                        if (currTurn != d.ballot().turn()) {
                            log.fine(
                                "Ignoring Deny from different turn. " +
                                    "curr turn: " +
                                    currTurn +
                                    " received: " +
                                    d.ballot().turn()
                            );
                            continue;
                        }

                        denies++;

                        // If we got a majority of denies, we definitely lost
                        if (denies >= this.majority) {
                            log.info(
                                "Received majority denies for turn " +
                                    currTurn +
                                    ", waiting for turn to be confirmed"
                            );
                            this.state.transitionToAwait(d.ballot());
                            return false;
                        }

                        // Store the higher ballot we saw
                        if (
                            state.higherB.isEmpty() ||
                            state.higherB.get().isLessThan(d.ballot())
                        ) {
                            state.higherB = java.util.Optional.of(d.ballot());
                        }
                    }
                    case Promise p -> {
                        // From a previous instance's propose phase - ignore
                        log.fine(
                            "Ignoring Promise in accept phase. " +
                                "curr turn: " +
                                currTurn +
                                " received turn: " +
                                p.ballot().turn()
                        );
                        continue;
                    }
                    case PromiseWithPreviousAcceptedValue p -> {
                        // From a previous instance's propose phase - ignore
                        log.fine(
                            "Ignoring PromiseWithPrev in accept phase. " +
                                "curr turn: " +
                                currTurn +
                                " received turn: " +
                                p.ballot().turn()
                        );
                        continue;
                    }
                    case Refuse r -> {
                        // From a previous instance's propose phase - ignore
                        log.fine(
                            "Ignoring Refuse in accept phase. " +
                                "curr turn: " +
                                currTurn +
                                " received turn: " +
                                r.ballot().turn()
                        );
                        continue;
                    }
                    default -> {
                        log.warning(
                            "Unknown message type: " + msg.getClass().getName()
                        );
                    }
                }

                if (majorityReached(acceptAcks)) {
                    return true;
                }

                // If we have both denies and acks, but neither reached majority,
                // check if we still have a chance
                if (
                    acceptAcks + denies >= this.majority &&
                    acceptAcks < this.majority
                ) {
                    // We can't possibly get a majority of accepts
                    log.info(
                        "Cannot achieve majority accepts for turn " +
                            currTurn +
                            ", waiting for turn to be confirmed"
                    );
                    this.state.transitionToAwait(
                        state.higherB.orElse(this.state.b)
                    );
                    return false;
                }
            } catch (InterruptedException e) {
                log.severe(
                    "Uncaught exception in accepts loop: " + e.getMessage()
                );
                System.exit(1);
            }
        }
    }

    protected void sendConfirms() {
        log.info("sending confirms for " + this.state.toString());
        state.transitionToConfirm();

        Confirm confirm = new Confirm(
            this.state.b,
            this.state.getMoveToPropose()
        );

        this.writer.broadcast(confirm);
    }

    protected ProposerState getFinalProposerState() {
        return this.state;
    }

    private Boolean majorityReached(Integer received) {
        return received >= this.majority;
    }
}
