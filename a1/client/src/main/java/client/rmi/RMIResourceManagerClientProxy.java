package client.rmi;

import client.common.TransportException;
import interfaces.IRMIResourceManager;
import interfaces.IResourceManagerService;
import java.rmi.RemoteException;
import java.util.Vector;

public final class RMIResourceManagerClientProxy
    implements IResourceManagerService, AutoCloseable {

    private final IRMIResourceManager stub;

    public RMIResourceManagerClientProxy(IRMIResourceManager stub) {
        this.stub = stub;
    }

    @Override
    public Boolean addFlight(
        Integer flightNum,
        Integer flightSeats,
        Integer flightPrice
    ) {
        try {
            return stub.addFlight(flightNum, flightSeats, flightPrice);
        } catch (RemoteException e) {
            throw new TransportException("addFlight failed", e);
        }
    }

    @Override
    public Boolean addCars(String location, Integer numCars, Integer price) {
        try {
            return stub.addCars(location, numCars, price);
        } catch (RemoteException e) {
            throw new TransportException("addCars failed", e);
        }
    }

    @Override
    public Boolean addRooms(String location, Integer numRooms, Integer price) {
        try {
            return stub.addRooms(location, numRooms, price);
        } catch (RemoteException e) {
            throw new TransportException("addRooms failed", e);
        }
    }

    @Override
    public Integer newCustomer() {
        try {
            return stub.newCustomer();
        } catch (RemoteException e) {
            throw new TransportException("newCustomer failed", e);
        }
    }

    @Override
    public Boolean newCustomer(Integer cid) {
        try {
            return stub.newCustomer(cid);
        } catch (RemoteException e) {
            throw new TransportException("newCustomer(id) failed", e);
        }
    }

    @Override
    public Boolean deleteFlight(Integer flightNum) {
        try {
            return stub.deleteFlight(flightNum);
        } catch (RemoteException e) {
            throw new TransportException("deleteFlight failed", e);
        }
    }

    @Override
    public Boolean deleteCars(String location) {
        try {
            return stub.deleteCars(location);
        } catch (RemoteException e) {
            throw new TransportException("deleteCars failed", e);
        }
    }

    @Override
    public Boolean deleteRooms(String location) {
        try {
            return stub.deleteRooms(location);
        } catch (RemoteException e) {
            throw new TransportException("deleteRooms failed", e);
        }
    }

    @Override
    public Boolean deleteCustomer(Integer customerID) {
        try {
            return stub.deleteCustomer(customerID);
        } catch (RemoteException e) {
            throw new TransportException("deleteCustomer failed", e);
        }
    }

    @Override
    public Integer queryFlight(Integer flightNumber) {
        try {
            return stub.queryFlight(flightNumber);
        } catch (RemoteException e) {
            throw new TransportException("queryFlight failed", e);
        }
    }

    @Override
    public Integer queryCars(String location) {
        try {
            return stub.queryCars(location);
        } catch (RemoteException e) {
            throw new TransportException("queryCars failed", e);
        }
    }

    @Override
    public Integer queryRooms(String location) {
        try {
            return stub.queryRooms(location);
        } catch (RemoteException e) {
            throw new TransportException("queryRooms failed", e);
        }
    }

    @Override
    public String queryCustomerInfo(Integer customerID) {
        try {
            return stub.queryCustomerInfo(customerID);
        } catch (RemoteException e) {
            throw new TransportException("queryCustomerInfo failed", e);
        }
    }

    @Override
    public Integer queryFlightPrice(Integer flightNumber) {
        try {
            return stub.queryFlightPrice(flightNumber);
        } catch (RemoteException e) {
            throw new TransportException("queryFlightPrice failed", e);
        }
    }

    @Override
    public Integer queryCarsPrice(String location) {
        try {
            return stub.queryCarsPrice(location);
        } catch (RemoteException e) {
            throw new TransportException("queryCarsPrice failed", e);
        }
    }

    @Override
    public Integer queryRoomsPrice(String location) {
        try {
            return stub.queryRoomsPrice(location);
        } catch (RemoteException e) {
            throw new TransportException("queryRoomsPrice failed", e);
        }
    }

    @Override
    public Boolean reserveFlight(Integer customerID, Integer flightNumber) {
        try {
            return stub.reserveFlight(customerID, flightNumber);
        } catch (RemoteException e) {
            throw new TransportException("reserveFlight failed", e);
        }
    }

    @Override
    public Boolean reserveCar(Integer customerID, String location) {
        try {
            return stub.reserveCar(customerID, location);
        } catch (RemoteException e) {
            throw new TransportException("reserveCar failed", e);
        }
    }

    @Override
    public Boolean reserveRoom(Integer customerID, String location) {
        try {
            return stub.reserveRoom(customerID, location);
        } catch (RemoteException e) {
            throw new TransportException("reserveRoom failed", e);
        }
    }

    @Override
    public Boolean bundle(
        Integer customerID,
        Vector<String> flightNumbers,
        String location,
        Boolean car,
        Boolean room
    ) {
        try {
            return stub.bundle(customerID, flightNumbers, location, car, room);
        } catch (RemoteException e) {
            throw new TransportException("bundle failed", e);
        }
    }

    @Override
    public String getName() {
        try {
            return stub.getName();
        } catch (RemoteException e) {
            throw new TransportException("getName failed", e);
        }
    }

    @Override
    public void close() {}
}
