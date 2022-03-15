package com.example.stockprediction.objects;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

public class MyLinkedHashSet <T> extends LinkedHashSet<T> {

    public int indexOf(T element)
    {
        Log.e("pttt", "indexOf: list="+this+", element="+element);
        // If element not present in the LinkedHashSet it
        // returns -1
        int index = -1;
        // get an iterator
        Iterator<T> iterator = this.iterator();
        int currentIndex = 0;
        while (iterator.hasNext()) {
            // If element present in the LinkedHashSet
            Log.e("pttt", "indexOf: element="+element+", currentElement="+element);
            if (iterator.next().equals(element)) {
                index = currentIndex;
                break;
            }
            currentIndex++;
        }
        // Return index of the element
        return index;
    }

//    public T getByIndex(int index){
//        T result = (T) new ArrayList<String>((Collection<? extends String>) this).get(index);
//        return result;
//    }
}
