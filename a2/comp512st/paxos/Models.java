package paxos;

import java.io.Serializable;

record GameMove(Integer playerNum, Character move) implements Serializable {}

record Ballot(Integer ballotId, Integer playerNum) implements
    Serializable, Comparable<Ballot> {
    public int compareTo(Ballot other) {
        // tie break only by ballotId for now
        int cmp = this.ballotId.compareTo(other.ballotId);
        if (cmp != 0) return cmp;

        // then by player number
        return this.playerNum.compareTo(other.playerNum);
    }

    Boolean isLessThan(Ballot other) {
        return this.compareTo(other) < 0;
    }

    Boolean isGreaterThan(Ballot other) {
        return this.compareTo(other) > 0;
    }
}

// Phase 1

interface ProposeResult {}

record ProposeSuccess() implements ProposeResult {}

record ProposeSuccessWithMove(GameMove move) implements ProposeResult {}

// someone got more motion than u, use smth greater than their ballot number next time lil bro
record ProposeFailure(Ballot ballot) implements ProposeResult {}

// Phase 2

interface AcceptResult {}

record AcceptSuccess() implements AcceptResult {}

record AcceptFailure(Ballot ballot) implements AcceptResult {}
