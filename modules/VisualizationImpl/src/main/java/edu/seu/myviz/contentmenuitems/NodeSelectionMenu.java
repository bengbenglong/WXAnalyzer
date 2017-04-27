/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.myviz.contentmenuitems;

import edu.seu.detaildisplayer.api.ShowDetail;
import javax.swing.Icon;
import javax.swing.JPopupMenu;
import org.gephi.visualization.apiimpl.contextmenuitems.BasicItem;
import org.gephi.visualization.spi.GraphContextMenuItem;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author hp-6380
 */
//@ServiceProvider(service= GraphContextMenuItem.class )
public class NodeSelectionMenu extends BasicItem{
    
    public NodeSelectionMenu(){
        
    }
    
//    public NodeSelectionMenu(final Node selectedNode){
//        
//        JMenuItem deleteM=new JMenuItem("删除", new ImageIcon(getClass().getResource("/edu/seu/myviz/image/delete.png")));
//        deleteM.addActionListener(new ActionListener(){
//            @Override
//            public void actionPerformed(ActionEvent e) {
//               NotifyDescriptor.Confirmation notifyDescriptor = new NotifyDescriptor.Confirmation(
//                                    "是否确认删除此节点",
//                                    "提示", NotifyDescriptor.YES_NO_OPTION);
//                if (DialogDisplayer.getDefault().notify(notifyDescriptor).equals(NotifyDescriptor.YES_OPTION)) {
//                    GraphElementsController gec = Lookup.getDefault().lookup(GraphElementsController.class);
//                    gec.deleteNode(selectedNode);
//                }
//                
//            }
//        });
//        
//        JMenuItem showDetailM=new JMenuItem("显示节点详细信息");
//        showDetailM.addActionListener(new ActionListener(){
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                
//            }
//        });
//    }

    @Override
    public void execute() {
        ShowDetail showDetail=Lookup.getDefault().lookup(ShowDetail.class);
        for(int i=0; i<nodes.length; i++){
            showDetail.showDetail(nodes[i]);
        }
    }

    @Override
    public String getName() {
        return "显示节点详情";
    }

    @Override
    public boolean canExecute() {
        return nodes!=null&&nodes.length>0;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public int getPosition() {
        return 0;
    }

    @Override
    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("edu/seu/viz/image/detail.png", false);
    }

}
