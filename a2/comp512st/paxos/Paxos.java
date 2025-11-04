package paxos;

import comp512.gcl.GCL;
import comp512.utils.FailCheck;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.logging.Logger;

// ANY OTHER classes, etc., that you add must be private to this package and not visible to the application layer.

// extend / implement whatever interface, etc. as required.
// NO OTHER public members / methods allowed. broadcastTOMsg, acceptTOMsg, and shutdownPaxos must be the only visible methods to the application layer.
//		You should also not change the signature of these methods (arguments and return value) other aspects maybe changed with reasonable design needs.
public class Paxos {

    private static Integer BASE_DELAY_MS = 50;
    private static Integer MAX_DELAY_MS = 1000;

    private GCL gcl;
    private Logger logger;
    private Random random;

    private Proposer proposer;
    private Acceptor acceptor;

    private Integer playerNum;
    private Integer moveCounter;

    // TODO: implement failcheck
    private FailCheck failCheck;

    public Paxos(
        Integer playerNum,
        String myProcess,
        String[] allGroupProcesses,
        Logger logger,
        FailCheck failCheck
    ) throws IOException, UnknownHostException {
        this.gcl = new GCL(myProcess, allGroupProcesses, null, logger);
        this.logger = logger;
        this.random = new Random(playerNum); // seed using unique player number

        GCLReader reader = new GCLReader(gcl, logger);
        GCLWriter writer = new GCLWriter(gcl, logger);
        Integer majority = (allGroupProcesses.length / 2) + 1;
        proposer = new Proposer(playerNum, majority, reader, writer, logger);
        acceptor = new Acceptor(
            reader,
            writer,
            logger,
            allGroupProcesses.length
        );

        this.playerNum = playerNum;
        this.moveCounter = 0;

        // Rember to call the failCheck.checkFailure(..) with appropriate arguments throughout your Paxos code to force fail points if necessary.
        this.failCheck = failCheck;
    }

    // This is what the application layer is going to call to send a message/value, such as the player and the move
    // Extend this to build whatever Paxos logic you need to make sure the messaging system is total order.
    // Here you will have to ensure that the CALL BLOCKS, and is returned ONLY when a majority (and immediately upon majority) of processes have accepted the value.
    public void broadcastTOMsg(Object[] val) throws InterruptedException {
        moveCounter++;

        Identifier id = new Identifier(playerNum, moveCounter);
        GameMove move = new GameMove(id, (Integer) val[0], (Character) val[1]);
        Integer attempt = 0;

        while (true) {
            // exponential backoff with jitter to prevent livelock contention
            Double jitter = 0.5 + random.nextDouble(); // [0.5, 1.5)
            Long delay = (long) (BASE_DELAY_MS * Math.pow(2, attempt) * jitter);
            delay = Math.min(delay, MAX_DELAY_MS); // cap delay

            logger.info(
                String.format(
                    "Trying `new round of Paxos` for move %s (attempt %d, delay=%dms)",
                    move,
                    attempt + 1,
                    delay
                )
            );
            GameMove committedMove = proposer.runInstance(move, delay);

            // only exit if the proposer managed to commit smth and that smth
            // was our move
            if (committedMove != null && committedMove.equals(move)) {
                break;
            }
        }
    }

    // This is what the application layer is calling to figure out what is the next message in the total order.
    // Messages delivered in ALL the processes in the group should deliver this in the same order.
    public Object acceptTOMsg() throws InterruptedException {
        GameMove move = acceptor.consumeMoveQ();
        return new Object[] { move.playerNum(), move.move() };
    }

    // Add any of your own shutdown code into this method.
    public void shutdownPaxos() throws InterruptedException {
        proposer.shutdown(playerNum);
        Thread.sleep(1000); // wait 1s for shutdown msgs to propagate
        acceptor.shutdown();
    }
}
