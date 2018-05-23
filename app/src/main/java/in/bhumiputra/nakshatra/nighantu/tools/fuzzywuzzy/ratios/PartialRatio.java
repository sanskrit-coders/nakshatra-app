package in.bhumiputra.nakshatra.nighantu.tools.fuzzywuzzy.ratios;

import in.bhumiputra.nakshatra.nighantu.tools.diffutils.DiffUtils;
import in.bhumiputra.nakshatra.nighantu.tools.diffutils.structs.MatchingBlock;
import in.bhumiputra.nakshatra.nighantu.tools.fuzzywuzzy.Ratio;
import in.bhumiputra.nakshatra.nighantu.tools.fuzzywuzzy.StringProcessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Partial ratio of similarity
 */
public class PartialRatio implements Ratio {

    /**
     * Computes a partial ratio between the strings
     *
     * @param s1 Input string
     * @param s2 Input string
     * @return The partial ratio
     */
    @Override
    public int apply(String s1, String s2) {

        String shorter;
        String longer;

        if (s1.length() < s2.length()){

            shorter = s1;
            longer = s2;

        } else {

            shorter = s2;
            longer = s1;

        }

        MatchingBlock[] matchingBlocks = DiffUtils.getMatchingBlocks(shorter, longer);

        List<Double> scores = new ArrayList<>();

        for (MatchingBlock mb : matchingBlocks) {

            int dist = mb.dpos - mb.spos;

            int long_start = dist > 0 ? dist : 0;
            int long_end = long_start + shorter.length();

            if(long_end > longer.length()) long_end = longer.length();

            String long_substr = longer.substring(long_start, long_end);

            double ratio = DiffUtils.getRatio(shorter, long_substr);

            if (ratio > .995) {
                return 100;
            } else {
                scores.add(ratio);
            }

        }

        return (int) Math.round(100 * Collections.max(scores));

    }

    @Override
    public int apply(String s1, String s2, StringProcessor sp) {
        return apply(sp.process(s1), sp.process(s2));
    }


}
