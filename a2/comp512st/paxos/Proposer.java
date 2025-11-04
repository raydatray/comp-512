package paxos;

import java.util.Optional;
import java.util.logging.Logger;

public class Proposer {

    private GCLReader gclReader;
    private GCLWriter gclWriter;
    private Logger logger;

    private BallotGenerator ballotGenerator;

    private Integer majority;

    private Acceptor acceptor;

    // todo: add a real timeout

    Proposer(
        GCLReader reader,
        GCLWriter writer,
        Logger logger,
        BallotGenerator ballotGenerator,
        Integer majority,
        Acceptor acceptor
    ) {
        this.gclReader = reader;
        this.gclWriter = writer;
        this.logger = logger;

        this.ballotGenerator = ballotGenerator;

        this.majority = majority;
        this.acceptor = acceptor;
    }

    // guarantees i will send this move
    // run a paxos instance who will return a ProposerState
    // if its confirmed, and self, we can exit
    // if not, we need to try again
    public void sendMessage(Ballot ballotToPropose, GameMove move) {
        while (true) {
            // Check if this turn is already confirmed before even trying
            Long currentTurn = ballotToPropose.turn();
            Long lastConfirmedTurn = acceptor.getLastConfirmedTurn();

            if (currentTurn <= lastConfirmedTurn) {
                // This turn is already decided, move to next turn
                ballotToPropose = ballotGenerator.nextTurn();
                continue;
            }

            ProposerInstance proposerInstance = new ProposerInstance(
                this.gclReader,
                this.gclWriter,
                this.logger,
                this.majority,
                ballotToPropose,
                move
            );

            Boolean installed = proposerInstance.runInstance();

            // if we installed some value, was it what we originally wanted?
            if (installed) {
                // First check if we got what we wanted
                if (proposerInstance.committedOriginalMove()) {
                    // Success! Our move was committed, we're done
                    return;
                }

                // We installed a value, but it wasn't our original move
                // This means we had to propagate someone else's previously accepted value
                // Try again with the next turn
                ballotToPropose = ballotGenerator.nextTurn();
                continue;
            } else {
                // if we did not install a value
                // We need to generate a higher ballot number for retry
                // Check if someone else confirmed this turn while we were trying

                lastConfirmedTurn = acceptor.getLastConfirmedTurn();

                if (currentTurn <= lastConfirmedTurn) {
                    // This turn was confirmed by someone else while we were attempting
                    // Check if the confirmed move is our move
                    Optional<GameMove> confirmedMove =
                        acceptor.getConfirmedMoveForTurn(currentTurn);
                    if (
                        confirmedMove.isPresent() &&
                        confirmedMove.get().equals(move)
                    ) {
                        // Success! Someone else confirmed our move for this turn
                        logger.info(
                            "Another proposer confirmed our move for turn " +
                                currentTurn
                        );
                        return;
                    }
                    // Different move was confirmed, move to next turn
                    ballotToPropose = ballotGenerator.nextTurn();
                } else {
                    // Turn not yet confirmed - generate a higher ballot and retry same turn
                    // Add a small random backoff to prevent thundering herd
                    try {
                        Thread.sleep((long) (Math.random() * 50)); // 0-50ms random backoff
                    } catch (Exception e) {}

                    ballotToPropose = ballotGenerator.higherBallot(
                        ballotToPropose
                    );
                }
                continue;
            }
        }
    }
}
