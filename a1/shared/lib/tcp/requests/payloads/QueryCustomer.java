package tcp.requests.payloads;

import interfaces.ITCPRequestPayload;

public record QueryCustomer(Integer customerID) implements ITCPRequestPayload {
}