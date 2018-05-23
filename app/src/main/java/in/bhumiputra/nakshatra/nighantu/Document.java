package in.bhumiputra.nakshatra.nighantu;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.util.*;
import java.io.IOException;
import java.io.FileOutputStream;
import in.bhumiputra.nakshatra.nighantu.anga.*;
import in.bhumiputra.nakshatra.nighantu.anga.Dictionary;

/**
 * this class gives final HTML document of result.
 * main method ids {@link #document}. It first searches for word, and get result datastructure, and then create page from templates, and result. If there are no results, then it generates an error report with suggestions and returns that.
 */
public class Document {
    
    public static final String htmlHeader= "<head>\n<meta charset=\"UTF-8\">\n<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n";
    public static String css= "<style type=\"text/css\" media=\"all\">\n"+ Style.style(Style.CLASSIC, Style.KH_SAMANYA)+ "</style>\n"; //"<style type=\"text/css\" media=\"all\">\n"+ Style.shaili(Style.PR_SAMANYA, Style.KH_CCHETI)+ "</style>\n";
    public static String title= "<title>@@title##</title>\n"; //NOTE: changed from source.
    private static String js= "<script type=\"text/javascript\">"+ Script.modati_rata+ "</script>\n"+ Script.bayati;
    
    private static String sep1= "<div class=\"vibhagini1\"><img src=\"vibhagini1.png\" alt=\"<hr>\"></div>\n";
    private static String sep2= "<div class=\"vibhagini2\"><img src=\"vibhagini2.gif\" alt=\"<hr>\" width=\"300\"></div>\n";
    
    private static final String shiraBlockStart= "<div class=\"vibhaga shiravibhaga\" id=\"shiravibhaga\" title=\"@@SHIRA##\">\n"; //tag should be closed in implimentation.
    private static final String paryayaBlockStart= "<div class=\"vibhaga paryayavibhaga\" id=\"paryayavibhaga\" title=\"@@PARYAYA##\">\n"; //tag should be closed in implimentation.
    
    private static final String dictionaryBlockStart = "<div class=\"nighantu @@NIGH##\" id=\"@@BlockIdPrefix##\" title=\"@@DisplayName##\">\n"; //tag should be closed in implimentation.
    private static final String fromDictionary = "<p class=\"nighnundi\"  onclick=\"" + "marchu('@@nighVishayaId##', this)" + "\">"+ "@@PAIKI_ICON##"+ "from <span class=\"nighperu\">@@DisplayName##</span>&nbsp;&nbsp;"+ "</p>\n";
    private static final String dictionaryContent = "<div class=\"nighantu_vishaya\" id=\"@@BlockIdPrefix##_vishaya\">\n"; //"_" is appended to distinguish dictionaryBlockStart, and this div.; tag should be closed in implementation.
    
    private static final String entryBlockStart = "<div class=\"aropa\" id=\"@@ID##\" title=\"@@AROPAM##\">"; //tag should be closed in implimentation.
    private static final String entryHeader = "<p class=\"aropaShirshika\">"+ "@@PANULUPETTE##"+ "<span class=\"shirshika\">@@AROPAM##</span>"+ "</p><hr>";
    private static final String taskBlockStart = "<span class=\"panulupette\">";
    private static final String ttsBox = "<span class=\"ttspette\" id=\"@@ID##_TTS\" onclick=\"window.Js.speak('@@AROPAM##', 0)\">@@TTSICONSRC##</span>";
    private static final String copyBox = "<span class=\"nakalupette\" id=\"@@ID##_nakalu\" onclick=\"aropamEnchukonu(this);\">@@NAKALUICONSRC##</span>";
    
