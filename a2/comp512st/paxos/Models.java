package paxos;

import java.io.Serializable;

record GameMove(Integer playerNum, char move) implements
    Serializable, Comparable<GameMove> {
    public int compareTo(GameMove other) {
        int cmp = this.playerNum.compareTo(other.playerNum);
        if (cmp != 0) return cmp;
        return Character.compare(this.move, other.move);
    }
}

// Phase 1

interface ProposeResult {}

record ProposeSuccess() implements ProposeResult {}

record ProposeSuccessWithMove(GameMove move) implements ProposeResult {}

// someone got more motion than u, use smth greater than their ballot number next time lil bro
record ProposeFailure(Long greaterBID) implements ProposeResult {}

// Phase 2

interface AcceptResult {}

record AcceptSuccess() implements AcceptResult {}

record AcceptFailure(Long greaterBID) implements AcceptResult {}
