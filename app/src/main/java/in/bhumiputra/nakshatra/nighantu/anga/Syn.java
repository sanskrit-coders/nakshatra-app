package in.bhumiputra.nakshatra.nighantu.anga;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * Created by damodarreddy on 2/26/18.
 */

public interface Syn {


    /**
     * @return unique id of {@link Dictionary Dictionary} object, to which this Idx object belongs to.
     */
    Id getId();
    /**
     *
     * @return index map.
     */
    Map<String, Map<String, LongTuple>> getSind();
    /**\
     * @return no of head words in syn file. normally same as synonym word count in ifo file.
     */
    int nosyns();

    /**
     *
     * @return true if syn file doesn't exists.
     */
    boolean isNull();

    /**
     *
     * @param padam word to be searched.
     * @return an Integer array of pointers to idx file.
     * a synonym file maps a synonym to an idx pointer to which it is synonym for.
     */
    Integer[] idxPointersFor(String padam);
    /**
     * this method is used to get syn words which starts with entered String. normally used in auto complete suggestions
     * @param padam input string to which suggestions needed.
     * @param printMode give default '0'.
     * @return a set of syn words which starts with {@param padam}.
     */
    Set<String> normalSuggestionsFor(String padam, int printMode);
    //Set<String> getTranslitSuggestionsFor(String padam, int printMode); //TobeEnabled.
    /**
     * gives suggestions which are very close to a word
     * @param padam word for which fuzzy suzzestions needed.
     * @param kanishta minimum(lower limit) percentage of match(close ness). (100 means exact match). ideal minimum is 60% for
     *                best results.
     * @return an {@link java.util.ArrayList ArrayList} of {@link FuzzySuggestion FuzzySuggestion} objects, whose closeness to {@param padam}
     * is greater than {@param kanishta}.
     */
    ArrayList<FuzzySuggestion> fuzzySuggestionsFor(String padam, int kanishta); //close words
}
