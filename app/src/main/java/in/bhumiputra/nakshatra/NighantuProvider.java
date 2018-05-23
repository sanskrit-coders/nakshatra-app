package in.bhumiputra.nakshatra;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;

import in.bhumiputra.nakshatra.nighantu.Dictionaries;
import in.bhumiputra.nakshatra.nighantu.Document;
import in.bhumiputra.nakshatra.nighantu.anga.FuzzySuggestion;

import static android.app.SearchManager.SUGGEST_COLUMN_ICON_1;
import static android.app.SearchManager.SUGGEST_COLUMN_TEXT_1;
/**
 * Created by damodarreddy on 12/28/17.
 */

public class NighantuProvider extends ContentProvider {

    public static String tag= "NighantuProvider";

    public static final String[] NS = {"_ID", "NS", "LIPIMARCHU"};
    public static final String[] FS = {"_ID", "FS", "NISHPATTI"};
    public static final String[] NFS = {"_ID", "SUGGESTION", "TYPE", "VALUE"}; //first seaches for normal suggestions, if they didn't exist then searches for fuzzy suggestions. and TYPE header determines it's type, weather normal, or fuzzy. value has differant meanings in two contexts.
    public static final String[] SUGGESTIONS = {"_ID", SUGGEST_COLUMN_TEXT_1, SUGGEST_COLUMN_ICON_1}; /*this searches
    just same as NFS, but projection is differant to suit SearchView. SUGGEST_COLUMN_TEXT1 is main suggestion, and SUGGEST_COLUMN_ICON_1
    is resource pointing to red questionmark(did you mean?) for distinguishing FuzzySuggestions from normal suggestions..
    */

    public static final Uri AKARADI= Uri.parse("content://in.bhumiputra.nakshatra.akaradi");
    public static final String AKARADISTR= "content://in.bhumiputra.nakshatra.akaradi";

    public static final int nSRes = R.drawable.normal_suggestion_go;
    public static final int fSRes = R.drawable.help;

    private Dictionaries dictionaries = null;
    private Document document = null;

    @Override
    public boolean onCreate() {
        return true;
    }

    private void modalu(boolean refresh) {
        if(refresh) {
            dictionaries = new Dictionaries(getContext());
            document = new Document(getContext(), dictionaries);
        }
        if(!refresh) {
            if(dictionaries == null) {
                dictionaries = new Dictionaries(getContext());
                document = new Document(getContext(), dictionaries);
            }
        }
    }


    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        modalu(false);
        if(projection== null) {
            projection= SUGGESTIONS;
        }
        final MatrixCursor cursor= new MatrixCursor(projection, 1);
        String padam= uri.getLastPathSegment();
        if((padam!= null) && (padam.length()> 0) && (padam.indexOf("?") >0)) {
            padam= padam.substring(0,padam.lastIndexOf("?"));
        }
        /*Log.i(tag, "Uri: "+uri.toString()+"\n"+
                "padam: "+ padam+ "\n"+
                "projection: "+ Arrays.toString(projection)+ "\n"+
                "selection: "+ selection
        );*/
        computeCursorFor(padam, projection, selectionArgs, cursor);

