package server.common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;;

class CarTest {

    @Test
    void testCarCreation() {
        Car car = new Car("Montreal", 10, 50);

        assertEquals("Montreal", car.getLocation());
        assertEquals(10, car.getCount());
        assertEquals(50, car.getPrice());
        assertEquals(0, car.getReserved());
    }

    @Test
    void testGetKey() {
        Car car = new Car("Montreal", 10, 50);
        assertEquals("car-montreal", car.getKey());
    }

    @Test
    void testGetKeyStatic() {
        assertEquals("car-montreal", Car.getKey("Montreal"));
        assertEquals("car-new york", Car.getKey("New York"));
    }

    @Test
    void testKeyIsLowercase() {
        Car car = new Car("MONTREAL", 10, 50);
        assertEquals("car-montreal", car.getKey());
    }

    @Test
    void testKeyWithSpaces() {
        Car car = new Car("New York", 5, 100);
        assertEquals("car-new york", car.getKey());
    }

    @Test
    void testClone() {
        Car original = new Car("Montreal", 10, 50);
        original.setReserved(5);

        Car cloned = (Car) original.clone();

        assertEquals(original.getLocation(), cloned.getLocation());
        assertEquals(original.getCount(), cloned.getCount());
        assertEquals(original.getPrice(), cloned.getPrice());
        assertEquals(original.getReserved(), cloned.getReserved());

        // Verify they are different objects
        assertNotSame(original, cloned);

        // Verify changes to one don't affect the other
        cloned.setCount(20);
        assertEquals(10, original.getCount());
    }
}