package in.bhumiputra.nakshatra.nighantu.anga;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * see {@link Idx Idx} interface documentation for this class's methods doc.
 * "L" in IdxL represents List, i.e it loads entire idx file to a datastructue {@link SortedListsMap SortedListMap<String, IntTuple>}.
 * we cannot use normal {@link Map} or a{@link java.util.List} for fallowing reasons.
 * <ul>
 * <li>idx file can contain single word may times, pointing to differant offset in dict file. and thus not unique.</li>
 * <li>And we need to address an idx entry by it's number.(for supporting syn file)</li>
 * <li>we need mapping behaviour too along with index behaviour, and repetitions should supoorted</li>
 * </ul>
 * for reasons like above {@link SortedListsMap}is coded.
 */

public class IdxL implements Idx {


    public static final byte idxWrdSep= Integer.valueOf(0).byteValue();
    private String tag= "Idx";

    public final Id id;
    public final File idxF;
    public final File iindF;
    public final Ifo ifo;

    private Map<String, Map<String, LongTuple>> iind;
    private SortedListsMap<String, IntTuple> idxMap;

    public IdxL(final Id id, final File idxF, final File iindF, final Ifo ifo) throws IOException {
        this.id= id;
        this.idxF= idxF;
        this.iindF= iindF;
        this.ifo= ifo;

        this.tag= "Idx:"+ this.ifo.bookname;

        if(this.ifo.idxfilesize!= this.idxF.length()) {
            throw new IOException("idxfilesize doesn't match. expected: "+ this.ifo.idxfilesize+ ", actual: "+ this.idxF.length());
        }

        computeInd();
        computeSortedListsMap();

        if(this.ifo.wordcount!= idxMap.keys.size()) {
            throw new IOException("wordcount doesn't match. expected: "+ this.ifo.wordcount+ ", actual: "+ this.idxMap.keys.size());
        }
    }


    private void computeInd() throws IOException {
        this.iind= new HashMap<>();
        try(DataInputStream indStream= new DataInputStream(new BufferedInputStream(new FileInputStream(iindF)))) {
            int fLV;
            int sLV;
            LongTuple lT;
            byte c;
            String key=null;
            while(true) {
                c= indStream.readByte();
                if(c==Integer.valueOf(255).byteValue()) {
                    fLV= indStream.readInt();
                    key=Character.valueOf((char)fLV).toString();
                    iind.put(key, new HashMap<String, LongTuple>());
                }
                sLV= indStream.readInt();
                lT= new LongTuple(indStream.readLong(), indStream.readLong());
                iind.get(key).put(Character.valueOf((char)sLV).toString(), lT);
            }
        } catch (EOFException e) {
            //
        } catch (IOException e) {
            Log.e(tag, "error in creating iind", e);
            throw e;
        }
    }

