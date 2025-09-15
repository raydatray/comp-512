package tcp.requests.payloads;

import interfaces.ITCPRequestPayload;

public record AddCustomerID(Integer customerID) implements ITCPRequestPayload {}
