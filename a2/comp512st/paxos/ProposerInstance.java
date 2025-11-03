package paxos;

import java.util.logging.Logger;

public class ProposerInstance {
    private GCLReader gclReader;
    private GCLWriter gclWriter;
    private Logger logger;

    private Integer majority;

    private ProposerState state;

    ProposerInstance(
        GCLReader reader,
        GCLWriter writer,
        Logger logger,
        Integer majority,
        Ballot ballotToPropose,
        GameMove moveToPropose
    ) {
        this.gclReader = reader;
        this.gclWriter = writer;
        this.logger = logger;

        this.majority = majority;

        this.state = new ProposerState(ballotToPropose, moveToPropose);
    }

    public Boolean runInstance() {
        Boolean majorityPromised = this.sendProposes();
        if (!majorityPromised) {
            // probably do something here
             return false;
        }
            


        Boolean majorityAccepted = this.sendAccepts();
        if (!majorityAccepted) {
            // probably do something here
            return false;
        }

        this.sendConfirms();
        return true;
    }

    public Boolean sendProposes() {
        state.transitionToPropose();
        Integer promises = 0;

        Propose propose = new Propose(this.state.ballotToPropose);
        this.gclWriter.broadcast(propose);

        while (true) {
            try {
                PaxosEnvelope<AcceptorMessage> envelope = this.gclReader.pollProposerQ();
                AcceptorMessage msg = envelope.message();


                switch (msg) {
                    case Promise p -> {
                        promises ++;
                    }
                    case PromiseWithPreviousAcceptedValue p -> {
                        promises ++;
                        state.propagatePreviousMove(p.previousMove());
                    }
                    case Refuse r -> {
                        // if we get a refuse we should immediately back off
                        // since the GCL is FIFO, if someone is already telling us 
                        // they have a higher ballotID than us, we are never going to win
                        // so continuing is pointless

                        state.transitionToAwaitTimeout(r.ballot());
                        return false;
                    }
                    default -> {
                        logger.warning("unknown message" + msg.getClass().getName());
                    }
                }

                if (majorityReached(promises)) {
                    return true;
                }
            } catch (InterruptedException e) {
                logger.severe("yo momma:" + e.getStackTrace());
                System.exit(1);
            }
        }
    }


    public Boolean sendAccepts() {
        state.transitionToAccept();
        Integer acceptAcks = 0;
        GameMove moveToPropose;

        if (this.state.moveState == MoveState.PROPAGATE) {
            moveToPropose = this.state.previousMove.get();
        } else {
            moveToPropose = this.state.move;
        }

        AcceptRequest acceptRequest = new AcceptRequest(this.state.ballotToPropose, moveToPropose);
        this.gclWriter.broadcast(acceptRequest);

        while (true) {
            try {
                PaxosEnvelope<AcceptorMessage> envelope = this.gclReader.pollProposerQ();
                AcceptorMessage msg = envelope.message();
                
                switch (msg) {
                    case AcceptAck a  -> {
                        acceptAcks ++;
                    }
                    case Deny d -> {
                        // if we get a deny, we should immediately back off 
                        // since the GCL is FIFO, if someone is already tellins us
                        // they have a higher ballotID than us, we are never going to win
                        // so continuing is pointless

                        this.state.transitionToAwaitTimeout(d.ballot());
                        return false;
                    }
                    default -> {
                        logger.warning("unknown message" + msg.getClass().getName());
                    }
                }

                if (majorityReached(acceptAcks)) {
                    return true;
                }
            } catch (InterruptedException e) {
                logger.severe("yo momma:" + e.getStackTrace());
                System.exit(1);
            }
        }
    }

    protected void sendConfirms() {
        state.transitionToConfirm();
        GameMove moveToConfirm;

        if (this.state.moveState == MoveState.PROPAGATE) {
            moveToConfirm = this.state.previousMove.get();
        } else {
            moveToConfirm = this.state.move;
        }

        Confirm confirm = new Confirm(this.state.ballotToPropose, moveToConfirm);

        this.gclWriter.broadcast(confirm);
    }

    protected ProposerState getFinalProposerState() {
        return this.state;
    }

    protected Boolean committedOriginalMove() {
        return this.state.phase == ProposerPhase.CONFIRM && this.state.moveState == MoveState.SELF;
    }
    
    private Boolean majorityReached(Integer received) {
        return received >= this.majority;
    }
}