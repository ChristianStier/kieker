package com.carrotsearch.hppc;

/**
 * Marker interface for containers that can check if they contain a given object in
 * at least time <code>O(log n)</code> and ideally in amortized 
 * constant time <code>O(1)</code>.
 */
 @javax.annotation.Generated(date = "2014-12-06T10:00:22+0100", value = "HPPC generated from: IntLookupContainer.java") 
public interface IntLookupContainer extends IntContainer
{
    public boolean contains(int e);
}
