package middleware.common;

import interfaces.IResourceManagerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Vector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class MiddlewareTest {

    IResourceManagerService flightRM;
    IResourceManagerService carRM;
    IResourceManagerService roomRM;

    Middleware mw;

    @BeforeEach
    void setUp() {
        flightRM = mock(IResourceManagerService.class);
        carRM = mock(IResourceManagerService.class);
        roomRM = mock(IResourceManagerService.class);
        mw = new Middleware("Middleware", flightRM, carRM, roomRM);
    }

    @Test
    void getName_returnsConfiguredName() {
        assertEquals("Middleware", mw.getName()); // getter maps to field
    }

    // --- Add / Delete delegation ---

    @Test
    void addFlight_goesToFlightRM_andPropagatesReturn() {
        when(flightRM.addFlight(7, 10, 200)).thenReturn(true);
        assertTrue(mw.addFlight(7, 10, 200));
        verify(flightRM).addFlight(7, 10, 200);
        verifyNoInteractions(carRM, roomRM);
    }

    @Test
    void addCars_goesToCarRM_andPropagatesReturn() {
        when(carRM.addCars("MTL", 3, 60)).thenReturn(false);
        assertFalse(mw.addCars("MTL", 3, 60));
        verify(carRM).addCars("MTL", 3, 60);
        verifyNoInteractions(flightRM, roomRM);
    }

    @Test
    void addRooms_goesToRoomRM_andPropagatesReturn() {
        when(roomRM.addRooms("YUL", 5, 80)).thenReturn(true);
        assertTrue(mw.addRooms("YUL", 5, 80));
        verify(roomRM).addRooms("YUL", 5, 80);
        verifyNoInteractions(flightRM, carRM);
    }

    @Test
    void delete_callsRightBackends_individually() {
        when(flightRM.deleteFlight(9)).thenReturn(true);
        when(carRM.deleteCars("MTL")).thenReturn(false);
        when(roomRM.deleteRooms("YUL")).thenReturn(true);

        assertTrue(mw.deleteFlight(9));
        assertFalse(mw.deleteCars("MTL"));
        assertTrue(mw.deleteRooms("YUL"));

        verify(flightRM).deleteFlight(9);
        verify(carRM).deleteCars("MTL");
        verify(roomRM).deleteRooms("YUL");
    }

    // --- Customer creation and fan-out ---

    @Test
    void newCustomer_generatesId_andCallsAllBackends_withSameId_andReturnsThatId() {
        // We can’t predict the ID, but we can capture it and ensure consistency.
        when(flightRM.newCustomer(anyInt())).thenReturn(true);
        when(carRM.newCustomer(anyInt())).thenReturn(true);
        when(roomRM.newCustomer(anyInt())).thenReturn(true);

        int cid = mw.newCustomer(); // generated inside Middleware (Calendar+random)

        ArgumentCaptor<Integer> c1 = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> c2 = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> c3 = ArgumentCaptor.forClass(Integer.class);

        verify(flightRM).newCustomer(c1.capture());
        verify(carRM).newCustomer(c2.capture());
        verify(roomRM).newCustomer(c3.capture());

        assertEquals(c1.getValue(), c2.getValue());
        assertEquals(c2.getValue(), c3.getValue());
        assertEquals(cid, c1.getValue());

        assertThat(cid).isPositive(); // Implementation produces a positive int (millis + 1..101).
    }

    @Nested
    class NewCustomerWithId {
        @Test
        void returnsTrueOnlyIfAllThreeBackendsSucceed() {
            when(flightRM.newCustomer(123)).thenReturn(true);
            when(carRM.newCustomer(123)).thenReturn(true);
            when(roomRM.newCustomer(123)).thenReturn(true);
            assertTrue(mw.newCustomer(123));

            reset(flightRM, carRM, roomRM);
            when(flightRM.newCustomer(123)).thenReturn(true);
            when(carRM.newCustomer(123)).thenReturn(false);
            // roomRM.newCustomer(123) is not evaluated if car fails (short-circuit)
            assertFalse(mw.newCustomer(123));
            verify(flightRM).newCustomer(123);
            verify(carRM).newCustomer(123);
            verifyNoInteractions(roomRM); // short-circuit on &&
        }

        @Test
        void shortCircuits_whenFirstBackendFails() {
            when(flightRM.newCustomer(999)).thenReturn(false);
            assertFalse(mw.newCustomer(999));
            verify(flightRM).newCustomer(999);
            verifyNoInteractions(carRM, roomRM); // nothing else invoked
        }
    }

    // --- Queries and prices ---

    @Test
    void simpleQueries_delegateCorrectly() {
        when(flightRM.queryFlight(5)).thenReturn(7);
        when(carRM.queryCars("MTL")).thenReturn(3);
        when(roomRM.queryRooms("YUL")).thenReturn(2);

        assertEquals(7, mw.queryFlight(5));
        assertEquals(3, mw.queryCars("MTL"));
        assertEquals(2, mw.queryRooms("YUL"));
    }

    @Test
    void priceQueries_delegateCorrectly() {
        when(flightRM.queryFlightPrice(77)).thenReturn(150);
        when(carRM.queryCarsPrice("MTL")).thenReturn(60);
        when(roomRM.queryRoomsPrice("YUL")).thenReturn(90);

        assertEquals(150, mw.queryFlightPrice(77));
        assertEquals(60, mw.queryCarsPrice("MTL"));
        assertEquals(90, mw.queryRoomsPrice("YUL"));
    }

    @Test
    void queryCustomerInfo_concatenatesInOrder_flightThenCarThenRoom() {
        when(flightRM.queryCustomerInfo(42)).thenReturn("F\n");
        when(carRM.queryCustomerInfo(42)).thenReturn("C\n");
        when(roomRM.queryCustomerInfo(42)).thenReturn("R\n");

        String info = mw.queryCustomerInfo(42);
        assertEquals("F\nC\nR\n", info); // defined as sum of the three strings in order.
    }

    // --- Reserve operations ---

    @Test
    void reserveDelegation_goesToCorrectBackend_andPropagatesReturn() {
        when(flightRM.reserveFlight(1, 10)).thenReturn(true);
        when(carRM.reserveCar(1, "MTL")).thenReturn(false);
        when(roomRM.reserveRoom(1, "YUL")).thenReturn(true);

        assertTrue(mw.reserveFlight(1, 10));
        assertFalse(mw.reserveCar(1, "MTL"));
        assertTrue(mw.reserveRoom(1, "YUL"));
    }

    // --- Bundle semantics ---

    @Test
    void bundle_noFlights_noCar_noRoom_returnsTrue() {
        Vector<String> flights = new Vector<>();
        assertTrue(mw.bundle(123, flights, "MTL", false, false));
        verifyNoInteractions(flightRM, carRM, roomRM);
    }

    @Test
    void bundle_reservesAllFlights_andCar_andRoom_onSuccess() {
        Vector<String> flights = new Vector<>();
        flights.add("11");
        flights.add("12");

        when(flightRM.reserveFlight(123, 11)).thenReturn(true);
        when(flightRM.reserveFlight(123, 12)).thenReturn(true);
        when(carRM.reserveCar(123, "MTL")).thenReturn(true);
        when(roomRM.reserveRoom(123, "MTL")).thenReturn(true);

        assertTrue(mw.bundle(123, flights, "MTL", true, true));

        verify(flightRM).reserveFlight(123, 11);
        verify(flightRM).reserveFlight(123, 12);
        verify(carRM).reserveCar(123, "MTL");
        verify(roomRM).reserveRoom(123, "MTL");
    }

    @Test
    void bundle_flightParsingError_returnsFalse_andStops() {
        Vector<String> flights = new Vector<>();
        flights.add("X"); // NumberFormatException during parse
        assertFalse(mw.bundle(999, flights, "MTL", true, true)); // returns false on Exception.

        verifyNoInteractions(carRM, roomRM);
        // flightRM.reserveFlight is never called because parse fails before call
        verifyNoInteractions(flightRM);
    }

    @Test
    void bundle_carFailure_returnsFalse_andSkipsRoom() {
        Vector<String> flights = new Vector<>();
        flights.add("21");

        when(flightRM.reserveFlight(123, 21)).thenReturn(true);
        when(carRM.reserveCar(123, "MTL")).thenReturn(false);

        assertFalse(mw.bundle(123, flights, "MTL", true, true));
        verify(flightRM).reserveFlight(123, 21);
        verify(carRM).reserveCar(123, "MTL");
        verifyNoInteractions(roomRM); // returns immediately on car failure.
    }

    @Test
    void bundle_roomFailure_returnsFalse_butFlightsAndCarWereAttempted() {
        Vector<String> flights = new Vector<>();
        flights.add("30");

        when(flightRM.reserveFlight(5, 30)).thenReturn(true);
        when(carRM.reserveCar(5, "YUL")).thenReturn(true);
        when(roomRM.reserveRoom(5, "YUL")).thenReturn(false);

        assertFalse(mw.bundle(5, flights, "YUL", true, true));

        verify(flightRM).reserveFlight(5, 30);
        verify(carRM).reserveCar(5, "YUL");
        verify(roomRM).reserveRoom(5, "YUL");
    }

    @Test
    void bundle_exceptionWhileReservingFlight_returnsFalse() {
        Vector<String> flights = new Vector<>();
        flights.add("44");

        when(flightRM.reserveFlight(8, 44)).thenThrow(new RuntimeException("boom"));

        assertFalse(mw.bundle(8, flights, "QC", false, false)); // exception → false.

        verify(flightRM).reserveFlight(8, 44);
        verifyNoInteractions(carRM, roomRM);
    }
}
