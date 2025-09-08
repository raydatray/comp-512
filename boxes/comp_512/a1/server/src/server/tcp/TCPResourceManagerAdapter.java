package server.tcp;

import java.net.SocketException;
import java.util.Vector;

import interfaces.IResourceManagerService;
import interfaces.ITCPResourceManager;
import tcp.payloads.AddFlight;

public class TCPResourceManagerAdapter implements ITCPResourceManager {
    private final IResourceManagerService service;

    public TCPResourceManagerAdapter(IResourceManagerService service) {
        this.service = service;
    }

    public Boolean addFlight(
            AddFlight p) throws SocketException {
        try {
            Integer flightNum = p.flightNum();
            Integer flightSeats = p.flightSeats();
            Integer flightPrice = p.flightPrice();

            return service.addFlight(flightNum, flightSeats, flightPrice);
        } catch (RuntimeException e) {
            throw new SocketException("addFlight failed", e);
        }
    }

    public Boolean addCars(String location, Integer numCars, Integer price)
            throws SocketException {
        return true;
    }

    public Boolean addRooms(String location, Integer numRooms, Integer price)
            throws SocketException {
        return true;
    }

    public Integer newCustomer() throws SocketException {
        return 0;
    }

    public Boolean newCustomer(Integer cid) throws SocketException {
        return true;
    }

    public Boolean deleteFlight(Integer flightNum) throws SocketException {
        return true;
    }

    public Boolean deleteCars(String location) throws SocketException {
        return true;
    }

    public Boolean deleteRooms(String location) throws SocketException {
        return true;
    }

    public Boolean deleteCustomer(Integer customerID) throws SocketException {
        return true;
    }

    public Integer queryFlight(Integer flightNumber) throws SocketException {
        return 0;
    }

    public Integer queryCars(String location) throws SocketException {
        return 0;
    }

    public Integer queryRooms(String location) throws SocketException {
        return 0;
    }

    public String queryCustomerInfo(Integer customerID) throws SocketException {
        return "";
    }

    public Integer queryFlightPrice(Integer flightNumber)
            throws SocketException {
        return 0;
    }

    public Integer queryCarsPrice(String location) throws SocketException {
        return 0;
    }

    public Integer queryRoomsPrice(String location) throws SocketException {
        return 0;
    }

    public Boolean reserveFlight(Integer customerID, Integer flightNumber)
            throws SocketException {
        return true;
    }

    public Boolean reserveCar(Integer customerID, String location)
            throws SocketException {
        return true;
    }

    public Boolean reserveRoom(Integer customerID, String location)
            throws SocketException {
        return true;
    }

    public Boolean bundle(
            Integer customerID,
            Vector<String> flightNumbers,
            String location,
            Boolean car,
            Boolean room) throws SocketException {
        return true;
    }

    public String getName() throws SocketException {
        return "";
    }
}
