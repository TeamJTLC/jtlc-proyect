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
package jtlc.main;

import com.alee.laf.WebLookAndFeel;
import com.alee.managers.language.LanguageManager;
import jtlc.main.common.Settings;
import jtlc.main.controller.Controller;
import jtlc.view.MainView;

/**
 * System Start
 * 
 * @author Baldani Sergio - Tardivo Cristian
 */
public class Start {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        // LookAndFeel (WebLaf)
        WebLookAndFeel.install();
        WebLookAndFeel.setDecorateDialogs(true);
        WebLookAndFeel.setDecorateFrames(true);
        WebLookAndFeel.setAllowLinuxTransparency(true);
        // Set initial language for WebLookAndFeel
        LanguageManager.setLanguage(Settings.getLocale());
        // Initialize jTLC main view and controller
        MainView view = new MainView();
        Controller controller = new Controller(view);
        // Set observer for main view
        view.addObserver(controller);
    }
}
