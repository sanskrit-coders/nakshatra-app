package in.bhumiputra.nakshatra.nighantu.anga;

public class LongTuple {
    public final long first;
    public final long second;
    
    public LongTuple(long first, long second) {
        this.first = first;
        this.second = second;
    }
    
    public long first() {
        return this.first;
    }
    
    public long second() {
        return this.second;
    }
    
    public String toString() {
        String str= String.format("first: %d \nsecond: %d", first, second);
        return str;
    }
    
    public boolean equals(LongTuple lt2) {
        return ((this.first == lt2.first) && (this.second == lt2.second));
    }
}
    
    