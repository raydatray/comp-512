package tcp.requests.payloads;

import interfaces.ITCPRequestPayload;

public record QueryFlight(Integer flightNumber) implements ITCPRequestPayload {
}