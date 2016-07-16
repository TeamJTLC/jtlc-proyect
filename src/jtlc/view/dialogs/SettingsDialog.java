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

import com.alee.extended.button.WebSwitch;
import com.alee.extended.filechooser.WebDirectoryChooser;
import com.alee.extended.panel.GroupPanel;
import com.alee.extended.panel.GroupingType;
import com.alee.laf.button.WebButton;
import com.alee.laf.combobox.WebComboBox;
import com.alee.laf.combobox.WebComboBoxCellRenderer;
import com.alee.laf.combobox.WebComboBoxElement;
import com.alee.laf.label.WebLabel;
import com.alee.laf.text.WebTextField;
import com.alee.managers.hotkey.ButtonHotkeyRunnable;
import com.alee.managers.hotkey.Hotkey;
import com.alee.managers.hotkey.HotkeyManager;
import com.alee.managers.language.data.TooltipWay;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.utils.swing.DialogOptions;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import jtlc.assets.Assets;
import jtlc.main.common.Pair;
import jtlc.view.dialogs.dto.SettingsDTO;

/**
 * System settings edit Dialog.
 * @author Cristian Tardivo
 */
public class SettingsDialog extends JDialog implements IDialog {
    private boolean result;
    private WebSwitch transitionsSwitch;
    private WebTextField directoryField;
    private WebComboBox languageComboBox;
    private final SettingsDTO data;
    
    /**
     * Create System settings edit dialog and init components
     * @param parent dialog parent/owner frame
     * @param dto data edit dialog dto
     */
    public SettingsDialog(JFrame parent, SettingsDTO dto) {
        super(parent, Assets.getString("EDIT_SYSTEM_SETTINGS"), true);
        data = dto;
        initComponents();
    }

