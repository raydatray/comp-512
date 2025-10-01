package client.common;

import interfaces.IResourceManagerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ClientTest {

    private ByteArrayOutputStream out, err;
    private PrintStream oldOut, oldErr;

    @BeforeEach
    void hookIO() {
        oldOut = System.out;
        oldErr = System.err;
        out = new ByteArrayOutputStream();
        err = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
    }

    @AfterEach
    void restoreIO() {
        System.setOut(oldOut);
        System.setErr(oldErr);
    }

    private static Vector<String> v(String... xs) {
        var vec = new Vector<String>();
        for (var x : xs)
            vec.add(x);
        return vec;
    }

    @Test
    void addFlight_happyPath_callsService() throws Exception {
        IResourceManagerService rm = mock(IResourceManagerService.class);
        when(rm.addFlight(17, 100, 250)).thenReturn(true);

        Client c = new Client(rm, () -> {
        });
        c.execute(Command.AddFlight, v("AddFlight", "17", "100", "250"));

        verify(rm).addFlight(17, 100, 250);
        assertTrue(out.toString().contains("Flight added"));
    }

    @Test
    void addFlight_wrongArgCount_throws_andNoServiceCall() {
        IResourceManagerService rm = mock(IResourceManagerService.class);
        Client c = new Client(rm, () -> {
        });
        assertThrows(IllegalArgumentException.class,
                () -> c.execute(Command.AddFlight, v("AddFlight", "1", "2")));
        verifyNoInteractions(rm);
    }

    @Test
    void addCars_addRooms_paths() throws Exception {
        IResourceManagerService rm = mock(IResourceManagerService.class);
        when(rm.addCars("YUL", 10, 55)).thenReturn(true);
        when(rm.addRooms("YUL", 5, 80)).thenReturn(false);

        Client c = new Client(rm, () -> {
        });
        c.execute(Command.AddCars, v("AddCars", "YUL", "10", "55"));
        c.execute(Command.AddRooms, v("AddRooms", "YUL", "5", "80"));

        verify(rm).addCars("YUL", 10, 55);
        verify(rm).addRooms("YUL", 5, 80);
        String s = out.toString();
        assertTrue(s.contains("Cars added"));
        assertTrue(s.contains("Rooms could not be added"));
    }

    @Test
    void addCustomer_and_addCustomerID() throws Exception {
        IResourceManagerService rm = mock(IResourceManagerService.class);
        when(rm.newCustomer()).thenReturn(123);
        when(rm.newCustomer(456)).thenReturn(true);

        Client c = new Client(rm, () -> {
        });
        c.execute(Command.AddCustomer, v("AddCustomer"));
        c.execute(Command.AddCustomerID, v("AddCustomerID", "456"));

        verify(rm).newCustomer();
        verify(rm).newCustomer(456);
        String s = out.toString();
        assertTrue(s.contains("Add customer ID: 123"));
        assertTrue(s.contains("Add customer ID: 456"));
    }

    @Test
    void delete_operations() throws Exception {
        IResourceManagerService rm = mock(IResourceManagerService.class);
        when(rm.deleteFlight(7)).thenReturn(true);
        when(rm.deleteCars("MTL")).thenReturn(false);
        when(rm.deleteRooms("MTL")).thenReturn(true);
        when(rm.deleteCustomer(321)).thenReturn(false);

        Client c = new Client(rm, () -> {
        });
        c.execute(Command.DeleteFlight, v("DeleteFlight", "7"));
        c.execute(Command.DeleteCars, v("DeleteCars", "MTL"));
        c.execute(Command.DeleteRooms, v("DeleteRooms", "MTL"));
        c.execute(Command.DeleteCustomer, v("DeleteCustomer", "321"));

        verify(rm).deleteFlight(7);
        verify(rm).deleteCars("MTL");
        verify(rm).deleteRooms("MTL");
        verify(rm).deleteCustomer(321);

        String s = out.toString();
        assertTrue(s.contains("Flight Deleted"));
        assertTrue(s.contains("Cars could not be deleted"));
        assertTrue(s.contains("Rooms Deleted"));
        assertTrue(s.contains("Customer could not be deleted"));
    }

    @Test
    void query_operations_prices_and_customer() throws Exception {
        IResourceManagerService rm = mock(IResourceManagerService.class);
        when(rm.queryFlight(7)).thenReturn(9);
        when(rm.queryCars("MTL")).thenReturn(3);
        when(rm.queryRooms("MTL")).thenReturn(2);
        when(rm.queryCustomerInfo(123)).thenReturn("Bill: $77\n");
        when(rm.queryFlightPrice(7)).thenReturn(150);
        when(rm.queryCarsPrice("MTL")).thenReturn(60);
        when(rm.queryRoomsPrice("MTL")).thenReturn(90);

        Client c = new Client(rm, () -> {
        });
        c.execute(Command.QueryFlight, v("QueryFlight", "7"));
        c.execute(Command.QueryCars, v("QueryCars", "MTL"));
        c.execute(Command.QueryRooms, v("QueryRooms", "MTL"));
        c.execute(Command.QueryCustomer, v("QueryCustomer", "123"));
        c.execute(Command.QueryFlightPrice, v("QueryFlightPrice", "7"));
        c.execute(Command.QueryCarsPrice, v("QueryCarsPrice", "MTL"));
        c.execute(Command.QueryRoomsPrice, v("QueryRoomsPrice", "MTL"));

        verify(rm).queryFlight(7);
        verify(rm).queryCars("MTL");
        verify(rm).queryRooms("MTL");
        verify(rm).queryCustomerInfo(123);
        verify(rm).queryFlightPrice(7);
        verify(rm).queryCarsPrice("MTL");
        verify(rm).queryRoomsPrice("MTL");

        String s = out.toString();
        assertTrue(s.contains("Number of seats available: 9"));
        assertTrue(s.contains("Number of cars at this location: 3"));
        assertTrue(s.contains("Number of rooms at this location: 2"));
        assertTrue(s.contains("Bill: $77"));
        assertTrue(s.contains("Price of a seat: 150"));
        assertTrue(s.contains("Price of cars at this location: 60"));
        assertTrue(s.contains("Price of rooms at this location: 90"));
    }

    @Test
    void reserve_operations_and_bundle_withArgumentCapture() throws Exception {
        IResourceManagerService rm = mock(IResourceManagerService.class);
        when(rm.reserveFlight(123, 7)).thenReturn(true);
        when(rm.reserveCar(123, "MTL")).thenReturn(false);
        when(rm.reserveRoom(123, "MTL")).thenReturn(true);
        when(rm.bundle(anyInt(), any(), anyString(), anyBoolean(), anyBoolean())).thenReturn(true);

        Client c = new Client(rm, () -> {
        });
        c.execute(Command.ReserveFlight, v("ReserveFlight", "123", "7"));
        c.execute(Command.ReserveCar, v("ReserveCar", "123", "MTL"));
        c.execute(Command.ReserveRoom, v("ReserveRoom", "123", "MTL"));
        c.execute(Command.Bundle, v("Bundle", "123", "11", "12", "MTL", "y", "n"));

        verify(rm).reserveFlight(123, 7);
        verify(rm).reserveCar(123, "MTL");
        verify(rm).reserveRoom(123, "MTL");

        // Capture bundle args
        ArgumentCaptor<Integer> cid = ArgumentCaptor.forClass(Integer.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Vector<String>> flights = ArgumentCaptor.forClass(Vector.class);
        ArgumentCaptor<String> loc = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Boolean> car = ArgumentCaptor.forClass(Boolean.class);
        ArgumentCaptor<Boolean> room = ArgumentCaptor.forClass(Boolean.class);

        verify(rm).bundle(cid.capture(), flights.capture(), loc.capture(), car.capture(), room.capture());
        assertEquals(123, cid.getValue());
        assertEquals(java.util.List.of("11", "12"), flights.getValue());
        assertEquals("MTL", loc.getValue());
        assertEquals(Boolean.TRUE, car.getValue());
        assertEquals(Boolean.FALSE, room.getValue());

        String s = out.toString();
        assertTrue(s.contains("Flight Reserved"));
        assertTrue(s.contains("Car could not be reserved"));
        assertTrue(s.contains("Room Reserved"));
        assertTrue(s.contains("Bundle Reserved"));
    }

    @Test
    void bundle_tooFewArgs_printsHelpfulError_andNoServiceCall() throws Exception {
        IResourceManagerService rm = mock(IResourceManagerService.class);
        Client c = new Client(rm, () -> {
        });
        c.execute(Command.Bundle, v("Bundle", "123", "MTL", "true")); // < 6 args
        verifyNoInteractions(rm);
        assertTrue(err.toString().toLowerCase().contains("bundle command expects"));
    }

    @Test
    void nonIntegerArgument_triggersNumberFormatException_andNoServiceCall() {
        IResourceManagerService rm = mock(IResourceManagerService.class);
        Client c = new Client(rm, () -> {
        });
        assertThrows(NumberFormatException.class,
                () -> c.execute(Command.ReserveFlight, v("ReserveFlight", "abc", "7")));
        verifyNoInteractions(rm);
    }

    @Test
    void parse_splitsByComma_andTrims() {
        Vector<String> args = Client.parse("AddFlight,  1 , 200,  300 ");
        assertEquals(4, args.size());
        assertEquals("AddFlight", args.get(0));
        assertEquals("1", args.get(1));
        assertEquals("200", args.get(2));
        assertEquals("300", args.get(3));
    }

    @Test
    void checkArgumentsCount_passesOrThrowsWithHelpfulMessage() {
        assertDoesNotThrow(() -> Client.checkArgumentsCount(3, 3));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Client.checkArgumentsCount(4, 2));
        assertTrue(ex.getMessage().toLowerCase().contains("invalid number of arguments"));
    }

    @Test
    void toInt_and_toBoolean_coverEdgeCases() {
        assertEquals(42, Client.toInt("42"));
        assertThrows(NumberFormatException.class, () -> Client.toInt("4.2"));
        assertTrue(Client.toBoolean("y"));
        assertTrue(Client.toBoolean("Y"));
        assertFalse(Client.toBoolean("n"));
        assertThrows(IllegalArgumentException.class, () -> Client.toBoolean("1")); // Boolean.valueOf("1") == false
    }

    @Test
    void connectServer_invokesReconnectCallback() {
        IResourceManagerService rm = mock(IResourceManagerService.class);
        final boolean[] called = { false };
        Client c = new Client(rm, () -> called[0] = true);
        c.connectServer();
        assertTrue(called[0], "reconnect callback should be invoked");
    }
}
