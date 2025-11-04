package paxos;

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
                // we got what we wanted
                if (proposerInstance.committedOriginalMove()) {
                    return;
                } else {
                    // we need to try again.
                    // update the ballot
                    // increment the turn you are fighting for
                    // increment the ballot
                    // forward the move
                    ProposerState state =
                        proposerInstance.getFinalProposerState();

                    // we should tell the proposer state to take the highest ballot + 1
                    ballotToPropose = ballotGenerator.nextTurn();
                    continue;
                }
            } else {
                // if we did not install a value
                // start the timeout
                // update our ballot
                // do not update the turn yet
                // poll the acceptor's last confirmed turn
                Long lastConfirmedTurn = acceptor.getLastConfirmedTurn();
                Long currentTurn = ballotToPropose.turn();

                // if we have confirmed a value for this turn, increment the ballot for the next turn
                if (currentTurn <= lastConfirmedTurn) {
                    ballotToPropose = ballotGenerator.nextTurn();
                } else {
                    // otherwise, retry with a higher ballot for the same turn
                    ballotToPropose = ballotGenerator.higherBallot(
                        ballotToPropose
                    );
                }
                continue;
            }
        }
    }
}
