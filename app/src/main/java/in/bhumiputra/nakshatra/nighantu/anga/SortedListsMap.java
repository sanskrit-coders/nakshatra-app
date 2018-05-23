package in.bhumiputra.nakshatra.nighantu.anga;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * we cannot use normal {@link Map} or a{@link java.util.List} for modelling {@link Idx}, etc fallowing reasons.
 * <ul>
 * <li>idx file can contain single word may times, pointing to differant offset in dict file. and thus not unique.</li>
 * <li>And we need to address an idx entry by it's number.(for supporting syn file)</li>
 * <li>we need mapping behaviour too along with index behaviour, and repetitions should supoorted</li>
 * </ul>
 * for these reasons this class helps.

 * though it implements {@link Map} , it is not actually one.
 * it maps between two sorted {@link List Lists}. maps an item in list1 to item in list2 at same index. and is unmodifiable. so no insert or delete etc.
 * we can get items by index, and maintains order, and duplicates allowed, etc. as List support them.
 * and as a map, when we want a value in list2 for a key in list1, then we first have to compute indexOf that key in list1 and return item at same index in list2.
 * but indexOf may take lot of time, as it should linearly compare all items in list with given key.
 * so binary search is used instead of linear search as list1 is sorted. hense searching and mapping is very fast. for example linear search may take at it's maximum 65536 comparisions in a list of 65536items. where as binary search does onle 16 comparisions in sorted list.
 * if duplicates are there, then we don't know which item will be returned in map's get(key) method.
 *
 * but above {@link Map} interface methods are implemented just for convinience, but we don't actually use them, and have no use.
 * we use methods {@link #getAllValuesOf(Object)} , {@link #lesser(Object)}, {@link #greater(Object)} etc.
 * and lists also exposed out, so that we can do whatever we want with them.
 */

public class SortedListsMap<K, V> implements Map<K, V> {


    public List<K> keys;
    public List<V> values;

    private List<K> keysIn;
    private List<V> valuesIn;

    private Comparator<K> comparator;

    public SortedListsMap(List<K> k, List<V> v, Comparator<K> c) {
        if((k== null) || (v== null)) {
            throw new NullPointerException("Lists should not be null.");
        }
        this.keysIn = k;
        this.valuesIn = v;

        this.keys = Collections.unmodifiableList(this.keysIn);
        this.values = Collections.unmodifiableList(this.valuesIn);

        this.comparator = c;
        if(this.comparator == null) {
            this.comparator = new Comparator<K>() {
                @Override
                public int compare(K o1, K o2) {
                    return ((Comparable<K>)o1).compareTo(o2);
                }
            };
        }
    }

    public SortedListsMap<K, V> swapLists(List<K> k, List<V> v) {
        if((k== null) || (v== null)) {
            throw new NullPointerException("Lists should not be null.");
        }
        this.keysIn = k;
        this.valuesIn = v;

        this.keys = Collections.unmodifiableList(this.keysIn);
        this.values = Collections.unmodifiableList(this.valuesIn);

        return this;
    }


    @Override
    public int size() {
        return keys.size();
    }

    @Override
    public boolean isEmpty() {
        return keys.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return keys.contains(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return values.contains(value);
    }

    @Override
    public V get(Object key) {
        return getValue((K) key);
    }

    @Override
    public V put(K key, V value) {
        return null;
    }

    @Override
    public V remove(Object key) {
        return null;
    }

    @Override
    public void putAll(@NonNull Map<? extends K, ? extends V> m) {

    }

    @Override
    public void clear() {

    }

    @NonNull
    @Override
    public Set<K> keySet() {
        TreeSet<K> samiti= new TreeSet<>(comparator);
        samiti.addAll(keys);
        return samiti;
    }

    @NonNull
    @Override
    public Collection<V> values() {
        return values;
    }

    @NonNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return new HashSet<>();
    }

    public int index(K key) {
        return Collections.binarySearch(keys, key, comparator);
    }

    public int firstIndexOf(K key, int anIndex) {
        if(anIndex< 0) { //means don'y know. should compute.
            anIndex= index(key);
        }

        if(anIndex< 0) {
            return anIndex;
        }

        int fIndex= anIndex;

        while(fIndex> 0) {
            fIndex--;
            if(comparator.compare(keys.get(fIndex), key)!= 0) {
                fIndex+= 1;
                break;
            }
        }
        return fIndex;
    }

    public int lastIndexOf(K key, int anIndex) {
        if(anIndex< 0) { //means don'y know. should compute.
            anIndex= index(key);
        }

        if(anIndex< 0) {
            return anIndex;
        }

        int lIndex= anIndex;

        while(lIndex< keys.size()- 1) {
            lIndex++;
            if(comparator.compare(keys.get(lIndex), key)!= 0) {
                lIndex-= 1;
                break;
            }
        }
        return lIndex;
    }

    public ArrayList<V> getAllValuesOf(K key) {
        ArrayList<V> results= new ArrayList<>();
        int fIndex= firstIndexOf(key, -1);

        if(fIndex< 0) {
            return results;
        }

        int tIndex= fIndex;
        while(tIndex< keys.size()) {
            if(comparator.compare(keys.get(tIndex), key)== 0) {
                results.add(values.get(tIndex));
                tIndex++;
            }
            else {
                break;
            }
        }

        return results;
    }

    public ArrayList<Tuple<K, V>> getAllKeyValuesOf(K key) {
        /*
        this method is used instead of getAllValuesOf() to preserve case(in case of String keys list). i.e.
        two keys may be equal 'according to comparator' , but they both may have differant case. and hense storing keys too.
         */
        ArrayList<Tuple<K, V>> results= new ArrayList<>();
        int fIndex= firstIndexOf(key, -1);

        if(fIndex< 0) {
            return results;
        }

        int tIndex= fIndex;
        while(tIndex< keys.size()) {
            if(comparator.compare(keys.get(tIndex), key)== 0) {
                results.add(new Tuple<>(keys.get(tIndex), values.get(tIndex)));
                tIndex++;
            }
            else {
                break;
            }
        }

        return results;
    }

    public V getValue(int nth) {
        if((nth<0) || (nth>= values.size())) {
            return null;
        }
        return values.get(nth);
    }

    public V getValue(K key) {
        int tIndex= firstIndexOf(key, -1);
        return getValue(tIndex);
    }

    public STuple<K> lesserGreater(K key) {
        if(key== null) {
            return new STuple<>(null, null);
        }

        int sthanam= index(key);
         if(sthanam< 0) {
             int insertionPlace= Math.abs(sthanam)-1;
             K lesser= (insertionPlace== 0) ? key : keys.get(insertionPlace- 1);
             K greater= (insertionPlace== keys.size())? key : keys.get(insertionPlace);
             return new STuple<>(lesser, greater);
         }

         else {
             int fIndex= firstIndexOf(key, sthanam);
             int lIndex= lastIndexOf(key, sthanam);
             K lesser= (fIndex== 0) ? key : keys.get(fIndex- 1);
             K greater= (lIndex== keys.size()-1) ? key : keys.get(lIndex+ 1);
             return new STuple<>(lesser, greater);
         }
    }

    public K lesser(K key) {
        if(key== null) {
            return null;
        }

        int sthanam= index(key);
        if(sthanam< 0) {
            int insertionPlace= Math.abs(sthanam)-1;
            return  (insertionPlace== 0) ? key : keys.get(insertionPlace- 1);
        }
        else {
            int fIndex= firstIndexOf(key, sthanam);
            return (fIndex== 0) ? key : keys.get(fIndex- 1);
        }
    }

    public K greater(K key) {
        if(key== null) {
            return null;
        }
        int sthanam= index(key);
        if(sthanam< 0) {
            int insertionPlace= Math.abs(sthanam)-1;
            return (insertionPlace== keys.size())? key : keys.get(insertionPlace);
        }
        else {
            int lIndex= lastIndexOf(key, sthanam);
            return (lIndex== keys.size()-1) ? key : keys.get(lIndex+ 1);
        }
    }

}
