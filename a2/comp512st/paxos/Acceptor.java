package paxos;

import comp512.utils.FailCheck;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

class Acceptor implements Runnable {

    private GCLReader reader;
    private GCLWriter writer;
    private Logger logger;

    private BlockingQueue<GameMove> moveQ;
    private BlockingQueue<Shutdown> shutdownQ;
    private Set<Identifier> committedMoves;
    private Integer groupSize;

    private Long maxBallot;
    private Long lastAcceptedBallot;
    private GameMove lastAcceptedMove;

    private FailCheck failCheck;

    private Thread ingester;
    private volatile Boolean running = true;

    Acceptor(
        GCLReader reader,
        GCLWriter writer,
        Logger logger,
        Integer groupSize,
        FailCheck failCheck
    ) {
        this.reader = reader;
        this.writer = writer;
        this.logger = logger;

        this.moveQ = new LinkedBlockingQueue<>();
        this.shutdownQ = new LinkedBlockingQueue<>();
        this.committedMoves = new HashSet<>();
        this.groupSize = groupSize;

        maxBallot = -1L;
        lastAcceptedBallot = null;
        lastAcceptedMove = null;

        this.failCheck = failCheck;

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
                        failCheck.checkFailure(
                            FailCheck.FailureType.RECEIVEPROPOSE
                        );

                        handlePropose(sender, p);

                        failCheck.checkFailure(
                            FailCheck.FailureType.AFTERSENDVOTE
                        );
                    }
                    case AcceptRequest a -> {
                        handleAcceptRequest(sender, a);
                    }
                    case Confirm c -> {
                        handleConfirm(sender, c);
                    }
                    case Shutdown s -> {
                        handleShutdown(sender, s);
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
                logger.info("Acceptor thread interrupted, shutting down.");
            }
        }
    }

    private void handlePropose(String sender, Propose propose) {
        Long proposeBallot = propose.ballot();

        if (proposeBallot <= maxBallot) {
            logger.info(
                "`Refusing propose` from " +
                    sender +
                    " with ballot " +
                    proposeBallot +
                    ", offering higher ballot " +
                    maxBallot
            );

            Refuse ref = new Refuse(maxBallot, proposeBallot);
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
        Long acceptRequestBallot = acceptRequest.ballot();

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

            Deny deny = new Deny(maxBallot, acceptRequestBallot);
            writer.send(sender, deny);
        }
    }

    private void handleConfirm(String sender, Confirm confirm) {
        logger.info("`Received confirm` " + confirm);

        // hash by game move ID, so we can handle duplicate confirms
        // in an idempotent manner
        if (committedMoves.add(confirm.move().id())) {
            moveQ.add(confirm.move());
        }

        lastAcceptedMove = null; // reset value for future instances
        // if we don't reset the last accepted move, acceptors will continuously
        // return promises with this accepted value and we will have an explosion
        // of duplicates
    }

    private void handleShutdown(String sender, Shutdown shutdown) {
        logger.info("`Received shutdown `" + shutdown);
        shutdownQ.add(shutdown);
    }

    GameMove consumeMoveQ() throws InterruptedException {
        return moveQ.take();
    }

    void shutdown() throws InterruptedException {
        logger.info("`Shutting down` Acceptor thread.");

        // waiting to gather all shutdown messages
        // allow up to 1 min for this, after which we just force shutdown
        // in case one of the instances crashed
        Set<Shutdown> shutdowns = new HashSet<>();
        Long startTime = System.currentTimeMillis();
        while (
            shutdowns.size() < groupSize &&
            System.currentTimeMillis() - startTime < 60_000
        ) {
            Shutdown shutdown = shutdownQ.poll(100, TimeUnit.MILLISECONDS);
            if (shutdown == null) continue;

            shutdowns.add(shutdown);
        }

        running = false;
        ingester.join(1000); // allow 1000ms grace period
        ingester.interrupt(); // interrupt if exceeds timeout

        // delegate acceptor responsibility to shutdown shared reader
        // (as opposed to proposer) since it has shutdown entrypoint
        reader.shutdown();
    }
}
