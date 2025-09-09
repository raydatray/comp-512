package middleware.common;

import interfaces.IResourceManagerService;

import java.util.Calendar;
import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Middleware implements IResourceManagerService {

    private static final Logger logger = LoggerFactory.getLogger(
            Middleware.class);

    String name;

    private final IResourceManagerService flightRM;
    private final IResourceManagerService carRM;
    private final IResourceManagerService roomRM;

    public Middleware(
            String name,
            IResourceManagerService flightRM,
            IResourceManagerService carRM,
            IResourceManagerService roomRM) {
        this.name = name;
        this.flightRM = flightRM;
        this.carRM = carRM;
        this.roomRM = roomRM;
    }

    public Boolean addFlight(
            Integer flightNum,
            Integer flightSeats,
            Integer flightPrice) {
        return this.flightRM.addFlight(flightNum, flightSeats, flightPrice);
    }

    public Boolean addCars(String location, Integer numCars, Integer price) {
        return this.carRM.addCars(location, numCars, price);
    }

    public Boolean addRooms(String location, Integer numRooms, Integer price) {
        return this.roomRM.addRooms(location, numRooms, price);
    }

    // TODO: implement some type of rollback mechanism in case one of the RMs fail
    public Integer newCustomer() {
        // Need to create cid at middleware level to prevent each RM generating a
        // different id
        Integer cid = Integer.parseInt(
                String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
                        String.valueOf(Math.round(Math.random() * 100 + 1)));

        this.flightRM.newCustomer(cid);
        this.carRM.newCustomer(cid);
        this.roomRM.newCustomer(cid);

        return cid;
    }

    // TODO: implement some type of rollback mechanism in case one of the RMs fail
    public Boolean newCustomer(Integer cid) {
        return this.flightRM.newCustomer(cid) && this.carRM.newCustomer(cid) && this.roomRM.newCustomer(cid);
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

    // TODO: implement some type of rollback mechanism in case one of the RMs fail
    public Boolean deleteCustomer(Integer customerID) {
        return this.carRM.deleteCustomer(customerID) && this.carRM.deleteCustomer(customerID)
                && this.carRM.deleteCustomer(customerID);
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
        return this.flightRM.queryCustomerInfo(customerID) + this.carRM.queryCustomerInfo(customerID)
                + this.roomRM.queryCustomerInfo(customerID);
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

    // TODO: buff error handling, add rollback mechanism in case any of the
    // reservations fail
    public Boolean bundle(
            Integer customerID,
            Vector<String> flightNumbers,
            String location,
            Boolean car,
            Boolean room) {
        try {
            for (String f : flightNumbers) {
                Integer flightNumber = Integer.parseInt(f);

                this.flightRM.reserveFlight(customerID, flightNumber);
            }

            if (car) {
                if (!this.carRM.reserveCar(customerID, location)) {
                    return false;
                }
            }

            if (room) {
                if (!this.roomRM.reserveRoom(customerID, location)) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    public String getName() {
        return this.name;
    }
}
