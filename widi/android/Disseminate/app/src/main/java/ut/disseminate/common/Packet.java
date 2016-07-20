package ut.disseminate.common;

import java.io.Serializable;

/**
 * Created by Aurelius on 11/4/14.
 */
public abstract class Packet implements Serializable {

    public static enum Type {
        BEACON,
        CHUNK
    }
    public Type identifier;
}
