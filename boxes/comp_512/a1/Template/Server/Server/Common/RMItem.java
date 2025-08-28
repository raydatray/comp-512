package Server.Common;

import java.io.*;

public abstract class RMItem implements Serializable, Cloneable {

    RMItem() {
        super();
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
