package in.bhumiputra.nakshatra.nighantu.anga;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * This is common interface an Idx parsing class should fallow.
 * {@link IdxL IdxL} is only implementation at present.
 * we normally don't use {@link Idx} object methods directly. but instead we use {@link Index} class's methods, which wraps both {@link Idx} and {@link Syn} objects, and take consideration of synonyms etc.
 */

public interface Idx {


    /**
     * @return unique id of {@link Dictionary Dictionary} object, to which this Idx object belongs to.
     */
    Id getId();

    /**\
     * @return no of head words in idx file. normally same as word count in ifo file.
     */
    int nohws();

    /**
     *
     * @return index map.
     */
    Map<String, Map<String, LongTuple>> getIind();


    /**
     *
     * @param padam word to be searched.
     * @return an arraylist of {@link Address Address} objects for exact searched word. each {@link Address Address} object models
     * an entry in idx file, and thus contains word, word_data_offset in uncompressed dict file, word_data_len. ArrayList is used
     * because one word may have many entries.
     *
     * For note, this class doesn't take any consideration about syn file. see {@link Index Index} class for this, which wraps both {@link Idx Idx},
     * and {@link Syn Syn} objects, and give final functionality
     */
    ArrayList<Address> addressesFor(String padam);

    /**
     *
     * @param pointer pointer to an entry in idx file. normally this pointer comes from syn file. which maps a synonym to an idx pointer.
     * @return  an {@link Address Address} object at that pointer.
     */
    Address addressAt(int pointer);


    Address addressAt(Number pointer);
    Address addressAt(long pointer);


    /**
     * this method is used to get idx words which starts with entered String. normally used in auto complete suggestions
     * @param padam input string to which suggestions needed.
     * @param printMode give default '0'.
     * @return a set of idx words which starts with {@param padam}.
     */
    Set<String> normalSuggestionsFor(String padam, int printMode);
    //Set<String> getTranslitSuggestionsFor(String padam, int printMode); //ToBeEnabled.

    /**
     * gives suggestions which are very close to a word
     * @param padam word for which fuzzy suzzestions needed.
     * @param kanishta minimum(lower limit) percentage of match(close ness). (100 means exact match). ideal minimum is 60% for
     *                best results.
     * @return an {@link java.util.ArrayList ArrayList} of {@link FuzzySuggestion FuzzySuggestion} objects, whose closeness to {@param padam}
     * is greater than {@param kanishta}.
     */
    ArrayList<FuzzySuggestion> fuzzySuggestionsFor(String padam, int kanishta); //close words

    /**
     *
     * @return a random word from idx file.
     */
    String randomWord(); //random word

    /**
     * alphabetically next word with in this idx file
     * @param padam a word
     * @return alphabetically next(gretater) word to padam. even if padam is not in this file, returns a word which is just greate than padam in lexicographical value.
     */
    String nextWord(String padam); //next word to given word in alphabeticle order
    /**
     * alphabetically previous word with in this idx file
     * @param padam a word
     * @return alphabetically previous(lesser) word to padam. even if padam is not in this file, returns a word which is just lesser than padam in lexicographical value.
     */
    String previousWord(String padam); //previous word in alphabeticle order

}
