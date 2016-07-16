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

import com.alee.utils.FileUtils;
import java.awt.Dimension;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.awt.Point;
import java.io.File;
import java.net.URISyntaxException;
import jtlc.main.Start;

/**
 * jTLC System Settings saver/loader
 * 
 * @author Cristian Tardivo
 */
public class Settings {
    // Default Settings file
    private static final String SETTINGS_FILE = "jtlc.settings.props";
    // Settings properties
    private static Properties properties;
    // Properties List
    private static final String WORK_SPACE_PATH = "workspace";
    private static final String SYSTEM_LOCALE = "locale";
    private static final String TRANSITIONS_STATUS = "transitions";
    private static final String WINDOW_STATE = "extended";
    private static final String WINDOW_SIZE = "size";
    private static final String WINDOW_LOCATION = "location";
    
    // Window states enum
    private static enum State {
        NORMAL(0), ICONIFIED(1), MAXIMIZED_HORIZ(2), MAXIMIZED_VERT(4), MAXIMIZED_BOTH(2 | 4);
        
        // State value
        public int value;
        
        // State with int value
        State(int value) {
            this.value = value;
        }
        
        // State from int value
        public static State fromValue(int value) {
            switch(value) {
                case 0: return NORMAL;
                case 1: return ICONIFIED;
                case 2: return MAXIMIZED_HORIZ;
                case 3: return MAXIMIZED_VERT;
                case 2 | 4: return MAXIMIZED_BOTH;
                default: return NORMAL;
            }
        }
    }
    
    /**
     * Get Settings file path
     * @return
     */
    private static String getSettingsPath() {
        try {
            // Get current execution jar path
            String jar = Start.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            // Get jar parent folder
            String path = new File(jar).getParentFile().getAbsolutePath();
            // Return path to settings file
            return path + File.separator + SETTINGS_FILE;
        } catch(URISyntaxException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    /**
     * Load Settings from file
     * @return true/false if settings was loaded correctly
     */
    private static boolean loadSettings() {
        try {
            // Load Propieties from file
            properties.load(new FileInputStream(getSettingsPath()));
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
    
    /**
     * Save Settings to file
     * @return true/false if settings was saved correctly
     */
    public static boolean saveSettings() {
        try {
            properties.store(new FileOutputStream(getSettingsPath()), "jTLC System Settings");
            return true;
        } catch (IOException ex) {
            return false;
        }        
    }
    
    /**
     * Get Settings properties instance
     * @return 
     */
    private static Properties getSettings() {
        if (properties == null) {
            properties = new Properties();
            // Init properties
            if (!loadSettings()) {
                // Default WorkSpace Path
                properties.setProperty(WORK_SPACE_PATH, FileUtils.getUserHomePath());
                // Default System Locale
                properties.setProperty(SYSTEM_LOCALE, "es");
                // Default Transitions Status
                properties.setProperty(TRANSITIONS_STATUS, "true");
                // Default Size
                properties.setProperty(WINDOW_SIZE, "800 600");
                // Default Location
                properties.setProperty(WINDOW_LOCATION, "0 0");
                // Default Stended State
                properties.setProperty(WINDOW_STATE, "NORMAL");
            }
        }
        return properties;
    }

    /**
     * Get System workspace path
     * @return 
     */
    public static String getWorkSpace() {
        return getSettings().getProperty(WORK_SPACE_PATH);
    }

    /**
     * Set System workspace path
     * @param workSpacePath 
     */
    public static void setWorkSpace(String workSpacePath) {
        getSettings().setProperty(WORK_SPACE_PATH, workSpacePath);
    }

    /**
     * Get current system locale
     * @return 
     */
    public static String getLocale() {
        return getSettings().getProperty(SYSTEM_LOCALE);
    }

    /**
     * Set current system locale
     * @param currentLocale 
     */
    public static void setLocale(String currentLocale) {
        getSettings().setProperty(SYSTEM_LOCALE, currentLocale);
    }
    
    /**
     * Get view transitions status
     * @return 
     */
    public static boolean isTransitionsEnabled() {
        return Boolean.valueOf(getSettings().getProperty(TRANSITIONS_STATUS));
    }

    /**
     * Set view transitons status
     * @param transitionsEnabled 
     */
    public static void setTransitionsEnabled(boolean transitionsEnabled) {
        getSettings().setProperty(TRANSITIONS_STATUS, String.valueOf(transitionsEnabled));
    }
    
    /**
     * Get Window extended state
     * @return 
     */
    public static int getExtendedState() {
        return State.valueOf(getSettings().getProperty(WINDOW_STATE)).value;        
    }
    
    /**
     * Set window extended state
     * @param value 
     */
    public static void setExtendedState(int value) {
        getSettings().setProperty(WINDOW_STATE, State.fromValue(value).toString());
    }
    
    /**
     * Get window size
     * @return 
     */
    public static Dimension getWindowSize() {
        String[] size = getSettings().getProperty(WINDOW_SIZE).split(" ");
        return new Dimension(Integer.valueOf(size[0]), Integer.valueOf(size[1]));
    }
    
    /**
     * Set window size
     * @param size 
     */
    public static void setWindowSize(Dimension size) {
        getSettings().setProperty(WINDOW_SIZE, size.width + " " + size.height);
    }
    
    /**
     * Get window location
     * @return 
     */
    public static Point getWindowLocation() {
        String[] location = getSettings().getProperty(WINDOW_LOCATION).split(" ");
        return new Point(Integer.valueOf(location[0]), Integer.valueOf(location[1]));
    }
    
    /**
     * Set window location
     * @param location 
     */
    public static void setWindowLocation(Point location) {
        getSettings().setProperty(WINDOW_LOCATION, (int)location.getX() + " " + (int)location.getY());
    }
}