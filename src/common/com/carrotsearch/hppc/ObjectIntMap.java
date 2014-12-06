package com.carrotsearch.hppc;

import com.carrotsearch.hppc.cursors.ObjectIntCursor;

/**
 * An associative container with unique binding from keys to a single value.
 */
 @javax.annotation.Generated(date = "2014-12-06T10:00:22+0100", value = "HPPC generated from: ObjectIntMap.java") 
public interface ObjectIntMap<KType> 
    extends ObjectIntAssociativeContainer<KType>
{
    /**
     * Place a given key and value in the container.
     * 
     * @return The value previously stored under the given key in the map is returned.
     */
    public int put(KType key, int value);

      
    /**
     * An equivalent of calling
     * <pre>
     *  putOrAdd(key, additionValue, additionValue);
     * </pre>
     * 
     * @param key The key of the value to adjust.
     * @param additionValue The value to put or add to the existing value if <code>key</code> exists.
     * @return Returns the current value associated with <code>key</code> (after changes).
     */
      
         public int addTo(KType key, int additionValue);
     

      
    /**
     * <a href="http://trove4j.sourceforge.net">Trove</a>-inspired API method. A logical 
     * equivalent of the following code:
     * <pre>
     *  if (containsKey(key))
     *  {
     *      int v = (int) (lget() + additionValue);
     *      lset(v);
     *      return v;
     *  }
     *  else
     *  {
     *     put(key, putValue);
     *     return putValue;
     *  }
     * </pre>
     * 
     * @param key The key of the value to adjust.
     * @param putValue The value to put if <code>key</code> does not exist.
     * @param additionValue The value to add to the existing value if <code>key</code> exists.
     * @return Returns the current value associated with <code>key</code> (after changes).
     */
      
         public int putOrAdd(KType key, int putValue, int additionValue);
     

    /**
     * @return Returns the value associated with the given key or the default value
     * for the key type, if the key is not associated with any value. 
     *
     * <b>Important note:</b> For primitive type values, the value returned for a non-existing
     * key may not be the default value of the primitive type (it may be any value previously
     * assigned to that slot).
     */
    public int get(KType key);

    /**
     * @return Returns the value associated with the given key or the provided default value if the
     * key is not associated with any value. 
     */
    public int getOrDefault(KType key, int defaultValue);

    /**
     * Puts all keys from another container to this map, replacing the values
     * of existing keys, if such keys are present.   
     * 
     * @return Returns the number of keys added to the map as a result of this
     * call (not previously present in the map). Values of existing keys are overwritten.
     */
    public int putAll(
        ObjectIntAssociativeContainer<? extends KType> container);

    /**
     * Puts all keys from an iterable cursor to this map, replacing the values
     * of existing keys, if such keys are present.   
     * 
     * @return Returns the number of keys added to the map as a result of this
     * call (not previously present in the map). Values of existing keys are overwritten.
     */
    public int putAll(
        Iterable<? extends ObjectIntCursor<? extends KType>> iterable);

    /**
     * Remove all values at the given key. The default value for the key type is returned
     * if the value does not exist in the map. 
     */
    public int remove(KType key);
    
    /**
     * Compares the specified object with this set for equality. Returns
     * <tt>true</tt> if and only if the specified object is also a
     * {@link ObjectIntMap} and both objects contains exactly the same key-value pairs.
     */
    public boolean equals(Object obj);

    /**
     * @return A hash code of elements stored in the map. The hash code
     * is defined as a sum of hash codes of keys and values stored
     * within the set). Because sum is commutative, this ensures that different order
     * of elements in a set does not affect the hash code.
     */
    public int hashCode();
}
