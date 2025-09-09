package middleware.common;

import interfaces.IResourceManagerService;
import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.common.ResourceManager;

public class Middleware implements IResourceManagerService {

    private static final Logger logger = LoggerFactory.getLogger(
        Middleware.class
    );

    String name;

    private final IResourceManagerService flightRM;
    private final IResourceManagerService carRM;
    private final IResourceManagerService roomRM;
    private final ResourceManager customerRM;

    public Middleware(
        String name,
        IResourceManagerService flightRM,
        IResourceManagerService carRM,
        IResourceManagerService roomRM,
        ResourceManager customerRM
    ) {
        this.name = name;
        this.flightRM = flightRM;
        this.carRM = carRM;
        this.roomRM = roomRM;
        this.customerRM = customerRM;
    }

    public Boolean addFlight(
        Integer flightNum,
        Integer flightSeats,
        Integer flightPrice
    ) {
        return this.flightRM.addFlight(flightNum, flightSeats, flightPrice);
    }

    public Boolean addCars(String location, Integer numCars, Integer price) {
        return this.carRM.addCars(location, numCars, price);
    }

    public Boolean addRooms(String location, Integer numRooms, Integer price) {
        return this.roomRM.addRooms(location, numRooms, price);
    }

    public Integer newCustomer() {
        //todo: call customer dir
        return 0;
    }

    public Boolean newCustomer(Integer cid) {
        //todo: call customer dir
        return true;
    }

    public Boolean deleteFlight(Integer flightNum) {
        return this.flightRM.deleteFlight(flightNum);
    }

    public Boolean deleteCars(String location) {
        return this.carRM.deleteCars(location);
    }

    public Boolean deleteRooms(String location) {
        return this.roomRM.deleteRooms(location);
    }

    public Boolean deleteCustomer(Integer customerID) {
        //todo: call customer rm
        return true;
    }

    public Integer queryFlight(Integer flightNumber) {
        return this.flightRM.queryFlight(flightNumber);
    }

    public Integer queryCars(String location) {
        return this.carRM.queryCars(location);
    }

    public Integer queryRooms(String location) {
        return this.roomRM.queryRooms(location);
    }

    public String queryCustomerInfo(Integer customerID) {
        //todo : call customer rm

        return "hi";
    }

    public Integer queryFlightPrice(Integer flightNumber) {
        return this.flightRM.queryFlightPrice(flightNumber);
    }

    public Integer queryCarsPrice(String location) {
        return this.carRM.queryCarsPrice(location);
    }

    public Integer queryRoomsPrice(String location) {
        return this.roomRM.queryRoomsPrice(location);
    }

    public Boolean reserveFlight(Integer customerID, Integer flightNumber) {
        return this.flightRM.reserveFlight(customerID, flightNumber);
    }

    public Boolean reserveCar(Integer customerID, String location) {
        return this.carRM.reserveCar(customerID, location);
    }

    public Boolean reserveRoom(Integer customerID, String location) {
        return this.roomRM.reserveRoom(customerID, location);
    }

    public Boolean bundle(
        Integer customerID,
        Vector<String> flightNumbers,
        String location,
        Boolean car,
        Boolean room
    ) {
        //todo

        return true;
    }

    public String getName() {
        return this.name;
    }
}
