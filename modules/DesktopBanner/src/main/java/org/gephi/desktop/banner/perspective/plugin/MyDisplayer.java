/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.desktop.banner.perspective.plugin;

import javax.swing.Icon;
import org.gephi.perspective.spi.Perspective;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author hp-6380
 */
//@ServiceProvider(service=Perspective.class, position=500)
public class MyDisplayer implements Perspective{
    @Override
    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/desktop/banner/perspective/plugin/resources/laboratory.png", false);
    }

    @Override
    public String getDisplayName() {
        return "图表显示";
    }

    @Override
    public String getName() {
        return "displayer";
    }
    
}
