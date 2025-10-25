package paxos;

import comp512.gcl.GCL;
import comp512.utils.FailCheck;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.logging.Logger;

// ANY OTHER classes, etc., that you add must be private to this package and not visible to the application layer.

// extend / implement whatever interface, etc. as required.
// NO OTHER public members / methods allowed. broadcastTOMsg, acceptTOMsg, and shutdownPaxos must be the only visible methods to the application layer.
//		You should also not change the signature of these methods (arguments and return value) other aspects maybe changed with reasonable design needs.
public class Paxos {

    private GCL gcl;
    private Logger logger;
    private Proposer proposer;
    private Acceptor acceptor;
    private Long ballotCounter;
    private FailCheck failCheck; // TODO: implement failcheck

    public Paxos(
        String myProcess,
        String[] allGroupProcesses,
        Logger logger,
        FailCheck failCheck
    ) throws IOException, UnknownHostException {
        this.gcl = new GCL(myProcess, allGroupProcesses, null, logger);
        this.logger = logger;

        GCLReader reader = new GCLReader(gcl, logger);
        GCLWriter writer = new GCLWriter(gcl, logger);
        Integer majority = (allGroupProcesses.length / 2) + 1;
        proposer = new Proposer(majority, reader, writer, logger);
        acceptor = new Acceptor(reader, writer, logger);

        ballotCounter = 0L;
        // Rember to call the failCheck.checkFailure(..) with appropriate arguments throughout your Paxos code to force fail points if necessary.
        this.failCheck = failCheck;
    }

    // This is what the application layer is going to call to send a message/value, such as the player and the move
    // Extend this to build whatever Paxos logic you need to make sure the messaging system is total order.
    // Here you will have to ensure that the CALL BLOCKS, and is returned ONLY when a majority (and immediately upon majority) of processes have accepted the value.
    public void broadcastTOMsg(Object[] val) {
        Long ballotId = ballotCounter;
        GameMove proposedMove = new GameMove(
            (Integer) val[0],
            (Character) val[1]
        );

        while (true) {
            // Phase 1 - propose self as leader
            ballotId = incrementAndGetBallotCounter();
            logger.info(
                "Initiating new round, proposing self as leader with ballot ID" +
                    ballotId
            );

            ProposeResult propRes = proposer.sendProposes(ballotId);
            switch (propRes) {
                case ProposeSuccess s -> {} // do nothing
                case ProposeSuccessWithMove s -> {
                    proposedMove = s.move(); // use previous move
                }
                case ProposeFailure f -> {
                    ballotCounter = f.greaterBID();
                    continue; // retry
                }
                default -> {
                    // nuke the system
                    logger.severe("Something went terribly wrong.");
                    System.exit(1);
                }
            }

            // Phase 2 - accept? reqs
            logger.info("Sending accept? messages" + proposedMove);

            AcceptResult accRes = proposer.sendAcceptRequests(
                ballotId,
                proposedMove
            );
            switch (accRes) {
                case AcceptSuccess s -> {} // do nothing
                case AcceptFailure f -> {
                    ballotCounter = f.greaterBID();
                    continue; // retry
                }
                default -> {
                    // nuke the system
                    logger.severe("Shake yo booty");
                    System.exit(1);
                }
            }

            // Phase 3 - move committed
            logger.info(
                "Committed value " + proposedMove + ", sending confirm messages"
            );
            proposer.sendConfirms(ballotId);

            // all done
            break;
        }
    }

    // This is what the application layer is calling to figure out what is the next message in the total order.
    // Messages delivered in ALL the processes in the group should deliver this in the same order.
    public Object acceptTOMsg() throws InterruptedException {
        GameMove move = acceptor.pollMoveQ();
        return new Object[] { move.playerNum(), move.move() };
    }

    // Add any of your own shutdown code into this method.
    public void shutdownPaxos() throws InterruptedException {
        acceptor.shutdown();
    }

    private Long incrementAndGetBallotCounter() {
        ballotCounter++;
        return ballotCounter;
    }
}
