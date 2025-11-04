package paxos;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class Acceptor implements Runnable {

    private final GCLReader reader;
    private final GCLWriter writer;
    private final Logger log;

    private final Map<Long, AcceptorState> turnMap;
    private final Map<Long, CompletableFuture<GameMove>> turnFutures;
    private final BlockingQueue<GameMove> moveQ;

    private Long lastCommitted;

    private volatile Boolean running;
    private Thread msgConsumer;

    Acceptor(GCLReader reader, GCLWriter writer, Logger log) {
        this.reader = reader;
        this.writer = writer;
        this.log = log;

        this.turnMap = new ConcurrentHashMap<>();
        this.turnFutures = new ConcurrentHashMap<>();
        this.moveQ = new LinkedBlockingQueue<>();

        this.lastCommitted = -1L;

        this.running = true;
        this.msgConsumer = new Thread(this);

        this.msgConsumer.start();
    }

    // this runs in a background thread
    public void run() {
        while (running) {
            try {
                PaxosEnvelope<ProposerMessage> envelope =
                    this.reader.consumeAcceptorQ();

                String sender = envelope.sender();
                ProposerMessage msg = envelope.message();

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
                        log.warning(
                            "unknown message " + msg.getClass().getName()
                        );
                    }
                }
            } catch (InterruptedException e) {
                // Exit gracefully when shutting down
                log.info("acceptor msg consumer interrupted, shutting down");
                break;
            }
        }
    }

    private synchronized void handlePropose(String sender, Propose msg) {
        log.info("handling propose for " + msg);

        Ballot currB = msg.ballot();
        Long turn = currB.turn();

        AcceptorState state = this.turnMap.get(turn);

        if (state == null) {
            state = new AcceptorState(currB, log);
            this.turnMap.put(turn, state);

            log.info("promising " + msg);

            this.writer.send(sender, new Promise(currB));
            return;
        }

        if (state.highestB.isGreaterThan(currB)) {
            log.info("rejecting " + msg + " higher ballot" + state.highestB);

            this.writer.send(sender, new Refuse(state.highestB));
            return;
        }

        if (state.highestB.equalTo(currB)) {
            log.info("already promised " + msg);

            this.writer.send(sender, new Promise(currB));
            return;
        }

        state.updateHighestB(currB);

        if (state.prevB.isPresent()) {
            // we know these calls are safe
            Ballot prevB = state.prevB.get();
            GameMove prevM = state.prevM.get();

            log.info("promising with propagation " + prevB + prevM + turn);

            this.writer.send(
                sender,
                new PromiseWithPreviousAcceptedValue(currB, prevB, prevM)
            );

            return;
        }

        log.info("promising " + msg);
        this.writer.send(sender, new Promise(currB));
        return;
    }

    private synchronized void handleAcceptRequest(
        String sender,
        AcceptRequest msg
    ) {
        log.info("handling accept? from " + msg);

        Ballot currB = msg.ballot();
        Long turn = currB.turn();

        AcceptorState state = this.turnMap.get(turn);

        // GCL is FIFO reliable, it should be impossible to get a null state
        if (state == null) {
            log.warning("encountered null state for turn " + turn);
            return;
        }

        if (state.highestB.isGreaterThan(currB)) {
            log.info("rejecting " + msg + " higher ballot" + state.highestB);

            this.writer.send(sender, new Deny(state.highestB));
            return;
        }

        state.transitionToAccept(currB, msg.move());
        log.info("accepting " + msg);

        this.writer.send(sender, new AcceptAck(currB));
    }

    private synchronized void handleConfirm(String sender, Confirm msg) {
        log.info("handling confirm from " + msg);

        Ballot currB = msg.ballot();
        Long turn = currB.turn();

        if (turn <= lastCommitted) {
            log.info(
                "turn " +
                    turn +
                    " already delivered to application (lastCommitted=" +
                    lastCommitted +
                    "), ignoring duplicate confirm"
            );
            return;
        }

        AcceptorState state = this.turnMap.get(turn);

        // GCL is FIFO reliable, it should be impossible to get a null state
        if (state == null) {
            log.warning("encountered null state for turn " + turn);
            return;
        }

        if (state.phase == AcceptorPhase.CONFIRMED) {
            log.warning(
                "already confirmed " +
                    turn +
                    " (lastCommitted=" +
                    lastCommitted +
                    "), skipping"
            );
            return;
        }

        // a confirm is authoritative, we must take it
        state.transitionToConfirm(currB, msg.move());
        this.deliverConfirmedMoves();

        CompletableFuture<GameMove> future = turnFutures.remove(turn);
        if (future != null) {
            future.complete(msg.move());
            log.info("notified future for turn " + turn);
        }
    }

    private void deliverConfirmedMoves() {
        Long next = lastCommitted + 1;

        while (true) {
            AcceptorState state = this.turnMap.get(next);

            if (state == null || state.phase != AcceptorPhase.CONFIRMED) {
                break;
            }

            if (next <= lastCommitted) {
                next++;
                continue;
            }

            GameMove move = state.prevM.get();
            this.moveQ.offer(move);

            long prev = lastCommitted;
            lastCommitted = next;

            log.info(
                "deliver turn " +
                    next +
                    " with move " +
                    move +
                    " (lastCommitted " +
                    prev +
                    " -> " +
                    lastCommitted +
                    ")"
            );

            next++;
        }
    }

    // Remove synchronization: avoid blocking the acceptor monitor on queue operations
    protected GameMove consume() throws InterruptedException {
        return this.moveQ.take();
    }

    // Remove synchronization: safe with CHM + CF
    protected CompletableFuture<GameMove> waitForTurnConfirmed(Long turn) {
        AcceptorState state = this.turnMap.get(turn);
        if (state != null && state.phase == AcceptorPhase.CONFIRMED) {
            return CompletableFuture.completedFuture(state.prevM.get());
        }
        return turnFutures.computeIfAbsent(turn, t ->
            new CompletableFuture<>()
        );
    }

    public void shutdown() throws InterruptedException {
        log.info("Shutting down Acceptor");
        this.running = false; // Signal the thread to stop

        // Interrupt the thread if it's blocked on queue operations
        if (msgConsumer != null && msgConsumer.isAlive()) {
            msgConsumer.interrupt();
            msgConsumer.join(2000); // Wait up to 2 seconds for clean shutdown

            if (msgConsumer.isAlive()) {
                log.warning("Acceptor thread did not terminate cleanly");
            }
        }

        log.info("Acceptor shut down complete");
    }
}
