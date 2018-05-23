package in.bhumiputra.nakshatra.nighantu;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import in.bhumiputra.nakshatra.PreferanceCentral;
import in.bhumiputra.nakshatra.nighantu.anga.Entry;
import in.bhumiputra.nakshatra.nighantu.anga.Address;
import in.bhumiputra.nakshatra.nighantu.anga.FuzzySuggestion;
import in.bhumiputra.nakshatra.nighantu.anga.Id;
import in.bhumiputra.nakshatra.nighantu.anga.Dictionary;
import in.bhumiputra.nakshatra.nighantu.anga.STuple;

import static in.bhumiputra.nakshatra.IndexerActivity.indexDirectoryName;

/**
 * this is main class which loads all dictionaries as {@link Dictionary} objects.
 */

public class Dictionaries {

    private static final String tag= "Nighantuvulu";

    private final Context context;
    public final int mode;
    public final long lastModified;
    private ArrayList<Dictionary> dictionaries;

    public Dictionaries(Context context) {
        this.context= context;

        int tVidham= Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(this.context).getString(PreferanceCentral.pref_indices_loading_mode, "1"));
        if(tVidham%2== 0) {
            tVidham++; //setting idx mode to 1. TODO!!! tolaginchu, itara vidhaalaku kuda maddatu ivvu.
        }
        this.mode = tVidham;

        SQLiteOpenHelper indexDbHelper= new IndexDbHelper(this.context);
        SQLiteDatabase db= indexDbHelper.getReadableDatabase();
        this.lastModified = context.getDatabasePath(IndexDbHelper.name).lastModified();

