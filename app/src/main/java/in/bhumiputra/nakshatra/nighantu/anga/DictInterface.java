package in.bhumiputra.nakshatra.nighantu.anga;

import java.io.IOException;
import java.util.ArrayList;

/**
 * This is common interface for classes modelling dict files(both dz compressed .dict.dz files and non compressed .dict files).
 * presently {@link Dict Dict} class implements this interface for non-compressed .dict files, and {@link Dz Dz} class implements it for compressed dz files, with pseudo random access.
 */

public interface DictInterface {


    /**
     * @return unique id of {@link Dictionary Dictionary} object, to which this Dict object belongs to.
     */
    Id getId();

    /**
     *
     * @return weather the file is compressed
     */
    boolean isCompressed();

    /**
     *
     * @return always true.
     */
    boolean isValid();

    /**
     * this method is only for classes dealing with dictzip files. like {@link Dz Dz} class.
     * @return chunk length(CHLEN) value from dz header. see Gzip, and DictZip specification for more details.
     */
    int chunkLength();

    /**
     * to get dict description for a word, we should first find it's address from idx file using {@link Idx#addressesFor(String)}.
     * then pass each {@link Address Address} object to this method to get corresponding {@link Entry Entry} object.
     * {@link Entry Entry} object is wrapper containing word, and description in it's fields.
     *
     * implementing classes should deal with decompression of small chunks, if file is dz compressed.
     * @param address   {@link Address Address} object. commonly we get it from {@link Idx#addressesFor(String)} method, or {@link Index#addressesTuple(String)} method. ( Index object is just wrapper arround Idx and Syn Objects, and deals with synonyms too.)
     * @return an {@link Entry Entry} object corresponding to given {@link Address Address} object.
     * @throws IOException in case an io error occurs in reading file, or a decompression error occurs.
     */
    Entry rawEntryAt(Address address) throws IOException;

    /**
     * wrapper around {@link DictInterface#rawEntryAt(Address) rawEntryAt(Address)} method. presently does nothing else than returning what {@link DictInterface#rawEntryAt(Address) rawEntryAt()} method returns.
     * @param chirunama
     * @return
     * @throws IOException
     */
    Entry entryAt(Address chirunama) throws IOException;

    /**
     *
     * @param addresses
     * @return an {@link ArrayList ArrayList} of {@link Entry} objects for each {@link Address Address} object in given ArrayList.
     * @throws IOException
     */
    ArrayList<Entry> entriesAt(ArrayList<Address> addresses) throws IOException;

    /**
     * this is the method, we mainly use.
     * @param adrTuple a two member tuple({@link STuple}) of {@link ArrayList} of {@link Address} objects. we commonly get this as result of {@link Index#addressesTuple(String)} method.
     *                 {@link STuple} just holds two blocks. (here we used them for headers, and synonyms blocks for distinguistion). each block holds an {@link ArrayList} of {@link Address} objects.
     * @return  an {@link Address} to {@link Entry} computed parallal datastructure.
     * @throws IOException
     */
    STuple<ArrayList<Entry>> entriesTuple(STuple<ArrayList<Address>> adrTuple) throws IOException;
}
