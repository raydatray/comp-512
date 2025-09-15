package tcp.requests.payloads;

import interfaces.ITCPRequestPayload;
import java.util.Vector;

public record Bundle(
    Integer customerID,
    Vector<String> flightNumbers,
    String location,
    Boolean car,
    Boolean room
) implements ITCPRequestPayload {}
