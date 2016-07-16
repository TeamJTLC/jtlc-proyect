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
package jtlc.view.dialogs.dto;

import jtlc.main.common.Settings;
import jtlc.view.dto.AbstractDTO;

/**
 * SettingsDTO implements DTO for SettingsDialog
 * 
 * @author Cristian Tardivo
 */
public class SettingsDTO extends AbstractDTO {
    private String workSpacePath;
    private String currentLocale;
    private boolean transitionsEnabled;
    
    
    /**
     * Create new settings dialog DTO from system settings.
     */
    public SettingsDTO() {
        this.workSpacePath = Settings.getWorkSpace();
        this.currentLocale = Settings.getLocale();
        this.transitionsEnabled = Settings.isTransitionsEnabled();
    }
    
    /**
     * Create new settings dialog DTO.
     * @param workSpacePath
     * @param currentLocale
     * @param transitionsEnabled 
     */
    public SettingsDTO(String workSpacePath, String currentLocale, boolean transitionsEnabled) {
        this.workSpacePath = workSpacePath;
        this.currentLocale = currentLocale;
        this.transitionsEnabled = transitionsEnabled;
    }

    /**
     * Get workspace path
     * @return 
     */
    public String getWorkSpacePath() {
        return workSpacePath;
    }
    
    /**
     * Set workspace path
     * @param workSpacePath 
     */
    public void setWorkSpacePath(String workSpacePath) {
        this.workSpacePath = workSpacePath;
    }

    /**
     * Get current locale key
     * @return 
     */
    public String getCurrentLocale() {
        return currentLocale;
    }

    /**
     * Set current locale key
     * @param currentLocale 
     */
    public void setCurrentLocale(String currentLocale) {
        this.currentLocale = currentLocale;
    }

    /**
     * Get transitions status
     * @return 
     */
    public boolean isTransitionsEnabled() {
        return transitionsEnabled;
    }
    
    /**
     * Set transitions status
     * @param transitionsEnabled 
     */
    public void setTransitionsEnabled(boolean transitionsEnabled) {
        this.transitionsEnabled = transitionsEnabled;
    }
}