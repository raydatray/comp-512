package tcp.requests.payloads;

import interfaces.ITCPRequestPayload;

public record ReserveFlight(Integer customerID, Integer flightNumber) implements ITCPRequestPayload {
}