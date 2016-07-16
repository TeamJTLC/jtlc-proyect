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
package jtlc.assets;

import com.alee.utils.ArrayUtils;
import java.awt.Component;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javax.swing.ImageIcon;
import jtlc.main.common.Pair;
import jtlc.main.common.Settings;
import jtlc.main.common.Triplet;

/**
 * Assets Class
 * Implements static methods that allows access to assets (string, icons, etc)
 * 
 * @author Baldani Sergio - Tardivo Cristian
 */
public class Assets {
    // Paths and names
    private static final String BUNDLES = "jtlc/assets/bundles/";
    private static final String DOCS = "/jtlc/assets/docs/";
    private static final String GRAPHICS = "/jtlc/assets/graphics/";
    private static final String TEMPLATES = "/jtlc/assets/templates/";
    // System Locales
    private static final String BUNDLE_NAME = "Bundle";
    public static final String[] AVAILABLE_LOCALES = {"es", "en", "pt"};
    private static Locale LOCALE = Locale.forLanguageTag(Settings.getLocale());    
    // System Components with text (for language change)
    private static final HashMap<Component,Triplet<Method,String[],Object[]>> COMPONENTS = new HashMap<>();
    
    /**
     * Load document template as inputStream from template assets folder.
     * @param name
     * @return
     */
    public static InputStream loadTemplate(String name) {
        return Assets.class.getResourceAsStream(TEMPLATES + name);
    }
    
    /**
     * Load document as String from documents assets folder
     * @param name document to load
     * @return string with document content coded in UTF-8
     */
    public static String loadDocument(String name) {
        try (InputStream stream = Assets.class.getResourceAsStream(DOCS + name)) {
            return new BufferedReader(new InputStreamReader(stream)).lines().parallel().collect(Collectors.joining("\r\n"));
        } catch (IOException ex) {
            System.err.println("Error reading document resource: " + name);
            return "CANT_READ_RESOURCE";
        }
    }
    
    /**
     * Load icon from assets folder as ImageIcon.
     * @param name
     * @return 
     */
    public static ImageIcon loadIcon(String name) {
        return new ImageIcon(Assets.class.getResource(GRAPHICS + name + ".png"));
    }
    
    /**
     * Load image from assets folder.
     * @param name
     * @return 
     */
    public static Image loadImage(String name) {
        return Toolkit.getDefaultToolkit().getImage(Assets.class.getResource(GRAPHICS + name + ".png"));
    }
    
    /**
     * Load and resize icon from assets folder as ImageIcon.
     * @param name image file name
     * @param size new icon size
     * @return 
     */
    public static ImageIcon loadIcon(String name, int size) {
        ImageIcon icon = loadIcon(name);
        Image scaled = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        icon.setImage(scaled);
        return icon;
    }
    
    /**
     * Load image from assets folder.
     * @param name image name
     * @param size new image size
     * @return 
     */
    public static Image loadImage(String name, int size) {
        ImageIcon icon = loadIcon(name);
        return icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
    }
    
    /**
     * Get short String with at least maxLength characters
     * @param string String to process
     * @param maxLength maximum string length
     * @param ellipses append ellipses
     * @return shorted string
     */
    public static String shortString(String string, int maxLength, boolean ellipses) {
        return (string.trim().length() > maxLength) ? string.trim().substring(0, maxLength).trim() + (ellipses ? "..." : "") : string.trim();
    }
    
    /**
     * Get string for internationalized key.
     * @param key string key
     * @param parameters optional parameters
     * @return 
     */
    public static String getString(String key, Object... parameters) {
        try {
            if (parameters.length == 0)
                return ResourceBundle.getBundle(BUNDLES + BUNDLE_NAME, LOCALE).getString(key);
            else
                return MessageFormat.format(ResourceBundle.getBundle(BUNDLES + BUNDLE_NAME, LOCALE).getString(key), parameters);
        } catch(MissingResourceException e) {
            e.printStackTrace(System.err);
            return key;
        }
    }
    
    /**
     * Get data for a locale, lenguage icon and lenguage name string key.
     * @param language
     * @return 
     */
    public static Pair<String,String> getLocaleData(String language) {
        if (language.equals("es"))
            return new Pair<>("ic_es-ES", "SPANISH");
        if (language.equals("en"))
            return new Pair<>("ic_en-UK", "ENGLISH");
        if (language.equals("pt"))
            return new Pair<>("ic_pt-BR", "PORTUGUESE");
        return new Pair<>("ic_off", "ERROR");
    }
    
    /**
     * Change current locale.
     * @param locale
     * @return 
     */
    public static boolean changeLocale(String locale) {
        // Check for valid locale and change it
        if (ArrayUtils.contains(locale, AVAILABLE_LOCALES)) {
            LOCALE = Locale.forLanguageTag(locale);
            Locale.setDefault(Locale.Category.DISPLAY, LOCALE);
            localeUpdate();
            return true;
        }
        return false;
    }
    
    /**
     * Associate component to locale key String for language change.
     * @param component refered component
     * @param methodName text update method name
     * @param text locale text key value
     */
    public static void associateComponent(Component component, String methodName, String... text) {
        try {
            // Single Argument
            if (text.length == 1) {
                Method method = component.getClass().getMethod(methodName, String.class);
                COMPONENTS.put(component, new Triplet<>(method, text, null));
            // Multiple Arguments
            } else if (text.length > 1) {
                Method method = component.getClass().getMethod(methodName, String[].class);
                COMPONENTS.put(component, new Triplet<>(method, text, null));
            }
        } catch (NoSuchMethodException | SecurityException ex) {
            System.err.println("Invalid method: " + methodName + " with String parameter  for component:" + component.getClass().getName());
        }
    }
    
    /**
     * Associate component to locale key String and parameters for language change.
     * @param component refered componet
     * @param methodName text update method name
     * @param text local text key value
     * @param parameters text parameters
     */
    public static void associateComponentAndParams(Component component, String methodName, String text, Object... parameters) {
        try {
            Method method = component.getClass().getMethod(methodName, String.class);
            COMPONENTS.put(component, new Triplet<>(method, new String[]{text}, parameters));
        } catch (NoSuchMethodException | SecurityException ex) {
            System.err.println("Invalid method: " + methodName + " with String parameter  for component:" + component.getClass().getName());
        }
    }
    
    /**
     * Tigger locale update to system components.
     */
    private static void localeUpdate() {
        COMPONENTS.forEach((Component comp, Triplet<Method, String[], Object[]> data) -> {
            String[] texts = data.getSecond();
            Object[] parameters = data.getThird();
            // Single Argument
            if (texts.length == 1) {
                // Get Translated component text parameter
                String param = (parameters == null)? getString(texts[0]) : getString(texts[0], parameters);
                try {
                    // Invoke text set method with single argument
                    data.getFirst().invoke(comp, param);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    System.err.println("Can't update element text: " + texts[0] + " for component: " +  comp.getClass().getName() + " using method " + data.getFirst().getName());
                }
            // Multiple Arguments
            } else if (texts.length > 1) {
                // Get Translated component text parameters
                String[] params = Arrays.stream(texts).map(text -> (parameters == null) ? getString(text) : getString(texts[0], parameters)).toArray(size -> new String[size]);
                try {
                    // Invoke text set method with va arguments
                    data.getFirst().invoke(comp, new Object[]{params});
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    System.err.println("Can't update element text: " + Arrays.toString(texts) + " for component: " +  comp.getClass().getName() + " using method " + data.getFirst().getName());
                }
            }
        });
    }
}