package server.common;

public class ReservedItem extends RMItem {

    private Integer m_nCount;
    private Integer m_nPrice;
    private String m_strReservableItemKey;
    private String m_strLocation;

    ReservedItem(String key, String location, Integer count, Integer price) {
        super();
        m_strReservableItemKey = key;
        m_strLocation = location;
        m_nCount = count;
        m_nPrice = price;
    }

    public String getReservableItemKey() {
        return m_strReservableItemKey;
    }

    public String getLocation() {
        return m_strLocation;
    }

    public void setCount(Integer count) {
        m_nCount = count;
    }

    public Integer getCount() {
        return m_nCount;
    }

    public void setPrice(Integer price) {
        m_nPrice = price;
    }

    public Integer getPrice() {
        return m_nPrice;
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