        return cursor;
    }




    public void computeCursorFor(String padam, String[] projection, String[] selectionArgs, MatrixCursor cursor) {

        String vidham = projection[1];
        //TODO following modes to be set with arguments from selectionArgs;
        int noSuggestions = 35;
        boolean transliterate = false;
        int printMode = 0;
        int ratioLowerLimit = 60;

        if (vidham.equals(NS[1])) {
            TreeSet<String> salahalu = dictionaries.noSuggestions(padam, transliterate, printMode, noSuggestions);
            if (salahalu != null) {
                int id = 0;
                for (String salaha : salahalu) {
                    MatrixCursor.RowBuilder rb = cursor.newRow();
                    rb.add(NS[0], id);
                    rb.add(NS[1], salaha);
                    rb.add(NS[2], 0); //lipiMarchu setted to 0.
                    id++;
                }
            }
        }

        else if (vidham.equals(FS[1])) {
            ArrayList<FuzzySuggestion> salahalu;
            try {
                salahalu = dictionaries.fuzzySuggestionsFS(padam, ratioLowerLimit);
            } catch (IOException ioe) {
                Log.e(tag, "FS: IOException in finding FuzzySuggestion for " + padam, ioe);
                salahalu = null;
            }
            if (salahalu != null) {
                int id = 0;
                for (FuzzySuggestion salaha : salahalu) {
                    MatrixCursor.RowBuilder rb = cursor.newRow();
                    rb.add(FS[0], id);
                    rb.add(FS[1], salaha.suggestion);
                    rb.add(FS[2], salaha.ratio);
                    id++;
                    if (id >= noSuggestions) {
                        break;
                    }
                }
            }
        }

        else if (vidham.equals(NFS[1])) {

            TreeSet<String> pSalahalu = dictionaries.noSuggestions(padam, transliterate, printMode, noSuggestions);
            if ((pSalahalu != null) && (pSalahalu.size() > 0)) {
                int id = 0;
                for (String pSalaha : pSalahalu) {
                    MatrixCursor.RowBuilder rb = cursor.newRow();
                    rb.add(NFS[0], id);
                    rb.add(NFS[1], pSalaha);
                    rb.add(NFS[2], 0);
                    rb.add(NFS[3], 0);
                    id++;
                }
            }
            else {
                ArrayList<FuzzySuggestion> dSalahalu;
                try {
                    dSalahalu = dictionaries.fuzzySuggestionsFS(padam, ratioLowerLimit);
                } catch (IOException ioe) {
                    Log.e(tag, "NFS: IOException in finding FuzzySuggestions for " + padam, ioe);
                    dSalahalu = null;
                }
                if ((dSalahalu != null) && (dSalahalu.size() > 0)) {
                    int id = 0;
                    for (FuzzySuggestion dSalaha : dSalahalu) {
                        MatrixCursor.RowBuilder rb = cursor.newRow();
                        rb.add(NFS[0], id);
                        rb.add(NFS[1], dSalaha.suggestion);
                        rb.add(NFS[2], 1); //RAKAM is setted to 1.
                        rb.add(NFS[3], dSalaha.ratio); //VILUVA is ratio in this context.
                        id++;
                        if (id >= noSuggestions) {
                            break;
                        }
                    }
                }
            }
        }

        else if (vidham.equals(SUGGESTIONS[1])) {
            TreeSet<String> pSalahalu = dictionaries.noSuggestions(padam, transliterate, printMode, noSuggestions);
            if ((pSalahalu != null) && (pSalahalu.size() > 0)) {
                int id = 0;
                for (String pSalaha : pSalahalu) {
                    //Log.i(tag, "suggestion: "+ pSalaha);
                    MatrixCursor.RowBuilder rb = cursor.newRow();
                    rb.add(SUGGESTIONS[0], id);
                    rb.add(SUGGESTIONS[1], pSalaha);
                    rb.add(SUGGESTIONS[2], nSRes); //RAKAM is setted to 1.
                    id++;
                }
            }
            else {
                ArrayList<FuzzySuggestion> dSalahalu;
                try {
                    dSalahalu = dictionaries.fuzzySuggestionsFS(padam, ratioLowerLimit);
                }
                catch (IOException ioe) {
                    Log.e(tag, "SUGGESTIONS: IOException in finding FuzzySuggestions for " + padam, ioe);
                    dSalahalu = null;
                }
                if ((dSalahalu != null) && (dSalahalu.size() > 0)) {
                int id = 0;
                    for (FuzzySuggestion dSalaha : dSalahalu) {
                        MatrixCursor.RowBuilder rb = cursor.newRow();
                        rb.add(SUGGESTIONS[0], id);
                        rb.add(SUGGESTIONS[1], dSalaha.suggestion);
                        rb.add(SUGGESTIONS[2], fSRes); //RAKAM is setted to 1.
                        id++;
                        if (id >= noSuggestions) {
                            break;
                        }
                    }
                }
            }
        }

    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException{
        modalu(false);
        ParcelFileDescriptor[] pipe= null;

        try {
            pipe= ParcelFileDescriptor.createPipe();
            new TransferThread(uri, new ParcelFileDescriptor.AutoCloseOutputStream(pipe[1])).start();
        }
        catch (IOException ioe) {
            Log.e(tag, "IOException in getting document", ioe);
        }
        return  pipe[0];
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        modalu(false);

        //Log.i(tag, "insert: "+ uri);

        if(uri.toString().startsWith(AKARADISTR+ "/response/random/")) {
            String bhasha = uri.getLastPathSegment();
            String padam = "";
            padam = dictionaries.randomWord(bhasha);

            return Uri.parse(padam);
        }

        else if(uri.toString().startsWith(AKARADISTR+ "/response/next/")) {
            String padam=  Uri.decode(uri.getLastPathSegment());
            String tPadam;
            tPadam= dictionaries.nextWord(padam);
            return Uri.parse(tPadam);
        }

        else if(uri.toString().startsWith(AKARADISTR+ "/response/previous/")) {
            String padam=  Uri.decode(uri.getLastPathSegment());
            String mPadam;
            mPadam= dictionaries.previousWord(padam);
            return Uri.parse(mPadam);
        }

        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        modalu(false);
        if(uri.toString().startsWith(AKARADISTR+ "/update/style/")) {
            int pr= Integer.valueOf(uri.getLastPathSegment());
            document.computeStyle(pr);
            return pr;
        }
        else if(uri.toString().startsWith(AKARADISTR+ "/update/db/")) {
            long dachinaMarpuSamayam= dictionaries.lastModified;
            long prastutaMarpuSamayam= 0;
            try {
                prastutaMarpuSamayam= getContext().getDatabasePath("indexDb").lastModified();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            if(dachinaMarpuSamayam!= prastutaMarpuSamayam) {
                modalu(true);
                return 1;
            }
            return 0;
        }
        else if(uri.toString().startsWith(AKARADISTR+ "/update/mode/")) {
            int vidham= Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(getContext()).getString(PreferanceCentral.pref_indices_loading_mode, "1"));
            int uVidham= dictionaries.mode;
            Log.i(tag, "mode: "+ vidham+ ", pMode: "+ uVidham);
            if(vidham!= uVidham) {
                modalu(true);
                return 1;
            }
            return 0;
        }
        return 0;
    }


    private class TransferThread extends Thread {
        Uri uri;
        ParcelFileDescriptor.AutoCloseOutputStream outStream;
        public TransferThread(Uri uri, ParcelFileDescriptor.AutoCloseOutputStream outStream) {
            this.uri= uri;
            this.outStream= outStream;
        }

        @Override
        public void run() {
            String puta= "";
            if(uri.toString().startsWith(AKARADISTR+"/nighantu/")) {
                try {
                    String padam = Uri.decode(uri.getLastPathSegment());
                    puta = document.document(padam, true);
                    //puta = puta.replaceAll("href=\"", "href=\"content://in.bhumiputra.nakshatra.akaradi/nighantu/");
                } catch (IOException e) {
                    Log.e(tag, "cannot get page", e);
                    e.printStackTrace();
                }
                try {
                    outStream.write(puta.getBytes("UTF8"));
                    if (outStream != null) {
                        outStream.flush();
                        outStream.close();
                    }
                } catch (IOException e) {
                    Log.e(tag, "cannot write to outParcel", e);
                    e.printStackTrace();
                }
            }

            else if(uri.toString().startsWith(AKARADISTR+"/internal/")) {
                String amsham= Uri.decode(uri.getLastPathSegment());

                if(amsham.equals("recents")) {
                    puta= Helper.recents;
                    try {
                        outStream.write(puta.getBytes("UTF8"));
                        if (outStream != null) {
                            outStream.flush();
                            outStream.close();
                        }
                    } catch (IOException e) {
                        Log.e(tag, "cannot write to outParcel", e);
                        e.printStackTrace();
                    }
                }

                else if(amsham.equals("wordoftheday")) {
                    puta= Helper.wordOfTheDayPage(getContext());
                    try {
                        outStream.write(puta.getBytes("UTF8"));
                        if (outStream != null) {
                            outStream.flush();
                            outStream.close();
                        }
                    } catch (IOException e) {
                        Log.e(tag, "cannot write to outParcel", e);
                        e.printStackTrace();
                    }
                }

            }

        }
    }

}
