package interfaces;

import java.util.Vector;

public interface IResourceManagerService {
    public Boolean addFlight(
        Integer flightNum,
        Integer flightSeats,
        Integer flightPrice
    );

    public Boolean addCars(String location, Integer numCars, Integer price);

    public Boolean addRooms(String location, Integer numRooms, Integer price);

    public Integer newCustomer();

    public Boolean newCustomer(Integer cid);

    public Boolean deleteFlight(Integer flightNum);

    public Boolean deleteCars(String location);

    public Boolean deleteRooms(String location);

    public Boolean deleteCustomer(Integer customerID);

    public Integer queryFlight(Integer flightNumber);

    public Integer queryCars(String location);

    public Integer queryRooms(String location);

    public String queryCustomerInfo(Integer customerID);

    public Integer queryFlightPrice(Integer flightNumber);

    public Integer queryCarsPrice(String location);

    public Integer queryRoomsPrice(String location);

    public Boolean reserveFlight(Integer customerID, Integer flightNumber);

    public Boolean reserveCar(Integer customerID, String location);

    public Boolean reserveRoom(Integer customerID, String location);

    public Boolean bundle(
        Integer customerID,
        Vector<String> flightNumbers,
        String location,
        Boolean car,
        Boolean room
    );

    public String getName();
}
