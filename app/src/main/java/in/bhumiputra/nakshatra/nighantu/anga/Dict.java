package in.bhumiputra.nakshatra.nighantu.anga;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import in.bhumiputra.nakshatra.nighantu.tools.RandomAccessInputStream;

/**
 * implementation of {@link DictInterface} for modelling uncompressed .dict files.
 * see {@link DictInterface} interface's doc for method's documentation.
 */

public class Dict implements DictInterface {


    public final Id id;
    public final File nighF;
    public final Ifo ifo;

    private RandomAccessInputStream randDict;

    public static final boolean isCompressed= false;
    public final boolean isValid= true;

    public Dict(final Id id, final File nighF, final  Ifo ifo) throws IOException {
        this.id= id;
        this.nighF= nighF;
        this.ifo= ifo;
        this.randDict = new RandomAccessInputStream(nighF, "r");
    }

    @Override
    public Id getId() {
        return id;
    }

    @Override
    public boolean isCompressed() {
        return isCompressed;
    }

    @Override
    public boolean isValid() {
        return isValid;
    }

    @Override
    public int chunkLength() {
        return 0;
    }

    @Override
    public Entry rawEntryAt(Address address) throws IOException {
        if(address == null) {
            return null;
        }
        int offset= address.offset;
        int len= address.len;

        randDict.seek(offset);
        byte[] buf= new byte[len];
        randDict.read(buf, 0, len);
        String vivaram= new String(buf, 0, len, "UTF-8");
        return new Entry(address.padam, vivaram);
    }

    @Override
    public Entry entryAt(Address chirunama) throws IOException {
        return rawEntryAt(chirunama);
    }

    @Override
    public ArrayList<Entry> entriesAt(ArrayList<Address> addresses) throws IOException {
        ArrayList<Entry> entries= new ArrayList<>();
        if(addresses == null) {
            return entries;
        }

        for(Address chirunama: addresses) {
            entries.add(entryAt(chirunama));
        }
        return entries;
    }

    @Override
    public STuple<ArrayList<Entry>> entriesTuple(STuple<ArrayList<Address>> adrTuple) throws IOException {
        ArrayList<Entry> shiraB= entriesAt(adrTuple.first);
        ArrayList<Entry> paryayaB= entriesAt(adrTuple.second);
        STuple<ArrayList<Entry>> dvayam= new STuple<>(shiraB, paryayaB);
        return dvayam;
    }
}
