package in.bhumiputra.nakshatra.nighantu.anga;

/**
 * Created by damodarreddy on 2/26/18.
 */

public class IntTuple {
    public final int first;
    public final int second;

    public IntTuple(int first, int second) {
        this.first = first;
        this.second = second;
    }

    public int first() {
        return this.first;
    }

    public int second() {
        return this.second;
    }

    public String toString() {
        String str= String.format("first: %d \nsecond: %d", first, second);
        return str;
    }

    public boolean equals(IntTuple id2) {
        return ((this.first == id2.first) && (this.second == id2.second));
    }
}
