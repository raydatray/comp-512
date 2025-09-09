package tcp.requests.payloads;

import interfaces.ITCPRequestPayload;

public record QueryCarsPrice(String location) implements ITCPRequestPayload {
}