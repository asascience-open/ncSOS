/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Some of below is from http://stackoverflow.com/questions/899138/python-like-list-comprehension-in-java
 * I've expanded on it to include other functions.
 * Static class that provides some nice list comprehension functions
 * @author SCowan
 */
public final class ListComprehension {
    public static interface Func<I,O> {
        public O apply(I in);
    }
    
    public static interface Func2P<I,II,O> {
        public O apply(I in, II inn);
    }
    
    public static interface FFunc<I> {
        public void apply(I in);
    }
    
    public static <I,O> List<O> map(List<I> in, Func<I,O> f) {
        List<O> out = new ArrayList<O>(in.size());
        for (I inObj : in) {
            out.add(f.apply(inObj));
        }
        return out;
    }
    
    public static <I,II,O> List<O> map(List<I> in, II inn, Func2P<I,II,O> f) {
        List<O> out = new ArrayList<O>(in.size());
        for (I inObj : in) {
            out.add(f.apply(inObj, inn));
        }
        return out;
    }
    
    public static <I> void foreach(List<I> in, FFunc<I> f) {
        for (I inObj : in) {
            f.apply(inObj);
        }
    }
    
    public static <I> List<I> filterOut(List<I> in, I weed) {
        List<I> out = new ArrayList<I>();
        for (I inObj : in) {
            // test for null to avoid null reference
            try {
                if (!inObj.equals(weed))
                    out.add(inObj);
            } catch (Exception ex) {
                if (weed != null)
                    out.add(inObj);
            }
        }
        return out;
    }
    
    public static <I> List<I> filter(List<I> in, I fil) {
        List<I> out = new ArrayList<I>();
        for (I inObj : in) {
            // test for null to avoid null reference
            try {
                if (inObj.equals(fil))
                    out.add(inObj);
            } catch (Exception ex) {
                if (fil == null)
                    out.add(inObj);
            }
        }
        return out;
    }
}
