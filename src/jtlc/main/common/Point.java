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
* Class Point
* Implements Integer Point using generic Pair of Integer.
* 
* @author Cristian Tardivo
*/
public class Point {

    private final Pair<Integer,Integer> point;
    
    /**
     * Empty Constructor
     * Point (0,0)
     */
    public Point() {
        point = new Pair<>(0,0);
    }
    
    /**
     * Specialized constructor
     * @param x first integer value
     * @param y second integer value
     */
    public Point(int x, int y) {
        point = new Pair<>(x,y);
    }
    
    /**
     * Create point from other point
     * @param other point to clone
     */
    public Point(Point other) {
        point = new Pair<>(other.getX(), other.getY());
    }
    
    /**
     * Retrieves first value
     * @return integer first value
     */
    public int getX() {
        return point.getFirst();
    }

    /**
     * Set first value
     * @param x integer value
     */
    public void setX(int x) {
        point.setFirst(x);
    }

    /**
     * Retrieves second value
     * @return integer second value
     */
    public int getY() {
        return point.getSecond();
    }
    
    /**
     * Set second value
     * @param y integer value
     */
    public void setY(int y) {
        point.setSecond(y);
    } 

    /**
     * Generate Point hashCode
     * @return integer hashCode
     */
    @Override
    public int hashCode() {
        return point.hashCode();
    }
    
    /**
     * Compare this point with other
     * @param other point to compare
     * @return equal or not equal
     */
    @Override
    public boolean equals(Object other) {
        if (other != null && other instanceof Point) {
            Point aux = (Point) other;
            return point.getFirst() == aux.getX() && point.getSecond() == aux.getY();
        }
        return false;
    }

    /**
     * Get Point string label
     * @return string point
     */
    @Override
    public String toString() { 
        return point.toString();
    }
    
    /**
     * Clone current point
     * @return clone of this point
     */
    @Override
    public Point clone() {
        return new Point(point.getFirst(),point.getSecond());
    }          
}