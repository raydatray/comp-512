package server.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ReservedItemTest {

    @Test
    void testReservedItemCreation() {
        ReservedItem item = new ReservedItem("flight-123", "123", 2, 200);

        assertEquals("flight-123", item.getReservableItemKey());
        assertEquals("123", item.getLocation());
        assertEquals(2, item.getCount());
        assertEquals(200, item.getPrice());
        assertEquals("flight-123", item.getKey()); // Key same as reservable item key
    }

    @Test
    void testSetCount() {
        ReservedItem item = new ReservedItem("car-montreal", "Montreal", 1, 50);
        item.setCount(3);
        assertEquals(3, item.getCount());
    }

    @Test
    void testSetPrice() {
        ReservedItem item = new ReservedItem("room-toronto", "Toronto", 1, 80);
        item.setPrice(90);
        assertEquals(90, item.getPrice());
    }

    @Test
    void testGetKeyLowercase() {
        ReservedItem item = new ReservedItem("FLIGHT-123", "123", 1, 200);
        assertEquals("flight-123", item.getKey());
    }

    @Test
    void testToString() {
        ReservedItem item = new ReservedItem("flight-123", "123", 2, 200);
        String str = item.toString();

        assertTrue(str.contains("hashkey='flight-123'"));
        assertTrue(str.contains("reservableItemKey='flight-123'"));
        assertTrue(str.contains("count='2'"));
        assertTrue(str.contains("price='200'"));
    }
}
