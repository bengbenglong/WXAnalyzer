/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.manipulators.nodes;

import edu.seu.detaildisplayer.api.ShowDetail;
import javax.swing.Icon;
import org.gephi.datalab.plugin.manipulators.nodes.BasicNodesManipulator;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.graph.api.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/**
 *
 * @author hp-6380
 */
public class PrintSelectedNodesTest extends BasicNodesManipulator{

    private Node[] nodes;
    
    @Override
    public void setup(Node[] nodes, Node clickedNode) {
        this.nodes=nodes;
    }

    @Override
    public void execute() {
        Node firstNode=nodes[0];
        ShowDetail showDetail=Lookup.getDefault().lookup(ShowDetail.class);
        showDetail.showDetail(firstNode);
    }

    @Override
    public String getName() {
        return "显示节点详情";
    }

    @Override
    public String getDescription() {
        return "显示节点详细信息";
    }

    @Override
    public boolean canExecute() {
        return nodes.length>0;
    }

    @Override
    public ManipulatorUI getUI() {
        return null;
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
        return ImageUtilities.loadImageIcon("edu/seu/datalab/plugin/images/detail.png", false);
    }
    
}
