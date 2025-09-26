package server.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class CustomerTest {
    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer(123);
    }

    @Test
    void testCustomerCreation() {
        assertEquals(123, customer.getID());
        assertNotNull(customer.getReservations());
        assertTrue(customer.getReservations().isEmpty());
    }

    @Test
    void testGetKey() {
        assertEquals("customer-123", customer.getKey());
    }

    @Test
    void testGetKeyStatic() {
        assertEquals("customer-456", Customer.getKey(456));
        assertEquals("customer-0", Customer.getKey(0));
    }

    @Test
    void testKeyIsLowercase() {
        Customer customer = new Customer(999);
        assertEquals("customer-999", customer.getKey());
    }

    @Test
    void testSetID() {
        customer.setID(999);
        assertEquals(999, customer.getID());
        assertEquals("customer-999", customer.getKey());
    }

    @Test
    void testReserve() {
        customer.reserve("flight-123", "123", 200);

        ReservedItem item = customer.getReservedItem("flight-123");
        assertNotNull(item);
        assertEquals("flight-123", item.getReservableItemKey());
        assertEquals("123", item.getLocation());
        assertEquals(1, item.getCount());
        assertEquals(200, item.getPrice());
    }

    @Test
    void testReserveMultipleSameItem() {
        customer.reserve("car-montreal", "Montreal", 50);
        customer.reserve("car-montreal", "Montreal", 55); // Price update

        ReservedItem item = customer.getReservedItem("car-montreal");
        assertNotNull(item);
        assertEquals(2, item.getCount());
        assertEquals(55, item.getPrice()); // Latest price should override
    }

    @Test
    void testReserveDifferentItems() {
        customer.reserve("flight-123", "123", 200);
        customer.reserve("car-montreal", "Montreal", 50);

        assertEquals(2, customer.getReservations().size());
        assertNotNull(customer.getReservedItem("flight-123"));
        assertNotNull(customer.getReservedItem("car-montreal"));
    }

    @Test
    void testGetReservedItemNonExistent() {
        assertNull(customer.getReservedItem("nonexistent"));
    }

    @Test
    void testGetBillEmpty() {
        String bill = customer.getBill();
        assertEquals("Bill for customer 123\n", bill);
    }

    @Test
    void testGetBillWithReservations() {
        customer.reserve("flight-123", "123", 200);
        customer.reserve("car-montreal", "Montreal", 50);

        String bill = customer.getBill();
        assertTrue(bill.contains("Bill for customer 123"));
        assertTrue(bill.contains("1 flight-123 $200"));
        assertTrue(bill.contains("1 car-montreal $50"));
    }

    @Test
    void testGetBillWithMultipleReservations() {
        customer.reserve("room-toronto", "Toronto", 80);
        customer.reserve("room-toronto", "Toronto", 85); // Second reservation, price update

        String bill = customer.getBill();
        assertTrue(bill.contains("Bill for customer 123"));
        assertTrue(bill.contains("2 room-toronto $85"));
    }

    @Test
    void testClone() {
        customer.reserve("flight-123", "123", 200);

        Customer cloned = (Customer) customer.clone();

        assertEquals(customer.getID(), cloned.getID());
        assertEquals(customer.getReservations().size(), cloned.getReservations().size());
        assertNotSame(customer, cloned);
        assertNotSame(customer.getReservations(), cloned.getReservations());

        // Verify changes to one don't affect the other
        cloned.reserve("car-montreal", "Montreal", 50);
        assertEquals(1, customer.getReservations().size());
        assertEquals(2, cloned.getReservations().size());
    }

    @Test
    void testToString() {
        customer.reserve("flight-123", "123", 200);
        String str = customer.toString();

        assertTrue(str.contains("--- BEGIN CUSTOMER"));
        assertTrue(str.contains("key='customer-123'"));
        assertTrue(str.contains("id='123'"));
        assertTrue(str.contains("--- END CUSTOMER ---"));
    }
}