    private void computeSortedListsMap() throws IOException {
        ArrayList<String> tWords= new ArrayList<>();
        ArrayList<IntTuple> tAddresses= new ArrayList<>();

        try(DataInputStream inStream= new DataInputStream(new BufferedInputStream(new FileInputStream(idxF)))) {
            try {
                while(true) {
                    String padam= readStringFrom(inStream, idxWrdSep);
                    int offset= inStream.readInt();
                    int len= inStream.readInt();
                    tWords.add(padam);
                    tAddresses.add(new IntTuple(offset, len));
                }
            } catch (EOFException e) {
                //
            } catch (IOException e) {
                Log.e(tag, "IOException 1 in computeSortedListsMap", e);
                throw e;
            }
        } catch (IOException e) {
            Log.e(tag, "IOException 2 in computeSortedListsMap", e);
            throw e;
        }
        idxMap= new SortedListsMap<>(tWords, tAddresses, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });
    }

    public static String readStringFrom(DataInputStream inStream, byte sep) throws IOException {
        if(inStream== null) {
            return null;
        }
        String str= null;
        byte[] bA= new byte[3000];
        try {
            int i= 0;
            byte c= 0;
            while((c= inStream.readByte())!= sep) {
                bA[i++]= c;
            }
            str= new String(bA, 0, i, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e("readStringFrom", "encoding error", e);
            //e.printStackTrace();
        } catch (EOFException e) {
            //
        } catch (IOException e) {
            Log.e("readStringFrom", "IOException in reading String", e);
            throw e;
            //e.printStackTrace();
        }

        return str;
    }



    @Override
    public Id getId() {
        return id;
    }

    @Override
    public int nohws() {
        return idxMap.keys.size();
    }

    @Override
    public Map<String, Map<String, LongTuple>> getIind() {
        return iind;
    }






    @Override
    public ArrayList<Address> addressesFor(String padam) {
        ArrayList<Address> addresses= new ArrayList<>();
        /*ArrayList<IntTuple> addrPairs= idxMap.getAllValuesOf(padam);
        for(IntTuple pair: addrPairs) {
            if(pair!= null) {
                addresses.add(new Address(padam, pair.first, pair.second));
            }
        }*///NOTE!!!
        ArrayList<Tuple<String, IntTuple>> addrPairs= idxMap.getAllKeyValuesOf(padam);
        for(Tuple<String, IntTuple> pair: addrPairs) {
            if(pair!= null) {
                addresses.add(new Address(
                        pair.first,
                        pair.second.first,
                        pair.second.second
                        ));
            }
        }
        return addresses;
    }

    @Override
    public Address addressAt(int pointer) {
        Address address= null;
        if((pointer < 0) || (pointer >= idxMap.keys.size())) {
            return address;
        }

        String padam= idxMap.keys.get(pointer);
        IntTuple pair= idxMap.getValue(pointer);

        if((pair!= null) && (padam!= null) ) {
            address= new Address(padam, pair.first, pair.second);
        }

        return address;
    }

    @Override
    public Address addressAt(Number pointer) {
        return addressAt(pointer.intValue());
    }

    @Override
    public Address addressAt(long pointer) {
        return addressAt(Long.valueOf(pointer).intValue());
    }





    private LongTuple range(String padam) {
        LongTuple range= null;
        String fL= null; //fL: first letter
        String sL= null;
        if(padam.length() >0) {
            fL= padam.substring(0,1).toLowerCase();
            if(padam.length() >1) {
                sL= padam.substring(1,2).toLowerCase();
            }
            else {
                sL= Character.toString((char)0);
            }
        }
        Map<String, LongTuple> fLB= iind.get(fL); //no need to check if fL is null, as HashMap allows null keys too.
        if(fLB!=null) {
            range= fLB.get(sL); //range can be null.
        }
        return range;
    }

    private LongTuple correctedRange(String padam, String referance) {
        LongTuple range= null;
        String fL= null; //fL: first letter
        String sL= null;
        if(padam.length() >0) {
            fL= padam.substring(0,1).toLowerCase();
            if(padam.length() >1) {
                sL= padam.substring(1,2).toLowerCase();
            }
            else {
                sL= Character.toString((char)0);
            }
        }
        Map<String, LongTuple> fLB= iind.get(fL); //no need to check if fL is null, as HashMap allows null keys too.
        if(fLB!=null) {
            range= iind.get(fL).get(sL);
            if(referance.length()== 1) {
                String[] keys= new String[1];
                keys= new TreeSet<>(fLB.keySet()).toArray(keys); // create copy. don't operate on view.
                LongTuple lastOne= fLB.get(keys[keys.length-1]);
                if(range!= null) {
                    range= new LongTuple(range.first, (lastOne.first +lastOne.second -range.first));
                }
            }
            else if(padam.length()== 1) {
                TreeSet<String> keySet= new TreeSet<String>(fLB.keySet()); //now keySet is not view, but a copy.
                String[] keys= new String[1];
                keys= keySet.toArray(keys);
                LongTuple lastOne= fLB.get(keys[keys.length-1]);
                if(range!= null) {
                    range= new LongTuple(range.first, (lastOne.first +lastOne.second -range.first));
                }
            }
        }
        return range;
    }

    private HashSet<String> suggestionsInRange(LongTuple range, String padam, int pm) {
        HashSet<String> suggestions= new HashSet<>();
        int maxNo= 25;
        int offset= (int) range.first;
        int len= (int) range.second;
        if(offset< 0) {
            return suggestions;
        }

        int parimanam= idxMap.keys.size();
        int pSankhya= 0;

        for(int s= 0; s< len; s++) {
            if(offset+s> parimanam) {
                break;
            }
            String pada= (String) idxMap.keys.get(offset+s);
            if( pada.toLowerCase().replaceAll("[ ,._-]", "").startsWith(padam.toLowerCase().replaceAll("[ ,._-]", ""))) {
                if(!(pada.matches("^.*_[0-9].*$"))) { //to omit results with underscore,ordinal.
                    boolean newOne= suggestions.add(pada);
                    if(newOne) {
                        pSankhya++;
                    }
                    if(pSankhya>= maxNo) {
                        break;
                    }
                }
            }
        }
        return suggestions;
    }

    @Override
    public Set<String> normalSuggestionsFor(String padam, int printMode) {
        HashSet<String> normalSuggestions= new HashSet<>();
        LongTuple range= correctedRange(padam, padam);
        if(range!= null) {
            normalSuggestions.addAll(suggestionsInRange(range, padam, printMode));
        }
        return normalSuggestions;
    }

    @Override
    public ArrayList<FuzzySuggestion> fuzzySuggestionsFor(String padam, int kanishta) {
        ArrayList<FuzzySuggestion> fuzzySuggestions= new ArrayList<>();
        ArrayList<LongTuple> ranges= fuzzyRanges(padam);
        fuzzySuggestions.addAll(fuzzySuggestionsInRanges(ranges, padam, kanishta));
        return fuzzySuggestions;
    }

    private ArrayList<LongTuple> fuzzyRanges(String padam) {
        ArrayList<LongTuple> vyaptulu= new ArrayList<LongTuple>();
        String[][] fsLetters= new String[2][];
        String fl= "";
        String sl= "";
        if(padam.length()>0) {
            fl= padam.substring(0,1);
            if(padam.length()> 1) {
                sl= padam.substring(1,2);
            }
        }

        String[] flp= new String[] {fl};
        String[] slp= new String[] {sl};
        fsLetters[0]= flp;
        fsLetters[1]= slp;


        for(String fLet: fsLetters[0]) {
            for(String sLet: fsLetters[1]) {
                LongTuple aRange= correctedRange(fLet+sLet, padam);
                if(aRange!= null) {
                    vyaptulu.add(aRange);
                }
            }
        }
        return vyaptulu;
    }

    private ArrayList<FuzzySuggestion> fuzzySuggestionsInRanges(ArrayList<LongTuple> ranges, String padam, int kanishta) {
        ArrayList<FuzzySuggestion> suggestions= new ArrayList<FuzzySuggestion>();
        HashSet<FuzzySuggestion> set= new HashSet<FuzzySuggestion>();

        int pSankhya= 0;
        int parimanam= idxMap.keys.size();
        for(LongTuple range: ranges) {
            if(range== null) {
                continue;
            }
            int offset= (int) range.first;
            int len= (int) range.second;
            if(offset< 0) {
                continue;
            }

            for(int s= 0; s< len; s++) {
                if(offset+s> parimanam) {
                    break;
                }
                String pada= (String) idxMap.keys.get(offset+s);
                FuzzySuggestion fs= new FuzzySuggestion(padam, pada);
                if(fs.isGoodFor(kanishta)) {
                    boolean newOne= set.add(fs);
                    if(newOne) {
                        pSankhya++; //no importance.
                    }
                }
            }
        }
        suggestions.addAll(set);
        /*Collections.sort(suggestions, new Comparator<DaggariSalaha>() {
            public int compare(DaggariSalaha ds1, DaggariSalaha ds2) {
                return -(ds1.ratio - ds2.ratio);
            }
        });*/ //will be sorted when needed, instead of pre sorting.
        return suggestions;
    }





    @Override
    public String randomWord() {
        int size= idxMap.keys.size();
        boolean ok= false;
        String padam= null;
        while(!ok) {
            int randomNumber= (int)Math.floor(Math.random()* size);
            padam= (String) idxMap.keys.get(randomNumber);
            if(padam!= null) {
                ok= true;
            }
        }
        return padam;
    }

    @Override
    public String nextWord(String padam) {
        return idxMap.greater(padam);
    }

    @Override
    public String previousWord(String padam) {
        return idxMap.lesser(padam);
    }
}
