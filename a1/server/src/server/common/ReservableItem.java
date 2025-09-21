package server.common;

public abstract class ReservableItem extends RMItem {

    private Integer nCount;
    private Integer nPrice;
    private Integer nReserved;
    private String location;

    public ReservableItem(String location, Integer count, Integer price) {
        super();
        location = location;
        nCount = count;
        nPrice = price;
        nReserved = 0;
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

    public void setReserved(Integer r) {
        nReserved = r;
    }

    public Integer getReserved() {
        return nReserved;
    }

    public String getLocation() {
        return location;
    }

    public String toString() {
        return (
            "RESERVABLEITEM key='" +
            getKey() +
            "', location='" +
            getLocation() +
            "', count='" +
            getCount() +
            "', price='" +
            getPrice() +
            "'"
        );
    }

    public abstract String getKey();

    public Object clone() {
        ReservableItem obj = (ReservableItem) super.clone();
        obj.location = location;
        obj.nCount = nCount;
        obj.nPrice = nPrice;
        obj.nReserved = nReserved;
        return obj;
    }
}
