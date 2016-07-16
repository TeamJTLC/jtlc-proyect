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
package jtlc.view.dialogs;

import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.text.WebEditorPane;
import java.awt.Container;
import java.awt.Dimension;
import javax.swing.JDialog;
import javax.swing.JFrame;
import jtlc.assets.Assets;
import jtlc.view.dto.AbstractDTO;

/**
 * Generic text panel dialog with HTML text support.
 * @author Cristian Tardivo
 */
public class TextPanelDialog extends JDialog implements IDialog {

    /**
     * Create generic text dialog with scrollable panel
     * @param parent dialog parent/owner frame
     * @param title dialog title
     * @param text dialog text
     * @param icon dialog icon (icon name from assets without extension)
     */
    public TextPanelDialog(JFrame parent, String title, String text, String icon) {
        super(parent, title, true);
        initComponents(text, icon);
    }
    
    /**
     * Init Dialog Components.
     * @param text main text to display
     * @param icon dialog icon
     */
    public void initComponents(String text, String icon) {
        // Create Scrollable HTML text panel
        WebEditorPane editorPane = new WebEditorPane("text/html", text);
        editorPane.setEditable(false);
        editorPane.setFocusable(false);
        WebScrollPane editorPaneScroll = new WebScrollPane(editorPane);
        editorPaneScroll.setPreferredSize(new Dimension(650, 400));
        editorPaneScroll.setFocusable(false);
        // Main Container
        WebPanel mainPanel = new WebPanel();
        mainPanel.setMargin(2, 5, 10, 5);
        mainPanel.add(editorPaneScroll);
        // Get dialong container and add scrollable panel
        Container container = getContentPane();
        container.add(mainPanel);
        // Dialog Icon
        setIconImage(Assets.loadImage(icon));
        // Dialog initial size
        setSize(650, 400);
        setResizable(true);
        pack();
        setLocationRelativeTo(getOwner());
        // Scroll to top
        editorPane.setSelectionStart(0);
        editorPane.setSelectionEnd(0); 
        // Set visible
        setVisible(true);
    }
    
    /**
     * This panel don't have results
     * @param <T>
     * @return throw exception
     */
    @Override
    public <T extends AbstractDTO> T getResults() {
        throw new UnsupportedOperationException("Not supported for this panel.");
    }
}
