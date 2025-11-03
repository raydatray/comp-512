package paxos;

import java.io.Serializable;

record GameMove(Integer playerNum, char move) implements Serializable {}

record Ballot(Integer processID, Long ballotID, Long turn) implements Serializable, Comparable<Ballot> {
    
    @Override
    public int compareTo(Ballot other) {
        // we don't care about turn. this should be handled by external logic
        
        // start by ballot id
        int ballotID = this.ballotID.compareTo(other.ballotID);
        if (ballotID != 0) return ballotID;

        // tie break by process id
        return this.processID.compareTo(other.processID);
    }

    public boolean isLessThan(Ballot other) {
        return this.compareTo(other) < 0;
    }
    
    public boolean isGreaterThan(Ballot other) {
        return this.compareTo(other) > 0;
    }
}

// Phase 1

interface ProposeResult {}

record ProposeSuccess() implements ProposeResult {}

record ProposeSuccessWithMove(GameMove move) implements ProposeResult {}

// someone got more motion than u, use smth greater than their ballot number next time lil bro
record ProposeFailure(Long greaterBallotID) implements ProposeResult {}

// Phase 2

interface AcceptResult {}

record AcceptSuccess() implements AcceptResult {}

record AcceptFailure(Long greaterBallotID) implements AcceptResult {}
