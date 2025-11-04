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
    private Integer playerNum;
    private Integer ballotCounter;

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
        this.playerNum = playerNum;
        this.ballotCounter = 0;
    }

    private Ballot getNewBallot() {
        ballotCounter++;
        return new Ballot(ballotCounter, playerNum);
    }

    GameMove runInstance(GameMove moveToCommit) throws InterruptedException {
        GameMove proposedMove = null;

        // Phase 1 - propose self as leader
        Ballot ballot = getNewBallot();
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

                // TODO: set ballot counter to higher ballot + 1

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

                // TODO: set ballot counter to higher ballot

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
            "`Confirming move` " + proposedMove + ", sending confirm messages"
        );
        sendConfirms(ballot);

        return proposedMove;
    }

    ProposeResult sendProposes(Ballot ballot) {
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
            try {
                PaxosEnvelope<AcceptorMessage> env = reader.pollProposerQ();

                if (env != null) {
                    String sender = env.sender();
                    AcceptorMessage msg = (AcceptorMessage) env.message();

                    switch (msg) {
                        case Promise prom -> {
                            promises.add(prom);

                            logger.info(
                                "`Promise received` with ballot " +
                                    prom.ballot() +
                                    " from " +
                                    sender +
                                    ", now at: " +
                                    promises.size() +
                                    " promises."
                            );

                            // TODO: REMOVE
                            if (
                                !prom
                                    .ballot()
                                    .ballotId()
                                    .equals(ballot.ballotId())
                            ) {
                                logger.warning(
                                    "`SOMETHING WENT WRONG`, RECEIVED UNEQUAL BALLOT: " +
                                        prom +
                                        " to ballot " +
                                        ballot
                                );
                            }
                        }
                        case Refuse ref -> {
                            refuses.add(ref);

                            logger.info(
                                "`Refuse received` with ballot " +
                                    ref.ballot() +
                                    " from " +
                                    sender +
                                    ", now at: " +
                                    refuses.size() +
                                    " refuses."
                            );

                            // TODO: REMOVE
                            if (
                                !ref
                                    .ballot()
                                    .ballotId()
                                    .equals(ballot.ballotId())
                            ) {
                                logger.warning(
                                    "`SOMETHING WENT WRONG`, RECEIVED UNEQUAL BALLOT: " +
                                        ref +
                                        " to ballot " +
                                        ballot
                                );
                            }
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
                return new ProposeFailure(ballot);
            }
        }

        if (promises.size() >= majority) {
            // if msg is piggy-backed w previous move, its ballot is necessarily smaller
            // so start at smallest ballot possible
            Ballot highestPromiseWithMoveBallot = new Ballot(-1, -1);
            GameMove lastAccepted = null;

            for (Promise promise : promises) {
                if (
                    promise.previousMove() != null &&
                    promise
                        .previousBallot()
                        .isGreaterThan(highestPromiseWithMoveBallot)
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
            // start at current ballot since refuses will all have higher ones
            Ballot highestRefuseBallot = ballot;

            for (Refuse refuse : refuses) {
                if (refuse.ballot().isGreaterThan(highestRefuseBallot)) {
                    highestRefuseBallot = refuse.ballot();
                }
            }

            return new ProposeFailure(highestRefuseBallot);
        }
    }

    AcceptResult sendAcceptRequests(Ballot ballot, GameMove move)
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
            PaxosEnvelope<AcceptorMessage> env = reader.pollProposerQ();

            if (env != null) {
                String sender = env.sender();
                AcceptorMessage msg = (AcceptorMessage) env.message();

                switch (msg) {
                    case AcceptAck ack -> {
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

                        // TODO: REMOVE
                        if (
                            !ack.ballot().ballotId().equals(ballot.ballotId())
                        ) {
                            logger.warning(
                                "`SOMETHING WENT WRONG`, RECEIVED UNEQUAL BALLOT " +
                                    ack +
                                    " to ballot " +
                                    ballot
                            );
                        }
                    }
                    case Deny deny -> {
                        denies.add(deny);

                        logger.info(
                            "`Deny received` with ballot " +
                                deny.ballot() +
                                " from " +
                                sender +
                                ", now at: " +
                                denies.size() +
                                " denies."
                        );

                        // TODO: REMOVE
                        if (
                            !deny.ballot().ballotId().equals(ballot.ballotId())
                        ) {
                            logger.warning(
                                "`SOMETHING WENT WRONG`, RECEIVED UNEQUAL BALLOT " +
                                    deny +
                                    " to ballot " +
                                    ballot
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
        }

        if (acceptAcks.size() >= majority) {
            return new AcceptSuccess();
        } else {
            // start at current ballot since denies imply higher ballots
            Ballot highestDenyBallot = ballot;

            for (Deny deny : denies) {
                if (deny.ballot().isGreaterThan(highestDenyBallot)) {
                    highestDenyBallot = deny.ballot();
                }
            }

            return new AcceptFailure(highestDenyBallot);
        }
    }

    void sendConfirms(Ballot ballot) {
        Confirm confirm = new Confirm(ballot);
        writer.broadcast(confirm);
    }
}
