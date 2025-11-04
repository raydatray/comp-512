package paxos;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

class Acceptor implements Runnable {

    private GCLReader reader;
    private GCLWriter writer;
    private Logger logger;

    private BlockingQueue<GameMove> moveQ = new LinkedBlockingQueue<>();

    private Ballot maxBallot;
    private Ballot lastAcceptedBallot;
    private GameMove lastAcceptedMove;

    private Thread ingester;
    private volatile Boolean running = true;

    Acceptor(GCLReader reader, GCLWriter writer, Logger logger) {
        maxBallot = new Ballot(-1, -1);
        lastAcceptedBallot = null;
        lastAcceptedMove = null;

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

    private void handlePropose(String sender, Propose propose) {
        Ballot proposeBallot = propose.ballot();

        if (proposeBallot.isLessThan(maxBallot)) {
            logger.info(
                "`Refusing propose` from " +
                    sender +
                    " with ballot " +
                    proposeBallot +
                    ", offering higher ballot " +
                    maxBallot
            );

            Refuse ref = new Refuse(maxBallot);
            writer.send(sender, ref);
        } else {
            Promise prom;

            if (lastAcceptedMove == null) {
                logger.info(
                    "`Sending promise` to " +
                        sender +
                        " with ballot " +
                        proposeBallot
                );

                prom = new Promise(proposeBallot, null, null);
            } else {
                logger.info(
                    "`Sending promise` to " +
                        sender +
                        " with ballot " +
                        proposeBallot +
                        " `and previous move` " +
                        lastAcceptedMove +
                        " whose ballot was " +
                        lastAcceptedBallot
                );

                prom = new Promise(
                    proposeBallot,
                    lastAcceptedBallot,
                    lastAcceptedMove
                );
            }

            writer.send(sender, prom);
            maxBallot = proposeBallot;
        }
    }

    private void handleAcceptRequest(
        String sender,
        AcceptRequest acceptRequest
    ) {
        Ballot acceptRequestBallot = acceptRequest.ballot();

        // cannot possibly receive an accept? request whose ballot is greater because GCL guarantees FIFO ordering
        // as such, we would have seen the promise with that higher ballot and would have updated our own maxBallot
        // state variable accordingly.
        if (acceptRequestBallot.equals(maxBallot)) {
            lastAcceptedBallot = acceptRequestBallot;
            lastAcceptedMove = acceptRequest.move();

            logger.info(
                "`Accepted AcceptRequest` move from " +
                    sender +
                    " with ballot " +
                    acceptRequestBallot +
                    ", sending AcceptAck"
            );

            AcceptAck ack = new AcceptAck(acceptRequestBallot);
            writer.send(sender, ack);
        } else {
            logger.info(
                "`Denying AcceptRequest` from " +
                    sender +
                    " with ballot " +
                    acceptRequestBallot +
                    ", offering higher ballot " +
                    maxBallot
            );

            Deny deny = new Deny(maxBallot);
            writer.send(sender, deny);
        }
    }

    private void handleConfirm(String sender, Confirm confirm) {
        logger.info(
            "`Received confirm` for ballot " +
                confirm.ballot() +
                ", committing move " +
                lastAcceptedMove +
                " to Q"
        );

        if (lastAcceptedMove == null) {
            logger.warning(
                "`SOMETHING WENT WRONG`: THE MOVE IS NULL WHEN WE WANT TO COMMIT IT... BALLOT:" +
                    confirm.ballot()
            );
        }

        moveQ.offer(lastAcceptedMove);
        lastAcceptedMove = null; // reset value for future instances
        // if we don't reset the last accepted move, acceptors will continuously
        // return promises with this accepted value and we will have an explosion
        // of duplicates
    }

    GameMove consumeMoveQ() throws InterruptedException {
        return moveQ.take();
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
