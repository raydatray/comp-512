package paxos;

import java.io.Serializable;

record GameMove(Integer playerNum, char move) implements Serializable {}

record Ballot(Integer processID, Long ballotID, Long turn) implements Serializable{}

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
