package server.common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class FlightTest {

    @Test
    void testFlightCreation() {
        Flight flight = new Flight(123, 150, 200);

        assertEquals("123", flight.getLocation()); // Flight number stored as location
        assertEquals(150, flight.getCount());
        assertEquals(200, flight.getPrice());
        assertEquals(0, flight.getReserved());
    }

    @Test
    void testGetKey() {
        Flight flight = new Flight(123, 150, 200);
        assertEquals("flight-123", flight.getKey());
    }

    @Test
    void testGetKeyStatic() {
        assertEquals("flight-456", Flight.getKey(456));
        assertEquals("flight-0", Flight.getKey(0));
    }

    @Test
    void testKeyIsLowercase() {
        Flight flight = new Flight(789, 100, 300);
        assertEquals("flight-789", flight.getKey());
    }

    @Test
    void testClone() {
        Flight original = new Flight(123, 150, 200);
        original.setReserved(10);

        Flight cloned = (Flight) original.clone();

        assertEquals(original.getLocation(), cloned.getLocation());
        assertEquals(original.getCount(), cloned.getCount());
        assertEquals(original.getPrice(), cloned.getPrice());
        assertEquals(original.getReserved(), cloned.getReserved());

        assertNotSame(original, cloned);
    }
}