package in.bhumiputra.nakshatra.nighantu.anga;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * this class is wrapper around {@link Idx} and {@link Syn} objects.
 * we mainly use this class for all searching and suggestions purposes. it takes consideration about synonyms too.
 */

public class Index {
    private final Id id;
    private final Idx IDX;
    private final Syn SYN;
    private final int noWs;

    public Index(final Id id, final Idx IDX, final Syn SYN) {
        this.id= id;
        this.IDX= IDX;
        this.SYN= SYN;
        this.noWs= this.IDX.nohws()+ this.SYN.nosyns();
    }

    //gets for major fields...
    public final Id getId() {
        return this.id;
    }

    public final Idx idx() {
        return this.IDX;
    }

    public final Syn syn() {
        return this.SYN;
    }

    public final int nows() {
        return this.noWs;
    }


    /**
     * for a word, it computes all adresses, and arrange them in two blocks headers and synonyms.
     * @param padam a word
     * @return a {@link STuple} of two {@link ArrayList Arraylists} each representing headers and synonym blocks respectively.
     */
    public final STuple<ArrayList<Address>> addressesTuple(String padam) {
        ArrayList<Address> shiraB= this.IDX.addressesFor(padam);
        ArrayList<Address> paryayaB= new ArrayList<>();
        Integer[] paryayaPointers= this.SYN.idxPointersFor(padam);
        if(paryayaPointers!=null) {
            paryayaB.addAll(addressesAtIdxPointers(paryayaPointers));
        }
        STuple<ArrayList<Address>> dvayam= new STuple<>(shiraB, paryayaB);
        return dvayam;
    }

    public final ArrayList<Address> addressesAtIdxPointers(Integer[] pointers) {
        ArrayList<Address> addresses= new ArrayList<>();
        if(pointers!=null) {
            for(Integer pointer: pointers) {
                addresses.add(this.IDX.addressAt(pointer.intValue()));
            }
        }
        return addresses;
    }

    /**
     * gives a set of suggestions starting with given word from both {@link Idx} and {@link Syn}. commonly used for Auto complete suggestions.
     * @param padam prefix word with which suggestions should startsWith.
     * @param printMode ignore it. give default '0'.
     * @return a set of normal suggestions startingWith given padam.
     */
    public final Set<String> normalSuggestionsFor(String padam, int printMode){
        Set<String> normalSuggestions= new HashSet<>();
        normalSuggestions.addAll(IDX.normalSuggestionsFor(padam, printMode));
        normalSuggestions.addAll(SYN.normalSuggestionsFor(padam, printMode));
        return normalSuggestions;
    }

    /**
     * gives a set of suggestions which are very close to given word in spelling(lexicographically). considers both {@link Idx} and {@link Syn} objects.
     * @param padam word for which fuzzy suzzestions needed.
     * @param kanishta minimum(lower limit) percentage of match(close ness). (100 means exact match). ideal minimum is 60% for
     *                best results.
     * @returnan {@link java.util.ArrayList ArrayList} of {@link FuzzySuggestion FuzzySuggestion} objects, whose closeness to {@param padam}
     * is greater than {@param kanishta}.
     */
    public final ArrayList<FuzzySuggestion> fuzzySuggestionsFor(String padam, int kanishta){
        ArrayList<FuzzySuggestion> fuzzySuggestions= new ArrayList<>();
        HashSet<FuzzySuggestion> samiti= new HashSet<>();
        samiti.addAll(IDX.fuzzySuggestionsFor(padam, kanishta));
        samiti.addAll(SYN.fuzzySuggestionsFor(padam, kanishta));
        fuzzySuggestions.addAll(samiti);
        /*Collections.sort(suggestions, new Comparator<DaggariSalaha>() {
                public int compare(DaggariSalaha ds1, DaggariSalaha ds2) {
                    return -(ds1.ratio - ds2.ratio); //"-" is for giving dessending order. i.e. from more closeness to less closeness.
                }
        });*/ //will be sorted when needed, instead of pre sorting.
        return fuzzySuggestions;
    }


    /**
     *
     * @return a random word.
     */
    public final String randomWord(){
        String idxPadam= IDX.randomWord();
        return idxPadam;
    }

    /**
     * alphabetically next word with in this idx file
     * @param padam a word
     * @return alphabetically next(gretater) word to padam. even if padam is not in idx file, returns a word which is just greate than padam in lexicographical value.
     */
    public final String nextWord(String padam) {
        if((padam== null) || (padam.isEmpty())) {
            return padam;
        }

        return IDX.nextWord(padam);
    }

    /**
     * alphabetically previous word with in this idx file
     * @param padam a word
     * @return alphabetically previous(lesser) word to padam. even if padam is not in idx file, returns a word which is just lesser than padam in lexicographical value.
     */
    public final String previousWord(String padam) {
        if((padam== null) || (padam.isEmpty())) {
            return padam;
        }

        return IDX.previousWord(padam);
    }
}
