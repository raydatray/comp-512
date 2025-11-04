package paxos;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

class Proposer {

    private static Integer TIMEOUT = 500;

    private GCLReader reader;
    private GCLWriter writer;
    private Logger logger;

    private Integer majority;
    private Long ballotCounter;

    Proposer(
        Integer playerNum,
        Integer majority,
        GCLReader reader,
        GCLWriter writer,
        Logger logger
    ) {
        this.writer = writer;
        this.reader = reader;
        this.logger = logger;

        this.majority = majority;
        this.ballotCounter = playerNum.longValue();
    }

    private Long getNewBallot() {
        ballotCounter++;
        return ballotCounter;
    }

    GameMove runInstance(GameMove moveToCommit, Long backoffDelay)
        throws InterruptedException {
        GameMove proposedMove = null;

        // Phase 1 - propose self as leader
        Long ballot = getNewBallot();
        logger.info(
            "`Initiating new round`, proposing self as leader with ballot " +
                ballot
        );

        ProposeResult propRes = sendProposes(ballot);
        switch (propRes) {
            case ProposeSuccess s -> {
                proposedMove = moveToCommit;

                logger.info(
                    "`Won propose phase` with winning ballot " +
                        ballot +
                        ", will try to commit `my own move` " +
                        moveToCommit
                );
            }
            case ProposeSuccessWithMove s -> {
                proposedMove = s.move(); // use previous move

                logger.info(
                    "`Won propose phase` with winning ballot " +
                        ballot +
                        ", got `previous move` " +
                        s.move()
                );
            }
            case ProposeFailure f -> {
                logger.info(
                    "`Lost propose phase` with ballot " +
                        ballot +
                        " to higher ballot " +
                        f.ballot()
                );

                // we have lost...
                // set our ballot counter to highest ballot received
                // from potential refuses (chances of receiving our
                // own ballot back if there were no refuses)
                // back off for a bit
                ballotCounter = f.ballot();
                Thread.sleep(backoffDelay);

                return null;
            }
            default -> {
                // nuke the system
                logger.severe("[PROPOSE] Shake yo booty");
                System.exit(1);
            }
        }

        // Phase 2 - accept? reqs
        logger.info(
            "Sending accept? messages with ballot " +
                ballot +
                " and move " +
                proposedMove
        );

        AcceptResult accRes = sendAcceptRequests(ballot, proposedMove);
        switch (accRes) {
            case AcceptSuccess s -> {
                logger.info(
                    "`Won accept? phase` with winning ballot " + ballot
                );
            }
            case AcceptFailure f -> {
                logger.info(
                    "`Lost accept? phase` with ballot " +
                        ballot +
                        "to higher ballot " +
                        f.ballot()
                );

                // we have lost...
                // set our ballot counter to highest ballot received
                // from potential denies (chances of receiving our
                // own ballot back if there were no denies)
                // back off for a bit
                ballotCounter = f.ballot();
                Thread.sleep(backoffDelay);

                return null;
            }
            default -> {
                // nuke the system
                logger.severe("Shake yo booty");
                System.exit(1);
            }
        }

        // Phase 3 - move committed
        logger.info(
            "`Confirming move` " +
                proposedMove +
                " with ballot " +
                ballot +
                ", sending confirm messages"
        );
        sendConfirms(ballot, proposedMove);

        // TODO: find a better way to cool down winner to avoid having him starve other players
        Thread.sleep(150);
        return proposedMove;
    }

