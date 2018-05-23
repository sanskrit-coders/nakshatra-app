package in.bhumiputra.nakshatra.nighantu.anga;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static java.lang.Math.*;

/**
 * models a random box with many colors of balls with differant frequencies.
 * and gives methods for picking a random ball(like a random word).
 * considers weights, and frequencies in picking a random ball. i.e. weighted random. if balls of one color are in high, then their chance for being random result is also high.
 */

public class RandomBox<T> {

    private final Set<Type<T>> types;
    private ArrayList<TypeRange<Type<T>>> ranges;
    private final float totalWeight;

    public static class Type<R> {
        public final R type;
        public final float weight;

        public Type(R type, float frequency) {
            this.type = type;
            this.weight = frequency;
        }

        public final R type() {
            return this.type;
        }

        public final float weight() {
            return this.weight;
        }

        public final float weightForOne(float totalWeight) {
            return (this.weight)/totalWeight;
        }
    }

    private static class TypeRange<K> {
        public final K type;
        public final Range range;

        public TypeRange(K type, float start, float end) {
            this.type = type;
            this.range = new Range(start, end);
        }

        public TypeRange(K type, Range range) {
            this.type = type;
            this.range = range;
        }
    }

    public static class Range {
        public final double start;
        public final double end;

        public Range(double start, double end) {
            this.start = start;
            this.end = end;
        }

        public final double start() {
            return this.start;
        }

        public final double end() {
            return this.end;
        }

        public boolean isInRange(double viluva) {
            return ((viluva>= start) && (viluva< end));
        }

        public boolean isInRange(double viluva, boolean modaluTo, boolean chivariTo) {
            return ((modaluTo? (viluva>= start) : (viluva> start)) && (chivariTo? (viluva<= end) : (viluva< end)));
        }

        public boolean mLoVunda(double viluva) {
            return isInRange(viluva, true, true);
        }

        public double length() {
            return abs(end - start);
        }
    }







    public RandomBox(Set<Type<T>> types) {
        this.types = types;
        this.totalWeight = totalOf(types);
        this.ranges = rangesFor(types);
    }

    @Deprecated
    public RandomBox(Collection<T> items, float weight) {
        HashSet<Type<T>> pTypes= new HashSet<>();
        for(T amsham: items) {
            pTypes.add(new Type<T>(amsham, weight));
        }
        this.types = pTypes;
        this.totalWeight = totalOf(types);
        this.ranges = rangesFor(types);
    }

    private float totalOf(Set<Type<T>> pTypes) {
        float total= 0;
        for(Type<T> kura: pTypes) {
            total+= ((kura.weight >= 0)? kura.weight : 0);
        }
        return total;
    }

    private ArrayList<TypeRange<Type<T>>> rangesFor(Set<Type<T>> pTypes) {
        float total= totalOf(pTypes);
        float start= 0;
        ArrayList<TypeRange<Type<T>>> pRanges= new ArrayList<>();
        for(Type<T> type: pTypes) {
            float len= ((type.weight >= 0)? type.weight : 0)/total;
            pRanges.add(new TypeRange<>(type, start, start+ len));
            start+= len;
        }
        return pRanges;
    }

    private float weightForOne(Type<T> kura) {
        return kura.weightForOne(totalWeight);
    }

    public Type<T> random() {
        double aValue= Math.random();
        for(TypeRange<Type<T>> kRange: ranges) {
            if(kRange.range.isInRange(aValue)) {
                return kRange.type;
            }
        }
        return null;
    }

    public T randomValue() {
        Type<T> aType= random();
        if(aType!= null) {
            return aType.type;
        }
        else {
            return null;
        }
    }

    public static RandomBox<LongTuple> randomBoxForInd(HashMap<String, HashMap<String, LongTuple>> ind) {
        HashSet<Type<LongTuple>> fsl= new HashSet<>(); //fsl: first second letters LongTuple samiti.
        for(HashMap<String, LongTuple> flP: new ArrayList<>(ind.values())) { //flP: firstLetterPette(block).
            for(LongTuple lt: new ArrayList<>(flP.values())) {
                fsl.add(new Type<>(lt, lt.second));
            }
        }
        return new RandomBox<>(fsl);
    }


}
