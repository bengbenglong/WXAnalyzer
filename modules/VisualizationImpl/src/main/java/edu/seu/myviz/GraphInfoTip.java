/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.myviz;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.openide.util.Lookup;

/**
 *
 * @author hp-6380
 */
public class GraphInfoTip extends javax.swing.JPanel {

    private Color blueColor=new Color(0,150,255);
    private int delay=500;
    private ActionListener action;
    private Timer timer;
    
//    private void test(int data) throws IOException {
//		Selector selector = Selector.open( );
//		SelectableChannel sc=null;
//		sc.register(selector, SelectionKey.OP_ACCEPT);
//    }
    
    /**
     * Creates new form NodeColorTip
     */
    public GraphInfoTip() {
        initComponents();
        final ProjectController pc=Lookup.getDefault().lookup(ProjectController.class);
        pc.addWorkspaceListener(new WorkspaceListener(){
            @Override
            public void initialize(Workspace workspace) {
            }

            @Override
            public void select(final Workspace workspace) {
                //先停止之前的
                if(timer!=null&&timer.isRunning()){
                    timer.stop();
                }
                
                //如果非0，则直接显示
                if(workspace.getLookup().lookup(GraphModel.class).getGraph().getNodeCount()==0){
                    action=new ActionListener(){
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if(workspace.getLookup().lookup(GraphModel.class).getGraph().getNodeCount()!=0){
                                setText(workspace.getLookup().lookup(GraphModel.class));
                            }
                        }
                    };
                    timer=new Timer(delay, action);
                    timer.start();
                }else{
                    SwingUtilities.invokeLater(new Runnable(){
                        @Override
                        public void run() {
                            setText(workspace.getLookup().lookup(GraphModel.class));
                        }
                    });
                }
                    
            }

            @Override
            public void unselect(final Workspace workspace) {
                
            }

            @Override
            public void close(Workspace workspace) {
            }

            @Override
            public void disable() {
            }
        });
    }
    
    private void setText(GraphModel graphModel){
        Graph graph=graphModel.getGraph();
//        String graphDes=(String) graph.getAttribute("GraphDes")+"\t\t";
        String firstNodeDes=(String) graph.getAttribute("FirstNodeDes");
        String secondNodeDes=(String) graph.getAttribute("SecondNodeDes");
        String firstNodeColor=(String) graph.getAttribute("FirstNodeColor");
        String secondNodeColor=(String) graph.getAttribute("SecondNodeColor");
        if(firstNodeDes!=null&&secondNodeDes!=null&&firstNodeColor!=null&&secondNodeColor!=null){
            extraImage.setIcon(null);
            extraImageDes.setText(null);
            
            
//            graphDesText.setText(graphDes);
            firstDes.setText(firstNodeDes);
            secondDes.setText(secondNodeDes);
            switch((String)graph.getAttribute("FirstNodeColor")){
                case("蓝色"):
                    firstImage.setIcon(new ImageIcon(getClass().getResource("/edu/seu/viz/image/blue.png")));
                    break;
                case("红色"):
                    firstImage.setIcon(new ImageIcon(getClass().getResource("/edu/seu/viz/image/red.png")));
                    break;
                case("紫色"):
                    firstImage.setIcon(new ImageIcon(getClass().getResource("/edu/seu/viz/image/magenta.png")));
                    break;
                case("橙色"):
                   firstImage.setIcon(new ImageIcon(getClass().getResource("/edu/seu/viz/image/orange.png")));
                    break;
                case("浅黄"):
                    firstImage.setIcon(new ImageIcon(getClass().getResource("/edu/seu/viz/image/yellow.png")));
                    break;
                default:firstImage.setIcon(null);
            }
            switch((String) graph.getAttribute("SecondNodeColor")){
                case("蓝色"):
                    secondImage.setIcon(new ImageIcon(getClass().getResource("/edu/seu/viz/image/blue.png")));
                    break;
                case("红色"):
                    secondImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/seu/viz/image/red.png")));
                    break;
                case("紫色"):
                    secondImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/seu/viz/image/magenta.png")));
                    break;
                case("橙色"):
                    secondImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/seu/viz/image/orange.png")));
                    break;
                case("浅黄"):
                    firstImage.setIcon(new ImageIcon(getClass().getResource("/edu/seu/viz/image/yellow.png")));
                    break;
                default:secondImage.setIcon(null);
            }
        }
        
        String graphDes=(String) graph.getAttribute("GraphDes");
        if(graphDes.contains("消息传播概率路径网络")||graphDes.contains("最短路径")){
            
            extraImage.setIcon(new ImageIcon(getClass().getResource("/edu/seu/viz/image/blue.png")));
            extraImageDes.setText("群");
            firstImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/seu/viz/image/orange.png")));
            firstDes.setText("消息源");
            secondImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/seu/viz/image/yellow.png")));
            secondDes.setText("扩散节点");
        }
        //然后添加边图示
        
        
        
        if(graphDes.contains("群组间消息传播网")||graphDes.contains("最短路径")){
            edgeAndDes.setIcon(new ImageIcon(getClass().getResource("/edu/seu/viz/image/lineD.png")));
            edgeAndDes.setText("消息传播方向");
        }else if(graphDes.contains("消息传播概率路径网络")){
            edgeAndDes.setIcon(new ImageIcon(getClass().getResource("/edu/seu/viz/image/line.png")));
            edgeAndDes.setText("群与群之间存在消息传播可能性");
        }else if(graphDes.contains("关联人")){
            edgeAndDes.setIcon(new ImageIcon(getClass().getResource("/edu/seu/viz/image/line.png")));
            edgeAndDes.setText("好友关系");
        }else if(graphDes.contains("群")){
            edgeAndDes.setIcon(new ImageIcon(getClass().getResource("/edu/seu/viz/image/line.png")));
            edgeAndDes.setText("群成员关系");
        }
        
        
            
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        edgeAndDes = new javax.swing.JLabel();
        blank4 = new javax.swing.JLabel();
        extraImage = new javax.swing.JLabel();
        extraImageDes = new javax.swing.JLabel();
        blank3 = new javax.swing.JLabel();
        firstImage = new javax.swing.JLabel();
        firstDes = new javax.swing.JLabel();
        blank2 = new javax.swing.JLabel();
        secondImage = new javax.swing.JLabel();
        secondDes = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setOpaque(false);
        setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        edgeAndDes.setFont(new java.awt.Font("微软雅黑", 0, 15)); // NOI18N
        add(edgeAndDes);

        org.openide.awt.Mnemonics.setLocalizedText(blank4, "    "); // NOI18N
        add(blank4);
        add(extraImage);

        extraImageDes.setFont(new java.awt.Font("微软雅黑", 0, 15)); // NOI18N
        add(extraImageDes);

        org.openide.awt.Mnemonics.setLocalizedText(blank3, "    "); // NOI18N
        add(blank3);
        add(firstImage);

        firstDes.setFont(new java.awt.Font("微软雅黑", 0, 15)); // NOI18N
        add(firstDes);

        org.openide.awt.Mnemonics.setLocalizedText(blank2, "    "); // NOI18N
        add(blank2);
        add(secondImage);

        secondDes.setFont(new java.awt.Font("微软雅黑", 0, 15)); // NOI18N
        add(secondDes);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel blank2;
    private javax.swing.JLabel blank3;
    private javax.swing.JLabel blank4;
    private javax.swing.JLabel edgeAndDes;
    private javax.swing.JLabel extraImage;
    private javax.swing.JLabel extraImageDes;
    private javax.swing.JLabel firstDes;
    private javax.swing.JLabel firstImage;
    private javax.swing.JLabel secondDes;
    private javax.swing.JLabel secondImage;
    // End of variables declaration//GEN-END:variables
}
