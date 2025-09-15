package interfaces;

import tcp.requests.payloads.*;
import tcp.responses.TCPBooleanResponseMessage;
import tcp.responses.TCPIntegerResponseMessage;
import tcp.responses.TCPStringResponseMessage;

public interface ITCPResourceManager {
    public TCPBooleanResponseMessage addFlight(AddFlight p);

    public TCPBooleanResponseMessage addCars(AddCars p);

    public TCPBooleanResponseMessage addRooms(AddRooms p);

    public TCPIntegerResponseMessage newCustomer();

    public TCPBooleanResponseMessage newCustomer(AddCustomerID p);

    public TCPBooleanResponseMessage deleteFlight(DeleteFlight p);

    public TCPBooleanResponseMessage deleteCars(DeleteCars p);

    public TCPBooleanResponseMessage deleteRooms(DeleteRooms p);

    public TCPBooleanResponseMessage deleteCustomer(DeleteCustomer p);

    public TCPIntegerResponseMessage queryFlight(QueryFlight p);

    public TCPIntegerResponseMessage queryCars(QueryCars p);

    public TCPIntegerResponseMessage queryRooms(QueryRooms p);

    public TCPStringResponseMessage queryCustomerInfo(QueryCustomer p);

    public TCPIntegerResponseMessage queryFlightPrice(QueryFlightPrice p);

    public TCPIntegerResponseMessage queryCarsPrice(QueryCarsPrice p);

    public TCPIntegerResponseMessage queryRoomsPrice(QueryRoomsPrice p);

    public TCPBooleanResponseMessage reserveFlight(ReserveFlight p);

    public TCPBooleanResponseMessage reserveCar(ReserveCar p);

    public TCPBooleanResponseMessage reserveRoom(ReserveRoom p);

    public TCPBooleanResponseMessage bundle(Bundle p);

    public TCPStringResponseMessage getName();
}
