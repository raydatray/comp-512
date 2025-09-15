package server.common;

import java.io.Serializable;

public abstract class RMItem implements Cloneable, Serializable {

    RMItem() {
        super();
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException exception) {
            return null;
        }
    }
}
