package paxos;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class Acceptor implements Runnable { 
    private GCLReader gclReader;
    private GCLWriter gclWriter;
    private Logger logger;

    private Long lastConfirmedMove;
    private Long lastDeliveredMove;

    private Map<Long, AcceptorTurnState> turnsMap;
    private BlockingQueue<GameMove> moveQ;

    private volatile Boolean running;
    private Thread gclAcceptorInQPoller;

    Acceptor(GCLReader reader, GCLWriter writer, Logger logger) {
        this.gclReader = reader;
        this.gclWriter = writer;
        this.logger = logger;
        
        this.lastDeliveredMove = -1L;
        this.lastConfirmedMove = -1L;

        this.turnsMap = new ConcurrentHashMap<>();
        this.moveQ = new LinkedBlockingQueue<>();

        this.running = true;
        this.gclAcceptorInQPoller = new Thread(this);

        this.gclAcceptorInQPoller.start();
    }

    public void run() {
        while (running) {
            try {
                PaxosEnvelope<ProposerMessage> envelope = this.gclReader.consumeAcceptorQ();
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
                        logger.warning("unknown message" + msg.getClass().getName());
                    }
                }
            } catch (InterruptedException e) {
                logger.severe("acceptor message ingester thread interrupted:" + e.getStackTrace());
                System.exit(1);
            }
        }
    }



    private synchronized void handlePropose(String sender, Propose msg) {
        Ballot currentBallot = msg.ballot();
        Long turn = currentBallot.turn();
        
        AcceptorTurnState state = this.turnsMap.get(turn);

        // we have not yet seen a ballot for this turn
        // write it to map
        // and accept it
        if (state == null) {
            state = new AcceptorTurnState(currentBallot);

            this.turnsMap.put(turn, state);
            this.gclWriter.send(sender, new Promise(currentBallot));

            return;
        }

        // check if current ballot is lower than what we've promised
        if (state.getHighestPromised().isGreaterThan(currentBallot)) {
            this.gclWriter.send(sender, new Refuse(state.getHighestPromised()));
            return;
        }

        // if the current ballot is higher - update our promise
        state.updatePromise(currentBallot);

        // send response based on whether we've previously accepted a value
        if (state.hasAcceptedValue()) {
            // we know these calls are safe
            Ballot previousBallot = state.getAcceptedBallot().get(); 
            GameMove previousMove = state.getAcceptedValue().get();


            this.gclWriter.send(sender, new PromiseWithPreviousAcceptedValue(
                currentBallot, 
                previousBallot,
                previousMove
            ));
        } else {
            this.gclWriter.send(sender, new Promise(currentBallot));
        }
    }

    private synchronized void handleAcceptRequest(String sender, AcceptRequest msg) {
        Ballot currentBallot = msg.ballot();
        Long turn = currentBallot.turn();

        AcceptorTurnState state = this.turnsMap.get(turn);

        // since gcl is guaranteed FIFO from the process
        // we do not have to worry about getting an accept?
        // before a propose

        // state should NEVER be null in this case

        // if our current is lower than what we've promised 
        // we say no
        if (state.getHighestPromised().isGreaterThan(currentBallot)) {
            this.gclWriter.send(sender, new Deny(state.getHighestPromised()));
            return;
        }

        // if its not, accept the value

        state.accept(currentBallot, msg.move());
        this.gclWriter.send(sender, new AcceptAck(currentBallot));
    }

    private synchronized void handleConfirm(String sender, Confirm msg) {
        Ballot currentBallot = msg.ballot();
        Long turn = currentBallot.turn();

        AcceptorTurnState state = this.turnsMap.get(turn);


        // since gcl is guaranteed FIFO from the process
        // we do not have to worry about getting a confirm
        // before a propose or accept?

        // state should NEVER be null in this case

        // if our current is lower than what we've promised 
        // we say no
        if (state.getHighestPromised().isGreaterThan(currentBallot)) {
            this.gclWriter.send(sender, new Deny(currentBallot));
            return;
        }

        // if its not
        // place it into the moveQ so that it can be consumed
        // update the lastConfirmed flag
        // todo: drop it from the map ?

        this.moveQ.offer(state.getAcceptedValue().get());
        this.lastConfirmedMove = state.getAcceptedBallot().get().turn();
    }
}