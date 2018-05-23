package in.bhumiputra.nakshatra;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.webkit.WebBackForwardList;
import android.webkit.WebView;

import java.util.Calendar;

import in.bhumiputra.nakshatra.nighantu.Document;

import static in.bhumiputra.nakshatra.NighantuProvider.AKARADISTR;

/**
 * Created by damodarreddy on 1/3/18.
 */

public class Helper {


    public static String recents ="";
    public static BHHelper bhDbHelper;

    public static final int application = 1;


    public static String backForwardPage(WebView wv, Context context) {
        if(wv== null) {
            return null;
        }


        String css= "<style type=\"text/css\" media=\"all\">\n"+ "body {background: #F8ECC2;  font-family: Tahoma, Verdana, \"Lucida Sans Unicode\", sans-serif; font-size: 1em;    line-height: 1.5em; padding:0em; } .patra { padding:0em;    max-width: 55.8em;    margin: 0 auto 0 auto;} .charitra {padding: 3em;padding-top: 3em;font-size:1.2em;line-height:1.7em;} .charitraModalu {font-style: italic; color: black; text-decoration: underline; } .charitraBlock {padding-left: 1em;} .charitraLanke {text-decoration: none; color: #595A5C;}\n"+ "</style>\n";
        String js= "";
        String title= "<title>Recents</title>\n";
        String historyBlockStart= "<div class=\"charitra\">\n";
        String backBlockStart= "<div class=\"venuka\" id=\"charitra_venuka\">\n<span class=\"charitraModalu\">BackHistory</span>\n<ol class=\"charitraBlock\">"; //tag to be closed.
        String forwardBlockStart= "<div class=\"mundu\" id=\"charitra_mundu\">\n<span class=\"charitraModalu\">ForwardHistory</span>\n<ol class=\"charitraBlock\">"; //tag to be closed.
        String historyLink= "<a class=\"charitraLanke\" href=\"@@ref##\">@@padam##</a>";
        String noHistoryBlockStart="<div class=\"noRecents\">\n";
        StringBuilder page= new StringBuilder("");


        if(wv.canGoBack() || wv.canGoForward()) {
            page.append("<html><body>\n"+ Document.htmlHeader+ title+ css+ js+ "</head>\n<div class=\"patra\">\n");
            page.append(historyBlockStart);
            WebBackForwardList wbfList= wv.copyBackForwardList();
            int size= wbfList.getSize();
            int present= wbfList.getCurrentIndex();
            if(wv.canGoBack()) {
                page.append(backBlockStart);
                for(int i= 0; i< present; i++) {
                    String ref= wbfList.getItemAtIndex(i).getTitle().replaceFirst("^", AKARADISTR+ "/nighantu/");
                    //instead of equating "ref" with .getOriginalUrl(), we are building ref from title. this is because, when another
                    //..page is loaded from javascript, originalUrl is not updated to new.(although we can use getUrl() instead of getOriginalUrl()).
                    String padam= wbfList.getItemAtIndex(i).getTitle();
                    if(!wbfList.getItemAtIndex(i).getUrl().startsWith(NighantuProvider.AKARADISTR+"/lopali/")) {
                        page.append("<li>" + historyLink.replaceAll("@@ref##", ref).replaceAll("@@padam##", padam) + "</li>\n");
                    }
                }
                page.append("</ol>\n</div>\n"); //closing <p class="venukaBlock">, and <div class="venuka">.
            }
            if(wv.canGoForward()) {
                page.append(forwardBlockStart);
                for(int i= present; i< size; i++) {
                    String ref= wbfList.getItemAtIndex(i).getTitle().replaceFirst("^", AKARADISTR+ "/nighantu/");
                    String padam= wbfList.getItemAtIndex(i).getTitle();
                    if(!wbfList.getItemAtIndex(i).getUrl().startsWith(NighantuProvider.AKARADISTR+"/lopali/")) {
                        page.append("<li>" + historyLink.replaceAll("@@ref##", ref).replaceAll("@@padam##", padam) + "</li>\n");
                    }
                }
                page.append("</ol>\n</div>\n"); //closing <p class="munduBlock">, and <div class="mundu">.
            }
            page.append("</div>\n"); //charitra block
            page.append("</div>\n</body></html>");
            recents = page.toString();
            return page.toString();
        }

        else {
            page.append("<html><body>\n"+ Document.htmlHeader+ title+ css+ js+ "</head>\n<div class=\"patra\">\n");
            page.append(historyBlockStart+ noHistoryBlockStart+ "No History"+ "</div>\n</div>\n</div>\n</body></html>");
            recents = page.toString();
            return page.toString();
        }

    }