    private static final String empty = "<div class=\"khali\"></div>\n";
    private static final String bigEmpty =  "<div class=\"peddakhali\"></div>\n";
    private static final String noResultReportStart= "<div class=\"noResult\">\n<span class=\"sorryblock\"><span id=\"sorry\">@@MANNINCHU##..........</span><BR>@@PHALITALU_LEVU_MUNDU## <b>@@padam##</b> @@PHALITALU_LEVU_TARVATA##.<BR></span>\n"; //tag should be closed in implimentation.
    private static final String suggestionsBlockStart = "<div class=\"salahalu\">\n";//tag should be closed in implimentation.
    private static final String fsBlockStart = "<div class=\"rakamSalahalu daggariSalahalu\">\n<span class=\"salahaModalu\">@@DAGGARI_PADALU##: </span>\n<p class=\"salahaBlock\">"; //tag should be closed in implimentation.
    private static final String nsBlockStart = "<div class=\"rakamSalahalu prarambhaSalahalu\">\n<span class=\"salahaModalu\">@@MODALU_PADALU## @@padam##: </span>\n<p class=\"salahaBlock\">"; //tag should be closed in implimentation.
    private static final String ssBlockStart = "<div class=\"rakamSalahalu vidiSalahalu\">\n<span class=\"salahaModalu\">@@VIDI_PADALU##: </span>\n<p class=\"salahaBlock\">";//tag should be closed in implimentation.
    private static final String suggestionLink = "<a class=\"salaha\" href=\"@@ref##\">@@padam##</a>";
    
    private static final String popupBlock = "<div class=\"pelalu_div\" id=\"pelalu_bottom\"><table class=\"pelalu_table\"><tr class=\"pelalu_tr\"><td class=\"pelalu_td\" id=\"pelalu_bottom_td_vetuku\" onclick=\"window.location.assign('content://in.bhumiputra.nakshatra.akaradi/nighantu/'+ empika);\">@@vetuku## @@vetuku_img##</td><td class=\"pelalu_td\" id=\"pelalu_bottom_td_paluku\" onclick=\"window.Js.speak(empika, 0)\">@@paluku## @@paluku_img##</td></tr><!--/td--></table></div>\n";
    private static final String attributionBlock = "<div class=\"apadana\" id=\"apadana_pathyam\">Some part of this text is extracted from the <a class=\"apadana_lanke\" href=\"https://ta.wiktionary.org\">Tamil Wiktionary</a>, and thus all text in this page is avialable under the <a class=\"apadana_lanke\" href=\"http://creativecommons.org/licenses/by-sa/3.0/\">\"Creative Commons Attribution-ShareAlike License v3.0\"</a></div>\n";
    private static final String seperator = "<p class=\"vibhagini\"><span class=\"sambandhita\">@@SAMBANDHITA_PADALU##</span></p>";

    static final String ttsIcon= Vectors.speakerIconAllGrey;
    static final String searchIcon = Vectors.searchIcon;
    static final String downArrowIcon = Vectors.downArrowIcon;
    static final String upArrowIcon = Vectors.upArrowIcon;
    static final String copyIcon = Vectors.copyIcon;

    private static final int ratioLowerLimit = 60; //for daggariSalahalu.
    private static final int maxNoSuggestions = 35;
    
    private final Dictionaries dictionaries;
    private Context context;
    private String document;
    private File debugOut;

    @Deprecated
    public Document(Dictionaries dictionaries) {
        this.dictionaries = dictionaries;
    }

    public Document(Context context, Dictionaries dictionaries) {
        this.context= context;
        this.dictionaries = dictionaries;
        this.debugOut= new File(Environment.getExternalStorageDirectory(), "nighantuvulu/patra.html");
    }

    public void computeStyle(int pr) {
        css= "<style type=\"text/css\" media=\"all\">\n"+ Style.style(pr, Style.KH_SAMANYA)+ "</style>\n";
    }
    
    public final boolean checkIfEmpty(ArrayList<STuple<ArrayList<Entry>>> list, boolean checkInside) {
        boolean check= true; //list is empty indeed.
        if(!checkInside) {
            check = (list == null) || (list.size() == 0);
        }
        else {
            if((list== null) || (list.size()== 0)) {
                check= true;
            }
            else {
                for(STuple<ArrayList<Entry>> nighB: list) {
                    if(!containsEmpty(nighB)) {
                        check= false; break;
                    }
                }
            }
        }
        return check;
    }
    
