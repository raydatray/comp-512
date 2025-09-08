package tcp.requests.payloads;

import interfaces.ITCPRequestPayload;

public record DeleteCustomer(Integer customerID) implements ITCPRequestPayload {
}