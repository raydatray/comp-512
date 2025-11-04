package paxos;

import comp512.utils.FailCheck;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

class Proposer {

    private static Integer TIMEOUT = 500;

    private GCLReader reader;
    private GCLWriter writer;
    private Logger logger;

    private Integer majority;
    private Long ballotCounter;

    private FailCheck failCheck;

    Proposer(
        Integer playerNum,
        Integer majority,
        GCLReader reader,
        GCLWriter writer,
        Logger logger,
        FailCheck failCheck
    ) {
        this.writer = writer;
        this.reader = reader;
        this.logger = logger;

        this.majority = majority;
        // start each proposer's ballot at playerNum (unique across processes)
        // this prevents the initial rush if everyone starts at the same value
        this.ballotCounter = playerNum.longValue();

        this.failCheck = failCheck;
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
                if (s.move() != null) {
                    proposedMove = s.move(); // use previous move

                    logger.info(
                        "`Won propose phase` with winning ballot " +
                            ballot +
                            ", got `previous move` " +
                            s.move()
                    );
                } else {
                    proposedMove = moveToCommit;

                    logger.info(
                        "`Won propose phase` with winning ballot " +
                            ballot +
                            ", will try to commit `my own move` " +
                            moveToCommit
                    );
                }

                failCheck.checkFailure(
                    FailCheck.FailureType.AFTERBECOMINGLEADER
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
                failCheck.checkFailure(FailCheck.FailureType.AFTERVALUEACCEPT);
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
        failCheck.checkFailure(FailCheck.FailureType.AFTERSENDPROPOSE);

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
                    // we need to drop stale promises (artifacts from previous rounds...)
                    if (!prom.ballot().equals(ballot)) {
                        logger.fine("`Dropping stale promise:` " + prom);
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
                    // we need to drop stale refuses (artifacts from previous rounds...)
                    if (!ref.refusedBallot().equals(ballot)) {
                        logger.fine("`Dropping stale refuse:` " + ref);
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
            // try to look for a promise containing a previous move
            // containing the highest previousBallot value
            Optional<Promise> highestPromiseWithMove = promises
                .stream()
                .filter(p -> p.previousMove() != null)
                .max(Comparator.comparing(Promise::previousBallot));

            if (highestPromiseWithMove.isPresent()) {
                return new ProposeSuccess(
                    highestPromiseWithMove.get().previousMove()
                );
            }

            return new ProposeSuccess(null);
        } else {
            // return highest ballot we got refused by in the refuses we
            // received
            // if no refuses, then just return our own ballot
            Long highestRefuseBallot = refuses
                .stream()
                .map(Refuse::ballot)
                .max(Comparator.naturalOrder())
                .orElse(ballot);
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
            if (envelope == null) continue; // keep polling until we get something...

            String sender = envelope.sender();

            switch (envelope.message()) {
                case AcceptAck ack -> {
                    // we need to drop stale acks (artifacts from previous rounds...)
                    if (!ack.ballot().equals(ballot)) {
                        logger.fine("`Dropping stale accept ack:` " + ack);
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
                    // we need to drop stale denies (artifacts from previous rounds...)
                    if (!deny.deniedBallot().equals(ballot)) {
                        logger.fine("`Dropping stale deny:` " + deny);
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
            // return highest ballot we got denied by in the denies we
            // received
            // if no denies, then just return our own ballot
            Long highestDenyBallot = denies
                .stream()
                .map(Deny::ballot)
                .max(Comparator.naturalOrder())
                .orElse(ballot);
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
