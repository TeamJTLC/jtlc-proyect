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

/**
 * Class Pair
 * Implements a Pair of generic objects
 * @param <A> class of first object
 * @param <B> class of second object
 * 
 * @author Cristian Tardivo
 */
public class Pair<A,B> {
    private A first;
    private B second;

    /**
     * Specialized constructor
     * @param first first generic value
     * @param second second generic value
     */
    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }
    
    /**
     * Clone constructor (only primitive types)
     * @param other pair to clone
     */
    public Pair(Pair<A,B> other) {
        A fst = other.getFirst();
        B snd = other.getSecond();
        // Only primitive values
        //if (fst.getClass().isPrimitive() && snd.getClass().isPrimitive()) {
            this.first = fst;
            this.second = snd;
          //  return;
        //}
        //throw new IllegalArgumentException("Use only with primitive values");
    }
        
    /**
     * Retrieves first value
     * @return A first value
     */
    public A getFirst() {
        return first;
    }

    /**
     * Set first value
     * @param first A value
     */
    public void setFirst(A first) {
        this.first = first;
    }

    /**
     * Retrieves second value
     * @return B second value
     */
    public B getSecond() {
        return second;
    }
    
    /**
     * Set second value
     * @param second B value
     */
    public void setSecond(B second) {
        this.second = second;
    }
    
    /**
     * Check if a Pair contains a element between his first and second values
     * Note: Only work for Float pairs/elements now.
     * @param element
     * @return 
     */
    public boolean contains(A element) {
        // Only for numbers
        if (first instanceof Number && second instanceof Number && element instanceof Number) {
            // If number is a float
            if (first instanceof Float && second instanceof Float && element instanceof Float) {
                int a = ((Float)element).compareTo((Float)first);
                int b = ((Float)element).compareTo((Float)second);
                //
                return (a >= 0) && (b <= 0);
            }
        }
        throw new IllegalArgumentException("Invalid element type to compare");
    }
    
    /**
     * Check if a Pair is between two elements A,B checking that
     * first value is greater or equal than A and second value
     * must be lower or equal than B.
     * Note: Only work for Float pairs/elements now.
     * @param firstA
     * @param secondB
     * @return 
     */
    public boolean between(A firstA, B secondB) {
        // Only for numbers
        if (first instanceof Number && second instanceof Number && firstA instanceof Number && secondB instanceof Number) {
            // If number is a float
            if (first instanceof Float && second instanceof Float && firstA instanceof Float && secondB instanceof Float) {
                int a = ((Float)first).compareTo((Float)firstA);
                int b = ((Float)second).compareTo((Float)secondB);
                //
                return (a >= 0) && (b <= 0);
            }
        }
        throw new IllegalArgumentException("Invalid element type to compare");
    }

    /**
     * Generate current Pair hashCode
     * @return integer hashCode
     */
    @Override
    public int hashCode() {
        int hashFirst = first != null ? first.hashCode() : 0;
        int hashSecond = second != null ? second.hashCode() : 0;
        return (hashFirst + hashSecond) * hashSecond + hashFirst;
    }
    
    /**
     * Compare this pair with other pair
     * @param other pair to compare
     * @return equal or not equal
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof Pair) {
            Pair aux = (Pair) other;
            // Invalid null Pair values
            if (aux.first == null && this.first != null) return false;
            if (aux.first != null && this.first == null) return false;
            if (aux.second == null && this.second != null) return false;
            if (aux.second != null && this.second == null) return false;
            // Compare elements
            boolean res = true;
            if (this.first != null) {
                if (aux.first.getClass() != this.first.getClass()) return false;
                res &= aux.first.equals(this.first);
            }
            if (this.second != null) {
                if (aux.second.getClass() != this.second.getClass()) return false;
                res &= aux.second.equals(this.second);
            }
            return res;
        }
        return false;
    }

    /**
     * Get Pair string label
     * @return string pair
     */
    @Override
    public String toString() { 
        return "(" + first.toString() + ", " + second.toString() + ")"; 
    }
}