package server.common;

public class Car extends ReservableItem {

    public Car(String location, Integer count, Integer price) {
        super(location, count, price);
    }

    public String getKey() {
        return Car.getKey(getLocation());
    }

    public static String getKey(String location) {
        String s = "car-" + location;
        return s.toLowerCase();
    }
}
