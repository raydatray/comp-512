package tcp.requests.payloads;

import java.util.Vector;

import interfaces.ITCPRequestPayload;

public record Bundle(Integer customerID, Vector<String> flightNumbers, String location, Boolean car, Boolean room)
                implements ITCPRequestPayload {
}