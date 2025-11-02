package paxos;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

class Proposer {

    private static Integer TIMEOUT = 250;
    private Integer majority;

    private GCLReader reader;
    private GCLWriter writer;
    private Logger logger;

    Proposer(
        Integer majority,
        GCLReader reader,
        GCLWriter writer,
        Logger logger
    ) {
        this.majority = majority;
        this.writer = writer;
        this.reader = reader;
        this.logger = logger;
    }

    ProposeResult sendProposes(Long ballotId) {
        List<Promise> promises = new ArrayList<>();
        List<Refuse> refuses = new ArrayList<>();

        Propose propMsg = new Propose(ballotId);
        writer.broadcast(propMsg);

        Long startTime = System.currentTimeMillis();

        while (true) {
            Long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed > TIMEOUT) {
                logger.warning("Timeout reached while waiting for promises.");
                break;
            }

            if (promises.size() > majority) {
                logger.info("Propose round has reached majority.");
                break;
            }

            try {
                PaxosEnvelope<AcceptorMessage> env = reader.pollProposerQ();

                if (env != null) {
                    String sender = env.sender();
                    AcceptorMessage msg = (AcceptorMessage) env.message();

                    switch (msg) {
                        case Promise prom -> {
                            Long promBID = prom.ballotId();

                            if (promBID.equals(ballotId)) {
                                promises.add(prom);

                                logger.info(
                                    "Promise received from +" +
                                        sender +
                                        ", now at: " +
                                        promises.size() +
                                        " promises."
                                );
                            }
                        }
                        case Refuse ref -> {
                            Long refBID = ref.ballotId();

                            logger.info(
                                "Refuse received from " +
                                    sender +
                                    " with higher ballot ID " +
                                    refBID
                            );

                            refuses.add(ref);
                        }
                        default -> {
                            logger.warning(
                                "Unknow message type " +
                                    msg.getClass().getName() +
                                    " received at proposer"
                            );
                        }
                    }
                }
            } catch (InterruptedException e) {
                logger.warning("Interrupted while awaiting promises.");
                return new ProposeFailure(ballotId);
            }
        }

        if (promises.size() >= majority) {
            Long maxPromWithMoveBID = -1L;
            GameMove lastAccepted = null;

            for (Promise p : promises) {
                if (
                    p.lastAcceptedBID() != null &&
                    Long.compare(maxPromWithMoveBID, p.ballotId()) < 0
                ) {
                    maxPromWithMoveBID = p.ballotId();
                    lastAccepted = p.lastAcceptedMove();
                }
            }

            if (lastAccepted != null) {
                return new ProposeSuccessWithMove(lastAccepted);
            } else {
                return new ProposeSuccess();
            }
        } else {
            Long maxRefBID = ballotId;

            for (Refuse r : refuses) {
                if (Long.compare(maxRefBID, r.ballotId()) < 0) {
                    maxRefBID = r.ballotId();
                }
            }

            return new ProposeFailure(maxRefBID);
        }
    }

    AcceptResult sendAcceptRequests(Long ballotId, GameMove move) {
        List<AcceptAck> acceptAcks = new ArrayList<>();
        List<Deny> denies = new ArrayList<>();

        AcceptRequest acceptReqMsg = new AcceptRequest(ballotId, move);
        writer.broadcast(acceptReqMsg);

        Long startTime = System.currentTimeMillis();

        while (true) {
            Long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed > TIMEOUT) {
                logger.warning(
                    "Timeout reached while waiting for accept acks."
                );
                break;
            }

            if (acceptAcks.size() >= majority) {
                logger.info("Accept round has reached majority.");
                break;
            }

            try {
                PaxosEnvelope<AcceptorMessage> env = reader.pollProposerQ();

                if (env != null && env.message() instanceof AcceptAck) {
                    String sender = env.sender();
                    AcceptorMessage msg = (AcceptorMessage) env.message();

                    switch (msg) {
                        case AcceptAck ack -> {
                            Long ackBID = ack.ballotId();

                            if (ackBID.equals(ballotId)) {
                                acceptAcks.add(ack);

                                logger.info(
                                    "AcceptAck received from " +
                                        sender +
                                        " , now at: " +
                                        acceptAcks.size() +
                                        " accept acks."
                                );
                            }
                        }
                        case Deny deny -> {
                            Long denyBID = deny.ballotId();

                            if (denyBID.equals(ballotId)) {
                                denies.add(deny);

                                logger.info(
                                    "Received deny from " +
                                        sender +
                                        " with higher ballot ID " +
                                        denyBID
                                );
                            }
                        }
                        default -> {
                            logger.warning(
                                "Unknown message type received at proposer"
                            );
                        }
                    }
                }
            } catch (InterruptedException e) {
                logger.warning("Interrupted while awaiting accept acks.");
                return new AcceptFailure(ballotId);
            }
        }

        if (acceptAcks.size() >= majority) {
            return new AcceptSuccess();
        } else {
            Long maxDenyBID = ballotId;

            for (Deny d : denies) {
                if (Long.compare(maxDenyBID, d.ballotId()) < 0) {
                    maxDenyBID = d.ballotId();
                }
            }

            return new AcceptFailure(maxDenyBID);
        }
    }

    void sendConfirms(Long ballotId) {
        Confirm confirmMsg = new Confirm(ballotId);
        writer.broadcast(confirmMsg);
    }
}
