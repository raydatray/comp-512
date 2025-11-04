// Proposer.java - Modified to pass acceptor to ProposerInstance
package paxos;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public class Proposer {

    private GCLReader reader;
    private GCLWriter writer;
    private Logger log;

    private BallotGenerator ballotGenerator;

    private Integer majority;

    private Acceptor acceptor;

    Proposer(
        GCLReader reader,
        GCLWriter writer,
        Logger log,
        BallotGenerator ballotGenerator,
        Integer majority,
        Acceptor acceptor
    ) {
        this.reader = reader;
        this.writer = writer;
        this.log = log;

        this.ballotGenerator = ballotGenerator;

        this.majority = majority;
        this.acceptor = acceptor;
    }

    public void sendMessage(Ballot ballotToPropose, GameMove move) {
        ProposerInstance i = new ProposerInstance(
            this.reader,
            this.writer,
            this.log,
            this.majority,
            ballotToPropose,
            move,
            this.acceptor // Pass acceptor to instance
        );

        while (true) {
            ProposerState s = i.runInstance();

            switch (s.phase) {
                case CONFIRM:
                    if (s.mState == MoveState.SELF) {
                        log.info(
                            "Successfully installed our own move for turn " +
                                s.b.turn()
                        );
                        return;
                    }
                    log.info(
                        "Propagated previous move for turn " +
                            s.b.turn() +
                            ", retrying with next turn"
                    );

                    Ballot newB = this.ballotGenerator.nextTurn();

                    i = new ProposerInstance(
                        this.reader,
                        this.writer,
                        this.log,
                        this.majority,
                        newB,
                        s.m,
                        this.acceptor
                    );

                    continue;
                case AWAIT:
                    // We lost - wait to see if this turn gets confirmed
                    log.info(
                        "Failed to install value for turn " +
                            s.b.turn() +
                            ", waiting for confirmation or timeout"
                    );

                    CompletableFuture<GameMove> future =
                        acceptor.waitForTurnConfirmed(s.b.turn());

                    try {
                        // Wait for confirmation with timeout
                        GameMove confirmedMove = future.get(
                            500, // Increased timeout since we're more patient
                            TimeUnit.MILLISECONDS
                        );

                        // Turn was confirmed by someone
                        log.info(
                            "Different move confirmed for turn " +
                                s.b.turn() +
                                ", moving to next turn"
                        );

                        // Check if our move was confirmed
                        if (confirmedMove.equals(s.m)) {
                            log.info(
                                "Our move was confirmed by another proposer!"
                            );
                            return; // Success!
                        }

                        // Different move was confirmed, try next turn
                        Ballot nextTurnB = this.ballotGenerator.nextTurn();
                        i = new ProposerInstance(
                            this.reader,
                            this.writer,
                            this.log,
                            this.majority,
                            nextTurnB,
                            s.m, // Still trying our original move
                            this.acceptor
                        );
                        continue;
                    } catch (TimeoutException e) {
                        // Timeout - turn wasn't confirmed, retry with higher ballot
                        log.info(
                            "Timeout waiting for turn " +
                                s.b.turn() +
                                " confirmation, retrying with higher ballot"
                        );

                        Ballot higherB = this.ballotGenerator.higherBallot(
                            s.higherB.orElse(s.b)
                        );

                        i = new ProposerInstance(
                            this.reader,
                            this.writer,
                            this.log,
                            this.majority,
                            higherB,
                            s.m, // Our original move
                            this.acceptor
                        );
                        continue;
                    } catch (InterruptedException e) {
                        log.severe(
                            "Interrupted while waiting: " + e.getMessage()
                        );
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(
                            "Interrupted during Paxos",
                            e
                        );
                    } catch (ExecutionException e) {
                        log.severe(
                            "Error waiting for confirmation: " + e.getMessage()
                        );
                        throw new RuntimeException("Error during Paxos", e);
                    }
                case PROPOSE:
                case ACCEPT:
                    log.severe("Impossible state reached: " + s.phase);
                    System.exit(1);
            }
        }
    }
}
