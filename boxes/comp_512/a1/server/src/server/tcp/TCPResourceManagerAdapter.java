package server.tcp;

import interfaces.IResourceManagerService;
import interfaces.ITCPResourceManager;
import tcp.requests.payloads.AddCars;
import tcp.requests.payloads.AddCustomerID;
import tcp.requests.payloads.AddFlight;
import tcp.requests.payloads.AddRooms;
import tcp.requests.payloads.Bundle;
import tcp.requests.payloads.DeleteCars;
import tcp.requests.payloads.DeleteCustomer;
import tcp.requests.payloads.DeleteFlight;
import tcp.requests.payloads.DeleteRooms;
import tcp.requests.payloads.QueryCars;
import tcp.requests.payloads.QueryCarsPrice;
import tcp.requests.payloads.QueryCustomer;
import tcp.requests.payloads.QueryFlight;
import tcp.requests.payloads.QueryFlightPrice;
import tcp.requests.payloads.QueryRooms;
import tcp.requests.payloads.QueryRoomsPrice;
import tcp.requests.payloads.ReserveCar;
import tcp.requests.payloads.ReserveFlight;
import tcp.requests.payloads.ReserveRoom;
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addCars'");
    }

    @Override
    public TCPBooleanResponseMessage addRooms(AddRooms p) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addRooms'");
    }

    @Override
    public TCPIntegerResponseMessage newCustomer() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'newCustomer'");
    }

    @Override
    public TCPBooleanResponseMessage newCustomer(AddCustomerID p) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'newCustomer'");
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteCars'");
    }

    @Override
    public TCPBooleanResponseMessage deleteRooms(DeleteRooms p) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteRooms'");
    }

    @Override
    public TCPBooleanResponseMessage deleteCustomer(DeleteCustomer p) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteCustomer'");
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'queryCars'");
    }

    @Override
    public TCPIntegerResponseMessage queryRooms(QueryRooms p) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'queryRooms'");
    }

    @Override
    public TCPStringResponseMessage queryCustomerInfo(QueryCustomer p) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'queryCustomerInfo'");
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'queryCarsPrice'");
    }

    @Override
    public TCPIntegerResponseMessage queryRoomsPrice(QueryRoomsPrice p) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'queryRoomsPrice'");
    }

    @Override
    public TCPBooleanResponseMessage reserveFlight(ReserveFlight p) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'reserveFlight'");
    }

    @Override
    public TCPBooleanResponseMessage reserveCar(ReserveCar p) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'reserveCar'");
    }

    @Override
    public TCPBooleanResponseMessage reserveRoom(ReserveRoom p) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'reserveRoom'");
    }

    @Override
    public TCPBooleanResponseMessage bundle(Bundle p) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'bundle'");
    }

    @Override
    public TCPStringResponseMessage getName() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getName'");
    }

}