    public static final boolean containsEmpty(STuple<ArrayList<Entry>> dvayam) {
        return (dvayam.first.isEmpty() && dvayam.second.isEmpty());
    }
    
    
    public final String document(String padam, boolean lagu) throws IOException{
        String patram= new String();
        ArrayList<STuple<ArrayList<Entry>>> phalitam= dictionaries.get(padam, lagu);
        int[] krama= new int[dictionaries.dictionaries().size()];
        for(int i=0; i<krama.length; i++) {
            krama[i]= i;
        }
        boolean empty= checkIfEmpty(phalitam, true);
        if(empty) {
            patram= noResultsReport(padam);
        }
        else {
            String header=header(padam);
            String pelalu= popup(padam);
            String shiraBhaga=shira(phalitam,krama);
            String paryayaBhaga=paryaya(phalitam,krama);
            String footer=footer();
            String spSeperater=""; //spSeperater:shira,paryaya seperater...
            if((shiraBhaga.isEmpty() || paryayaBhaga.isEmpty())) {
                spSeperater="";
            }
            else {
                //spSeperater= seperator.replaceAll("@@SAMBANDHITA_PADALU##", context.getString(R.string.sambandha));
                spSeperater= seperator.replaceAll("@@SAMBANDHITA_PADALU##", "Related words");
            }
            //patram="<html><body>\n" + header+ popup+ "<div class=\"patra\" id=\"patra\">\n"+ shiraBhaga+ spSeperater+ paryayaBhaga+ attributionBlock+ "</div>\n"+ footer;
            patram="<html><body>\n" + header+ pelalu+ "<div class=\"patra\" id=\"patra\">\n"+ shiraBhaga+ spSeperater+ paryayaBhaga+ "</div>\n"+ footer;
        }
        writeToDebugOut(patram);
        return patram;
    }

    public void writeToDebugOut(String patram) {
        try(FileOutputStream outStream= new FileOutputStream(debugOut)) {
            byte[] bA= patram.getBytes("UTF8");
            outStream.write(bA, 0, bA.length);
        }
        catch(Exception ioe) {}
    }

    public final String header(String padam) {
        //css= "<style type=\"text/css\" media=\"all\">\n"+ Style.shaili(Style.CLASSIC, Style.KH_SAMANYA)+ "</style>\n";
        String header=htmlHeader+title.replaceFirst("@@title##", padam)+css+js+"</head>\n";
        return header;
    }

    public final String popup(String padam) {
        /*String popup= popupBlock.replaceAll("@@vetuku##", context.getResources().getText(R.string.vetuku).toString()).replaceAll("@@paluku##", context.getResources().getText(R.string.paluku).toString())
                .replaceAll("@@vetuku_img##", searchIcon.replaceFirst("@@CLASS1##", "pelalu_img").replaceFirst("@@CLASS2##", "").replaceAll("viewBox='0 0 17 17'", "viewBox='0 0 16 16'").replaceAll("height='27' width='27'", "height='16' width='16'")).replaceAll("@@paluku_img##", ttsIcon.replaceFirst("@@CLASS1##", "pelalu_img").replaceFirst("@@CLASS2##", "").replaceAll("viewBox='0 0 17 17'", "viewBox='0 0 16 16'").replaceAll("height='27' width='27'", "height='16' width='16'"));
                */
        String popup= popupBlock.replaceAll("@@vetuku##", "Search").replaceAll("@@paluku##", "Pronounce")
                .replaceAll("@@vetuku_img##", searchIcon.replaceFirst("@@CLASS1##", "pelalu_img").replaceFirst("@@CLASS2##", "").replaceAll("viewBox='0 0 17 17'", "viewBox='0 0 16 16'").replaceAll("height='27' width='27'", "height='16' width='16'")).replaceAll("@@paluku_img##", ttsIcon.replaceFirst("@@CLASS1##", "pelalu_img").replaceFirst("@@CLASS2##", "").replaceAll("viewBox='0 0 17 17'", "viewBox='0 0 16 16'").replaceAll("height='27' width='27'", "height='16' width='16'"));
        return popup;
    }
    
