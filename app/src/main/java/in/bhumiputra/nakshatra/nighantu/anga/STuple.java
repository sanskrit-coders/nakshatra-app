package in.bhumiputra.nakshatra.nighantu.anga;

/**
 * Created by damodarreddy on 5/10/18.
 */

public class STuple<K> {
    public final K first;
    public final K second;

    public STuple(K first, K second) {
        this.first = first;
        this.second = second;
    }

    public K first() {
        return this.first;
    }

    public K second() {
        return this.second;
    }

    public K nth(int n) {
        if((n<=0) || (n>2)) {
            throw new ArrayIndexOutOfBoundsException(n);
        }
        return (n== 1) ? first : second;
    }

    public K nth0(int n) {
        return nth(n+1);
    }

    @Override
    public boolean equals(Object o) {
        STuple<K> o2= (STuple<K>) o;
        return this.first.equals(o2.first) && this.second.equals(o2.second);
    }
}
