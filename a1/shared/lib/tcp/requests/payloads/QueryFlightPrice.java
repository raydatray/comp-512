package tcp.requests.payloads;

import interfaces.ITCPRequestPayload;

public record QueryFlightPrice(Integer flightNumber) implements
    ITCPRequestPayload {}
