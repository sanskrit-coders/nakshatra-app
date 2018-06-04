package in.bhumiputra.nakshatra.nighantu.anga;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import in.bhumiputra.nakshatra.nighantu.tools.RandomAccessInputStream;

import static in.bhumiputra.nakshatra.nighantu.anga.IdxL.readStringFrom;

/**
 * see {@link Syn Syn} interface documentation for this class's methods doc.
 * "N" in SynN represents Nilva(Storage). this class doesn't loads entire syn file into RAM as {@link SynL} did. instead reads from storage when needed.
 * if syn file contains toooo many words (like all possible transliterations), loading it entirely ino RAM, may not be good thing.
 * for that reason by default {@link SynN} class is used to manage syn files instead of {@link SynL}(mode 1).
 * there is a preferance option in preferances to choose loading mode.
 */

public class SynN implements Syn {

    public static final byte synWrdSep= Integer.valueOf(0).byteValue();
    private String tag= "SynN";

    public final Id id;
    public final File synF;
    public final File sindF;
    public final Ifo ifo;

    private Map<String, Map<String, LongTuple>> sind;
    private RandomAccessInputStream randSyn;
    private DataInputStream synStream;
    public boolean isNull;

    public SynN(final Id id, final File synF, final File sindF, final Ifo ifo) {
        this.id= id;
        this.synF= synF;
        this.sindF= sindF;
        this.ifo= ifo;

        this.tag= "SynN:"+ this.ifo.bookname;

        if((this.synF== null) || (this.sindF== null) || (!this.synF.canRead())) {
            this.isNull= true;
            this.sind= null;
        }
        else {
            try {
                randSyn= new RandomAccessInputStream(synF, "r");
                synStream= new DataInputStream(randSyn);
                computeInd();
            }
            catch (IOException e) { //as syn is optional, catching expression, and setting isNull to true.
                this.isNull= true;
                this.sind= null;
            }
        }
    }

