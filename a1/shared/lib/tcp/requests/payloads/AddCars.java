package tcp.requests.payloads;

import interfaces.ITCPRequestPayload;

public record AddCars(
    String location,
    Integer numCars,
    Integer price
) implements ITCPRequestPayload {}
