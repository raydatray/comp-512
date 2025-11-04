package paxos;

import comp512.gcl.GCL;
import comp512.gcl.GCMessage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

class GCLReader implements Runnable {

    private GCL gcl;
    private Logger logger;

    private BlockingQueue<PaxosEnvelope<AcceptorMessage>> proposerInQ;
    private BlockingQueue<PaxosEnvelope<ProposerMessage>> acceptorInQ;

    private volatile Boolean running;
    private Thread ingester;

    GCLReader(GCL gcl, Logger logger) {
        this.gcl = gcl;
        this.logger = logger;

        proposerInQ = new LinkedBlockingQueue<>();
        acceptorInQ = new LinkedBlockingQueue<>();

        running = true;
        ingester = new Thread(this);

        ingester.start();
    }

    public void run() {
        while (running) {
            try {
                GCMessage msg = gcl.readGCMessage();
                String sender = msg.senderProcess;
                Object val = msg.val;

                logger.fine("Received message " + val + " from " + sender);

                switch (val) {
                    case ProposerMessage proposerMsg -> {
                        logger.fine(
                            "Appending " + proposerMsg + " to acceptor inQ"
                        );

                        acceptorInQ.put(
                            new PaxosEnvelope<ProposerMessage>(
                                sender,
                                (ProposerMessage) val
                            )
                        );
                    }
                    case AcceptorMessage acceptorMsg -> {
                        logger.fine(
                            "Appending " + acceptorMsg + " to proposer inQ"
                        );

                        proposerInQ.put(
                            new PaxosEnvelope<AcceptorMessage>(
                                sender,
                                (AcceptorMessage) val
                            )
                        );
                    }
                    default -> {
                        logger.warning(
                            "Unknown message format from " + sender + ": " + msg
                        );
                    }
                }
            } catch (InterruptedException e) {
                logger.info("GCLReader thread interrupted, shutting down.");
            }
        }
    }

    PaxosEnvelope<AcceptorMessage> pollProposerQ() throws InterruptedException {
        logger.fine("Polling proposer inQ.");
        return proposerInQ.poll(25, TimeUnit.MILLISECONDS);
    }

    PaxosEnvelope<ProposerMessage> consumeAcceptorQ()
        throws InterruptedException {
        logger.fine("Consuming from acceptor inQ.");
        return acceptorInQ.take();
    }

    void shutdown() throws InterruptedException {
        running = false;
        ingester.join(1000); // permit grace period
        ingester.interrupt();
        gcl.shutdownGCL();
    }
}
