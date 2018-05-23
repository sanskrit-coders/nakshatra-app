package in.bhumiputra.nakshatra.nighantu.anga;

public class Tuple<K,L> {
    public final K first;
    public final L second;
    
    public Tuple(K first, L second) {
        this.first = first;
        this.second = second;
    }
    
    public K modati() {
        return this.first;
    }
    
    public L rendava() {
        return this.second;
    }

    public Object nth(int n) {
        if(n== 1) {
            return first;
        }
        else if(n== 2) {
            return second;
        }
        return null;
    }
    
}
    
    