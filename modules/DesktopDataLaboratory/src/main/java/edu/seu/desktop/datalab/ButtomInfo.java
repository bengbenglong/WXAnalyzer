/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.desktop.datalab;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;
import org.openide.util.Lookup;

/**
 *
 * @author hp-6380
 */
public class ButtomInfo extends javax.swing.JPanel {

    /**
     * Creates new form ButtonInfo
     */
    public ButtomInfo() {
        initComponents();
        initAction();
    }
    
    private void initAction(){
        int delay=500;
        ActionListener al=new ActionListener(){
           @Override
           public void actionPerformed(ActionEvent e){
                ProjectController pc=Lookup.getDefault().lookup(ProjectController.class);
                
                if(pc.getCurrentProject()!=null){
                    GraphController graphCtrl=Lookup.getDefault().lookup(GraphController.class);
                    GraphModel graphModel=graphCtrl.getGraphModel();
                    if(graphModel!=null){
                        Graph graph=graphModel.getGraph();
                        int nodeNum=graph.getNodeCount();
                        int edgeNum=graph.getEdgeCount();
                        nodeNumLabel.setText(String.valueOf(nodeNum));
                        edgeNumLabel.setText(String.valueOf(edgeNum));
                    }else{
                        nodeNumLabel.setText(String.valueOf(0));
                        edgeNumLabel.setText(String.valueOf(0));
                    }
                    
                }else{
                    nodeNumLabel.setText(String.valueOf(0));
                    edgeNumLabel.setText(String.valueOf(0));
                }
           }
        };
        new Timer(delay, al).start();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        nodeNumLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        edgeNumLabel = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ButtomInfo.class, "ButtomInfo.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(nodeNumLabel, org.openide.util.NbBundle.getMessage(ButtomInfo.class, "ButtomInfo.nodeNumLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ButtomInfo.class, "ButtomInfo.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(edgeNumLabel, org.openide.util.NbBundle.getMessage(ButtomInfo.class, "ButtomInfo.edgeNumLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nodeNumLabel)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(edgeNumLabel)
                .addGap(0, 290, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel1)
                .addComponent(nodeNumLabel)
                .addComponent(jLabel2)
                .addComponent(edgeNumLabel))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel edgeNumLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel nodeNumLabel;
    // End of variables declaration//GEN-END:variables
}
