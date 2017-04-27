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
 * Database Perspective. 从数据库提取并显示数据面板。
 * @author Li Zhenglong
 */
//@ServiceProvider(service=Perspective.class,position=500)
public class MyLabPerspective implements Perspective{
    
    @Override
    public String getDisplayName() {
        return "数据库操作";
        
    }

    @Override
    public String getName() {
        return "mydatalab";
    }

    @Override
    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/desktop/banner/perspective/plugin/resources/database.png", false);
    }
    
}
