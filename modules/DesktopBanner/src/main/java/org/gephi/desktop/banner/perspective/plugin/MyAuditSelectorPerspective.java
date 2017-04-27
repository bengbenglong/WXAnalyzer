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
//@ServiceProvider(service=Perspective.class, position=400)
public class MyAuditSelectorPerspective implements Perspective{

    @Override
    public String getDisplayName() {
        return "监控数据选择";
    }

    @Override
    public String getName() {
        return "myAuditSelector";
    }

    @Override
    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/desktop/banner/perspective/plugin/resources/eventSelect_16.png", false);
    }
    
}
