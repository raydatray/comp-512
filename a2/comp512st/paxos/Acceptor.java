package paxos;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class Acceptor implements Runnable { 
    private GCLReader gclReader;
    private GCLWriter gclWriter;
    private Logger logger;

    private Long lastConfirmedMove;
    private Long lastDeliveredMove;

    private Map<Long, AcceptorTurnState> turnsMap;

    private volatile Boolean running;
    private Thread gclAcceptorInQPoller;

    private

    Acceptor(GCLReader reader, GCLWriter writer, Logger logger) {
        this.gclReader = reader;
        this.gclWriter = writer;
        this.logger = logger;
        
        this.lastDeliveredMove = -1L;
        this.lastConfirmedMove = -1L;

        this.turnsMap = new ConcurrentHashMap<>();

        this.running = true;
        this.gclAcceptorInQPoller = new Thread();

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

                    }
                    case AcceptRequest a -> {

                    }
                    case Confirm c -> {

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



    private void handlePropose(String sender, Propose msg) {
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
            gclWriter.send(sender, new Refuse(state.getHighestPromised()));
            return;
        }

        // if the current ballot is higher - update our promise
        state.updatePromise(currentBallot);

        // send response based on whether we've previously accepted a value
        if (state.hasAcceptedValue()) {
            // we know these calls are safe
            Ballot previousBallot = state.getAcceptedBallot().get(); 
            GameMove previousMove = state.getAcceptedValue().get();


            gclWriter.send(sender, new PromiseWithPreviousAcceptedValue(
                currentBallot, 
                previousBallot,
                previousMove
            ));
        } else {
            gclWriter.send(sender, new Promise(currentBallot));
        }
    }





}