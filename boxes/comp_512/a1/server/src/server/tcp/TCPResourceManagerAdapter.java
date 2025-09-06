package server.tcp;

import java.util.Vector;

import interfaces.IResourceManagerService;
import interfaces.ITCPResourceManager;
import tcp.requests.payloads.*;
import tcp.responses.TCPBooleanResponseMessage;
import tcp.responses.TCPIntegerResponseMessage;
import tcp.responses.TCPStringResponseMessage;

public class TCPResourceManagerAdapter implements ITCPResourceManager {
    private final IResourceManagerService service;

    public TCPResourceManagerAdapter(IResourceManagerService service) {
        this.service = service;
    }

    @Override
    public TCPBooleanResponseMessage addFlight(
            AddFlight p) {
        try {
            Integer flightNum = p.flightNum();
            Integer flightSeats = p.flightSeats();
            Integer flightPrice = p.flightPrice();

            Boolean ok = service.addFlight(flightNum, flightSeats, flightPrice);
            return new TCPBooleanResponseMessage(ok, null);
        } catch (RuntimeException e) {
            return new TCPBooleanResponseMessage(null, e.getMessage());
        }
    }

    @Override
    public TCPBooleanResponseMessage addCars(AddCars p) {
        try {
            String location = p.location();
            Integer numCars = p.numCars();
            Integer price = p.price();

            Boolean ok = service.addCars(location, numCars, price);
            return new TCPBooleanResponseMessage(ok, null);
        } catch (RuntimeException e) {
            return new TCPBooleanResponseMessage(null, e.getMessage());
        }
    }

    @Override
    public TCPBooleanResponseMessage addRooms(AddRooms p) {
        try {
            String location = p.location();
            Integer numCars = p.numRooms();
            Integer price = p.price();

            Boolean ok = service.addRooms(location, numCars, price);
            return new TCPBooleanResponseMessage(ok, null);
        } catch (RuntimeException e) {
            return new TCPBooleanResponseMessage(null, e.getMessage());
        }
    }

    @Override
    public TCPIntegerResponseMessage newCustomer() {
        try {
            Integer customerID = service.newCustomer();
            return new TCPIntegerResponseMessage(customerID, null);
        } catch (RuntimeException e) {
            return new TCPIntegerResponseMessage(null, e.getMessage());
        }
    }

    @Override
    public TCPBooleanResponseMessage newCustomer(AddCustomerID p) {
        try {
            Integer customerID = p.customerID();

            Boolean ok = service.newCustomer(customerID);
            return new TCPBooleanResponseMessage(ok, null);
        } catch (RuntimeException e) {
            return new TCPBooleanResponseMessage(null, e.getMessage());
        }
    }

    @Override
    public TCPBooleanResponseMessage deleteFlight(DeleteFlight p) {
        try {
            Integer flightNumber = p.flightNumber();

            Boolean ok = service.deleteFlight(flightNumber);
            return new TCPBooleanResponseMessage(ok, "");
        } catch (RuntimeException e) {
            return new TCPBooleanResponseMessage(null, e.getMessage());
        }
    }

    @Override
    public TCPBooleanResponseMessage deleteCars(DeleteCars p) {
        try {
            String location = p.location();

            Boolean ok = service.deleteCars(location);
            return new TCPBooleanResponseMessage(ok, null);
        } catch (RuntimeException e) {
            return new TCPBooleanResponseMessage(null, e.getMessage());
        }
    }

    @Override
    public TCPBooleanResponseMessage deleteRooms(DeleteRooms p) {
        try {
            String location = p.location();

            Boolean ok = service.deleteRooms(location);
            return new TCPBooleanResponseMessage(ok, null);
        } catch (RuntimeException e) {
            return new TCPBooleanResponseMessage(null, e.getMessage());
        }
    }

    @Override
    public TCPBooleanResponseMessage deleteCustomer(DeleteCustomer p) {
        try {
            Integer customerID = p.customerID();

            Boolean ok = service.deleteCustomer(customerID);
            return new TCPBooleanResponseMessage(ok, null);
        } catch (RuntimeException e) {
            return new TCPBooleanResponseMessage(null, e.getMessage());
        }
    }

    @Override
    public TCPIntegerResponseMessage queryFlight(QueryFlight p) {
        try {
            Integer flightNumber = p.flightNumber();

            Integer numSeats = service.queryFlight(flightNumber);
            return new TCPIntegerResponseMessage(numSeats, "");
        } catch (RuntimeException e) {
            return new TCPIntegerResponseMessage(null, e.getMessage());
        }
    }

