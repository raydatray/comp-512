package paxos;

import java.io.Serializable;

interface PaxosMessage extends Serializable {}

interface ProposerMessage extends PaxosMessage {}

interface AcceptorMessage extends PaxosMessage {}

record PaxosEnvelope<T extends PaxosMessage>(
    String sender,
    T message
) implements Serializable {}

// PHASE I

record Propose(Long ballot) implements ProposerMessage {}

record Promise(
    Long ballot,
    Long previousBallot,
    GameMove previousMove
) implements AcceptorMessage {}

record Refuse(Long ballot, Long refusedBallot) implements AcceptorMessage {}

// PHASE II

record AcceptRequest(Long ballot, GameMove move) implements ProposerMessage {}

record AcceptAck(Long ballot) implements AcceptorMessage {}

record Deny(Long ballot, Long deniedBallot) implements AcceptorMessage {}

// PHASE III

record Confirm(Long ballot, GameMove move) implements ProposerMessage {}
