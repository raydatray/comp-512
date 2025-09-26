package server.common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class RoomTest {

    @Test
    void testRoomCreation() {
        Room room = new Room("Toronto", 25, 80);

        assertEquals("Toronto", room.getLocation());
        assertEquals(25, room.getCount());
        assertEquals(80, room.getPrice());
        assertEquals(0, room.getReserved());
    }

    @Test
    void testGetKey() {
        Room room = new Room("Toronto", 25, 80);
        assertEquals("room-toronto", room.getKey());
    }

    @Test
    void testGetKeyStatic() {
        assertEquals("room-vancouver", Room.getKey("Vancouver"));
        assertEquals("room-quebec city", Room.getKey("Quebec City"));
    }

    @Test
    void testKeyIsLowercase() {
        Room room = new Room("TORONTO", 25, 80);
        assertEquals("room-toronto", room.getKey());
    }

    @Test
    void testClone() {
        Room original = new Room("Toronto", 25, 80);
        original.setReserved(3);

        Room cloned = (Room) original.clone();

        assertEquals(original.getLocation(), cloned.getLocation());
        assertEquals(original.getCount(), cloned.getCount());
        assertEquals(original.getPrice(), cloned.getPrice());
        assertEquals(original.getReserved(), cloned.getReserved());

        assertNotSame(original, cloned);
    }
}
