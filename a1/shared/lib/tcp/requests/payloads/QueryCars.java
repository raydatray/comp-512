package tcp.requests.payloads;

import interfaces.ITCPRequestPayload;

public record QueryCars(String location) implements ITCPRequestPayload {}
