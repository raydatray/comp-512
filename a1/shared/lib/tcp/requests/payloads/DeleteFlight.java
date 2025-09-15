package tcp.requests.payloads;

import interfaces.ITCPRequestPayload;

public record DeleteFlight(Integer flightNumber) implements
    ITCPRequestPayload {}
