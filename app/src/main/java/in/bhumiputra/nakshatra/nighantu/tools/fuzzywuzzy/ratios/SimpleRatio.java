package in.bhumiputra.nakshatra.nighantu.tools.fuzzywuzzy.ratios;

import in.bhumiputra.nakshatra.nighantu.tools.diffutils.DiffUtils;
import in.bhumiputra.nakshatra.nighantu.tools.fuzzywuzzy.Ratio;
import in.bhumiputra.nakshatra.nighantu.tools.fuzzywuzzy.StringProcessor;

public class SimpleRatio implements Ratio {

    /**
     * Computes a simple Levenshtein distance ratio between the strings
     *
     * @param s1 Input string
     * @param s2 Input string
     * @return The resulting ratio of similarity
     */
    @Override
    public int apply(String s1, String s2) {

        return (int) Math.round(100 * DiffUtils.getRatio(s1, s2));

    }

    @Override
    public int apply(String s1, String s2, StringProcessor sp) {
        return apply(sp.process(s1), sp.process(s2));
    }
}