    public final String shira(ArrayList<STuple<ArrayList<Entry>>> phalitam, int[] krama) {
        String shiram= "";
        if(checkIfEmpty(phalitam, true)) {
            shiram= "";
        }
        else {
            StringBuilder shira= new StringBuilder("");
            int nonEmptyNighs= 0;
            int nighNo= 0;
            nighBlock: for(STuple<ArrayList<Entry>> nighB: phalitam) {
                Dictionary tNighantu= dictionaries.dictionaries().get(nighNo);
                if(nighB.first.isEmpty()) {
                    nighNo++;
                    continue nighBlock;
                }
                else {
                    String isFirstNighClass= "";
                    if(nonEmptyNighs== 0) {
                        isFirstNighClass= " firstnigh";
                    }
                    nonEmptyNighs++;
                    ArrayList<Entry> shiraB= nighB.first;
                    String nighCode= tNighantu.directoryPath.hashCode()+ "_"+ tNighantu.nighantuPrefix;
                    String nighDisplayName= tNighantu.ifo.bookname;
                    String blockIdPrefix="shira_"+ nighCode;
                    shira.append(dictionaryBlockStart.replaceFirst("@@NIGH##", nighCode+isFirstNighClass).replaceFirst("@@BlockIdPrefix##", blockIdPrefix).replaceFirst("@@DisplayName##", nighDisplayName));
                    shira.append(fromDictionary.replaceFirst("@@DisplayName##", nighDisplayName).replaceFirst("@@nighVishayaId##", blockIdPrefix+"_vishaya").replaceFirst("@@PAIKI_ICON##", upArrowIcon));
                    shira.append(dictionaryContent.replaceFirst("@@BlockIdPrefix##", blockIdPrefix));
                    int aropaSankhya= 0;
                    for(Entry aropa: shiraB) {
                        /*String aropaId= blockIdPrefix+ "_"+ aropa.entry;
                        shira.append(entryBlockStart.replaceFirst("@@ID##", aropaId).replaceFirst("@@AROPAM##", aropa.entry));
                        */
                        String peru= aropa.entry;
                        String aropaId= blockIdPrefix+ "_"+ peru.hashCode()+ "_as"+ aropaSankhya; //NOTE.
                        shira.append(entryBlockStart.replaceFirst("@@ID##", aropaId).replaceFirst("@@AROPAM##", peru));

                        String panuluPette= taskBlockStart;
                        if(Bhasha.bhasha(peru).equals(Bhasha.EN)) {
                            panuluPette+= ttsBox.replaceAll("@@ID##", aropaId).replaceAll("@@AROPAM##", peru).replaceFirst("@@TTSICONSRC##", ttsIcon.replaceFirst("@@CLASS1##", "ttsicon").replaceFirst("@@CLASS2##", ""));
                        }
                        panuluPette+= copyBox.replaceAll("@@ID##", aropaId).replaceAll("@@NAKALUICONSRC##", copyIcon);
                        panuluPette+= "</span>";

                        shira.append(entryHeader.replaceAll("@@PANULUPETTE##", panuluPette).replaceAll("@@AROPAM##", peru));

                        String sts= tNighantu.ifo.ifoMap().get("sametypesequence");
                        boolean shouldEncloseInPre= (sts!= null) && (sts.equalsIgnoreCase("m"));
                        if(shouldEncloseInPre) {
                            //shira.append("<pre>"); //TODO
                        }
                        shira.append(aropa.description.replaceAll("\\n", "<br>"));
                        if(shouldEncloseInPre) {
                            //shira.append("</pre>"); //TODO
                        }
                        shira.append("</div>\n"); //closing <div class="aropa"> block...
                        aropaSankhya++;
                    }
                    shira.append("</div>\n</div>\n"); //closing <div class="nighantu_vishaya"> block and <div class="nighantu"> block...
                    nighNo++;
                }
            }
            if(!shira.toString().equals("")) {
                shira.insert(0,shiraBlockStart.replaceAll("@@SHIRA##", ""));
                shira.append("</div>\n"); //closing above inserted <div class="shira" id="shira"> block...
            }
            shiram=shira.toString();
        }
        return shiram;
    }
    
