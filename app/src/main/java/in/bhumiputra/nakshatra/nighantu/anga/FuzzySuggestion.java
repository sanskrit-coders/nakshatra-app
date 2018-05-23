package in.bhumiputra.nakshatra.nighantu.anga;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import in.bhumiputra.nakshatra.nighantu.tools.fuzzywuzzy.*;

public class FuzzySuggestion implements Comparable<FuzzySuggestion> {
    public static final int mode= 0;
    public static final int ratioLowerLimit = 60;
    public final String padam;
    public final String suggestion;
    public final int ratio;
    public final boolean isGood;
    
    public FuzzySuggestion(String padam, String suggestion) {
        this.padam= padam;
        this.suggestion = suggestion;
        this.ratio = Math.abs(FuzzySearch.ratio(padam.toLowerCase(), suggestion.toLowerCase()));
        this.isGood = this.ratio >= ratioLowerLimit;
    }
    
    public final String padam() {
        return this.padam;
    }
    public final String suggestion() {
        return this.suggestion;
    }
    public final int ratio() {
        return this.ratio;
    }
    public final boolean isGood() {
        return this.isGood;
    }
    public final boolean isGoodFor(int kanishta) {
        return (this.ratio >= kanishta);
    }
    public final boolean isHamming() {
        return (this.padam.length()== this.suggestion.length());
    }
    
    @Override
    public boolean equals(Object obj) {
        boolean equality= false;
        if(obj instanceof FuzzySuggestion) {
            FuzzySuggestion dSalaha2= (FuzzySuggestion)obj;
            equality= (this.padam.equals(dSalaha2.padam) && this.suggestion.equals(dSalaha2.suggestion));
        }
        return equality;
    }
    
    @Override
    public int hashCode() {
        return (this.padam.hashCode() ^ this.suggestion.hashCode());
    }
    
    public final int compareTo(FuzzySuggestion dSalaha2) {
        return -(this.ratio - dSalaha2.ratio); // this order is for dessending order. i.e when a sort method called, it will sort from high ratio to low ratio.
    }
    
    public final String toString() {
        String str= "{"+ this.padam+ ","+ this.suggestion + ","+ this.ratio + "}";
        return str;
    }

    public static final void sort(List<FuzzySuggestion> list, int krama) {
        /*
        krama= 0: ascending;
        krama= 1: descending;
         */
        final int sign= (krama== 0) ? 1 : -1 ;
        Collections.sort(list, new Comparator<FuzzySuggestion>() {
            @Override
            public int compare(FuzzySuggestion ds1, FuzzySuggestion ds2) {
                return sign*(ds1.ratio - ds2.ratio);
            }
        });
    }
}


