package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;

public interface IRMIResourceManager extends Remote {
    public Boolean addFlight(
        Integer flightNum,
        Integer flightSeats,
        Integer flightPrice
    ) throws RemoteException;

    public Boolean addCars(String location, Integer numCars, Integer price)
        throws RemoteException;

    public Boolean addRooms(String location, Integer numRooms, Integer price)
        throws RemoteException;

    public Integer newCustomer() throws RemoteException;

    public Boolean newCustomer(Integer cid) throws RemoteException;

    public Boolean deleteFlight(Integer flightNum) throws RemoteException;

    public Boolean deleteCars(String location) throws RemoteException;

    public Boolean deleteRooms(String location) throws RemoteException;

    public Boolean deleteCustomer(Integer customerID) throws RemoteException;

    public Integer queryFlight(Integer flightNumber) throws RemoteException;

    public Integer queryCars(String location) throws RemoteException;

    public Integer queryRooms(String location) throws RemoteException;

    public String queryCustomerInfo(Integer customerID) throws RemoteException;

    public Integer queryFlightPrice(Integer flightNumber)
        throws RemoteException;

    public Integer queryCarsPrice(String location) throws RemoteException;

    public Integer queryRoomsPrice(String location) throws RemoteException;

    public Boolean reserveFlight(Integer customerID, Integer flightNumber)
        throws RemoteException;

    public Boolean reserveCar(Integer customerID, String location)
        throws RemoteException;

    public Boolean reserveRoom(Integer customerID, String location)
        throws RemoteException;

    public Boolean bundle(
        Integer customerID,
        Vector<String> flightNumbers,
        String location,
        Boolean car,
        Boolean room
    ) throws RemoteException;

    public String getName() throws RemoteException;
}
