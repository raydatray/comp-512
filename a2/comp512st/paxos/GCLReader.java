package paxos;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import comp512.gcl.GCL;
import comp512.gcl.GCMessage;

public class GCLReader implements Runnable {
    private GCL gcl;
    private Logger logger;

    // queue of acceptor -> proposer 
    private BlockingQueue<PaxosEnvelope<AcceptorMessage>> proposerInQ;

    private BlockingQueue<PaxosEnvelope<ProposerMessage>> acceptorInQ;

    private volatile Boolean running;
    private Thread gclPoller;

    GCLReader(GCL gcl, Logger logger) {
        this.gcl = gcl;
        this.logger = logger;

        this.proposerInQ = new LinkedBlockingQueue<>();
        this.acceptorInQ = new LinkedBlockingQueue<>();

        this.running = true;
        this.gclPoller = new Thread(this);

        this.gclPoller.start();
    }

    public void run() {
        while (running) {
            try {
                GCMessage msg = gcl.readGCMessage();
                String sender = msg.senderProcess;
                Object val = msg.val;

                logger.fine("received message " + val + " from " + sender);

                if (
                    val instanceof Propose ||
                    val instanceof AcceptRequest ||
                    val instanceof Confirm
                ) {
                    logger.fine(
                        "appending message " + val + " to acceptor inQ"
                    );

                    acceptorInQ.put(
                        new PaxosEnvelope<ProposerMessage>(
                            sender,
                            (ProposerMessage) val
                        )
                    );
                } else if (
                    val instanceof Promise ||
                    val instanceof Refuse ||
                    val instanceof AcceptAck ||
                    val instanceof Deny
                ) {
                    logger.fine(
                        "appending message " + val + " to proposer inQ"
                    );

                    proposerInQ.put(
                        new PaxosEnvelope<AcceptorMessage>(
                            sender,
                            (AcceptorMessage) val
                        )
                    );
                } else {
                    logger.warning(
                        "unknown message format from " + sender + ": " + msg
                    );
                }
            } catch (InterruptedException e) {
                logger.severe("thread interrupted: " + e.getStackTrace());
                System.exit(1);
            }
        }
    }

    PaxosEnvelope<AcceptorMessage> pollProposerQ() throws InterruptedException {
        logger.fine("polling proposer inQ.");
        return proposerInQ.poll(100, TimeUnit.MILLISECONDS);
    }

    PaxosEnvelope<ProposerMessage> consumeAcceptorQ()
        throws InterruptedException {
        logger.fine("Consuming from acceptor inQ.");
        return acceptorInQ.take();
    }

    void shutdown() throws InterruptedException {
        running = false;

        //todo: do we need to ensure all messages cleared?
        gclPoller.join(1000); // permit grace period
        gclPoller.interrupt();
        gcl.shutdownGCL();
    }
    
}
