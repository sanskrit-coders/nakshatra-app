package in.bhumiputra.nakshatra.nighantu.tools.fuzzywuzzy;

import in.bhumiputra.nakshatra.nighantu.tools.fuzzywuzzy.algorithms.Utils;
import in.bhumiputra.nakshatra.nighantu.tools.fuzzywuzzy.model.ExtractedResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Extractor {

    private int cutoff;

    public Extractor() {
        this.cutoff = 0;
    }

    public Extractor(int cutoff) {
        this.cutoff = cutoff;
    }

    public Extractor with(int cutoff) {
        this.setCutoff(cutoff);
        return this;
    }

    /**
     * Returns the list of choices with their associated scores of similarity in a list
     * of {@link ExtractedResult}
     *
     * @param query The query string
     * @param choices The list of choices
     * @param func The function to apply
     * @return The list of results
     */
    public List<ExtractedResult> extractWithoutOrder(String query, Collection<String> choices, Applicable func) {

        List<ExtractedResult> yields = new ArrayList<>();

        for (String s : choices) {

            int score = func.apply(query, s);

            if (score >= cutoff) {
                yields.add(new ExtractedResult(s, score));
            }

        }

        return yields;

    }

    /**
     * Find the single best match above a score in a list of choices.
     *
     * @param query  A string to match against
     * @param choice A list of choices
     * @param func   Scoring function
     * @return An object containing the best match and it's score
     */
    public ExtractedResult extractOne(String query, Collection<String> choice, Applicable func) {

        List<ExtractedResult> extracted = extractWithoutOrder(query, choice, func);

        return Collections.max(extracted);

    }

    /**
     * Creates a <b>sorted</b> list of {@link ExtractedResult}  which contain the
     * top @param limit most similar choices
     *
     * @param query   The query string
     * @param choices A list of choices
     * @param func    The scoring function
     * @return A list of the results
     */
    public List<ExtractedResult> extractTop(String query, Collection<String> choices, Applicable func) {

        List<ExtractedResult> best = extractWithoutOrder(query, choices, func);
        Collections.sort(best, Collections.<ExtractedResult>reverseOrder());

        return best;
    }

    /**
     * Creates a <b>sorted</b> list of {@link ExtractedResult} which contain the
     * top @param limit most similar choices
     *
     * @param query   The query string
     * @param choices A list of choices
     * @param limit   Limits the number of results and speeds up
     *                the search (k-top heap sort) is used
     * @return A list of the results
     */
    public List<ExtractedResult> extractTop(String query, Collection<String> choices, Applicable func, int limit) {

        List<ExtractedResult> best = extractWithoutOrder(query, choices, func);

        List<ExtractedResult> results = Utils.findTopKHeap(best, limit);
        Collections.reverse(results);

        return results;
    }

    public int getCutoff() {
        return cutoff;
    }

    public void setCutoff(int cutoff) {
        this.cutoff = cutoff;
    }
}
