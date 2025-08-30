package server.common;

public abstract class ReservableItem extends RMItem {
    private Integer m_nCount;
    private Integer m_nPrice;
    private Integer m_nReserved;
    private String m_location;

    public ReservableItem(String location, Integer count, Integer price) {
        super();
        m_location = location;
        m_nCount = count;
        m_nPrice = price;
        m_nReserved = 0;
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

    public void setReserved(Integer r) {
        m_nReserved = r;
    }

    public Integer getReserved() {
        return m_nReserved;
    }

    public String getLocation() {
        return m_location;
    }

    public String toString() {
        return "RESERVABLEITEM key='" + getKey() + "', location='" + getLocation() +
                "', count='" + getCount() + "', price='" + getPrice() + "'";
    }

    public abstract String getKey();

    public Object clone() {
        ReservableItem obj = (ReservableItem) super.clone();
        obj.m_location = m_location;
        obj.m_nCount = m_nCount;
        obj.m_nPrice = m_nPrice;
        obj.m_nReserved = m_nReserved;
        return obj;
    }
}