    public final String paryaya(ArrayList<STuple<ArrayList<Entry>>> phalitam, int[] krama) {
        String paryayam= "";
        if(checkIfEmpty(phalitam, true)) {
            paryayam= "";
        }
        else {
            StringBuilder paryaya= new StringBuilder("");
            int nonEmptyNighs= 0;
            int nighNo= 0;
            nighBlock: for(STuple<ArrayList<Entry>> nighB: phalitam) {
                Dictionary tNighantu= dictionaries.dictionaries().get(nighNo);
                if(nighB.second.isEmpty()) {
                    nighNo++;
                    continue nighBlock;
                }
                else {
                    String isFirstNighClass= "";
                    if(nonEmptyNighs== 0) {
                        isFirstNighClass= " firstnigh";
                    }
                    nonEmptyNighs++;
                    ArrayList<Entry> paryayaB= nighB.second;
                    String nighCode= tNighantu.directoryPath.hashCode()+ "_"+ tNighantu.nighantuPrefix;
                    String nighDisplayName= tNighantu.ifo.bookname;
                    String blockIdPrefix="paryaya_"+ nighCode;
                    paryaya.append(dictionaryBlockStart.replaceFirst("@@NIGH##", nighCode+isFirstNighClass).replaceFirst("@@BlockIdPrefix##", blockIdPrefix).replaceFirst("@@DisplayName##", nighDisplayName));
                    paryaya.append(fromDictionary.replaceFirst("@@DisplayName##", nighDisplayName).replaceFirst("@@nighVishayaId##", blockIdPrefix+"_vishaya").replaceFirst("@@PAIKI_ICON##", upArrowIcon));
                    paryaya.append(dictionaryContent.replaceFirst("@@BlockIdPrefix##", blockIdPrefix));
                    int aropaSankhya= 0;
                    for(Entry aropa: paryayaB) {
                        /*String aropaId= blockIdPrefix+ "_"+ aropa.entry;
                        paryaya.append(entryBlockStart.replaceFirst("@@ID##", aropaId).replaceFirst("@@AROPAM##", aropa.entry));
                        */
                        String peru= aropa.entry;
                        String aropaId= blockIdPrefix+ "_"+ peru.hashCode()+ "_as"+ aropaSankhya;; //NOTE.
                        paryaya.append(entryBlockStart.replaceFirst("@@ID##", aropaId).replaceFirst("@@AROPAM##", peru));

                        String panuluPette= taskBlockStart;
                        if(Bhasha.bhasha(peru).equals(Bhasha.EN)) {
                            panuluPette+= ttsBox.replaceAll("@@ID##", aropaId).replaceAll("@@AROPAM##", peru).replaceFirst("@@TTSICONSRC##", ttsIcon.replaceFirst("@@CLASS1##", "ttsicon").replaceFirst("@@CLASS2##", ""));
                        }
                        panuluPette+= copyBox.replaceAll("@@ID##", aropaId).replaceAll("@@NAKALUICONSRC##", copyIcon);
                        panuluPette+= "</span>";

                        paryaya.append(entryHeader.replaceAll("@@PANULUPETTE##", panuluPette).replaceAll("@@AROPAM##", peru));

                        String sts= tNighantu.ifo.ifoMap().get("sametypesequence");
                        boolean shouldEncloseInPre= (sts!= null) && (sts.equalsIgnoreCase("m"));
                        if(shouldEncloseInPre) {
                            //paryaya.append("<pre>"); //TODO
                        }
                        paryaya.append(aropa.description.replaceAll("\\n", "<br>"));
                        if(shouldEncloseInPre) {
                            //paryaya.append("</pre>"); //TODO
                        }
                        paryaya.append("</div>\n"); //closing <div class="aropa"> block...
                        aropaSankhya++;
                    }
                    paryaya.append("</div>\n</div>\n"); //closing <div class="nighantu_vishhaya"> block and <div class="nighantu"> block...
                    nighNo++;
                }
            }
            if(!paryaya.toString().equals("")) {
                paryaya.insert(0,paryayaBlockStart.replaceAll("@@PARYAYA##", ""));
                paryaya.append("</div>\n"); //closing above inserted <div class="paryaya" id="paryaya"> block...
            }
            paryayam=paryaya.toString();
        }
        return paryayam;
    }
    
    public final String footer() {
        String footer="</body></html>\n";
        return footer;
    }
    
    public final String noResultsReport(String padam) throws IOException{
        StringBuilder report=new StringBuilder("");
        //report.append( "<html><body>\n"+ header(padam)+ popup(padam)+ "<div class=\"patra\">\n"+noResultReportStart.replaceFirst("@@padam##", Html.escapeHtml(padam)).replaceFirst("@@MANNINCHU##", context.getString(R.string.kshamapanalu)).replaceFirst("@@PHALITALU_LEVU_MUNDU##", context.getString(R.string.phalitalu_levu_mundu)).replaceFirst("@@PHALITALU_LEVU_TARVATA##", context.getString(R.string.phalitalu_levu_tarvati)));
        report.append( "<html><body>\n"+ header(padam)+ popup(padam)+ "<div class=\"patra\">\n"+noResultReportStart.replaceFirst("@@padam##", padam).replaceFirst("@@MANNINCHU##", "Sorry").replaceFirst("@@PHALITALU_LEVU_MUNDU##", "No results found for").replaceFirst("@@PHALITALU_LEVU_TARVATA##", ""));
        
        String salahaReport= suggestionReportFor(padam);
        report.append(salahaReport);
        report.append("</div>\n</div>"+footer());
        return report.toString();
    }
    
