/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.myviz.contentmenuitems;

import edu.seu.networkbuild.api.BuildAttribute;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import org.gephi.datalab.spi.ContextMenuItemManipulator;
import org.gephi.visualization.apiimpl.contextmenuitems.BasicItem;
import org.gephi.visualization.spi.GraphContextMenuItem;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author hp-6380
 */
@ServiceProvider(service=GraphContextMenuItem.class)
public class DiffRangeMenu extends BasicItem{

    @Override
    public void execute() {
    }
    
    @Override
    public ContextMenuItemManipulator[] getSubItems() {
        List<ContextMenuItemManipulator> items=new ArrayList<>();
        items.add(new DiffRangeSubItem());
        items.add(new DiffRangeNewWorkspaceSubItem());
        items.add(new DiffMinSpaningTreeSubItem());
        
//        items[2]=new DiffMinSpaningTreeSubItem();
        return items.toArray(new ContextMenuItemManipulator[0]);
    }
    

    @Override
    public String getName() {
        return "消息传播网络";
    }

    @Override
    public boolean canExecute() {
        
        return graph!=null&&graph.getAttribute(BuildAttribute.BUILD_TYPE)==BuildAttribute.DIFF_GROUP_TYPE;
    }

    @Override
    public int getType() {
        return 200;
    }

    @Override
    public int getPosition() {
        return 0;
    }

    @Override
    public Icon getIcon() {
        return null;
    }
    
}
