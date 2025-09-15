package tcp.requests.payloads;

import interfaces.ITCPRequestPayload;

public record ReserveRoom(Integer customerID, String location) implements
    ITCPRequestPayload {}
