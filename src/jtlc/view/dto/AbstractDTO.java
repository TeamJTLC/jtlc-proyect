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
package jtlc.view.dto;

import java.io.Serializable;

/**
 * AbstractDTO
 * Implements basic structure of view panels DTO (Data Transfer Object).
 * 
 * @author Cristian Tardivo
 */
public abstract class AbstractDTO implements Serializable {
    private boolean modified;

    /**
     * Default constructor.
     */
    public AbstractDTO() {
        modified = false;
    }
    
    /**
     * Check if DTO has changed
     * @return 
     */
    public boolean hasChanged() {
        return modified;
    }
    
    /**
     * Set if DTO has changed
     * @param status changed/no-changed
     */
    public void setChanged(boolean status) {
        modified = status;
    }
}