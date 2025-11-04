package paxos;

public class BallotGenerator {

    protected Integer processID;
    protected Long ballotID;
    protected Long currTurn;

    protected BallotGenerator(Integer processID) {
        this.processID = processID;
        this.ballotID = 0L;
        this.currTurn = 0L;
    }

    protected Ballot nextTurn() {
        this.ballotID++;
        this.currTurn++;

        return new Ballot(this.processID, this.ballotID, this.currTurn);
    }

    protected Ballot higherBallot(Ballot previousBallot) {
        Long tempBallot = this.ballotID + 1;
        this.ballotID = Math.max(tempBallot, previousBallot.ballotID() + 1);

        return new Ballot(this.processID, this.ballotID, previousBallot.turn());
    }
}
