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
package jtlc.core.reports;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.script.ScriptEngineManager;
import jtlc.assets.Assets;
import org.jdom.JDOMException;
import org.jopendocument.dom.template.EngineTemplate;
import org.jopendocument.dom.template.TemplateException;
import org.jopendocument.dom.template.engine.ScriptEngineDataModel;

/**
 * This class implement a template for reports creation.
 * Allows to load templates from assets folder.
 * The template uses javascript code to replace values and fill cells.
 * 
 * @author Cristian Tardivo
 */
public class Template extends EngineTemplate {
        
    /**
     * Load template from system file
     * @param f file to load
     * @throws IOException
     * @throws TemplateException
     * @throws JDOMException 
     */
    public Template(File f) throws IOException, TemplateException, JDOMException {
        super(f, new ScriptEngineDataModel(new ScriptEngineManager().getEngineByName("javascript")));
    }
    
    /**
     * Load template from inputstream
     * @param is inputstream to read
     * @throws IOException
     * @throws TemplateException
     * @throws JDOMException 
     */
    public Template(InputStream is) throws IOException, TemplateException, JDOMException {
        super(is, new ScriptEngineDataModel(new ScriptEngineManager().getEngineByName("javascript")));
    }
    
    /**
     * Load template from templates assets folder
     * @param name Template name with extension
     * @throws IOException
     * @throws TemplateException
     * @throws JDOMException 
     */
    public Template(String name) throws IOException, TemplateException, JDOMException {
        this(Assets.loadTemplate(name));
    }
}