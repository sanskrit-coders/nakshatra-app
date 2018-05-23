package in.bhumiputra.nakshatra.nighantu.anga;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;

/**
 * model for a stardict dictionary. just computes and wraps {@link Idx}, {@link Syn}, {@link DictInterface}, {@link Ifo} objects.
 *
 */

public class Dictionary {
    public final Id id;
    public final File directoryPath;
    public final File indDirectoryPath;
    public final String nighantuPrefix;

    public final Idx idx;
    public final Syn syn;
    public final Index index;
    public final Ifo ifo;
    public final DictInterface nigh;

    public final int loadingMode;

    public Dictionary(final Id id,
                      final File directoryPath,
                      final File indDirectoryPath,
                      final String nighantuPrefix,
                      final boolean isCompressed,
                      final int loadingMode
                    ) throws IOException {
        this.id= id;
        this.directoryPath = directoryPath;
        this.indDirectoryPath = indDirectoryPath;
        this.nighantuPrefix= nighantuPrefix;
        this.loadingMode = loadingMode;

        this.ifo= new Ifo(id, new File(directoryPath, nighantuPrefix+ ".ifo"));
        this.nigh= isCompressed ? new Dz(id, new File(directoryPath, nighantuPrefix+ ".dict.dz"), this.ifo) :
                new Dict(id, new File(directoryPath, nighantuPrefix+ ".dict"), this.ifo);
        this.idx= new IdxL(id, new File(directoryPath, nighantuPrefix+ ".idx"), new File(indDirectoryPath, nighantuPrefix+ ".iind"), this.ifo);
        this.syn= ( (this.loadingMode /2== 0) ? new SynN(id, new File(directoryPath, nighantuPrefix+ ".syn"), new File(indDirectoryPath, nighantuPrefix+ ".sind0"), this.ifo)
        : new SynL(id, new File(directoryPath, nighantuPrefix+ ".syn"), new File(indDirectoryPath, nighantuPrefix+ ".sind"), this.ifo));
        this.index= new Index(id, idx, syn);
    }


    public final Id getId() {
        return id;
    }

    public final Idx idx() {
        return this.idx;
    }

    public final Syn syn() {
        return this.syn;
    }

    public final Index index() {
        return this.index;
    }

    public final DictInterface nigh() {
        return this.nigh;
    }

    public final Ifo ifo() {
        return this.ifo;
    }


    /*
    instead of fallowing methods, it is more flexible to use methods of members like idx, syn, nigh, index .. to search, get suggestions, etc. thus we can fully customise result.
    fallowing methods are just convinient methods using above member methods. these example wrapper methods do very basic things search, suggest, random, next word, previous word, etc.
    we don't use these in actual usage. because this just models one dictionary, in multiple dictionary environment we have to consider pulling synonyms for other dictionaries too.
    so we use only methods of it's members like idx, syn, index, DictInterface etc.
     */


    public STuple<ArrayList<Entry>> vetuku(String padam) throws IOException {
        STuple<ArrayList<Address>> chirunamalu= index.addressesTuple(padam);
        STuple<ArrayList<Entry>> aropalu= nigh.entriesTuple(chirunamalu);
        return aropalu;
    }

    public TreeSet<String> salahalu(String padam) { //suggestions.
        //TreeSet is used to maintain order.
        TreeSet<String> samiti= new TreeSet<>();
        samiti.addAll(index.normalSuggestionsFor(padam, 0));
        return samiti;
    }

    public ArrayList<String> daggariSalahalu(String padam, int kanishta) { //close suggestions.
        ArrayList<FuzzySuggestion> dSJabita= index.fuzzySuggestionsFor(padam, kanishta);
        FuzzySuggestion.sort(dSJabita, 1);
        ArrayList<String> dSalahalu= new ArrayList<>();
        for(FuzzySuggestion ds: dSJabita) {
            dSalahalu.add(ds.suggestion);
        }
        return dSalahalu;
    }

    public String edoOkaPadam() {
        return index.randomWord();
    }

    public String tarvatiPadam(String padam) {
        return index.nextWord(padam);
    }

    public String mundaliPadam(String padam) {
        return index.previousWord(padam);
    }
}