    public static String wordOfTheDayPage(Context context) {
        String template= "<!DOCTYPE html> <html> <head>     <title>@@RojukoPadam##</title>     <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />     <meta name=\"viewport\" content=\"width=device-width\" />     <style type=\"text/css\" media=\"all\">           body {             background: #fffdf6;             padding:0em;             color: #000000;         }          .patra {             padding:0em;             max-width: 55.8em;             margin: 0 auto 0 auto;             font-family: \"Lucida Sans Unicode\", \"nsr\", \"sans-serif\";             font-size: 1em;             line-height: 1.5em;         }          .rojuko_padam {             background: #F8ECC2;             box-shadow: 0.05em 0.05em 0.5em black;             border-radius: 0.05em;             margin: 0 auto 1em auto;         }          .rojuko_padam_pette {             padding: 0.5em;             border-bottom: 1px solid grey;         }          .rojuko_padam_shiram {             padding: 0.5em;             font-style: italic;             text-align: left;         }          .rojuko_padam_padam {             font-weight: bold;             text-align: right;             font-style: italic;             font-size: 1.4em;         }          .rojuko_padam_aw_suchana {             color: grey;             border-top: 1px solid grey;         }          a {            text-decoration: none;            color:  #2f4f4f;         }          .suchana {             text-indent: 2em;             font-size: 0.7em;             line-height: 1.35em;         }       </style> </head> <body>     <div class=\"patra\">         <div class=\"rojuko_padam\">             <div class=\"rojuko_padam_pette rojuko_padam_anglam\">                 <p class=\"rojuko_padam_shiram anglam_shiram\">@@AnglaRojukoPadam##</p>                 <p class=\"rojuko_padam_padam anglam_padam\"><a class=\"padam_lanke\" href=\"@@AnglaPadamHr##\">@@AnglaPadam##</a></p>             </div>         </div>          <div class=\"rojuko_padam_aw_suchana\">             <p class=\"suchana\">@@AW_SUCHANA##</p>         </div>     </div> </body>  </html>";
        SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(context);

        String title= context.getString(R.string.wod_title);
        String header= context.getString(R.string.wod);
        String aw_suggestion= context.getString(R.string.aw_suggestion);

        String wod= pref.getString(PreferanceCentral.pref_word_of_the_day, "___");

        String wodHyperLink= NighantuProvider.AKARADISTR+ "/nighantu/"+ Uri.encode(wod);

        String page= template.replaceAll("@@RojukoPadam##", title)
                .replaceAll("@@AnglaRojukoPadam##", header)
                .replaceAll("@@AnglaPadam##", wod)
                .replaceAll("@@AnglaPadamHr##", wodHyperLink)
                .replaceAll("@@AW_SUCHANA##", aw_suggestion);

        return page;
    }


    public static BHHelper initBHHelper(Context context) {
        if(bhDbHelper == null) {
            bhDbHelper = new BHHelper(context);
        }
        return bhDbHelper;
    }

    public static boolean closeBHHelper() {
        if(bhDbHelper != null) {
            bhDbHelper.close();
        }
        bhDbHelper = null;
        return true;
    }

    public static class BHHelper extends SQLiteOpenHelper {

        private static final String databaseName = "bmHist.db";
        private static final int schema= 1;
        private Context context;

        public BHHelper(Context context) {
            super(context, databaseName, null, schema);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE bookmarks\n" +
                    "(id integer primary key,\n" +
                    "padam text not null collate nocase unique on conflict replace,\n" +
                    "time integer not null default 0,\n" +
                    "priority integer default 1,\n" +
                    "exist text not null collate nocase default 'true');");

            db.execSQL("CREATE TABLE history\n" +
                    "(id integer primary key,\n" +
                    "padam text not null collate nocase unique on conflict replace,\n" +
                    "time integer not null default 0,\n" +
                    "frequency integer default 1,\n" +
                    "exist text not null collate nocase default 'true');");

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
