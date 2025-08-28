package Server.Common;

public class ReservedItem extends RMItem {

    private int m_nCount;
    private int m_nPrice;
    private String m_strReservableItemKey;
    private String m_strLocation;

    ReservedItem(String key, String location, int count, int price) {
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

    public void setCount(int count) {
        m_nCount = count;
    }

    public int getCount() {
        return m_nCount;
    }

    public void setPrice(int price) {
        m_nPrice = price;
    }

    public int getPrice() {
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

    public String getKey() {
        String s = getReservableItemKey();
        return s.toLowerCase();
    }
}
