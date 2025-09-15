package tcp.requests.payloads;

import interfaces.ITCPRequestPayload;

public record AddFlight(
    int flightNum,
    int flightSeats,
    int flightPrice
) implements ITCPRequestPayload {}
