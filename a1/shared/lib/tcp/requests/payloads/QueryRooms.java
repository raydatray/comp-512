package tcp.requests.payloads;

import interfaces.ITCPRequestPayload;

public record QueryRooms(String location) implements ITCPRequestPayload {}