    @Override
    public TCPIntegerResponseMessage queryCars(QueryCars p) {
        try {
            String location = p.location();

            Integer numCars = service.queryCars(location);
            return new TCPIntegerResponseMessage(numCars, null);
        } catch (RuntimeException e) {
            return new TCPIntegerResponseMessage(null, e.getMessage());
        }
    }

    @Override
    public TCPIntegerResponseMessage queryRooms(QueryRooms p) {
        try {
            String location = p.location();

            Integer numRooms = service.queryRooms(location);
            return new TCPIntegerResponseMessage(numRooms, null);
        } catch (RuntimeException e) {
            return new TCPIntegerResponseMessage(null, e.getMessage());
        }
    }

    @Override
    public TCPStringResponseMessage queryCustomerInfo(QueryCustomer p) {
        try {
            Integer customerID = p.customerID();

            String customerInfo = service.queryCustomerInfo(customerID);
            return new TCPStringResponseMessage(customerInfo, null);
        } catch (RuntimeException e) {
            return new TCPStringResponseMessage(null, e.getMessage());
        }
    }

    @Override
    public TCPIntegerResponseMessage queryFlightPrice(QueryFlightPrice p) {
        try {
            Integer flightNumber = p.flightNumber();

            Integer flightPrice = service.queryFlightPrice(flightNumber);
            return new TCPIntegerResponseMessage(flightPrice, null);
        } catch (RuntimeException e) {
            return new TCPIntegerResponseMessage(null, e.getMessage());
        }
    }

    @Override
    public TCPIntegerResponseMessage queryCarsPrice(QueryCarsPrice p) {
        try {
            String location = p.location();

            Integer carPrice = service.queryCarsPrice(location);
            return new TCPIntegerResponseMessage(carPrice, null);
        } catch (RuntimeException e) {
            return new TCPIntegerResponseMessage(null, e.getMessage());
        }
    }

    @Override
    public TCPIntegerResponseMessage queryRoomsPrice(QueryRoomsPrice p) {
        try {
            String location = p.location();

            Integer roomPrice = service.queryRoomsPrice(location);
            return new TCPIntegerResponseMessage(roomPrice, null);
        } catch (RuntimeException e) {
            return new TCPIntegerResponseMessage(null, e.getMessage());
        }
    }

    @Override
    public TCPBooleanResponseMessage reserveFlight(ReserveFlight p) {
        try {
            Integer customerID = p.customerID();
            Integer flightNumber = p.flightNumber();

            Boolean ok = service.reserveFlight(customerID, flightNumber);
            return new TCPBooleanResponseMessage(ok, null);
        } catch (RuntimeException e) {
            return new TCPBooleanResponseMessage(null, e.getMessage());
        }
    }

    @Override
    public TCPBooleanResponseMessage reserveCar(ReserveCar p) {
        try {
            Integer customerID = p.customerID();
            String location = p.location();

            Boolean ok = service.reserveCar(customerID, location);
            return new TCPBooleanResponseMessage(ok, null);
        } catch (RuntimeException e) {
            return new TCPBooleanResponseMessage(null, e.getMessage());
        }
    }

    @Override
    public TCPBooleanResponseMessage reserveRoom(ReserveRoom p) {
        try {
            Integer customerID = p.customerID();
            String location = p.location();

            Boolean ok = service.reserveRoom(customerID, location);
            return new TCPBooleanResponseMessage(ok, null);
        } catch (RuntimeException e) {
            return new TCPBooleanResponseMessage(null, e.getMessage());
        }
    }

    @Override
    public TCPBooleanResponseMessage bundle(Bundle p) {
        try {
            Integer customerID = p.customerID();
            Vector<String> flightNumbers = p.flightNumbers();
            String location = p.location();
            Boolean car = p.car();
            Boolean room = p.room();

            Boolean ok = service.bundle(customerID, flightNumbers, location, car, room);
            return new TCPBooleanResponseMessage(ok, null);
        } catch (RuntimeException e) {
            return new TCPBooleanResponseMessage(null, e.getMessage());
        }
    }

    @Override
    public TCPStringResponseMessage getName() {
        try {
            String name = service.getName();
            return new TCPStringResponseMessage(name, null);
        } catch (RuntimeException e) {
            return new TCPStringResponseMessage(null, e.getMessage());
        }
    }

}
