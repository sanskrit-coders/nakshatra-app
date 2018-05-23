package in.bhumiputra.nakshatra;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static in.bhumiputra.nakshatra.NighantuProvider.AKARADISTR;
import static in.bhumiputra.nakshatra.nighantu.anga.IdxL.readStringFrom;

public class IndexerActivity extends AppCompatActivity {

    public static final int RESULT_CODE_NO_DICTIONARIES= 99;
    public static final String ACTION_REINDEX= "nakshatra.indexer.action.REINDEX";


    private static final String tag= "IndexerActivity";
    public static final String dictionaryDirectoryName = "dictdata";
    public static final String indexDirectoryName = "indices";

    private SQLiteOpenHelper dbHelper;
    private SQLiteDatabase idb; //index db

    List<DictionaryDetails> toBeIndexed;
    List<DictionaryDetails> selected;
    List<String> indexStatuses;

    boolean dictionariesAvailable= true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indexer);
        dbHelper = new IndexDbHelper(this);
        idb = dbHelper.getWritableDatabase();

        handleIntent(getIntent());

        List<DictionaryDetails> sDetails= new ArrayList<>();
        sDetails.addAll(storedDetails(idb));

        File storageDirectory= Environment.getExternalStorageDirectory();
        File dictionaryDirectory= new File(storageDirectory, dictionaryDirectoryName);
        createDirectory(dictionaryDirectory);

        List<DictionaryDetails> pDetails= new ArrayList<>(); //p: present
        pDetails.addAll(dictionaryDetailsInDirectory(dictionaryDirectory));

        File indDirectory= new File(this.getFilesDir(), indexDirectoryName);
        createDirectory(indDirectory);

        Set<DictionaryDetails> toBeRemoved= new HashSet<>(sDetails);
        toBeRemoved.removeAll(pDetails);
        delete(toBeRemoved, idb);

        if(pDetails.size()== 0) {
            dictionariesAvailable= false;
            Fragment prevFragment= getSupportFragmentManager().findFragmentByTag("df");
            FragmentTransaction ft= getSupportFragmentManager().beginTransaction();
            if(prevFragment!=null) {
                ft.remove(prevFragment);
            }
            ft.add(R.id.ind_fragment_holder, new NoDictionariesFragment(), "df");
            ft.commit();
            return;
        }

        Toast.makeText(this, "Scanning dictionaries(in dictdata/ folder), and creating indices. may take some time", Toast.LENGTH_SHORT).show();

        Set<DictionaryDetails> remained= new HashSet<>(sDetails);
        remained.retainAll(pDetails);
        Set<DictionaryDetails> changed= changed(remained);
        update(changed, idb);

        Set<DictionaryDetails> newOnes= new HashSet<>(pDetails);
        newOnes.removeAll(sDetails);
        //index(newOnes, idb);
        toBeIndexed= Collections.unmodifiableList(new ArrayList<>(newOnes));

        if(toBeIndexed.size()== 0) {
            this.finish();
            overridePendingTransition(0, 0);
            return;
        }

        selected= new ArrayList<>();
        Fragment prevFragment= getSupportFragmentManager().findFragmentByTag("df");
        FragmentTransaction ft= getSupportFragmentManager().beginTransaction();
        if(prevFragment!= null) {
            ft.remove(prevFragment);
        }
        ft.add(R.id.ind_fragment_holder, new DictionarySelectionFragment(), "df");
        ft.commit();


    }


    private void handleIntent(Intent intent) {
        String action= intent.getAction();
        if(action.equalsIgnoreCase(ACTION_REINDEX)) {
            idb.execSQL("DELETE FROM dictionaries ;");
            File indDirectory= new File(this.getFilesDir(), indexDirectoryName);
            deleteFile(indDirectory);
        }

    }

    @Override
    public void onDestroy() {
        this.dbHelper.close();
        getContentResolver().update(Uri.parse(AKARADISTR+ "/update/db/"), null, null, null);
        super.onDestroy();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if((keyCode == KeyEvent.KEYCODE_BACK) && !dictionariesAvailable) {
            setResult(RESULT_CODE_NO_DICTIONARIES);
        }
        return super.onKeyDown(keyCode, event);
    }

    private void createDirectory(File tDirectory) {
        try {
            if (tDirectory.exists()) {
                if (tDirectory.isDirectory()) {
                    return;
                } else {
                    //tDirectory.delete();
                    tDirectory.mkdirs();
                }
            } else {
                tDirectory.mkdirs();
            }
        } catch (SecurityException e) {
            Log.e(tag, "error in creating directory "+ tDirectory, e);
        }
    }

    private Set<DictionaryDetails> storedDetails(SQLiteDatabase db) {
        Set<DictionaryDetails> tDetails= new LinkedHashSet<>();
        Cursor cursor= db.rawQuery("SELECT * from dictionaries;", null);
        int sankhya= cursor.getCount();
        //Log.i(tag, "dachinaviSankhya: "+ cursor.getCount());
        for(int i= 0; i< sankhya; i++) {
            cursor.moveToPosition(i);
            DictionaryDetails samacharam= (new DictionaryDetails(
                    cursor.getString(1),
                    cursor.getLong(3),
                    cursor.getLong(4),
                    cursor.getLong(5),
                    cursor.getLong(6)
            ));
            //Log.i(tag, "dachinaDari: "+ samacharam);
            tDetails.add(samacharam);
        }
        return tDetails;
    }

    private Set<DictionaryDetails> dictionaryDetailsInDirectory(File tDirectory) {
        Set<DictionaryDetails> tDetails= new LinkedHashSet<>();
        if(!(tDirectory.canRead() && tDirectory.isDirectory())) {
            return tDetails;
        }

        String[] nighList= tDirectory.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return (name.endsWith(".dict.dz") || name.endsWith(".dict"));
            }
        });

        for(String name: nighList) {
            try {
                DictionaryDetails tSamacharam= new DictionaryDetails(
                        tDirectory,
                        name.replaceAll("\\.dz", "").replaceAll("\\.dict", "")
                );
                tDetails.add(tSamacharam);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File[] directoryList= tDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return (pathname.canRead() && pathname.isDirectory());
            }
        });

        for(File innerDirectory: directoryList) { //l: lopali
            tDetails.addAll(dictionaryDetailsInDirectory(innerDirectory));
        }
        return tDetails;
    }

    private Set<DictionaryDetails> changed(Set<DictionaryDetails> set) {
        Set<DictionaryDetails> changed= new HashSet<>();
        for(DictionaryDetails tDetail: set) {
            if(!tDetail.hasCorrectTimeStamps()) {
                changed.add(tDetail);
            }
        }
        return changed;
    }



    private void delete(Set<DictionaryDetails> toBeRemoved, SQLiteDatabase db) {
        db.beginTransaction();
        try {
            for(DictionaryDetails tDetail: toBeRemoved) {
                int hashCode= tDetail.path.hashCode();
                File indDirectory= new File(this.getFilesDir(), indexDirectoryName);
                File dictionaryIndDirectory= new File(indDirectory, hashCode+"");
                deleteFile(dictionaryIndDirectory);
                db.execSQL("DELETE FROM dictionaries " +
                        "WHERE path= '"+ tDetail.path.replaceAll("'", "''")+ "';");
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void deleteFile(File aFile) {
        if(!aFile.canWrite()) {
            return;
        }
        if(!aFile.isDirectory()) {
            aFile.delete();
            return;
        }
        File[] amshalu= aFile.listFiles();
        for (File amsham : amshalu) {
            deleteFile(amsham);
        }
        aFile.delete();
    }


    private void update(Set<DictionaryDetails> changed, SQLiteDatabase db) {
        db.beginTransaction();
        try {
            for(DictionaryDetails tDetail: changed) {
                createIndices(tDetail, false);
                File synF= new File(tDetail.directory, tDetail.name + ".syn");
                if(synF.canRead()) {
                    createIndices(tDetail, true);
                }

                File dzF= new File(tDetail.directory, tDetail.name + ".dict.dz");
                File nighF= dzF.canRead() ? dzF : new File(tDetail.directory, tDetail.name + ".dict");

                long night= nighF.lastModified();
                long idxt= new File(tDetail.directory, tDetail.name + ".idx").lastModified();
                long ifot= new File(tDetail.directory, tDetail.name + ".ifo").lastModified();
                long synt= synF.canRead() ? synF.lastModified() : 0;

                db.execSQL("INSERT INTO dictionaries (path, hash, night, idxt, ifot, synt)" +
                        "VALUES(" +
                        "'"+ tDetail.path + "'"+ "," +
                        tDetail.path.hashCode()+ "," +
                        night+ "," +
                        idxt+ "," +
                        ifot+ "," +
                        synt+ ");"
                );
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void index(Set<DictionaryDetails> newOnes, SQLiteDatabase db) {
        db.beginTransaction();
        try {
            for(DictionaryDetails tDetail: newOnes) {
                createIndices(tDetail, false);
                File synF= new File(tDetail.directory, tDetail.name + ".syn");
                if(synF.canRead()) {
                    createIndices(tDetail, true);
                }

                db.execSQL("INSERT INTO dictionaries (path, hash, night, idxt, ifot, synt)" +
                        "VALUES(" +
                        "'"+ tDetail.path + "'"+ "," +
                        tDetail.path.hashCode()+ "," +
                        tDetail.night+ "," +
                        tDetail.idxt+ "," +
                        tDetail.ifot+ "," +
                        tDetail.synt+ ");"
                );
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private class IndexDbHelper extends SQLiteOpenHelper {

        private static final String name = "indexDb";
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

    public static class DictionaryDetails {
        public final String path;
        public final String directory;
        public final String name;

        public final long night;
        public final long idxt;
        public final long ifot;
        public final long synt;

        public DictionaryDetails(final String path, final long night, final long idxt, final long ifot, final long synt) {
            //this constructer just stores 'abstract' info. acts as info container. doesn't checks files.
            this.path = path;
            this.directory = path.substring(0, path.lastIndexOf(File.separator)+ 1);
            this.name = path.substring(path.lastIndexOf(File.separator));

            this.night= night;
            this.idxt= idxt;
            this.ifot= ifot;
            this.synt= synt;
        }

        public DictionaryDetails(final String path) throws IOException {
            //this constructor checks files, and info is not externally provided, but actual time stamps.
            this.path = path;
            this.directory = path.substring(0, path.lastIndexOf(File.separator));
            this.name = path.substring(path.lastIndexOf(File.separator)+ 1);

            if(!hasAllMandatoryFiles()) {
                throw new IOException("not exist.");
            }

            File dzF= new File(directory, name + ".dict.dz");
            File nighF= dzF.canRead() ? dzF : new File(directory, name + ".dict");

            this.night= nighF.lastModified();
            this.idxt= new File(directory, name + ".idx").lastModified();
            this.ifot= new File(directory, name + ".ifo").lastModified();
            File synF= new File(directory, name + ".syn");
            this.synt= synF.canRead() ? synF.lastModified() : 0;
        }

        public DictionaryDetails(final File tSanchayam, final String tPeru) throws IOException {
            this(new File(tSanchayam, tPeru).toString());
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

        @Override
        public boolean equals(Object o1) {
            if(!(o1 instanceof DictionaryDetails)) {
                return false;
            }
            return path.equals(((DictionaryDetails) o1).path);
        }

        @Override
        public int hashCode() {
            return path.hashCode();
        }

        @Override
        public String toString() {
            return path;
        }


    }


    private void createIndices(DictionaryDetails tDetail, boolean isForSyn) {
        String extn= isForSyn ? ".syn" : ".idx";
        String indExtn= isForSyn ? ".sind" : ".iind";
        String ind0Extn= indExtn+ "0";

        String name= tDetail.name;
        int hash= tDetail.path.hashCode();
        File indDirectory= new File(this.getFilesDir(), indexDirectoryName);
        File dictionaryIndDirectory= new File(indDirectory, hash+ "");
        createDirectory(dictionaryIndDirectory);

        byte sLBSep= Integer.valueOf(254).byteValue();
        byte fLBSep= Integer.valueOf(255).byteValue();
        byte wrdSep= Integer.valueOf(0).byteValue();

        File angF= new File(tDetail.path + extn); //ang: angam.(idx/syn)
        long angFSize= angF.length();

        try (
                DataInputStream angStream= new DataInputStream(new BufferedInputStream(new FileInputStream(angF)));
                DataOutputStream indStream= new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(dictionaryIndDirectory, name+ indExtn))));
                DataOutputStream ind0Stream= new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(dictionaryIndDirectory, name+ ind0Extn))));
                ) {
            long byteCount= 0L;
            long wordCount= 0L;
            boolean start= true;
            int fLV=0; //fLV: firstLetterValue.
            int sLV=0;
            int pFLV=0; //previous firstLetterValue.
            int pSLV=-1; //actual initial second letter value can be zero. so pSLV initialised to -1(<0), so that change can be detected.
            long prevBlockStartCount= 0L;
            long prevBlockStartAddr= 0L;

            try {
                block: while(true) {
                    String pada= readStringFrom(angStream, wrdSep);
                    //System.out.println("byteCount: " + byteCount);//debug...
                    //System.out.println("pada: " + pada);//debug...
                    angStream.skipBytes(isForSyn ? 4 : 8);

                    fLV= (int)Character.toLowerCase(pada.charAt(0));
                    sLV=0; //to be Setted...
                    if(pada.length() >1 ) { sLV= (int)Character.toLowerCase(pada.charAt(1)); }
                    else { sLV= 0; }

                    if(fLV> pFLV) {
                        if(!start) { //i.e. it is not the very first block 
                            long munduPettePodavu= wordCount-prevBlockStartCount;
                            indStream.writeLong(munduPettePodavu);
                            long munduPetteParimanam= byteCount- prevBlockStartAddr;
                            ind0Stream.writeLong(munduPetteParimanam);
                        }
                        indStream.writeByte(fLBSep);
                        indStream.writeInt(fLV);
                        indStream.writeInt(sLV);
                        indStream.writeLong(wordCount);

                        ind0Stream.writeByte(fLBSep);
                        ind0Stream.writeInt(fLV);
                        ind0Stream.writeInt(sLV);
                        ind0Stream.writeLong(byteCount);

                        prevBlockStartCount= wordCount;
                        prevBlockStartAddr= byteCount;
                        pFLV= fLV;
                        pSLV= sLV;
                        start=false;
                    }

                    else if(sLV>pSLV) {
                        if(!start) { //i.e. it is not the very first block 
                            long munduPettePodavu= wordCount-prevBlockStartCount;
                            indStream.writeLong(munduPettePodavu);
                            long munduPetteParimanam= byteCount- prevBlockStartAddr;
                            ind0Stream.writeLong(munduPetteParimanam);
                        }
                        indStream.writeByte(sLBSep);
                        indStream.writeInt(sLV);
                        indStream.writeLong(wordCount);

                        ind0Stream.writeByte(sLBSep);
                        ind0Stream.writeInt(sLV);
                        ind0Stream.writeLong(byteCount);

                        prevBlockStartCount= wordCount;
                        prevBlockStartAddr= byteCount;
                        pFLV= fLV;
                        pSLV= sLV;
                        start=false;
                    }

                    wordCount+= 1; //
                    byteCount+= pada.getBytes("UTF8").length + 1L + 4L + (isForSyn? 0 : 4L) ; //1L: bytesInwrdSep; 4L: bytes in offset address in idx file; 4L: bytes in length of aropa in idx file.
                    if(byteCount>=angFSize) {
                        break block;
                    }
                }
            }
            catch(EOFException e) { }
            long munduPettePodavu= wordCount-prevBlockStartCount;
            indStream.writeLong(munduPettePodavu);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * fragment classes, methods from now on...
     */

    public static class NoDictionariesFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view= inflater.inflate(R.layout.fragment_ind1_no_dictionaries, container, false);
            TextView sdu= view.findViewById(R.id.fri1_sdu_app_link);
            sdu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("market://details?id=sanskritcode.sanskritdictionaryupdater"));
                    if(intent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivity(intent);
                    }
                    else {
                        String url= "http://play.google.com/store/apps/details?id=sanskritcode.sanskritdictionaryupdater";
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                            startActivity(intent);
                        }
                    }
                }
            });
            return view;
        }
    }

    public void startIndexingSelectedDictionaries() {
        if(selected.size()== 0) {
            this.finish();
            overridePendingTransition(0, 0);
            return;
        }

        indexStatuses= new ArrayList<>();
        for(int i= 0; i< selected.size(); i++) {
            indexStatuses.add("Indexing...");
        }

        Fragment prevFragment= getSupportFragmentManager().findFragmentByTag("df");
        FragmentTransaction ft= getSupportFragmentManager().beginTransaction();
        if(prevFragment!= null) {
            ft.remove(prevFragment);
        }
        IndexingProgressFragment indexingProgressFragment= new IndexingProgressFragment();
        ft.add(R.id.ind_fragment_holder, indexingProgressFragment, "df");
        ft.commit();
        indexWithFeedBack(selected, idb, indexingProgressFragment);
        this.finish();
        overridePendingTransition(0, 0);
    }

    public void onIndexingProgressFragmentViewCreated(IndexingProgressFragment ipf)  { //not used as of now.
        indexWithFeedBack(selected, idb, ipf);
        this.finish();
        overridePendingTransition(0, 0);
    }

    private void indexWithFeedBack(List<DictionaryDetails> tSelected, SQLiteDatabase tDb, IndexingProgressFragment feedbackFragment) {
        tDb.beginTransaction();
        try {
            int nth= 0;
            for(DictionaryDetails tDetail: tSelected) {
                createIndices(tDetail, false);
                File synF= new File(tDetail.directory, tDetail.name + ".syn");
                if(synF.canRead()) {
                    createIndices(tDetail, true);
                }
                indexStatuses.set(nth++, "Done!");
                feedbackFragment.refresh();
                tDb.execSQL("INSERT INTO dictionaries (path, hash, night, idxt, ifot, synt)" +
                        "VALUES(" +
                        "'"+ tDetail.path + "'"+ "," +
                        tDetail.path.hashCode()+ "," +
                        tDetail.night+ "," +
                        tDetail.idxt+ "," +
                        tDetail.ifot+ "," +
                        tDetail.synt+ ");"
                );
            }
            tDb.setTransactionSuccessful();
        } finally {
            tDb.endTransaction();
        }
    }
}