    private void computeInd() throws IOException {
        this.sind= new HashMap<>();
        try(DataInputStream indStream= new DataInputStream(new BufferedInputStream(new FileInputStream(sindF)))) {
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
                    sind.put(key, new HashMap<String, LongTuple>());
                }
                sLV= indStream.readInt();
                lT= new LongTuple(indStream.readLong(), indStream.readLong());
                sind.get(key).put(Character.valueOf((char)sLV).toString(), lT);
            }
        } catch (EOFException e) {
            //
        } catch (IOException e) {
            Log.e(tag, "error in creating sind", e);
            throw e;
        }
    }

    @Override
    public Id getId() {
        return id;
    }

    @Override
    public Map<String, Map<String, LongTuple>> getSind() {
        return sind;
    }

    @Override
    public int nosyns() {
        if(this.isNull) {
            return 0;
        }
        String synwordcountS= ifo.ifoMap().get("synwordcount");
        if(synwordcountS== null) {
            return 0;
        }
        int synwordcount;
        try {
            synwordcount= Integer.valueOf(synwordcountS);
        } catch (NumberFormatException e) {
            synwordcount= 0;
        }
        return synwordcount;
    }

    @Override
    public boolean isNull() {
        return isNull;
    }

    @Override
    public Integer[] idxPointersFor(String padam) {
        if(this.isNull) {
            return null;
        }
        ArrayList<Integer> pointers= new ArrayList<>();

        LongTuple range= range(padam);
        if(range!= null) {
            pointers.addAll(searchInRange(range, padam));
        }
        Integer[] pA= new Integer[pointers.size()];
        pA= pointers.toArray(pA);
        return pA;
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
        Map<String, LongTuple> fLB= sind.get(fL);
        if(fLB!=null) {
            range= fLB.get(sL);
        }
        return range;
    }

    private ArrayList<Integer> searchInRange(LongTuple range, String padam) {
        ArrayList<Integer> tPointers= new ArrayList<>();
        long byteCount= 0L;
        int sankhya= 0;

        try {
            randSyn.seek(range.first);
            while(byteCount< range.second) {
                String pada= readStringFrom(synStream, synWrdSep);
                if(padam.equalsIgnoreCase(pada)) {
                    int tSuchika= synStream.readInt();
                    tPointers.add(tSuchika);
                    sankhya++;
                }
                else {
                    randSyn.skip(4L);
                    if(sankhya>= 1) {
                        break;
                    }
                }
                byteCount+= pada.getBytes("UTF8").length+ 1L+ 4L;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tPointers;
    }

    private LongTuple correctedRange(String padam, String referance) { //correction is , if padam has only one letter fl, and also referance, then instead of only range for sl null, the range will be total range of first letter fl, whatever be sl.
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
        Map<String, LongTuple> fLB= sind.get(fL);
        if(fLB!=null) {
            range= sind.get(fL).get(sL);
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
                else {
                    LongTuple veryFirstOne= fLB.get(keys[0]);
                    if(veryFirstOne!= null) {
                        range = new LongTuple(veryFirstOne.first, (lastOne.first + lastOne.second - veryFirstOne.first));
                    }
                }
            }
        }
        return range;
    }

    @Override
    public Set<String> normalSuggestionsFor(String padam, int printMode) {
        HashSet<String> normalSuggestions= new HashSet<>();
        if(this.isNull) {
            return normalSuggestions;
        }
        LongTuple range= correctedRange(padam, padam);
        if(range!= null) {
            normalSuggestions.addAll(suggestionsInRange(range, padam, printMode));
        }
        return normalSuggestions;
    }

    private Set<String> suggestionsInRange(LongTuple range, String padam, int printMode) {
        Set<String> suggestions= new HashSet<>();
        int maxNo= 25;

        long startAddr= range.first;
        long len= range.second;

        long byteCount= 0L;
        int sankhya= 0;

        try {
            randSyn.seek(startAddr);
            while(byteCount< len) {
                String pada= readStringFrom(synStream, synWrdSep);
                if( pada.toLowerCase().replaceAll("[ ,._-]", "").startsWith(padam.toLowerCase().replaceAll("[ ,._-]", ""))) {
                    if(!(pada.matches("^.*_[0-9].*$"))) { //to omit results with underscore,ordinal.
                        boolean newOne= suggestions.add(pada);
                        if(newOne) {
                            sankhya++;
                        }
                        if(sankhya>= maxNo) {
                            break;
                        }
                    }
                }
                randSyn.skip(4L);
                byteCount+= pada.getBytes("UTF8").length+ 1L+ 4L;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return suggestions;
    }

    @Override
    public ArrayList<FuzzySuggestion> fuzzySuggestionsFor(String padam, int kanishta) {
        ArrayList<FuzzySuggestion> fuzzySuggestions= new ArrayList<>();
        if(this.isNull) {
            return fuzzySuggestions;
        }
        ArrayList<LongTuple> ranges= fuzzyRanges(padam);
        fuzzySuggestions.addAll(fuzzySuggestionsInRanges(ranges, padam, kanishta));
        return fuzzySuggestions;
    }

    private ArrayList<LongTuple> fuzzyRanges(String padam) {
        ArrayList<LongTuple> ranges= new ArrayList<LongTuple>();
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
                    ranges.add(aRange);
                }
            }
        }
        return ranges;
    }

    private ArrayList<FuzzySuggestion> fuzzySuggestionsInRanges(ArrayList<LongTuple> ranges, String padam, int kanishta) {
        ArrayList<FuzzySuggestion> suggestions= new ArrayList<FuzzySuggestion>();
        HashSet<FuzzySuggestion> set= new HashSet<>();

        int sankhya= 0;
        for(LongTuple range: ranges) {
            if(range== null) {
                continue;
            }
            long startAddr= range.first;
            long len= range.second;
            long byteCount= 0L;

            try {
                randSyn.seek(startAddr);
                while(byteCount< len) {
                    String pada= readStringFrom(synStream, synWrdSep);
                    FuzzySuggestion fs= new FuzzySuggestion(padam, pada);
                    if(fs.isGoodFor(kanishta)) {
                        boolean newOne= set.add(fs);
                        if(newOne) {
                            sankhya++; //no importance.
                        }
                    }
                    randSyn.skip(4L);
                    byteCount+= pada.getBytes("UTF8").length+ 1L+ 4L;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        suggestions.addAll(set);
        /*Collections.sort(suggestions, new Comparator<DaggariSalaha>() {
            public int compare(DaggariSalaha ds1, DaggariSalaha ds2) {
                return -(ds1.ratio - ds2.ratio);
            }
        });*/ // will be sorted when needed, instead of pre sorting.
        return suggestions;
    }
}
