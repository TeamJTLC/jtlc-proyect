/*
 * Copyright (C) 2015 Baldani Sergio - Tardivo Cristian
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jtlc.main.common;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Function Class test
 * @author cristian
 * @param <I> index number type
 * @param <V> value number type
 */
public class Function<I extends Number & Comparable, V extends Number & Comparable> implements Iterable<Pair<I,V>> { 
    private final List<Pair<I,V>> values;
    
    public Function(int size) {
        values = new ArrayList<>(size);
    }
    
    public Function() {
        values = new LinkedList<>();
    }
    
    public void put(I x, V y) {
        ListIterator<Pair<I,V>> itr = values.listIterator();
        while(itr.hasNext()) {
            I tmp = itr.next().getFirst();
            if (tmp.compareTo(x) > 0) {
                itr.previous();
                break;
            }
        }
        itr.add(new Pair<>(x,y));
    }
    
    public V get(I x) {
        for (Pair<I,V> p: values) {
            if (p.getFirst().compareTo(x) == 0)
                return p.getSecond();
        }
        return null;
    }
    
    @Override
    public Iterator<Pair<I,V>> iterator() {
        return values.iterator();
    }
    
    public int size() {
        return values.size();
    }
    
    public List<Pair<I,V>> subList(int from, int to) {
        return values.subList(from, to);
    }
    
    /**
     * Split "function" as arrays
     * example Pair<Integer[], Float[]> res = test.getAsArrays(Integer[].class, Float[].class);
     * @param indexType Class of indexs values array
     * @param valueType Class of number values array 
     * @return Pair of arrays
     */ 
    public Pair<I[],V[]> getAsArrays(Class<I[]> indexType, Class<V[]> valueType) {
        I[] domain = indexType.cast(Array.newInstance(indexType.getComponentType(), values.size())); 
        V[] images = valueType.cast(Array.newInstance(valueType.getComponentType(), values.size()));
        //
        int i = 0;
        for (Pair<I,V> p: values) {
            domain[i] = p.getFirst();
            images[i] = p.getSecond();
            i++;
        }
        //
        return new Pair<>(domain,images);
    }
}
