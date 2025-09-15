package tcp.requests.payloads;

import interfaces.ITCPRequestPayload;

public record DeleteRooms(String location) implements ITCPRequestPayload {}
