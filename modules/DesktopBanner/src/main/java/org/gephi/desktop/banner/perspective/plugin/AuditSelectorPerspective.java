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
@ServiceProvider(service=Perspective.class,position=0)
public class AuditSelectorPerspective implements Perspective{

    @Override
    public String getDisplayName() {
        return "查询选择";
    }

    @Override
    public String getName() {
        return "auditSelector";
    }

    @Override
    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/desktop/banner/perspective/plugin/resources/control_48.png", false);
    }
    
}
