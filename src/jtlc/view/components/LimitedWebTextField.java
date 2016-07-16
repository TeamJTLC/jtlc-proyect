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
package jtlc.view.components;

import com.alee.laf.text.WebTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * Limited Lenth TextField (using as base WebTextField)
 * Implements a limited characters (count) input text field.
 * 
 * @author Cristian Tardivo
 */
public class LimitedWebTextField extends WebTextField {

    /**
     * Create a new Limited TextField
     * @param limit characters limit
     */
    public LimitedWebTextField(int limit) {
        super();
        super.setDocument(new JTextFieldLimit(limit));
    }
    
    /**
     * Private helper class. Implements textfield limiter
     */
    private class JTextFieldLimit extends PlainDocument {
        private final int limit;
        
        /**
         * Init textfield limit
         * @param limit 
         */
        JTextFieldLimit(int limit) {
            super();
            this.limit = limit;
        }

        /**
         * insertString override method.
         * Check string length before insert a new character/string
         * @param offset
         * @param str
         * @param attr
         * @throws BadLocationException 
         */
        @Override
        public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
            if (str == null)
                return;
                
            if ((getLength() + str.length()) <= limit)
                super.insertString(offset, str, attr);
        }
    }
}