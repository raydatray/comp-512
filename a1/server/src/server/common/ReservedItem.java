package server.common;

public class ReservedItem extends RMItem {

    private Integer nCount;
    private Integer nPrice;
    private String strReservableItemKey;
    private String strLocation;

    ReservedItem(String key, String location, Integer count, Integer price) {
        super();
        strReservableItemKey = key;
        strLocation = location;
        nCount = count;
        nPrice = price;
    }

    public String getReservableItemKey() {
        return strReservableItemKey;
    }

    public String getLocation() {
        return strLocation;
    }

    public void setCount(Integer count) {
        nCount = count;
    }

    public Integer getCount() {
        return nCount;
    }

    public void setPrice(Integer price) {
        nPrice = price;
    }

    public Integer getPrice() {
        return nPrice;
    }

    public String toString() {
        return (
            "hashkey='" +
            getKey() +
            "', reservableItemKey='" +
            getReservableItemKey() +
            "', count='" +
            getCount() +
            "', price='" +
            getPrice() +
            "'"
        );
    }

    // NOTE: hashKey is the same as the ReservableItem hashkey--this would have to
    // change if we
    // weren't lumping all reservable items under the same price...
    public String getKey() {
        String s = getReservableItemKey();
        return s.toLowerCase();
    }
}
