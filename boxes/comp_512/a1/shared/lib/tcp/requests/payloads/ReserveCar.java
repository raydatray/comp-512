package tcp.requests.payloads;

import interfaces.ITCPRequestPayload;

public record ReserveCar(Integer customerID, String location) implements ITCPRequestPayload {
}