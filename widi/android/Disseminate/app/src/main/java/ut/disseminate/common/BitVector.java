package ut.disseminate.common;

import java.io.Serializable;

public class BitVector implements Serializable {
	
	public long data;
	public BitVector(long data) {
		this.data = data;
	}
	
	public void setBit(int bit) {
		data = data | 1L << bit;
	}
	
	public boolean testBit(int bit) {
		//BigInteger.valueOf(data).testBit(bit);
		return ((data & (1L << bit)) != 0); 
	}
	
	public void unsetBit(int bit) {
		data = data & ~(1L << bit); 
	}
	
	public boolean testDiversity(BitVector otherBv) {
		return (((~data) & (otherBv.data)) != 0);
	}

    public BitVector oppositeIntersection(BitVector neighborBv) {
        return new BitVector((data) & (~neighborBv.data));
    }

    public boolean isCompleted() {
        if (data == -1L) {
            return true;
        } else {
            return false;
        }
    }
}