    ProposeResult sendProposes(Long ballot) throws InterruptedException {
        List<Promise> promises = new ArrayList<>();
        List<Refuse> refuses = new ArrayList<>();

        Propose propMsg = new Propose(ballot);
        writer.broadcast(propMsg);

        Long startTime = System.currentTimeMillis();
        while (
            promises.size() < majority &&
            refuses.size() < majority &&
            System.currentTimeMillis() - startTime < TIMEOUT
        ) {
            PaxosEnvelope<AcceptorMessage> envelope = reader.pollProposerQ();
            if (envelope == null) continue;

            String sender = envelope.sender();
            switch (envelope.message()) {
                case Promise prom -> {
                    if (!prom.ballot().equals(ballot)) {
                        logger.warning(
                            "`SOMETHING WENT WRONG:` got stale promise " +
                                prom +
                                " from " +
                                sender
                        );
                        continue;
                    }

                    promises.add(prom);

                    logger.info(
                        "`Promise received` " +
                            prom +
                            " from " +
                            sender +
                            ", now at: " +
                            promises.size() +
                            " promises."
                    );
                }
                case Refuse ref -> {
                    // check if the refuse response refused our specific ballot
                    if (!ref.refusedBallot().equals(ballot)) {
                        logger.warning(
                            "`SOMETHING WENT WRONG:` got stale refuse " +
                                ref +
                                " from " +
                                sender
                        );
                        continue;
                    }

                    refuses.add(ref);

                    logger.info(
                        "`Refuse received` with higher ballot " +
                            ref.ballot() +
                            " from " +
                            sender
                    );
                }
                default -> {
                    logger.warning(
                        "Unknow message " +
                            envelope.message() +
                            " received at proposer"
                    );
                }
            }
        }

        if (promises.size() >= majority) {
            // if msg is piggy-backed w previous move, its ballot is necessarily smaller
            // so start at smallest ballot possible
            Long highestPromiseWithMoveBallot = -1L;
            GameMove lastAccepted = null;

            for (Promise promise : promises) {
                if (
                    promise.previousMove() != null &&
                    promise.previousBallot() > highestPromiseWithMoveBallot
                ) {
                    highestPromiseWithMoveBallot = promise.ballot();
                    lastAccepted = promise.previousMove();
                }
            }

            if (lastAccepted != null) {
                return new ProposeSuccessWithMove(lastAccepted);
            } else {
                return new ProposeSuccess();
            }
        } else {
            // refuse implies higher ballot, so start at our own
            // also just in case there are no refuses
            Long highestRefuseBallot = ballot;

            for (Refuse refuse : refuses) {
                if (refuse.ballot() > highestRefuseBallot) {
                    highestRefuseBallot = refuse.ballot();
                }
            }

            return new ProposeFailure(highestRefuseBallot);
        }
    }

    AcceptResult sendAcceptRequests(Long ballot, GameMove move)
        throws InterruptedException {
        List<AcceptAck> acceptAcks = new ArrayList<>();
        List<Deny> denies = new ArrayList<>();

        AcceptRequest acceptReqMsg = new AcceptRequest(ballot, move);
        writer.broadcast(acceptReqMsg);

        Long startTime = System.currentTimeMillis();
        while (
            acceptAcks.size() < majority &&
            denies.size() < majority &&
            System.currentTimeMillis() - startTime < TIMEOUT
        ) {
            PaxosEnvelope<AcceptorMessage> envelope = reader.pollProposerQ();
            if (envelope == null) continue;

            String sender = envelope.sender();

            switch (envelope.message()) {
                case AcceptAck ack -> {
                    if (!ack.ballot().equals(ballot)) {
                        logger.warning(
                            "`SOMETHING WENT WRONG:` got stale acceptAck " +
                                ack +
                                " from " +
                                sender
                        );
                        continue;
                    }

                    acceptAcks.add(ack);

                    logger.info(
                        "`AcceptAck received` with ballot " +
                            ack.ballot() +
                            " from " +
                            sender +
                            ", now at: " +
                            acceptAcks.size() +
                            " AcceptAcks."
                    );
                }
                case Deny deny -> {
                    // check if deny response denied our specific ballot
                    if (!deny.deniedBallot().equals(ballot)) {
                        logger.warning(
                            "`SOMETHING WENT WRONG:` got stale deny " +
                                deny +
                                " from " +
                                sender
                        );
                        continue;
                    }

                    denies.add(deny);

                    logger.info(
                        "`Deny received` with higher ballot " +
                            deny.ballot() +
                            " from " +
                            sender
                    );
                }
                default -> {
                    logger.warning("Unknown message type received at proposer");
                }
            }
        }

        if (acceptAcks.size() >= majority) {
            return new AcceptSuccess();
        } else {
            // start at our own ballot since deny implies higher ballot
            // also just in case there are no denies
            Long highestDenyBallot = ballot;

            for (Deny deny : denies) {
                if (deny.ballot() > highestDenyBallot) {
                    highestDenyBallot = deny.ballot();
                }
            }

            return new AcceptFailure(highestDenyBallot);
        }
    }

    void sendConfirms(Long ballot, GameMove move) {
        Confirm confirm = new Confirm(ballot, move);
        writer.broadcast(confirm);
    }

    void shutdown(Integer playerNum) {
        Shutdown shutdown = new Shutdown(playerNum);
        writer.broadcast(shutdown);
    }
}