    /**
     * Init Dialog Components.
     */
    private void initComponents() {
        // Container/Layaout
        Container container = getContentPane();
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints cns = new GridBagConstraints();
        Insets insetsLeft = new Insets(5, 10, 5, 3);
        Insets insetsRigth = new Insets(5, 3, 5, 10);
        Insets insetsButtons = new Insets(10, 50, 10, 50);
        // Set Layout
        container.setLayout(layout);
        /********************
         * Create Components
         ********************/
        // Labels
        WebLabel workSpaceLabel = new WebLabel(Assets.getString("WORK_SPACE") + ":", WebLabel.RIGHT);
        WebLabel selectLanguageLabel = new WebLabel(Assets.getString("SELECT_LANGUAGE") + ":", WebLabel.RIGHT);
        WebLabel enableTransitionsLabel = new WebLabel(Assets.getString("ENABLE_TRANSITIONS") + ":", WebLabel.RIGHT);
        // Transitions on/off switch
        transitionsSwitch = new WebSwitch(data.isTransitionsEnabled());
        TooltipManager.setTooltip(transitionsSwitch, Assets.getString("ENABLE_DISABLE_TRANSITIONS"), TooltipWay.right, 500);
        transitionsSwitch.getLeftComponent().setText(Assets.getString("ON"));
        transitionsSwitch.getRightComponent().setText(Assets.getString("OFF"));
        transitionsSwitch.setRound(3);
        // Text field with button as trailing component
        directoryField = new WebTextField(data.getWorkSpacePath(), 30);
        directoryField.setInputPrompt(Assets.getString("SELECT_FOLDER"));
        directoryField.setEditable(false);
        directoryField.setRound(0);
        WebButton folderButton = new WebButton(Assets.loadIcon("ic_folder" , 18));
        TooltipManager.setTooltip(folderButton, Assets.getString("SELECT_FOLDER"), TooltipWay.left, 500);
        folderButton.setDrawSides(false, true, false, false);
        folderButton.setDrawLines(false, true, false, false);
        folderButton.setDrawFocus(false);
        directoryField.setTrailingComponent(folderButton);
        WebDirectoryChooser directoryChooser = new WebDirectoryChooser(this);
        File selectedFolder = new File(data.getWorkSpacePath());
        directoryChooser.setSelectedDirectory(selectedFolder);
        folderButton.addActionListener((final ActionEvent e) -> {
            directoryChooser.setVisible(true);            
            if (directoryChooser.getResult() == DialogOptions.OK_OPTION) {
                final File file = directoryChooser.getSelectedDirectory();
                directoryField.setText(file.getAbsolutePath());
            }
        });
        // Language ComboBox
        languageComboBox = new WebComboBox(Assets.AVAILABLE_LOCALES);
        TooltipManager.setTooltip(languageComboBox, Assets.getString("SELECT_LANGUAGE"), TooltipWay.right, 500);
        languageComboBox.setRound(3);
        languageComboBox.setSelectedItem(data.getCurrentLocale());
        languageComboBox.setRenderer(new WebComboBoxCellRenderer() {
            @Override
            public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
                final String language = (String) value;
                final WebComboBoxElement renderer = (WebComboBoxElement) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                Pair<String, String> data = Assets.getLocaleData(language);
                renderer.setIcon(Assets.loadIcon(data.getFirst(), 18));
                renderer.setText(Assets.getString(data.getSecond()));
                return renderer;
            }
        });
        // Accept Button
        WebButton accept = new WebButton(Assets.getString("ACCEPT"));
        accept.addActionListener(acceptListener);
        HotkeyManager.registerHotkey(this, accept, Hotkey.ENTER, new ButtonHotkeyRunnable(accept, 150), TooltipWay.trailing);
        TooltipManager.setTooltip(accept, Assets.getString("ACCEPT_CHANGES"), TooltipWay.up, 200);
        // Cancel Button
        WebButton cancel = new WebButton(Assets.getString("CANCEL"));
        cancel.addActionListener(cancelListener);
        HotkeyManager.registerHotkey(this, cancel, Hotkey.ESCAPE, new ButtonHotkeyRunnable(cancel, 150), TooltipWay.trailing);
        TooltipManager.setTooltip(cancel, Assets.getString("DISCARD_CHANGES"), TooltipWay.up, 200);
        /*****************
         * Add Components
         *****************/
        // Init constrains
        cns.fill = GridBagConstraints.HORIZONTAL;
        // Workspace label
        cns.gridx = 0; cns.gridy = 0; cns.insets = insetsLeft;
        container.add(workSpaceLabel, cns);
        // Workspace field
        cns.gridx = 1; cns.gridy = 0; cns.insets = insetsRigth; cns.gridwidth = 3;
        container.add(directoryField, cns);
        // Language label
        cns.gridx = 0; cns.gridy = 1; cns.insets = insetsLeft; cns.gridwidth = 1; 
        container.add(selectLanguageLabel, cns);
        // Language Combobox
        cns.gridx = 1; cns.gridy = 1; cns.insets = insetsRigth; cns.gridwidth = 2;
        container.add(languageComboBox, cns);
        // Enable transitions label
        cns.gridx = 0; cns.gridy = 2; cns.insets = insetsLeft; cns.gridwidth = 1; 
        container.add(enableTransitionsLabel, cns);
        // Enable transitions switch
        cns.gridx = 1; cns.gridy = 2; cns.insets = insetsRigth;
        container.add(transitionsSwitch, cns);
        // Accept/Cancel button
        cns.gridx = 1; cns.gridy = 3; cns.gridwidth = 0; cns.insets = insetsButtons;  
        container.add(new GroupPanel(GroupingType.none, 4, true, accept, cancel), cns);
        // Dialog Icon
        setIconImage(Assets.loadImage("ic_settings"));
        // Dialog size
        setResizable(false);
        pack();
        setLocationRelativeTo(getOwner());
        setVisible(true);
    }

    /**
     * Cancel Button Listener
     */
    ActionListener cancelListener = (ActionEvent e) -> {
        result = false;
        dispose();
    };
    
    /**
     * Accept Button Listener
     */
    ActionListener acceptListener = (ActionEvent e) -> {
        boolean valid = true;
        // Checks
        String path = directoryField.getText().trim();
        if (path.isEmpty()) {
            TooltipManager.showOneTimeTooltip(directoryField, null, "<html><center><font color=orange>"+ Assets.getString("INVALID_PATH") +"</font></center></html>", TooltipWay.leading);
            valid = false;
        }
        // If valid data close dialog
        if (valid) {
            result = true;
            dispose();
        }
    };
    
    /**
     * Get Dialog Result DTO
     * @return SettingsDTO with changes 
     */
    @Override
    public SettingsDTO getResults() {
        // If accept return dto with data and mark as changed (if necessary)
        if (result) {
            // Workspace change
            String workspace = directoryField.getText().trim();
            if (!data.getWorkSpacePath().equals(workspace)) {
                data.setWorkSpacePath(workspace);
                data.setChanged(true);
            }
            // Locale chante
            String locale = (String)languageComboBox.getSelectedItem();
            if (!data.getCurrentLocale().equals(locale)) {
                data.setCurrentLocale(locale);
                data.setChanged(true);
            }
            // Transitions
            boolean transitions = transitionsSwitch.isSelected();
            if (data.isTransitionsEnabled() != transitions) {
                data.setTransitionsEnabled(transitions);
                data.setChanged(true);
            }
            return data;
        }
        // if cancel process return empty dto mark as not changed
        return data;
    }
}