        this.dictionaries = new ArrayList<>();
        dictionaries.addAll(getDictionaries(db));
        indexDbHelper.close();
    }


    private List<Dictionary> getDictionaries(SQLiteDatabase db) {
        List<Dictionary> tDictionaries= new ArrayList<>();
        List<DictionaryDetails> tDetails= new ArrayList<>();
        Cursor cursor= db.rawQuery("SELECT * from dictionaries;", null);
        int sankhya= cursor.getCount();
        for(int i= 0; i< sankhya; i++) {
            cursor.moveToPosition(i);
            DictionaryDetails samacharam= (new DictionaryDetails(
                    cursor.getString(1),
                    cursor.getLong(3),
                    cursor.getLong(4),
                    cursor.getLong(5),
                    cursor.getLong(6)
            ));
            if(!samacharam.hasAllMandatoryFiles()) {
                continue;
            }
            if(!samacharam.hasCorrectTimeStamps()) {
                continue;
            }
            tDetails.add(samacharam);
        }

        ArrayList<Future<Dictionary>> futArL= new ArrayList<>();
        ExecutorService exec= Executors.newCachedThreadPool();
        for(DictionaryDetails tSamacharam: tDetails) {
            File anuSanchayam= new File(context.getFilesDir(), indexDirectoryName);
            futArL.add(exec.submit(new NighantuCreater(tSamacharam, new File(anuSanchayam, tSamacharam.path.hashCode()+""))));
        }

        for(Future<Dictionary> fN: futArL) {
            Dictionary nighantu;
            try {
                nighantu= fN.get();
            } catch (ExecutionException e) {
                nighantu= null;
                e.printStackTrace();
            } catch (InterruptedException e) {
                nighantu= null;
                e.printStackTrace();
            }
            if(nighantu!= null) {
                tDictionaries.add(nighantu);
            }
        }
        exec.shutdown();
        return tDictionaries;
    }


    private class IndexDbHelper extends SQLiteOpenHelper {

        public static final String name = "indexDb";
        private static final int schema = 1;

        private final Context context;

        public IndexDbHelper(Context context) {
            super(context, name, null, schema);
            this.context= context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE dictionaries" +
                    "(id integer primary key, " +
                    "path text not null unique on conflict replace, " +
                    "hash integer not null unique on conflict replace, " +
                    "night integer default 0, " +
                    "idxt integer default 0, " +
                    "ifot integer default 0, " +
                    "synt integer default 0, " +
                    "nth integer default 0);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    private class DictionaryDetails {
        public final String path;
        public final String directory;
        public final String name;

        public final long night;
        public final long idxt;
        public final long ifot;
        public final long synt;

        public DictionaryDetails(final String path, final long night, final long idxt, final long ifot, final long synt) {
            this.path = path;
            this.directory = path.substring(0, path.lastIndexOf(File.separator));
            this.name = path.substring(path.lastIndexOf(File.separator)+ 1); //NOTE!!!

            this.night= night;
            this.idxt= idxt;
            this.ifot= ifot;
            this.synt= synt;
        }

        public boolean hasAllMandatoryFiles() {
            if(!(new File(directory, name + ".dict.dz").canRead()
                    || new File(directory, name + ".dict").canRead())) {
                return false;
            }
            if(!new File(directory, name + ".idx").canRead()) {
                return false;
            }
            if(!new File(directory, name + ".ifo").canRead()) {
                return false;
            }
            return true;
        }

        public boolean hasCorrectTimeStamps() {
            if(!((new File(directory, name + ".dict.dz").lastModified()== night)
                    || (new File(directory, name + ".dict").lastModified()== night))) {
                return false;
            }
            if(!(new File(directory, name + ".idx").lastModified()== idxt)) {
                return false;
            }
            if(!(new File(directory, name + ".ifo").lastModified()== ifot)) {
                return false;
            }
            File synF= new File(directory, name + ".syn");
            if(synF.canRead()) {
                if(!(synF.lastModified()== synt)) {
                    return false;
                }
            }
            return true;
        }
    }

    private class NighantuCreater implements Callable<Dictionary> {

        private final DictionaryDetails details;
        private final File indDirectory;

        public NighantuCreater(final DictionaryDetails details, final File indDirectory) {
            this.details = details;
            this.indDirectory = indDirectory;
        }

        @Override
        public Dictionary call() throws Exception {
            File dictionaryDirectory= new File(details.directory);
            String directoryName= dictionaryDirectory.getName();
            String name= details.name;
            boolean isCompressed= new File(dictionaryDirectory, name+ ".dict.dz").canRead();
            Dictionary nighantu;
            try {
                nighantu= new Dictionary(
                        new Id(new File(dictionaryDirectory, name+ ".dict.dz"), name),
                        dictionaryDirectory,
                        indDirectory,
                        name,
                        isCompressed,
                        mode
                );
            } catch (IOException e) {
                nighantu= null;
            }
            return nighantu;
        }
    }


    public List<Dictionary> dictionaries() {
        return Collections.unmodifiableList(dictionaries);
    }





    /*
    fallowing methods are just wrappers to static methods in classes 'Search', and 'Suggestions'
     */


    /**
     *
     * @param padam word to be searched.
     * @param pull weather to pull synonyms to a dictionary from remaining other dictionaries too.
     * @return ArrayList<STuple<ArrayList<Address>>>. this arrylist contains many blocks, each block represents results from a single dictionary. and each dictionary block (STuple) contains two inner blocks, which each representing headers and synonyms from that single dictionary. that each inner header or synonym block contains arraylist of Address objects. Ufff!!!
     */
    public final ArrayList<STuple<ArrayList<Address>>> search(String padam, boolean pull) {
        return Search.search(this, padam, pull);
    }

    /**
     *
     * @param padam word to be searched.
     * @param pull weather to pull synonyms to a dictionary from remaining other dictionaries too.
     * @return ArrayList<STuple<ArrayList<Address>>>. this arrylist contains many blocks, each block represents results from a single dictionary. and each dictionary block (STuple) contains two inner blocks, which each representing headers and synonyms from that single dictionary. that each inner header or synonym block contains arraylist of Entry objects.
     */    public final ArrayList<STuple<ArrayList<Entry>>> get(String padam, boolean pull) {
        return Search.get(this, padam, pull);
    }

    /**
     *
     * @param bhasha language of random word. to be implemented. presently no significance.
     * @return a random word from all dictionaries, considering their weightage.
     */
    public final String randomWord(String bhasha){
        return Search.randomWord(this, bhasha);
    }

    /**
     *
     * @param padam
     * @return lexicographically just greater word to given padam in all dictionaries. strictly speaking returns a word with lexicographically lowest value among all words in all dictionaries which are lexicographically greater than padam.
     */
    public final String nextWord(String padam) {
        return Search.nextWord(this, padam);
    }

    /**
     *
     * @param padam
     * @return lexicographically just lesser word to given padam in all dictionaries. strictly speaking returns a word with lexicographically greatest value among all words in all dictionaries which are lexicographically lesser than padam.
     */    public final String previousWord(String padam) {
        return Search.previousWord(this, padam);
    }


    /**
     *
     * @param padam padam
     * @param transliterate weather to give traslitered suggestions or not. presently not implemented. give false.
     * @param printMode related to transliteration. not implemented. give '0'.
     * @param noSuggestions number of suggestions.
     * @return an alphabetically sorted TreeSet of suggestions limited to maximum of given noSuggestions.
     */
    public final TreeSet<String> noSuggestions(String padam, boolean transliterate, int printMode, int noSuggestions) {
        return Suggestions.noSuggestions(this, padam, transliterate, printMode, noSuggestions);
    }

    /**
     *
     * @param padam padam
     * @param transliterate weather to give traslitered suggestions or not. presently not implemented. give false.
     * @param printMode related to transliteration. not implemented. give '0'.
     * @return an alphabetically sorted TreeSet of suggestions.
     */
    public final Set<String> suggestions(String padam, boolean transliterate, int printMode) {
        return Suggestions.suggestions(this, padam, transliterate, printMode);
    }

    /**
     *
     * @param padam padam
     * @param ratioLowerLimit minimum(lower limit) percentage of match(close ness). (100 means exact match). ideal minimum is 60% for
     *                best results.
     * @param noSuggestions number of suggestions.
     * @return an ArrayList of fuzzy suggestions whose closeness to given padam is greater than ratioLowerLimit. limited to noSuggestions
     * @throws IOException
     */
    public final ArrayList<String> noFuzzySuggestions(String padam, int ratioLowerLimit, int noSuggestions) throws IOException{
        return Suggestions.noFuzzySuggestions(this, padam, ratioLowerLimit, noSuggestions);
    }

    /**
     *
     * @param padam padam
     * @param ratioLowerLimit minimum(lower limit) percentage of match(close ness). (100 means exact match). ideal minimum is 60% for
     *                best results.
     * @return an ArrayList of fuzzy suggestion strings whose closeness to given padam is greater than ratioLowerLimit.
     * @throws IOException
     */
    public final ArrayList<String> fuzzySuggestions(String padam, int ratioLowerLimit) throws IOException{
        return Suggestions.fuzzySuggestions(this, padam, ratioLowerLimit);
    }

    /**
     *
     * @param padam padam
     * @param ratioLowerLimit minimum(lower limit) percentage of match(close ness). (100 means exact match). ideal minimum is 60% for
     *                best results.
     * @return an ArrayList of {@link FuzzySuggestion} objects, whose closeness to given padam is greater than ratioLowerLimit.
     * @throws IOException
     */
    public final ArrayList<FuzzySuggestion> fuzzySuggestionsFS(String padam, int ratioLowerLimit) throws IOException{
        return Suggestions.fuzzySuggestionsFS(this, padam, ratioLowerLimit);
    }
}
