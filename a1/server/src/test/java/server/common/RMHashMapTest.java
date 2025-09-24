package server.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RMHashMapTest {
    private RMHashMap map;

    @BeforeEach
    void setUp() {
        map = new RMHashMap();
    }

    @Test
    void testEmptyMap() {
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
    }

    @Test
    void testPutAndGet() {
        Flight flight = new Flight(123, 150, 200);
        map.put(flight.getKey(), flight);

        RMItem retrieved = map.get("flight-123");
        assertNotNull(retrieved);
        assertTrue(retrieved instanceof Flight);
    }

    @Test
    void testPutMultipleItems() {
        Flight flight = new Flight(123, 150, 200);
        Car car = new Car("Montreal", 10, 50);

        map.put(flight.getKey(), flight);
        map.put(car.getKey(), car);

        assertEquals(2, map.size());
        assertNotNull(map.get("flight-123"));
        assertNotNull(map.get("car-montreal"));
    }

    @Test
    void testRemove() {
        Flight flight = new Flight(123, 150, 200);
        map.put(flight.getKey(), flight);

        assertEquals(1, map.size());

        RMItem removed = map.remove("flight-123");
        assertNotNull(removed);
        assertEquals(0, map.size());
        assertNull(map.get("flight-123"));
    }

    @Test
    void testClone() {
        Flight flight = new Flight(123, 150, 200);
        Car car = new Car("Montreal", 10, 50);

        map.put(flight.getKey(), flight);
        map.put(car.getKey(), car);

        RMHashMap cloned = (RMHashMap) map.clone();

        assertEquals(map.size(), cloned.size());
        assertNotSame(map, cloned);

        // Verify deep clone - items should be different objects
        RMItem originalFlight = map.get("flight-123");
        RMItem clonedFlight = cloned.get("flight-123");
        assertNotSame(originalFlight, clonedFlight);

        // Changes to clone shouldn't affect original
        cloned.remove("flight-123");
        assertEquals(2, map.size());
        assertEquals(1, cloned.size());
    }

    @Test
    void testToString() {
        Flight flight = new Flight(123, 150, 200);
        map.put(flight.getKey(), flight);

        String str = map.toString();
        assertTrue(str.contains("--- BEGIN RMHashMap ---"));
        assertTrue(str.contains("--- END RMHashMap ---"));
        assertTrue(str.contains("[KEY='flight-123']"));
    }
}
