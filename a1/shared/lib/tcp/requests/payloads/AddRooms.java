package tcp.requests.payloads;

import interfaces.ITCPRequestPayload;

public record AddRooms(
    String location,
    Integer numRooms,
    Integer price
) implements ITCPRequestPayload {}
