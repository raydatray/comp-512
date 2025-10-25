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

record Propose(Long ballotId) implements ProposerMessage {}

record Promise(
    Long ballotId,
    Long lastAcceptedBID,
    GameMove lastAcceptedMove
) implements AcceptorMessage {}

record Refuse(Long ballotId) implements AcceptorMessage {}

// PHASE II

record AcceptRequest(Long ballotId, GameMove move) implements ProposerMessage {}

record AcceptAck(Long ballotId) implements AcceptorMessage {}

record Deny(Long ballotId) implements AcceptorMessage {}

// PHASE III

record Confirm(Long ballotId) implements ProposerMessage {}
