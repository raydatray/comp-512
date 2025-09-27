package server.rmi;

import interfaces.IRMIResourceManager;
import interfaces.IResourceManagerService;
import java.rmi.RemoteException;
import java.util.Vector;

public class RMIResourceManagerAdapter implements IRMIResourceManager {

    private final IResourceManagerService service;

    public RMIResourceManagerAdapter(IResourceManagerService service) {
        this.service = service;
    }

    @Override
    public Boolean addFlight(
        Integer flightNum,
        Integer flightSeats,
        Integer flightPrice
    ) throws RemoteException {
        try {
            return service.addFlight(flightNum, flightSeats, flightPrice);
        } catch (RuntimeException e) {
            throw new RemoteException("addFlight failed", e);
        }
    }

    @Override
    public Boolean addCars(String location, Integer numCars, Integer price)
        throws RemoteException {
        try {
            return service.addCars(location, numCars, price);
        } catch (RuntimeException e) {
            throw new RemoteException("addCars failed", e);
        }
    }

    @Override
    public Boolean addRooms(String location, Integer numRooms, Integer price)
        throws RemoteException {
        try {
            return service.addRooms(location, numRooms, price);
        } catch (RuntimeException e) {
            throw new RemoteException("addRooms failed", e);
        }
    }

    @Override
    public Integer newCustomer() throws RemoteException {
        try {
            return service.newCustomer();
        } catch (RuntimeException e) {
            throw new RemoteException("newCustomer failed", e);
        }
    }

    @Override
    public Boolean newCustomer(Integer cid) throws RemoteException {
        try {
            return service.newCustomer(cid);
        } catch (RuntimeException e) {
            throw new RemoteException("newCustomer(id) failed", e);
        }
    }

    @Override
    public Boolean deleteFlight(Integer flightNum) throws RemoteException {
        try {
            return service.deleteFlight(flightNum);
        } catch (RuntimeException e) {
            throw new RemoteException("deleteFlight failed", e);
        }
    }

    @Override
    public Boolean deleteCars(String location) throws RemoteException {
        try {
            return service.deleteCars(location);
        } catch (RuntimeException e) {
            throw new RemoteException("deleteCars failed", e);
        }
    }

    @Override
    public Boolean deleteRooms(String location) throws RemoteException {
        try {
            return service.deleteRooms(location);
        } catch (RuntimeException e) {
            throw new RemoteException("deleteRooms failed", e);
        }
    }

    @Override
    public Boolean deleteCustomer(Integer customerID) throws RemoteException {
        try {
            return service.deleteCustomer(customerID);
        } catch (RuntimeException e) {
            throw new RemoteException("deleteCustomer failed", e);
        }
    }

    @Override
    public Integer queryFlight(Integer flightNumber) throws RemoteException {
        try {
            return service.queryFlight(flightNumber);
        } catch (RuntimeException e) {
            throw new RemoteException("queryFlight failed", e);
        }
    }

    @Override
    public Integer queryCars(String location) throws RemoteException {
        try {
            return service.queryCars(location);
        } catch (RuntimeException e) {
            throw new RemoteException("queryCars failed", e);
        }
    }

    @Override
    public Integer queryRooms(String location) throws RemoteException {
        try {
            return service.queryRooms(location);
        } catch (RuntimeException e) {
            throw new RemoteException("queryRooms failed", e);
        }
    }

    @Override
    public String queryCustomerInfo(Integer customerID) throws RemoteException {
        try {
            return service.queryCustomerInfo(customerID);
        } catch (RuntimeException e) {
            throw new RemoteException("queryCustomerInfo failed", e);
        }
    }

    @Override
    public Integer queryFlightPrice(Integer flightNumber)
        throws RemoteException {
        try {
            return service.queryFlightPrice(flightNumber);
        } catch (RuntimeException e) {
            throw new RemoteException("queryFlightPrice failed", e);
        }
    }

    @Override
    public Integer queryCarsPrice(String location) throws RemoteException {
        try {
            return service.queryCarsPrice(location);
        } catch (RuntimeException e) {
            throw new RemoteException("queryCarsPrice failed", e);
        }
    }

    @Override
    public Integer queryRoomsPrice(String location) throws RemoteException {
        try {
            return service.queryRoomsPrice(location);
        } catch (RuntimeException e) {
            throw new RemoteException("queryRoomsPrice failed", e);
        }
    }

    @Override
    public Boolean reserveFlight(Integer customerID, Integer flightNumber)
        throws RemoteException {
        try {
            return service.reserveFlight(customerID, flightNumber);
        } catch (RuntimeException e) {
            throw new RemoteException("reserveFlight failed", e);
        }
    }

    @Override
    public Boolean reserveCar(Integer customerID, String location)
        throws RemoteException {
        try {
            return service.reserveCar(customerID, location);
        } catch (RuntimeException e) {
            throw new RemoteException("reserveCar failed", e);
        }
    }

    @Override
    public Boolean reserveRoom(Integer customerID, String location)
        throws RemoteException {
        try {
            return service.reserveRoom(customerID, location);
        } catch (RuntimeException e) {
            throw new RemoteException("reserveRoom failed", e);
        }
    }

    @Override
    public Boolean bundle(
        Integer customerID,
        Vector<String> flightNumbers,
        String location,
        Boolean car,
        Boolean room
    ) throws RemoteException {
        try {
            return service.bundle(
                customerID,
                flightNumbers,
                location,
                car,
                room
            );
        } catch (RuntimeException e) {
            throw new RemoteException("bundle failed", e);
        }
    }

    @Override
    public String getName() throws RemoteException {
        try {
            return service.getName();
        } catch (RuntimeException e) {
            throw new RemoteException("getName failed", e);
        }
    }
}
