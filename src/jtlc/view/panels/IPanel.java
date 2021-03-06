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
package jtlc.view.panels;

import jtlc.view.dto.AbstractDTO;

/**
 * Basic Panel interface
 * Allows to get panel result as dto
 * and update panel component to new dto values
 * @author Cristian Tardivo
 */
public interface IPanel {
    
    /**
     * Get Panel Result DTO
     * @param <T> Result dto Class
     * @return Concrete panel DTO as AbstractDTO with changes 
     */
    public <T extends AbstractDTO> T getResults();
    
    /**
     * Update panel components to new values in dto
     * @param dto Panel Data-Transfer-Object
     */
    public void updatePanel(AbstractDTO dto);
}