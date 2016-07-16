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

import com.alee.extended.date.WebDateField;
import com.alee.extended.panel.GroupPanel;
import com.alee.extended.panel.GroupingType;
import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.text.WebTextArea;
import com.alee.laf.text.WebTextField;
import com.alee.managers.hotkey.ButtonHotkeyRunnable;
import com.alee.managers.hotkey.Hotkey;
import com.alee.managers.hotkey.HotkeyManager;
import com.alee.managers.language.data.TooltipWay;
import com.alee.managers.tooltip.TooltipManager;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingConstants;
import jtlc.assets.Assets;
import jtlc.view.dialogs.dto.ProjectDTO;

/**
 * Project creation data Dialog.
 * @author Cristian Tardivo
 */
public class ProjectDialog extends JDialog implements IDialog {
    private boolean result;
    private WebTextField nameField;
    private WebDateField sdateField;
    private WebDateField adateField;
    private WebTextArea descriptionText;
    
    /**
     * Create New project creation data dialog and init components
     * @param parent dialog parent/owner frame
     */
    public ProjectDialog(JFrame parent) {
        super(parent, Assets.getString("CREATE_NEW_PROJECT"), true);
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
        WebLabel nameLabel = new WebLabel(Assets.getString("EXPERIMENT_NAME") + ":", WebLabel.RIGHT);
        WebLabel sdateLabel = new WebLabel(Assets.getString("SAMPLE_DATE") + ":", WebLabel.RIGHT);
        WebLabel adateLabel = new WebLabel(Assets.getString("ANALYSIS_DATE") + ":",  WebLabel.RIGHT);
        WebLabel descriptionLabel = new WebLabel(Assets.getString("DESCRIPTION") + ":",  WebLabel.RIGHT);
        // Name Field
        nameField = new WebTextField();
        nameField.setInputPrompt(Assets.getString("ENTER_EXPERIMENT_NAME"));
        nameField.setInputPromptFont(nameField.getFont().deriveFont(Font.ITALIC));
        // Sample date Field
        sdateField = new WebDateField(new Date());
        sdateField.setInputPrompt(Assets.getString("ENTER_DATE"));
        sdateField.setDateFormat(new SimpleDateFormat(Assets.getString("SHORT_DATE_FORMAT")));
        sdateField.setHorizontalAlignment(WebLabel.CENTER);
        sdateField.setInputPromptPosition(SwingConstants.CENTER);
        // Analysis date Field
        adateField = new WebDateField(new Date());
        adateField.setInputPrompt(Assets.getString("ENTER_DATE"));
        adateField.setDateFormat(new SimpleDateFormat(Assets.getString("SHORT_DATE_FORMAT")));
        adateField.setHorizontalAlignment(WebLabel.CENTER);
        adateField.setInputPromptPosition(SwingConstants.CENTER);
        // Description text area + scroll
        descriptionText = new WebTextArea();
        descriptionText.setInputPrompt(Assets.getString("ADD_PROJECT_DESCRIPTION"));
        descriptionText.setInputPromptVerticalPosition(SwingConstants.TOP);
        descriptionText.setInputPromptHorizontalPosition(SwingConstants.LEFT);
        descriptionText.setLineWrap(true);
        descriptionText.setWrapStyleWord(true);
        WebScrollPane descriptionField = new WebScrollPane(descriptionText);
        descriptionField.setPreferredSize(new Dimension(200, 150));
        // Accept Button
        WebButton accept = new WebButton(Assets.getString("ACCEPT"));
        accept.addActionListener(acceptListener);
        HotkeyManager.registerHotkey(this, accept, Hotkey.ENTER, new ButtonHotkeyRunnable(accept, 150), TooltipWay.trailing);
        TooltipManager.setTooltip(accept, Assets.getString("CONFIRM_DATA"), TooltipWay.up, 200);
        // Cancel Button
        WebButton cancel = new WebButton(Assets.getString("CANCEL"));
        cancel.addActionListener(cancelListener);
        HotkeyManager.registerHotkey(this, cancel, Hotkey.ESCAPE, new ButtonHotkeyRunnable(cancel, 150), TooltipWay.trailing);
        TooltipManager.setTooltip(cancel, Assets.getString("CANCEL_PROCESS"), TooltipWay.up, 200);
        // Add focus listener to avoid tigger "Enter" hotkey when is writting on description field
        ProjectDialog owner = this;
        descriptionText.addFocusListener(new FocusListener() {
            // When focus is gained, remove "enter" hotkey
            @Override
            public void focusGained(FocusEvent e) {
                accept.removeHotkeys();
            }
            // When focus is lost, add "enter" hotkey
            @Override
            public void focusLost(FocusEvent e) {
                HotkeyManager.registerHotkey(owner, accept, Hotkey.ENTER, new ButtonHotkeyRunnable(accept, 150), TooltipWay.trailing);
            }
        });
        /*****************
         * Add Components
         *****************/
        // Init constrains
        cns.fill = GridBagConstraints.HORIZONTAL;
        // Name label
        cns.gridx = 0; cns.gridy = 0; cns.insets = insetsLeft;
        container.add(nameLabel, cns);
        // Name field
        cns.gridx = 1; cns.gridy = 0; cns.insets = insetsRigth;
        container.add(nameField, cns);
        // Sample date label
        cns.gridx = 0; cns.gridy = 1; cns.insets = insetsLeft;
        container.add(sdateLabel, cns);
        // Sample date field
        cns.gridx = 1; cns.gridy = 1; cns.insets = insetsRigth;
        container.add(sdateField, cns);
        // Analysis date label
        cns.gridx = 0; cns.gridy = 2; cns.insets = insetsLeft;
        container.add(adateLabel, cns);
        // Analysis date field
        cns.gridx = 1; cns.gridy = 2; cns.insets = insetsRigth;
        container.add(adateField, cns);
        // Description label
        cns.gridx = 0; cns.gridy = 3; cns.anchor = GridBagConstraints.PAGE_START; cns.insets = insetsLeft;
        container.add(descriptionLabel, cns);
        // Description field
        cns.gridx = 1; cns.gridy = 3; cns.insets = insetsRigth;
        container.add(descriptionField, cns);
        // Accept/Cancel button
        cns.gridx = 0; cns.gridy = 4; cns.gridwidth = 2; cns.insets = insetsButtons;
        container.add(new GroupPanel(GroupingType.fillAll, 4, true, accept, cancel), cns);
        // Dialog Icon
        setIconImage(Assets.loadImage("ic_new"));
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
        // Check name
        String name = nameField.getText();
        if (name.isEmpty() || name.trim().isEmpty()) {
            TooltipManager.showOneTimeTooltip(nameField, null, "<html><center><font color=orange>"+ Assets.getString("INVALID_NAME") +"</font></center></html>", TooltipWay.leading);
            valid = false;
        }
        // Check sample date
        Date sdate = sdateField.getDate();
        if (sdate == null) {
            TooltipManager.showOneTimeTooltip(sdateField, null, "<html><center><font color=orange>"+ Assets.getString("INVALID_DATE") +"</font></center></html>", TooltipWay.leading);
            valid = false;
        }
        // Check analysis date
        Date adate = adateField.getDate();
        if (adate == null) {
            TooltipManager.showOneTimeTooltip(adateField, null, "<html><center><font color=orange>"+ Assets.getString("INVALID_DATE") +"</font></center></html>", TooltipWay.leading);
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
     * @return ProjectDTO with changes 
     */
    @Override
    public ProjectDTO getResults() {
        // If accept return new dto with data and mark as changed
        if (result) {
            ProjectDTO dto = new ProjectDTO(nameField.getText().trim(), descriptionText.getText().trim(), sdateField.getDate(), adateField.getDate());
            dto.setChanged(true);
            return dto;
        }
        // if cancel process return empty dto mark as not changed
        return new ProjectDTO();
    }
}