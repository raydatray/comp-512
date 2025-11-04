package paxos;

import comp512.gcl.*;
import comp512.utils.*;
import java.io.*;
import java.net.UnknownHostException;
import java.util.logging.*;

public class Paxos {

    GCL gcl;
    FailCheck failCheck;

    private Integer majority;

    private GCLReader reader;
    private GCLWriter writer;

    private Proposer proposer;
    private Acceptor acceptor;

    private BallotGenerator ballotGenerator;

    public Paxos(
        Integer playerNum,
        String myProcess,
        String[] allGroupProcesses,
        Logger logger,
        FailCheck failCheck
    ) throws IOException, UnknownHostException {
        this.gcl = new GCL(myProcess, allGroupProcesses, null, logger);
        this.failCheck = failCheck;

        this.reader = new GCLReader(gcl, logger);
        this.writer = new GCLWriter(gcl, logger);

        Integer majority = (allGroupProcesses.length / 2) + 1;

        this.ballotGenerator = new BallotGenerator(playerNum);

        this.acceptor = new Acceptor(reader, writer, logger);
        this.proposer = new Proposer(
            reader,
            writer,
            logger,
            ballotGenerator,
            majority,
            this.acceptor
        );
    }

    // This is what the application layer is going to call to send a message/value, such as the player and the move
    public void broadcastTOMsg(Object[] val) {
        Ballot ballotToPropose = this.ballotGenerator.nextTurn();
        GameMove move = new GameMove((Integer) val[0], (Character) val[1]);
        this.proposer.sendMessage(ballotToPropose, move);
    }

    // This is what the application layer is calling to figure out what is the next message in the total order.
    // Messages delivered in ALL the processes in the group should deliver this in the same order.
    public Object acceptTOMsg() throws InterruptedException {
        GameMove move = this.acceptor.consume();

        return new Object[] { move.pNum(), move.m() };
    }

    // Add any of your own shutdown code into this method.
    public void shutdownPaxos() {
        try {
            // Give time for final messages to be processed
            Thread.sleep(1000);

            // Shut down Paxos components first (before GCL)
            if (acceptor != null) {
                acceptor.shutdown();
            }

            if (reader != null) {
                reader.shutdown();
            }

            // Note: GCLReader has its own shutdown, but we need to call it
            // You might need to expose reader/writer in Paxos constructor
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Finally shut down GCL
            gcl.shutdownGCL();
        }
    }
}
