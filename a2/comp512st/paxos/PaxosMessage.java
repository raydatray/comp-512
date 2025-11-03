package paxos;

import java.io.Serializable;

interface PaxosMessage extends Serializable {
    Ballot ballot();
}

interface ProposerMessage extends PaxosMessage {}

interface AcceptorMessage extends PaxosMessage {}

record PaxosEnvelope<T extends PaxosMessage>(
    String sender,
    T message
) implements Serializable {}

// PHASE I

record Propose(Ballot ballot) implements ProposerMessage {}

record Promise(Ballot ballot) implements AcceptorMessage {}

record PromiseWithPreviousAcceptedValue(Ballot ballot, Ballot previousBallot, GameMove previousMove) implements AcceptorMessage {}

record Refuse(Ballot ballot) implements AcceptorMessage {}

// PHASE II

record AcceptRequest(Ballot ballot, GameMove move) implements ProposerMessage {}

record AcceptAck(Ballot ballot) implements AcceptorMessage {}

record Deny(Ballot ballot) implements AcceptorMessage {}

// PHASE III

record Confirm(Ballot ballot, GameMove move) implements ProposerMessage {}
