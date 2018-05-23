package in.bhumiputra.nakshatra.nighantu;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import in.bhumiputra.nakshatra.nighantu.anga.FuzzySuggestion;
import in.bhumiputra.nakshatra.nighantu.anga.Dictionary;

/**
 * this is main class provides static helper methods for getting normal auto complete and fuzzy suggestions from {@link Dictionaries} object.
 * {@link Dictionaries} class has wrapper methods for all fallowing methods. better use them instead of using these static methods directly.
 * see {@link Dictionaries} class for documentation of fallowing methods.
 */

public class Suggestions {

    public static Set<String> suggestions(Dictionaries dictionaries, String padam, boolean transliterate, int printMode) {
        //TreeSet<String> suggestions= new TreeSet<>();
        Set<String> samiti= new HashSet<>();
        ExecutorService exec= Executors.newCachedThreadPool();
        ArrayList<Future<Set<String>>> futArL= new ArrayList<>();

        for(Dictionary nighantu: dictionaries.dictionaries()) {
            futArL.add(exec.submit(new SuggestionsFromDictionary(nighantu, padam, transliterate, printMode)));
        }

        for(Future<Set<String>> fH: futArL) {
            try {
                Set<String> nighSamiti = fH.get();
                if (nighSamiti!= null) {
                    samiti.addAll(nighSamiti);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        exec.shutdown();

        //suggestions.addAll(samiti);
        return samiti;
    }

    public static TreeSet<String> noSuggestions(Dictionaries dictionaries, String padam, boolean transliterate, int printMode, int noSuggestions) {
        TreeSet<String> salahalu= new TreeSet<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });
        ArrayList<String> salahaJabita= new ArrayList<>(suggestions(dictionaries, padam, transliterate, printMode));
        salahalu.addAll(salahaJabita.subList(0, Math.min(noSuggestions, salahaJabita.size())));
        return salahalu;
    }

    public static class SuggestionsFromDictionary implements Callable<Set<String>> {
        Dictionary dictionary;
        String padam;
        boolean transliterate;
        int printMode;

        public SuggestionsFromDictionary(Dictionary dictionary, String padam, boolean transliterate, int printMode) {
            this.dictionary = dictionary;
            this.padam= padam;
            this.transliterate = transliterate;
            this.printMode= printMode;
        }

        public Set<String> call() throws Exception{
            HashSet<String> salahaluFN= new HashSet<>(); //salahaluFN: suggestions from nigh;
            if(dictionary == null) {
                return salahaluFN;
            }
            if(!transliterate) {
                Set<String> salahaluNormal= dictionary.index().normalSuggestionsFor(padam, printMode);
                if(salahaluNormal!= null) {
                    salahaluFN.addAll(salahaluNormal);
                }
            }
            /*else if(transliterate) {
                HashSet<String> salahaluLM= dictionary.index().getTranslitSuggestionsFor(padam, printMode);
                if(salahaluLM!= null) {
                    salahaluFN.addAll(salahaluLM);
                }
            }*/
            return salahaluFN;
        }
    }




    public static final ArrayList<FuzzySuggestion> fuzzySuggestionsFS(Dictionaries dictionaries, String padam, int ratioLowerLimit){
        ArrayList<FuzzySuggestion> salahalu= new ArrayList<>();
        HashSet<FuzzySuggestion> samiti= new HashSet<>();
        ExecutorService exec= Executors.newCachedThreadPool();
        ArrayList<Future<ArrayList<FuzzySuggestion>>> futArL= new ArrayList<>();

        for(Dictionary nighantu: dictionaries.dictionaries()) {
            futArL.add(exec.submit(new FuzzySuggestionsFromDictionary(nighantu, padam, ratioLowerLimit)));
        }

        for(Future<ArrayList<FuzzySuggestion>> fA: futArL) {
            try {
                ArrayList<FuzzySuggestion> nighJabita = fA.get();
                if (nighJabita != null) {
                    samiti.addAll(nighJabita);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        exec.shutdown();
        salahalu.addAll(samiti);
        /*Collections.sort(suggestions, new Comparator<DaggariSalaha>() {
            public int compare(DaggariSalaha ds1, DaggariSalaha ds2) {
                return -(ds1.ratio - ds2.ratio); //"-" is for giving dessending order. i.e. from more closeness to less closeness.
            }
        });*/
        FuzzySuggestion.sort(salahalu, 1);
        return salahalu;
    }

    public static final ArrayList<String> fuzzySuggestions(Dictionaries dictionaries, String padam, int ratioLowerLimit){
        ArrayList<String> salahalu= new ArrayList<>();
        ArrayList<FuzzySuggestion> salahaluDS= new ArrayList<>();
        salahaluDS.addAll(fuzzySuggestionsFS(dictionaries, padam, ratioLowerLimit));
        for(FuzzySuggestion ds: salahaluDS) {
            String salaha= ds.suggestion;
            salahalu.add(salaha);
        }
        return salahalu;
    }

    public static final ArrayList<String> noFuzzySuggestions(Dictionaries dictionaries, String padam, int ratioLowerLimit, int noSuggestions){
        ArrayList<String> salahalu= new ArrayList<>();
        ArrayList<String> anniSalahalu= new ArrayList<>();
        anniSalahalu.addAll(fuzzySuggestions(dictionaries, padam, ratioLowerLimit));
        salahalu.addAll(anniSalahalu.subList(0, Math.min(noSuggestions, anniSalahalu.size())));
        return salahalu;
    }

    public static class FuzzySuggestionsFromDictionary implements Callable<ArrayList<FuzzySuggestion>> {
        Dictionary dictionary;
        String padam;
        int ratioLowerLimit;

        public FuzzySuggestionsFromDictionary(Dictionary dictionary, String padam, int ratioLowerLimit) {
            this.dictionary = dictionary;
            this.padam= padam;
            this.ratioLowerLimit = ratioLowerLimit;
        }

        public ArrayList<FuzzySuggestion> call() throws Exception{
            ArrayList<FuzzySuggestion> salahaluFN= new ArrayList<>();
            if(dictionary == null) {
                return salahaluFN;
            }
            ArrayList<FuzzySuggestion> dsFN= dictionary.index().fuzzySuggestionsFor(padam, ratioLowerLimit);
            if(dsFN!=null) {
                salahaluFN.addAll(dsFN);
            }
            return salahaluFN;
        }
    }
}
