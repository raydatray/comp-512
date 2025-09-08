package server.common;

public class Flight extends ReservableItem {
    public Flight(Integer flightNum, Integer flightSeats, Integer flightPrice) {
        super(Integer.valueOf(flightNum).toString(), flightSeats, flightPrice);
    }

    public String getKey() {
        return Flight.getKey(Integer.parseInt(getLocation()));
    }

    public static String getKey(Integer flightNum) {
        String s = "flight-" + flightNum;
        return s.toLowerCase();
    }
}
