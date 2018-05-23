package in.bhumiputra.nakshatra.nighantu;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import in.bhumiputra.nakshatra.nighantu.anga.Entry;
import in.bhumiputra.nakshatra.nighantu.anga.Address;
import in.bhumiputra.nakshatra.nighantu.anga.RandomBox;
import in.bhumiputra.nakshatra.nighantu.anga.Dictionary;
import in.bhumiputra.nakshatra.nighantu.anga.STuple;

/**
 * this is main class provides static helper methods for searching, and getting results from {@link Dictionaries} object.
 * {@link Dictionaries} class has wrapper methods for all fallowing methods. better use them instead of using these static methods directly.
 * see {@link Dictionaries} cl
 */

public class Search {

    public static ArrayList<STuple<ArrayList<Address>>> search(Dictionaries dictionaries, String padam, boolean pull) {

        ArrayList<STuple<ArrayList<Address>>> result= new ArrayList<>();
        ArrayList<Future<STuple<ArrayList<Address>>>> futArl= new ArrayList<>();

        ExecutorService exec= Executors.newCachedThreadPool();

        for(Dictionary nighantu: dictionaries.dictionaries()) {
            futArl.add(exec.submit(new SearchInDictionary(nighantu, padam)));
        }

        for(Future<STuple<ArrayList<Address>>> fD: futArl) {
            try {
                result.add(fD.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                result.add(new STuple<>(new ArrayList<Address>(), new ArrayList<Address>()));
            }
        }
        exec.shutdown();

        if(pull) {
            pull(dictionaries, padam, result);
        }
        return result;
    }

    private static void pull(Dictionaries nighantuvulu, String padam, ArrayList<STuple<ArrayList<Address>>> addresses) {
        HashSet<String> allParyayas= new HashSet<>();
        ArrayList<HashSet<String>> lagina= new ArrayList<>();
        int nighNo=0;

        for(STuple<ArrayList<Address>> nighB: addresses) {
            lagina.add(new HashSet<String>());
            if(nighB.second !=null) {
                for(Address chirunama: nighB.second) {
                    if(chirunama!= null) { //NOTE...
                        allParyayas.add(chirunama.padam);
                        lagina.get(nighNo).add(chirunama.padam);
                    }
                }
            }
            nighNo++;
        }
        //below no need to check if allParyayas, or nighBs are null, as they all are guarantily initialzed above. but their size may be zero.
        for(HashSet<String> nighB: lagina) {
            HashSet<String> nakalu= new HashSet<>(nighB);
            nighB.addAll(allParyayas);
            nighB.removeAll(nakalu);
        }

        nighNo=0;
        for(HashSet<String> nighB: lagina) {
            if(nighantuvulu.dictionaries().get(nighNo)!= null) {
                LinkedHashSet<Address> samiti= new LinkedHashSet<>(); //NOTTE!!! nakalulu vadakattadaniki tatkalika samiti.
                for(String pada: nighB) {
                    STuple<ArrayList<Address>> chDvayam= nighantuvulu.dictionaries().get(nighNo).index().addressesTuple(pada);
                    ArrayList<Address> kalipina= new ArrayList<>();
                    if(chDvayam!= null) {
                        if(chDvayam.first != null) {
                            kalipina.addAll(chDvayam.first);
                        }
                        if(chDvayam.second != null) {
                            kalipina.addAll(chDvayam.second);
                        }
                    }
                    //phalitam.get(nighNo).second.addAll(kalipina);
                    samiti.addAll(kalipina);
                }
                addresses.get(nighNo).second.removeAll(samiti); //nakalulu vadakattadaniki.
                addresses.get(nighNo).second.addAll(samiti);
                /*LinkedHashSet<Chirunama> tSamiti= new LinkedHashSet<>(phalitam.get(nighNo).second);
                phalitam.get(nighNo).second.clear();
                phalitam.get(nighNo).second.addAll(tSamiti);*/
            }
            addresses.get(nighNo).second.removeAll( addresses.get(nighNo).first);
            nighNo++;
        }
    }

    public static ArrayList<STuple<ArrayList<Entry>>> get(Dictionaries dictionaries, String padam, boolean pull) {

        ArrayList<STuple<ArrayList<Entry>>> aropaPhalitam= new ArrayList<>();
        ArrayList<Future<STuple<ArrayList<Entry>>>> futArl= new ArrayList<>();

        ArrayList<STuple<ArrayList<Address>>> chirunamaJabita= search(dictionaries, padam, pull);

        ExecutorService exec= Executors.newCachedThreadPool();

        int nighNo= 0;
        for(STuple<ArrayList<Address>> nighB: chirunamaJabita) {
            Dictionary nighantu= dictionaries.dictionaries().get(nighNo);
            futArl.add(exec.submit(new GetFromDictionary(nighantu, nighB)));
            nighNo++;
        }

        for(Future<STuple<ArrayList<Entry>>> fD: futArl) {
            try {
                aropaPhalitam.add(fD.get());
            } catch (InterruptedException | ExecutionException e) {
                aropaPhalitam.add(new STuple<>(new ArrayList<Entry>(), new ArrayList<Entry>()));
                e.printStackTrace();
            }
        }
        exec.shutdown();
        return aropaPhalitam;
    }

    public static String randomWord(Dictionaries dictionaries, String bhasha) {
        HashSet<RandomBox.Type<String>> padalu= new HashSet<>();
        for(Dictionary nighantu: dictionaries.dictionaries()) {
            padalu.add(new RandomBox.Type<>(nighantu.index().randomWord(), nighantu.idx().nohws())); //NOTE!!!
        }

        RandomBox<String> randomBox= new RandomBox<>(padalu);
        String randomWord= randomBox.randomValue();

        return (randomWord== null) ? "" : randomWord; //NOTE!!!
    }

    public static String nextWord(Dictionaries dictionaries, String padam) {
        String tPadam= padam;
        ArrayList<String> tPadalu= new ArrayList<>();
        ArrayList<Future<String>> futArL= new ArrayList<>();
        ExecutorService exec=Executors.newCachedThreadPool();

        for(Dictionary nigh: dictionaries.dictionaries()) {
            futArL.add(exec.submit(new NextWordFromDictionary(nigh, padam)));
        }

        for(Future<String> fs: futArL) {
            try {
                String tP = fs.get();
                if (!tP.equalsIgnoreCase(padam)) {
                    tPadalu.add(tP);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        exec.shutdown();

        if(!tPadalu.isEmpty()) {
            tPadam = tPadalu.get(0);
            for (String tP : tPadalu) {
                if (tP.compareToIgnoreCase(tPadam) <= 0) {
                    tPadam = tP;
                }
            }
        }

        return tPadam;
    }

    public static String previousWord(Dictionaries dictionaries, String padam) {
        String mPadam= padam;
        ArrayList<String> mPadalu= new ArrayList<>();
        ArrayList<Future<String>> futArL= new ArrayList<>();
        ExecutorService exec=Executors.newCachedThreadPool();

        for(Dictionary nigh: dictionaries.dictionaries()) {
            futArL.add(exec.submit(new PreviousWordFromDictionary(nigh, padam)));
        }

        for(Future<String> fs: futArL) {
            try {
                String mP = fs.get();
                if (!mP.equalsIgnoreCase(padam)) {
                    mPadalu.add(mP);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        exec.shutdown();

        if(!mPadalu.isEmpty()) {
            mPadam = mPadalu.get(0);
            for (String mP : mPadalu) {
                if (mP.compareToIgnoreCase(mPadam) >= 0) {
                    mPadam = mP;
                }
            }
        }

        return mPadam;
    }

    public static class SearchInDictionary implements Callable<STuple<ArrayList<Address>>> {
        Dictionary dictionary;
        String padam;

        public SearchInDictionary(Dictionary dictionary, String padam) {
            this.dictionary = dictionary;
            this.padam= padam;
        }

        @Override
        public STuple<ArrayList<Address>> call() throws Exception {
            STuple<ArrayList<Address>> chDvayam= new STuple<>(new ArrayList<Address>(), new ArrayList<Address>());
            if(dictionary == null) {
                return chDvayam;
            }
            chDvayam= dictionary.index.addressesTuple(padam);
            return chDvayam;
        }
    }

    public static class GetFromDictionary implements Callable<STuple<ArrayList<Entry>>> {
        Dictionary dictionary;
        STuple<ArrayList<Address>> adrTuple;

        public GetFromDictionary(Dictionary dictionary, STuple<ArrayList<Address>> adrTuple) {
            this.dictionary = dictionary;
            this.adrTuple = adrTuple;
        }

        @Override
        public STuple<ArrayList<Entry>> call() throws Exception {
            if(dictionary == null) {
                return new STuple<>(new ArrayList<Entry>(), new ArrayList<Entry>());
            }
            STuple<ArrayList<Entry>> aropaluDvayam= dictionary.nigh().entriesTuple(adrTuple);
            return aropaluDvayam;
        }
    }

    public static class NextWordFromDictionary implements Callable<String> {

        Dictionary dictionary;
        String padam;

        public NextWordFromDictionary(Dictionary dictionary, String padam) {
            this.dictionary = dictionary;
            this.padam= padam;
        }

        @Override
        public String call() throws Exception {
            if(dictionary == null) {
                return padam;
            }
            String tPadam= dictionary.index().nextWord(padam);
            return tPadam;
        }
    }

    public static class PreviousWordFromDictionary implements  Callable<String> {

        Dictionary dictionary;
        String padam;

        public PreviousWordFromDictionary(Dictionary dictionary, String padam) {
            this.dictionary = dictionary;
            this.padam= padam;
        }

        @Override
        public String call() throws Exception {
            if(dictionary == null) {
                return padam;
            }
            String mPadam= dictionary.index().previousWord(padam);
            return mPadam;
        }
    }
}
