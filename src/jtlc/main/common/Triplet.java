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
 * Class Triplet
 * Implements a triplet of generic objects
 * 
 * @param <A> class of first object
 * @param <B> class of second object
 * @param <C> class of thrid object
 * 
 * @author Cristian Tardivo
 */
public class Triplet<A,B,C> extends Pair<A,B> {
    private C third;
    
    /**
     * Specialized constructor
     * @param first first generic value
     * @param second second generic value
     * @param third third generic value
     */
    public Triplet(A first, B second, C third) {
        super(first, second);
        this.third = third;
    }
    
    /**
     * Create Triplet from Pair
     * @param pair Starting pair
     * @param third new third value
     */
    public Triplet(Pair<A,B> pair, C third) {
        super(pair.getFirst(), pair.getSecond());
        this.third = third;
    }
    
    /**
     * Create Triplet from other triplet (only primitive types)
     * @param other triplet to clone
     */
    public Triplet(Triplet<A,B,C> other) {
        super(other.getFirst(), other.getSecond());
        // Only primitive values
        //if (other.third.getClass().isPrimitive())
            this.third = other.third;
        // else
            //throw new IllegalArgumentException("Use only with primitive values");
    }
    
    /**
     * Retrieves third value
     * @return 
     */
    public C getThird() {
        return third;
    }
    
    /**
     * Set third value
     * @param third 
     */
    public void setThird(C third) {
        this.third = third;
    }
    
    /**
     * Generate current Triplet hashCode
     * @return integer hashCode
     */
    @Override
    public int hashCode() {
        int hc = super.hashCode();
        hc = (third != null)? (hc / third.hashCode()) + hc : hc;
        return hc;
    }
    
    /**
     * Compare this triplet with other triplet
     * @param other triplet to compare
     * @return equal or not equal
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof Triplet) {
            // Compare Pair
            boolean res = super.equals(other);
            // Compare Triplet
            Triplet aux = (Triplet) other;
            if (aux.third == null && this.third != null) return false;
            if (aux.third != null && this.third == null) return false;
            // Compare elements
            if (this.third != null && aux.third != null) {
                if (aux.third.getClass() != this.third.getClass()) return false;
                res &= aux.third.equals(this.third);
            }
            return res;
        }
        return false;
    }
    
    /**
     * Get Triplet string label
     * @return string pair
     */
    @Override
    public String toString() { 
           return "(" + getFirst().toString() + ", " + getSecond().toString() + ", " + third.toString() + ")"; 
    }
}