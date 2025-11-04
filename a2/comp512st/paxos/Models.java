package paxos;

import java.io.Serializable;

record GameMove(Integer playerNum, Character move) implements Serializable {}

// Phase 1

interface ProposeResult {}

record ProposeSuccess() implements ProposeResult {}

record ProposeSuccessWithMove(GameMove move) implements ProposeResult {}

// someone got more motion than u, use smth greater than their ballot number next time lil bro
record ProposeFailure(Long ballotId) implements ProposeResult {}

// Phase 2

interface AcceptResult {}

record AcceptSuccess() implements AcceptResult {}

record AcceptFailure(Long ballotId) implements AcceptResult {}
