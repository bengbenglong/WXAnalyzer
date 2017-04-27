/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.myviz.contentmenuitems;

import edu.seu.myviz.egoNetBuilder.EgoNetBuilder;
import javax.swing.Icon;
import org.gephi.visualization.apiimpl.contextmenuitems.BasicItem;
import org.gephi.visualization.spi.GraphContextMenuItem;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author hp-6380
 */
@ServiceProvider(service=GraphContextMenuItem.class)
public class FriendEgoMenu extends BasicItem{

    
    @Override
    public void execute() {
        String nodeType=(String) nodes[0].getAttribute("节点性质");
        EgoNetBuilder builder=new EgoNetBuilder();
        if(nodeType.contains("关联人")||nodeType.contains("群成员")){
            builder.buildEgoNet(EgoNetBuilder.FRINED_TYPE, nodes);
        }else if(nodeType.equals("群")){
            builder.buildEgoNet(EgoNetBuilder.GROUP_TYPE, nodes);
        }
    }

    @Override
    public String getName() {
        return "构建该节点的二阶关系网络";
    }

    @Override
    public boolean canExecute() {
        
        return graph!=null&&nodes.length>0;
    }

    @Override
    public int getType() {
        return 100;
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
