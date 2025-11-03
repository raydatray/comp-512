package paxos;

public class BallotGenerator {
    protected Integer processID;
    protected Long ballotID;


    protected BallotGenerator(Integer processID) {
        this.processID = processID;
        this.ballotID = 0L;
    }

    protected Ballot nextTurn(Long currTurn) {
        this.ballotID ++;
        Long nextTurn = currTurn ++;
        
        return new Ballot(this.processID, this.ballotID, nextTurn);
    }
}