    public final String suggestionReportFor(String padam) throws IOException{
        //String bhasha= Bhasha.bhasha(padam);
        String bhasha= Bhasha.EN;
        StringBuilder sReport= new StringBuilder("");
        
        if(bhasha.equalsIgnoreCase(Bhasha.EN)) {
            ArrayList<String> dSalahalu= dictionaries.noFuzzySuggestions(padam, ratioLowerLimit, maxNoSuggestions);
            TreeSet<String> pSalahalu= dictionaries.noSuggestions(padam, false, 0, maxNoSuggestions);
            LinkedHashSet<String> vSalahalu= split(padam);
            if(!dSalahalu.isEmpty() || !pSalahalu.isEmpty() || !vSalahalu.isEmpty()) {
                sReport.append(suggestionsBlockStart);
                
                if(!dSalahalu.isEmpty()) {
                    //sReport.append(fsBlockStart.replaceAll("@@DAGGARI_PADALU##", context.getString(R.string.daggari_padalu)));
                    sReport.append(fsBlockStart.replaceAll("@@DAGGARI_PADALU##", "Close words"));
                    for(String salaha: dSalahalu) {
                        sReport.append(suggestionLink.replaceAll("@@ref##", salaha).replaceAll("@@padam##", salaha)+ ", ");
                    }
                    sReport.delete(sReport.length() - 2, sReport.length());
                    sReport.append("</p>\n</div>\n"); //closing <p class="salahaBlock">, and <div class="daggariSalahalu">.
                }
                
                if(!pSalahalu.isEmpty()) {
                    //sReport.append(nsBlockStart.replaceAll("@@padam##", padam).replaceAll("@@MODALU_PADALU##", context.getString(R.string.modalu_padalu)));
                    sReport.append(nsBlockStart.replaceAll("@@padam##", padam).replaceAll("@@MODALU_PADALU##", "Words starting with"));
                    for(String salaha: pSalahalu) {
                        sReport.append(suggestionLink.replaceAll("@@ref##", salaha).replaceAll("@@padam##", salaha)+ ", ");
                    }
                    sReport.delete(sReport.length() - 2, sReport.length());
                    sReport.append("</p>\n</div>\n"); //closing <p class="salahaBlock">, and <div class="prarambhaSalahalu">.
                }

                if(!vSalahalu.isEmpty()) {
                    //sReport.append(ssBlockStart.replaceAll("@@VIDI_PADALU##", context.getString(R.string.vidi_padalu)));
                    sReport.append(ssBlockStart.replaceAll("@@VIDI_PADALU##", "Splitted words"));
                    for(String salaha: vSalahalu) {
                        sReport.append(suggestionLink.replaceAll("@@ref##", salaha).replaceAll("@@padam##", salaha)+ ", ");
                    }
                    sReport.delete(sReport.length() - 2, sReport.length());
                    sReport.append("</p>\n</div>\n"); //closing <p class="salahaBlock">, and <div class="prarambhaSalahalu">.
                }
                
                sReport.append("</div>"); //closing <div clss="salahalu">.
            }
        }
        
        /*else if(bhasha.equalsIgnoreCase(Bhasha.EN)) {
            
        }*/
        return sReport.toString();
    }

    public final LinkedHashSet<String> split(String padam) {
        LinkedHashSet<String> vidiPadalu= new LinkedHashSet<>();
        if(!padam.replaceAll("\n", " ").matches(".*[,.\"'>< ;:/\t\n()-].*")) {
            return vidiPadalu;
        }
        Collections.addAll(vidiPadalu, padam.split("[,.\"'>< ;:/\t\n()-]")); //TODO! may need to escape html special chars.
        return vidiPadalu;
    }
    
}
    
    
    