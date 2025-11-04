package paxos;

import java.io.Serializable;

// game ID to make handling confirms idempotent
record Identifier(Integer playerNum, Integer turn) implements Serializable {}

record GameMove(Identifier id, Integer playerNum, Character move) implements
    Serializable {}

// Phase 1

interface ProposeResult {}

record ProposeSuccess(GameMove move) implements ProposeResult {}

// someone got more motion than u, use smth greater than their ballot number next time lil bro
record ProposeFailure(Long ballot) implements ProposeResult {}

// Phase 2

interface AcceptResult {}

record AcceptSuccess() implements AcceptResult {}

record AcceptFailure(Long ballot) implements AcceptResult {}
