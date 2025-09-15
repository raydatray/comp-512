package client.tcp;

import client.common.TransportException;
import interfaces.IResourceManagerService;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Vector;
import tcp.requests.TCPRequestMessage;
import tcp.requests.payloads.*;
import tcp.responses.TCPBooleanResponseMessage;
import tcp.responses.TCPIntegerResponseMessage;
import tcp.responses.TCPStringResponseMessage;

public class TCPResourceManagerClientProxy
    implements IResourceManagerService, AutoCloseable {

    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;

    public TCPResourceManagerClientProxy(String host, int port)
        throws IOException {
        socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public Boolean addFlight(
        Integer flightNum,
        Integer flightSeats,
        Integer flightPrice
    ) {
        try {
            AddFlight payload = new AddFlight(
                flightNum,
                flightSeats,
                flightPrice
            );
            TCPRequestMessage<AddFlight> request = new TCPRequestMessage<>(
                payload
            );

            out.writeObject(request);
            out.flush();

            Object response = in.readObject();
            return ((TCPBooleanResponseMessage) response).ok();
        } catch (Exception e) {
            throw new TransportException("addFlight failed", e);
        }
    }

    @Override
    public Boolean addCars(String location, Integer numCars, Integer price) {
        try {
            AddCars payload = new AddCars(location, numCars, price);
            TCPRequestMessage<AddCars> request = new TCPRequestMessage<>(
                payload
            );

            out.writeObject(request);
            out.flush();

            Object response = in.readObject();
            return ((TCPBooleanResponseMessage) response).ok();
        } catch (Exception e) {
            throw new TransportException("addCars failed", e);
        }
    }

    @Override
    public Boolean addRooms(String location, Integer numRooms, Integer price) {
        try {
            AddRooms payload = new AddRooms(location, numRooms, price);
            TCPRequestMessage<AddRooms> request = new TCPRequestMessage<>(
                payload
            );

            out.writeObject(request);
            out.flush();

            Object response = in.readObject();
            return ((TCPBooleanResponseMessage) response).ok();
        } catch (Exception e) {
            throw new TransportException("addRooms failed", e);
        }
    }

    @Override
    public Integer newCustomer() {
        try {
            AddCustomerID payload = new AddCustomerID(null);
            TCPRequestMessage<AddCustomerID> request = new TCPRequestMessage<>(
                payload
            );

            out.writeObject(request);
            out.flush();

            Object response = in.readObject();
            return ((TCPIntegerResponseMessage) response).result();
        } catch (Exception e) {
            throw new TransportException("newCustomer failed", e);
        }
    }

    @Override
    public Boolean newCustomer(Integer cid) {
        try {
            AddCustomerID payload = new AddCustomerID(cid);
            TCPRequestMessage<AddCustomerID> request = new TCPRequestMessage<>(
                payload
            );

            out.writeObject(request);
            out.flush();

            Object response = in.readObject();
            return ((TCPBooleanResponseMessage) response).ok();
        } catch (Exception e) {
            throw new TransportException("newCustomer with id failed", e);
        }
    }

    @Override
    public Boolean deleteFlight(Integer flightNum) {
        try {
            DeleteFlight payload = new DeleteFlight(flightNum);
            TCPRequestMessage<DeleteFlight> request = new TCPRequestMessage<>(
                payload
            );

            out.writeObject(request);
            out.flush();

            Object response = in.readObject();
            return ((TCPBooleanResponseMessage) response).ok();
        } catch (Exception e) {
            throw new TransportException("deleteFlight failed", e);
        }
    }

    @Override
    public Boolean deleteCars(String location) {
        try {
            DeleteCars payload = new DeleteCars(location);
            TCPRequestMessage<DeleteCars> request = new TCPRequestMessage<>(
                payload
            );

            out.writeObject(request);
            out.flush();

            Object response = in.readObject();
            return ((TCPBooleanResponseMessage) response).ok();
        } catch (Exception e) {
            throw new TransportException("deleteCars failed", e);
        }
    }

    @Override
    public Boolean deleteRooms(String location) {
        try {
            DeleteRooms payload = new DeleteRooms(location);
            TCPRequestMessage<DeleteRooms> request = new TCPRequestMessage<>(
                payload
            );

            out.writeObject(request);
            out.flush();

            Object response = in.readObject();
            return ((TCPBooleanResponseMessage) response).ok();
        } catch (Exception e) {
            throw new TransportException("deleteRooms failed", e);
        }
    }

    @Override
    public Boolean deleteCustomer(Integer customerID) {
        try {
            DeleteCustomer payload = new DeleteCustomer(customerID);
            TCPRequestMessage<DeleteCustomer> request = new TCPRequestMessage<>(
                payload
            );

            out.writeObject(request);
            out.flush();

            Object response = in.readObject();
            return ((TCPBooleanResponseMessage) response).ok();
        } catch (Exception e) {
            throw new TransportException("deleteCustomer failed", e);
        }
    }

    @Override
    public Integer queryFlight(Integer flightNumber) {
        try {
            QueryFlight payload = new QueryFlight(flightNumber);
            TCPRequestMessage<QueryFlight> request = new TCPRequestMessage<>(
                payload
            );

            out.writeObject(request);
            out.flush();

            Object response = in.readObject();
            return ((TCPIntegerResponseMessage) response).result();
        } catch (Exception e) {
            throw new TransportException("queryFlight failed", e);
        }
    }

    @Override
    public Integer queryCars(String location) {
        try {
            QueryCars payload = new QueryCars(location);
            TCPRequestMessage<QueryCars> request = new TCPRequestMessage<>(
                payload
            );

            out.writeObject(request);
            out.flush();

            Object response = in.readObject();
            return ((TCPIntegerResponseMessage) response).result();
        } catch (Exception e) {
            throw new TransportException("queryCars failed", e);
        }
    }

    @Override
    public Integer queryRooms(String location) {
        try {
            QueryRooms payload = new QueryRooms(location);
            TCPRequestMessage<QueryRooms> request = new TCPRequestMessage<>(
                payload
            );

            out.writeObject(request);
            out.flush();

            Object response = in.readObject();
            return ((TCPIntegerResponseMessage) response).result();
        } catch (Exception e) {
            throw new TransportException("queryRooms failed", e);
        }
    }

    @Override
    public String queryCustomerInfo(Integer customerID) {
        try {
            QueryCustomer payload = new QueryCustomer(customerID);
            TCPRequestMessage<QueryCustomer> request = new TCPRequestMessage<>(
                payload
            );

            out.writeObject(request);
            out.flush();

            Object response = in.readObject();
            return ((TCPStringResponseMessage) response).result();
        } catch (Exception e) {
            throw new TransportException("queryCustomerInfo failed", e);
        }
    }

    @Override
    public Integer queryFlightPrice(Integer flightNumber) {
        try {
            QueryFlightPrice payload = new QueryFlightPrice(flightNumber);
            TCPRequestMessage<QueryFlightPrice> request =
                new TCPRequestMessage<>(payload);

            out.writeObject(request);
            out.flush();

            Object response = in.readObject();

            return ((TCPIntegerResponseMessage) response).result();
        } catch (Exception e) {
            throw new TransportException("queryFlightPrice failed", e);
        }
    }

    @Override
    public Integer queryCarsPrice(String location) {
        try {
            QueryCarsPrice payload = new QueryCarsPrice(location);
            TCPRequestMessage<QueryCarsPrice> request = new TCPRequestMessage<>(
                payload
            );

            out.writeObject(request);
            out.flush();

            Object response = in.readObject();

            return ((TCPIntegerResponseMessage) response).result();
        } catch (Exception e) {
            throw new TransportException("queryCarsPrice failed", e);
        }
    }

    @Override
    public Integer queryRoomsPrice(String location) {
        try {
            QueryRoomsPrice payload = new QueryRoomsPrice(location);
            TCPRequestMessage<QueryRoomsPrice> request =
                new TCPRequestMessage<>(payload);

            out.writeObject(request);
            out.flush();

            Object response = in.readObject();

            return ((TCPIntegerResponseMessage) response).result();
        } catch (Exception e) {
            throw new TransportException("queryRoomsPrice failed", e);
        }
    }

    @Override
    public Boolean reserveFlight(Integer customerID, Integer flightNumber) {
        try {
            ReserveFlight payload = new ReserveFlight(customerID, flightNumber);
            TCPRequestMessage<ReserveFlight> request = new TCPRequestMessage<>(
                payload
            );

            out.writeObject(request);
            out.flush();

            Object response = in.readObject();

            return ((TCPBooleanResponseMessage) response).ok();
        } catch (Exception e) {
            throw new TransportException("reserveFlight failed", e);
        }
    }

    @Override
    public Boolean reserveCar(Integer customerID, String location) {
        try {
            ReserveCar payload = new ReserveCar(customerID, location);
            TCPRequestMessage<ReserveCar> request = new TCPRequestMessage<>(
                payload
            );

            out.writeObject(request);
            out.flush();

            Object response = in.readObject();

            return ((TCPBooleanResponseMessage) response).ok();
        } catch (Exception e) {
            throw new TransportException("reserveCar failed", e);
        }
    }

    @Override
    public Boolean reserveRoom(Integer customerID, String location) {
        try {
            ReserveRoom payload = new ReserveRoom(customerID, location);
            TCPRequestMessage<ReserveRoom> request = new TCPRequestMessage<>(
                payload
            );

            out.writeObject(request);
            out.flush();

            Object response = in.readObject();

            return ((TCPBooleanResponseMessage) response).ok();
        } catch (Exception e) {
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
            Bundle payload = new Bundle(
                customerID,
                flightNumbers,
                location,
                car,
                room
            );
            TCPRequestMessage<Bundle> request = new TCPRequestMessage<>(
                payload
            );

            out.writeObject(request);
            out.flush();

            Object response = in.readObject();

            return ((TCPBooleanResponseMessage) response).ok();
        } catch (Exception e) {
            throw new TransportException("bundle failed", e);
        }
    }

    @Override
    public String getName() {
        try {
            GetName payload = new GetName();

            out.writeObject(payload);
            out.flush();

            Object response = in.readObject();

            return ((TCPStringResponseMessage) response).result();
        } catch (Exception e) {
            throw new TransportException("getName failed", e);
        }
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
