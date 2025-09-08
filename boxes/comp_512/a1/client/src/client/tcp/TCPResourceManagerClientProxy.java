package client.tcp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Vector;

import client.common.TransportException;
import interfaces.IResourceManagerService;
import tcp.TCPRequestMessage;
import tcp.TCPResponseMessage;
import tcp.payloads.*;

public class TCPResourceManagerClientProxy implements IResourceManagerService, AutoCloseable {
    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;

    public TCPResourceManagerClientProxy(String host, int port) throws IOException {
        socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public Boolean addFlight(
            Integer flightNum,
            Integer flightSeats,
            Integer flightPrice) {
        try {
            AddFlight payload = new AddFlight(flightNum, flightSeats, flightPrice);
            TCPRequestMessage<AddFlight> request = new TCPRequestMessage<>(payload);

            out.writeObject(request);
            out.flush();

            Object response = in.readObject();

            return ((TCPResponseMessage) response).getSuccess();
        } catch (Exception e) {
            throw new TransportException("addFlight failed", e);
        }
    }

    @Override
    public Boolean addCars(String location, Integer numCars, Integer price) {
        // TODO: Implement
        return true;
    }

    @Override
    public Boolean addRooms(String location, Integer numRooms, Integer price) {
        // TODO: Implement
        return true;
    }

    @Override
    public Integer newCustomer() {
        // TODO: Implement
        return 0;
    }

    @Override
    public Boolean newCustomer(Integer cid) {
        // TODO: Implement
        return true;
    }

    @Override
    public Boolean deleteFlight(Integer flightNum) {
        // TODO: Implement
        return true;
    }

    @Override
    public Boolean deleteCars(String location) {
        // TODO: Implement
        return true;
    }

    @Override
    public Boolean deleteRooms(String location) {
        // TODO: Implement
        return true;
    }

    @Override
    public Boolean deleteCustomer(Integer customerID) {
        // TODO: Implement
        return true;
    }

    @Override
    public Integer queryFlight(Integer flightNumber) {
        // TODO: Implement
        return 0;
    }

    @Override
    public Integer queryCars(String location) {
        // TODO: Implement
        return 0;
    }

    @Override
    public Integer queryRooms(String location) {
        // TODO: Implement
        return 0;
    }

    @Override
    public String queryCustomerInfo(Integer customerID) {
        // TODO: Implement
        return "";
    }

    @Override
    public Integer queryFlightPrice(Integer flightNumber) {
        // TODO: Implement
        return 0;
    }

    @Override
    public Integer queryCarsPrice(String location) {
        // TODO: Implement
        return 0;
    }

    @Override
    public Integer queryRoomsPrice(String location) {
        // TODO: Implement
        return 0;
    }

    @Override
    public Boolean reserveFlight(Integer customerID, Integer flightNumber) {
        // TODO: Implement
        return true;
    }

    @Override
    public Boolean reserveCar(Integer customerID, String location) {
        // TODO: Implement
        return true;
    }

    @Override
    public Boolean reserveRoom(Integer customerID, String location) {
        // TODO: Implement
        return true;
    }

    @Override
    public Boolean bundle(
            Integer customerID,
            Vector<String> flightNumbers,
            String location,
            Boolean car,
            Boolean room) {
        // TODO: Implement
        return true;
    }

    @Override
    public String getName() {
        // TODO: Implement
        return "";
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
