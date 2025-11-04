package paxos;

import java.io.Serializable;

record Ballot(Integer pID, Long bID, Long turn) implements
    Serializable, Comparable<Ballot> {
    @Override
    public int compareTo(Ballot other) {
        // start by ballot id
        int bCompare = this.bID.compareTo(other.bID);
        if (bCompare != 0) return bCompare;

        // tie break by process id
        return this.pID.compareTo(other.pID);
    }

    public boolean isLessThan(Ballot other) {
        return this.compareTo(other) < 0;
    }

    public boolean isGreaterThan(Ballot other) {
        return this.compareTo(other) > 0;
    }

    public boolean equalTo(Ballot other) {
        return this.compareTo(other) == 0;
    }
}
