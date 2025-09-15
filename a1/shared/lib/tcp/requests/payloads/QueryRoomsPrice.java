package tcp.requests.payloads;

import interfaces.ITCPRequestPayload;

public record QueryRoomsPrice(String location) implements ITCPRequestPayload {}
