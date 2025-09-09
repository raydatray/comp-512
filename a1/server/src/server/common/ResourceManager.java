package server.common;

import interfaces.IResourceManagerService;
import java.util.Calendar;
import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceManager implements IResourceManagerService {

    private static final Logger logger = LoggerFactory.getLogger(
        ResourceManager.class
    );
    protected String m_name = "";
    protected RMHashMap m_data = new RMHashMap();

    public ResourceManager(String p_name) {
        m_name = p_name;
    }

    // Reads a data item
    protected RMItem readData(String key) {
        synchronized (m_data) {
            RMItem item = m_data.get(key);
            if (item != null) {
                return (RMItem) item.clone();
            }
            return null;
        }
    }

    // Writes a data item
    protected void writeData(String key, RMItem value) {
        synchronized (m_data) {
            m_data.put(key, value);
        }
    }

    // Remove the item out of storage
    protected void removeData(String key) {
        synchronized (m_data) {
            m_data.remove(key);
        }
    }

    // Deletes the encar item
    protected Boolean deleteItem(String key) {
        logger.debug("key={}", key);
        ReservableItem curObj = (ReservableItem) readData(key);
        // Check if there is such an item in the storage
        if (curObj == null) {
            logger.warn("failed: key={} - does not exist", key);
            return false;
        } else {
            if (curObj.getReserved() == 0) {
                removeData(curObj.getKey());
                logger.info("succeeded: key={}", key);
                return true;
            } else {
                logger.info(
                    "blocked: key={} - can't be deleted because some customers have reserved it",
                    key
                );
                return false;
            }
        }
    }

    // Query the number of available seats/rooms/cars
    protected Integer queryNum(String key) {
        logger.debug("key={}", key);
        ReservableItem curObj = (ReservableItem) readData(key);
        Integer value = 0;
        if (curObj == null) {
            logger.warn("key={} - does not exist", key);
        } else {
            value = curObj.getCount();
            logger.info("count for key={} is {}", key, value);
        }
        return value;
    }

    // Query the price of an item
    protected Integer queryPrice(String key) {
        logger.debug("key={}", key);
        ReservableItem curObj = (ReservableItem) readData(key);
        Integer value = 0;
        if (curObj == null) {
            logger.warn("key={} - does not exist", key);
        } else {
            value = curObj.getPrice();
            logger.info("cost for key={} is {}", key, value);
        }
        return value;
    }

    // Reserve an item
    protected Boolean reserveItem(
        Integer customerID,
        String key,
        String location
    ) {
        logger.debug(
            "customerId={}, key={}, location={}",
            customerID,
            key,
            location
        );

        Customer customer = (Customer) readData(Customer.getKey(customerID));
        if (customer == null) {
            logger.warn(
                "failed: customerId={}, key={}, location={} - customer does not exist",
                customerID,
                key,
                location
            );
            return false;
        }

        // Check if the item is available
        ReservableItem item = (ReservableItem) readData(key);
        if (item == null) {
            logger.warn(
                "failed: customerId={}, key={}, location={} - item does not exist",
                customerID,
                key,
                location
            );
            return false;
        } else if (item.getCount() == 0) {
            logger.warn(
                "failed: customerId={}, key={}, location={} - item out of stock",
                customerID,
                key,
                location
            );
            return false;
        } else {
            customer.reserve(key, location, item.getPrice());
            writeData(customer.getKey(), customer);

            // Decrease the number of available items in the storage
            item.setCount(item.getCount() - 1);
            item.setReserved(item.getReserved() + 1);
            writeData(item.getKey(), item);

            logger.info(
                "succeeded: customerId={}, key={}, location={}",
                customerID,
                key,
                location
            );
            return true;
        }
    }

    // Create a new flight, or add seats to existing flight
    // NOTE: if flightPrice <= 0 and the flight already exists, it maintains its
    // current price
    public Boolean addFlight(
        Integer flightNum,
        Integer flightSeats,
        Integer flightPrice
    ) {
        logger.debug(
            "flightNum={}, seats={}, price={}",
            flightNum,
            flightSeats,
            flightPrice
        );

        Flight curObj = (Flight) readData(Flight.getKey(flightNum));
        if (curObj == null) {
            // Doesn't exist yet, add it
            Flight newObj = new Flight(flightNum, flightSeats, flightPrice);
            writeData(newObj.getKey(), newObj);

            logger.info(
                "succeeded: flightNum={}, seats={}, price=${}",
                flightNum,
                flightSeats,
                flightPrice
            );
        } else {
            logger.info(
                "modifying existing: flightNum={}, currSeats={}, currPrice={}",
                flightNum,
                curObj.getCount(),
                curObj.getPrice()
            );

            // Add seats to existing flight and update the price if greater than zero
            curObj.setCount(curObj.getCount() + flightSeats);
            if (flightPrice > 0) {
                curObj.setPrice(flightPrice);
            }
            writeData(curObj.getKey(), curObj);

            logger.info(
                "modification suceeded: flightNum={}, seats={}, price=${}",
                flightNum,
                curObj.getCount(),
                flightPrice
            );
        }
        return true;
    }

    // Create a new car location or add cars to an existing location
    // NOTE: if price <= 0 and the location already exists, it maintains its current
    // price
    public Boolean addCars(String location, Integer count, Integer price) {
        logger.debug(
            "location={}, count={}, price=${}",
            location,
            count,
            price
        );

        Car curObj = (Car) readData(Car.getKey(location));
        if (curObj == null) {
            // Car location doesn't exist yet, add it
            Car newObj = new Car(location, count, price);
            writeData(newObj.getKey(), newObj);

            logger.info(
                "succeeded: location={}, count={}, price=${}",
                location,
                count,
                price
            );
        } else {
            logger.info(
                "modifying existing: location={}, currCount={}, currPrice={}",
                location,
                curObj.getCount(),
                curObj.getPrice()
            );

            // Add count to existing car location and update price if greater than zero
            curObj.setCount(curObj.getCount() + count);
            if (price > 0) {
                curObj.setPrice(price);
            }
            writeData(curObj.getKey(), curObj);

            logger.info(
                "modification succeeded: location={}, count={}, price=${}",
                location,
                curObj.getCount(),
                price
            );
        }
        return true;
    }

    // Create a new room location or add rooms to an existing location
    // NOTE: if price <= 0 and the room location already exists, it maintains its
    // current price
    public Boolean addRooms(String location, Integer count, Integer price) {
        logger.debug(
            "location={}, count={}, price=${}",
            location,
            count,
            price
        );

        Room curObj = (Room) readData(Room.getKey(location));
        if (curObj == null) {
            // Room location doesn't exist yet, add it
            Room newObj = new Room(location, count, price);
            writeData(newObj.getKey(), newObj);

            logger.info(
                "succeeded: location={}, count={}, price=${}",
                location,
                count,
                price
            );
        } else {
            logger.info(
                "modifying existing: location={}, currCount={}, currPrice={}",
                location,
                curObj.getCount(),
                curObj.getPrice()
            );

            // Add count to existing object and update price if greater than zero
            curObj.setCount(curObj.getCount() + count);
            if (price > 0) {
                curObj.setPrice(price);
            }
            writeData(curObj.getKey(), curObj);

            logger.info(
                "modification succeeded: location={}, count={}, price=${}",
                location,
                curObj.getCount(),
                price
            );
        }
        return true;
    }

    // Deletes flight
    public Boolean deleteFlight(Integer flightNum) {
        return deleteItem(Flight.getKey(flightNum));
    }

    // Delete cars at a location
    public Boolean deleteCars(String location) {
        return deleteItem(Car.getKey(location));
    }

    // Delete rooms at a location
    public Boolean deleteRooms(String location) {
        return deleteItem(Room.getKey(location));
    }

    // Returns the number of empty seats in this flight
    public Integer queryFlight(Integer flightNum) {
        return queryNum(Flight.getKey(flightNum));
    }

    // Returns the number of cars available at a location
    public Integer queryCars(String location) {
        return queryNum(Car.getKey(location));
    }

    // Returns the amount of rooms available at a location
    public Integer queryRooms(String location) {
        return queryNum(Room.getKey(location));
    }

    // Returns price of a seat in this flight
    public Integer queryFlightPrice(Integer flightNum) {
        return queryPrice(Flight.getKey(flightNum));
    }

    // Returns price of cars at this location
    public Integer queryCarsPrice(String location) {
        return queryPrice(Car.getKey(location));
    }

    // Returns room price at this location
    public Integer queryRoomsPrice(String location) {
        return queryPrice(Room.getKey(location));
    }

    public String queryCustomerInfo(Integer customerID) {
        logger.debug("customerId={}", customerID);
        Customer customer = (Customer) readData(Customer.getKey(customerID));

        if (customer == null) {
            logger.warn("customerId={} - does not exist", customerID);
            // NOTE: don't change this--WC counts on this value indicating a customer does
            // not exist...
            return "";
        } else {
            logger.info("customerId={}:\n{}", customerID, customer.getBill());
            return customer.getBill();
        }
    }

    public Integer newCustomer() {
        // Generate a globally unique ID for the new customer; if it generates
        // duplicates for you, then adjust
        Integer cid = Integer.parseInt(
            String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
            String.valueOf(Math.round(Math.random() * 100 + 1))
        );
        Customer customer = new Customer(cid);
        writeData(customer.getKey(), customer);
        logger.info("succeeded: generated customerId={}", cid);
        return cid;
    }

    public Boolean newCustomer(Integer customerID) {
        logger.debug("customerId={}", customerID);
        Customer customer = (Customer) readData(Customer.getKey(customerID));
        if (customer == null) {
            customer = new Customer(customerID);
            writeData(customer.getKey(), customer);
            logger.info("succeeded: customerId={}", customerID);
            return true;
        } else {
            logger.warn("failed: customerId={} - already exists", customerID);
            return false;
        }
    }

    public Boolean deleteCustomer(Integer customerID) {
        logger.debug("customerId={}", customerID);
        Customer customer = (Customer) readData(Customer.getKey(customerID));
        if (customer == null) {
            logger.warn("failed customerId={} - does not exist", customerID);
            return false;
        } else {
            // Increase the reserved numbers of all reservable items which the customer
            // reserved.
            RMHashMap reservations = customer.getReservations();
            for (String reservedKey : reservations.keySet()) {
                ReservedItem reserveditem = customer.getReservedItem(
                    reservedKey
                );
                logger.info(
                    "customerId {} has reserved {} {} times",
                    customerID,
                    reserveditem.getKey(),
                    reserveditem.getCount()
                );
                ReservableItem item = (ReservableItem) readData(
                    reserveditem.getKey()
                );
                logger.info(
                    "item {} is reserved {} times and still available {} times",
                    reserveditem.getKey(),
                    item.getReserved(),
                    item.getCount()
                );
                item.setReserved(item.getReserved() - reserveditem.getCount());
                item.setCount(item.getCount() + reserveditem.getCount());
                writeData(item.getKey(), item);
            }

            // Remove the customer from the storage
            removeData(customer.getKey());
            logger.info("succeeded for customerId={}", customerID);
            return true;
        }
    }

    // Adds flight reservation to this customer
    public Boolean reserveFlight(Integer customerID, Integer flightNum) {
        return reserveItem(
            customerID,
            Flight.getKey(flightNum),
            String.valueOf(flightNum)
        );
    }

    // Adds car reservation to this customer
    public Boolean reserveCar(Integer customerID, String location) {
        return reserveItem(customerID, Car.getKey(location), location);
    }

    // Adds room reservation to this customer
    public Boolean reserveRoom(Integer customerID, String location) {
        return reserveItem(customerID, Room.getKey(location), location);
    }

    // Reserve bundle
    public Boolean bundle(
        Integer customerId,
        Vector<String> flightNumbers,
        String location,
        Boolean car,
        Boolean room
    ) {
        return false;
    }

    public String getName() {
        return m_name;
    }
}
