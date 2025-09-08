package tcp.requests.payloads;

import interfaces.ITCPRequestPayload;

public record DeleteCars(String location) implements ITCPRequestPayload {
}