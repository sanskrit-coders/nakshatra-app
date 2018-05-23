package in.bhumiputra.nakshatra.nighantu.anga;

import java.util.Collections;
import java.util.LinkedHashSet;

/**
 * Created by damodarreddy on 5/10/18.
 */

public class Bhasha {
    //TODO: just draft. info of other indian lang lipis, and categorisation, transliteration, other helpers, all should go here.
    public static final String[] angla1 = {"_","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};

    public static final LinkedHashSet<String> anglaSamiti1= new LinkedHashSet<String>();

    public static final String[] angla2={"","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};

    public static final LinkedHashSet<String> anglaSamiti2= new LinkedHashSet<String>();

    public static final String anglaStr1="_abcdefghijklmnopqrstuvwxyz";

    public static final String anglaStr2="abcdefghijklmnopqrstuvwxyz";

    public static final String anglaStr0=anglaStr1+anglaStr2;

    public static final String EN= "en";

    static {
        Collections.addAll(anglaSamiti1, angla1);
        Collections.addAll(anglaSamiti2, angla2);
    }


    public static String bhasha(String padam) {
        String bhasha="ot";
        if(padam==null) { bhasha= "00";  }//"00": padam is empty...
        else if(padam.length()==0) { bhasha= "00"; }//"00": padam is empty...
        /*else if(padam.matches("[a-zA-Z][a-zA-Z ,\\n\\r0-9-]*")) {
            bhasha= EN;
        }*/
        else if(padam.matches("[a-zA-Z].*")) {
            bhasha= EN;
        }
        else {
            bhasha= "ot";
        }
        return bhasha;
    }

}
