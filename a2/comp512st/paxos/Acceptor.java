package paxos;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

class Acceptor implements Runnable {

    private Long maxBID;
    private Long lastAcceptedBID;
    private GameMove move;

    private GCLReader reader;
    private GCLWriter writer;
    private Logger logger;

    private BlockingQueue<GameMove> moveQ = new LinkedBlockingQueue<>();

    private Thread ingester;
    private volatile Boolean running = true;

    Acceptor(GCLReader reader, GCLWriter writer, Logger logger) {
        maxBID = -1L;
        lastAcceptedBID = -1L;
        move = null;

        this.reader = reader;
        this.writer = writer;
        this.logger = logger;

        ingester = new Thread(this);
        ingester.start();
    }

    public void run() {
        while (running) {
            try {
                PaxosEnvelope<ProposerMessage> env = reader.consumeAcceptorQ();
                String sender = env.sender();
                ProposerMessage msg = env.message();

                switch (msg) {
                    case Propose p -> {
                        handlePropose(sender, p);
                    }
                    case AcceptRequest a -> {
                        handleAcceptRequest(sender, a);
                    }
                    case Confirm c -> {
                        handleConfirm(sender, c);
                    }
                    default -> {
                        logger.warning(
                            "Unknow message type " +
                                msg.getClass().getName() +
                                " received at acceptor"
                        );
                    }
                }
            } catch (InterruptedException e) {
                logger.severe("Thread interrupted: " + e.getStackTrace());
                System.exit(1);
            }
        }
    }

    private void handlePropose(String sender, Propose msg) {
        Long ballotId = msg.ballotId();

        if (Long.compare(ballotId, maxBID) <= 0) {
            logger.info(
                "Refusing Propose from " +
                    sender +
                    " with ballot ID " +
                    ballotId +
                    ", offering higher ballot ID " +
                    maxBID
            );

            Refuse ref = new Refuse(maxBID);
            writer.send(sender, ref);
        } else {
            Promise prom;
            if (move == null) {
                prom = new Promise(ballotId, null, null);
            } else {
                prom = new Promise(ballotId, lastAcceptedBID, move);
            }

            logger.info(
                "Sending Promise to " + sender + " with ballot ID " + ballotId
            );

            writer.send(sender, prom);
            maxBID = ballotId;
        }
    }

    GameMove pollMoveQ() throws InterruptedException {
        return moveQ.take();
    }

    private void handleAcceptRequest(String sender, AcceptRequest msg) {
        if (msg.ballotId().equals(maxBID)) {
            move = msg.move();
            lastAcceptedBID = msg.ballotId();

            logger.info(
                "Accepting move from " +
                    sender +
                    " with ballot ID " +
                    msg.ballotId()
            );

            AcceptAck ack = new AcceptAck(msg.ballotId());
            writer.send(sender, ack);
        } else {
            logger.info(
                "Denying AcceptRequest from " +
                    sender +
                    " with ballotId: " +
                    msg.ballotId()
            );

            Deny deny = new Deny(
                Math.max(maxBID, Math.max(lastAcceptedBID, msg.ballotId()))
            );
            writer.send(sender, deny);
        }
    }

    private void handleConfirm(String sender, Confirm msg) {
        if (move != null) {
            moveQ.offer(move);
        }

        move = null; // reset value
    }

    void shutdown() throws InterruptedException {
        logger.info("Shutting down Acceptor thread.");
        running = false;
        ingester.join(1000); // allow 1000ms grace period
        ingester.interrupt(); // interrupt if exceeds timeout

        // delegate acceptor responsibility to shutdown shared reader
        // (as opposed to proposer) since it has shutdown entrypoint
        reader.shutdown();
    }
}
