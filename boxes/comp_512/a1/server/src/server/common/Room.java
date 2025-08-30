package server.common;

public class Room extends ReservableItem {
    public Room(String location, Integer count, Integer price) {
        super(location, count, price);
    }

    public String getKey() {
        return Room.getKey(getLocation());
    }

    public static String getKey(String location) {
        String s = "room-" + location;
        return s.toLowerCase();
    }
}
