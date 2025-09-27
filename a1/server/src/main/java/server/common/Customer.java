package server.common;

public class Customer extends RMItem {

    private Integer ID;
    private RMHashMap reservations;

    public Customer(Integer id) {
        super();
        reservations = new RMHashMap();
        ID = id;
    }

    public void setID(Integer id) {
        ID = id;
    }

    public Integer getID() {
        return ID;
    }

    public void reserve(String key, String location, Integer price) {
        ReservedItem reservedItem = getReservedItem(key);
        if (reservedItem == null) {
            // Customer doesn't already have a reservation for this resource, so create a new one now
            reservedItem = new ReservedItem(key, location, 1, price);
        } else {
            reservedItem.setCount(reservedItem.getCount() + 1);
            // NOTE: latest price overrides existing price
            reservedItem.setPrice(price);
        }
        reservations.put(reservedItem.getKey(), reservedItem);
    }

    public ReservedItem getReservedItem(String key) {
        return (ReservedItem) reservations.get(key);
    }

    public String getBill() {
        String s = "Bill for customer " + ID + "\n";
        for (String key : reservations.keySet()) {
            ReservedItem item = (ReservedItem) reservations.get(key);
            s +=
                +item.getCount() +
                " " +
                item.getReservableItemKey() +
                " $" +
                item.getPrice() +
                "\n";
        }
        return s;
    }

    public String toString() {
        String ret = "--- BEGIN CUSTOMER key='";
        ret +=
            getKey() +
            "', id='" +
            getID() +
            "', reservations=>\n" +
            reservations.toString() +
            "\n";
        ret += "--- END CUSTOMER ---";
        return ret;
    }

    public static String getKey(Integer customerID) {
        String s = "customer-" + customerID;
        return s.toLowerCase();
    }

    public String getKey() {
        return Customer.getKey(getID());
    }

    public RMHashMap getReservations() {
        return reservations;
    }

    public Object clone() {
        Customer obj = (Customer) super.clone();
        obj.ID = ID;
        obj.reservations = (RMHashMap) reservations.clone();
        return obj;
    }
}
