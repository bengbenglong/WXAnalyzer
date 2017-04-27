/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.dataselector2;

import edu.seu.layout.LayoutImpl3;
import edu.seu.layout.MyLayoutController;
import edu.seu.networkbuild.api.BuildDiffNet;
import edu.seu.statistics.api.CalStatistics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.perspective.api.PerspectiveController;
import org.gephi.perspective.spi.Perspective;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.utils.progress.ProgressTicketProvider;
import org.gephi.visualization.VizController;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

/**
 *
 * @author hp-6380
 */
public class HomeIndex2 extends javax.swing.JPanel {

    private volatile static HomeIndex2 instance;
    private CardPanel2 tp;
    /**
     * Creates new form HomeIndex2
     */
    
    private HomeIndex2(CardPanel2 tp) {
        initComponents();
        initAction();
        this.tp=tp;
    }
    
    public static synchronized HomeIndex2 getInstance(CardPanel2 tp){
        if(instance==null){
            instance=new HomeIndex2(tp);
        }
        return instance;
    }
    
    private void initAction(){
        friendEgo.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                tp.showPanel(Selector2.FRIEND_EGO_SELECTOR);
                
            }
        });
        
        
        friends.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                tp.showPanel(Selector2.FRIENDS_SELECTOR);
            }
        });
        
        groupEgo.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                tp.showPanel(Selector2.GROUP_EGO_SELECTOR);
            }
        });
        
        groups.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                tp.showPanel(Selector2.GROUPS_SELECTOR);
            }
        });
        
        keyword.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                tp.showPanel(Selector2.KEY_SELECTOR);
            }
        });
        
        diff.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                
                int type=JOptionPane.showConfirmDialog(null, "点击该按钮将无需手动选择任何数据，直接根据数据库中最近1年的微信通信记录构建消息传播概率路径网络，根据数据量的大小，可能会耗费数分钟至几十分钟分钟时间，请确定继续吗？", "提示", JOptionPane.YES_NO_OPTION);
                if(type==JOptionPane.YES_OPTION){
                    //将界面切换到可视化界面
                    WindowManager.getDefault().invokeWhenUIReady(new Runnable(){
                        @Override
                        public void run() {
                            PerspectiveController pc=Lookup.getDefault().lookup(PerspectiveController.class);
                            for(Perspective p:pc.getPerspectives()){
                                if(p.getName().equals("overview")){
                                    pc.selectPerspective(p);
                                }
                            }
                        }
                    });
                    
                    new SwingWorker<Void,Void>(){
                        ProgressTicketProvider progressProvider = Lookup.getDefault().lookup(ProgressTicketProvider.class);
                        ProgressTicket ticket = null;
                        @Override
                        protected Void doInBackground() throws Exception {
                            if(progressProvider!=null){
                                ticket=progressProvider.createTicket("开始构建消息传播路径网络网络...", null);
                            }

                            try{
                                BuildDiffNet diffBuilder=Lookup.getDefault().lookup(BuildDiffNet.class);
                                diffBuilder.build();

                                ticket.setDisplayName("计算节点中心性参数...");
                                ticket.start(5);
                                ticket.progress(4);
                                
                                CalStatistics calSta=Lookup.getDefault().lookup(CalStatistics.class);
                                calSta.execute();

//                                ticket.setDisplayName("网络节点布局...");
//                                ticket.progress(4);
//                                LayoutImpl layout=new LayoutImpl();
//                                layout.forceAtlasLayout2(graphModel);
                                ticket.finish();
                                
                                //定位图中心，然后进行节点布局
                                VizController.getInstance().getGraphIO().centerOnGraph();
                                GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getGraphModel();
                                final LayoutImpl3 layout=MyLayoutController.getLayout(graphModel);
                                ticket=progressProvider.createTicket(graphModel.getGraph().getAttribute("GraphDes")+"节点布局中...", new Cancellable(){
                                    @Override
                                    public boolean cancel() {
                                        layout.cancel(true);
                                        return true;
                                    }
                                });
                                ticket.start();
                                MyLayoutController layoutContainer=new MyLayoutController(layout);
                                if(graphModel==null||layout==null){

                                }else if(layout.isRunning()){
                                    layoutContainer.stopLayout();
                                }else{
                                    layoutContainer.startLayout();
                                }
                                //建好之后布局
                                ticket.finish();

                            }catch(Exception e){
                                e.printStackTrace();
                                if(ticket!=null){
                                    ticket.finish();
                                }
                                JOptionPane.showMessageDialog(null, "构建消息传播网络中出现错误");
                            }
                            return null;
                        }

                        @Override
                        protected void done(){
                           ticket.finish();
                        }
                    }.execute();
                }
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        friendEgo = new javax.swing.JButton();
        friends = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        keyword = new javax.swing.JButton();
        diff = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        groupEgo = new javax.swing.JButton();
        groups = new javax.swing.JButton();

        setBackground(new java.awt.Color(0, 153, 153));

        friendEgo.setFont(new java.awt.Font("方正姚体", 0, 36)); // NOI18N
        friendEgo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/seu/dataselector/button/1_48.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(friendEgo, "单一关联人二阶关系网络"); // NOI18N
        friendEgo.setMaximumSize(new java.awt.Dimension(600, 200));
        friendEgo.setPreferredSize(new java.awt.Dimension(500, 100));

        friends.setFont(new java.awt.Font("方正姚体", 0, 36)); // NOI18N
        friends.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/seu/dataselector/button/2_48.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(friends, "多关联人关系网络"); // NOI18N
        friends.setMaximumSize(new java.awt.Dimension(600, 200));
        friends.setPreferredSize(new java.awt.Dimension(500, 100));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(friendEgo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(72, 72, 72)
                .addComponent(friends, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(friendEgo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(friends, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(50, 50, 50))
        );

        keyword.setFont(new java.awt.Font("方正姚体", 0, 36)); // NOI18N
        keyword.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/seu/dataselector/button/5_48.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(keyword, org.openide.util.NbBundle.getMessage(HomeIndex2.class, "HomeIndex2.keyword.text")); // NOI18N
        keyword.setMaximumSize(new java.awt.Dimension(600, 200));
        keyword.setPreferredSize(new java.awt.Dimension(500, 100));

        diff.setFont(new java.awt.Font("方正姚体", 0, 36)); // NOI18N
        diff.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/seu/dataselector/button/6_48.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(diff, org.openide.util.NbBundle.getMessage(HomeIndex2.class, "HomeIndex2.diff.text")); // NOI18N
        diff.setMaximumSize(new java.awt.Dimension(600, 200));
        diff.setPreferredSize(new java.awt.Dimension(500, 100));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(keyword, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(72, 72, 72)
                .addComponent(diff, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(keyword, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(diff, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(52, 52, 52))
        );

        groupEgo.setFont(new java.awt.Font("方正姚体", 0, 36)); // NOI18N
        groupEgo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/seu/dataselector/button/3_48.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(groupEgo, org.openide.util.NbBundle.getMessage(HomeIndex2.class, "HomeIndex2.groupEgo.text")); // NOI18N
        groupEgo.setToolTipText(null);
        groupEgo.setMaximumSize(new java.awt.Dimension(600, 200));
        groupEgo.setPreferredSize(new java.awt.Dimension(500, 100));

        groups.setFont(new java.awt.Font("方正姚体", 0, 36)); // NOI18N
        groups.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/seu/dataselector/button/4_48.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(groups, org.openide.util.NbBundle.getMessage(HomeIndex2.class, "HomeIndex2.groups.text")); // NOI18N
        groups.setMaximumSize(new java.awt.Dimension(600, 200));
        groups.setPreferredSize(new java.awt.Dimension(500, 100));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(groupEgo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(72, 72, 72)
                .addComponent(groups, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(groupEgo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(groups, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(50, 50, 50))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton diff;
    private javax.swing.JButton friendEgo;
    private javax.swing.JButton friends;
    private javax.swing.JButton groupEgo;
    private javax.swing.JButton groups;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JButton keyword;
    // End of variables declaration//GEN-END:variables
}
