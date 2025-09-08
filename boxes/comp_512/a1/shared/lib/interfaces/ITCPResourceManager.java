package interfaces;

import java.net.SocketException;
import java.util.Vector;

import tcp.payloads.*;

public interface ITCPResourceManager {
        public Boolean addFlight(AddFlight p) throws SocketException;

        public Boolean addCars(String location, Integer numCars, Integer price)
                        throws SocketException;

        public Boolean addRooms(String location, Integer numRooms, Integer price)
                        throws SocketException;

        public Integer newCustomer() throws SocketException;

        public Boolean newCustomer(Integer cid) throws SocketException;

        public Boolean deleteFlight(Integer flightNum) throws SocketException;

        public Boolean deleteCars(String location) throws SocketException;

        public Boolean deleteRooms(String location) throws SocketException;

        public Boolean deleteCustomer(Integer customerID) throws SocketException;

        public Integer queryFlight(Integer flightNumber) throws SocketException;

        public Integer queryCars(String location) throws SocketException;

        public Integer queryRooms(String location) throws SocketException;

        public String queryCustomerInfo(Integer customerID) throws SocketException;

        public Integer queryFlightPrice(Integer flightNumber)
                        throws SocketException;

        public Integer queryCarsPrice(String location) throws SocketException;

        public Integer queryRoomsPrice(String location) throws SocketException;

        public Boolean reserveFlight(Integer customerID, Integer flightNumber)
                        throws SocketException;

        public Boolean reserveCar(Integer customerID, String location)
                        throws SocketException;

        public Boolean reserveRoom(Integer customerID, String location)
                        throws SocketException;

        public Boolean bundle(
                        Integer customerID,
                        Vector<String> flightNumbers,
                        String location,
                        Boolean car,
                        Boolean room) throws SocketException;

        public String getName() throws SocketException;
}
