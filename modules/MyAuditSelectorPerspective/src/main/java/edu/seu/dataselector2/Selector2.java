/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.dataselector2;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author hp-6380
 */
public class Selector2 extends javax.swing.JPanel {
    
    private static volatile Selector2 instance;
//    private DataSelectorTopComponent tp;
    private CardPanel2 tp;
    private Color backSelectedColor=new Color(153,204,255);
    private Color backDefaultColor=new Color(240,240,240);

    public static final String KEY_SELECTOR="KEY_SELECTOR";
    public static final String FRIEND_EGO_SELECTOR="FRIEND_EGO_SELECTOR";
    public static final String FRIENDS_SELECTOR="FRIENDS_SELECTOR";
    public static final String GROUP_EGO_SELECTOR="GROUP_EGO_SELECTOR";
    public static final String GROUPS_SELECTOR="GROUPS_SELECTOR";
    
    private KeySelector2 keySelector;
    private FriendEgoSelector friendEgoSelector;
    private FriendsSelector friendsSelector; 
    private GroupEgoSelector groupEgoSelector;
    private GroupsSelector groupsSelector;
    
    private Set<JButton> buttonSet=new HashSet<>();
    
    private CardLayout card=new CardLayout();
    /**
     * Creates new form Selector
     */
    private Selector2(CardPanel2 tp) {
        try{
            initComponents();
            initAction();
            this.tp=tp;

            panel.setLayout(card);
            keySelector=KeySelector2.getInstance();
            friendEgoSelector=FriendEgoSelector.getInstance();
            friendsSelector=FriendsSelector.getInstance();
            groupEgoSelector=GroupEgoSelector.getInstance();
            groupsSelector=GroupsSelector.getInstance();
            panel.add(keySelector);
            panel.add(friendEgoSelector);
            panel.add(friendsSelector);
            panel.add(groupEgoSelector);
            panel.add(groupsSelector);

            card.addLayoutComponent(keySelector, KEY_SELECTOR);
            card.addLayoutComponent(friendEgoSelector, FRIEND_EGO_SELECTOR);
            card.addLayoutComponent(friendsSelector, FRIENDS_SELECTOR);
            card.addLayoutComponent(groupEgoSelector, GROUP_EGO_SELECTOR);
            card.addLayoutComponent(groupsSelector, GROUPS_SELECTOR);
            
        }catch(Exception ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "selector 出错");
        }
        
        buttonSet.add(keyword);
        buttonSet.add(groups);
        buttonSet.add(groupEgo);
        buttonSet.add(friends);
        buttonSet.add(friendEgo);
            
    }
    
    public static synchronized Selector2 getInstance(CardPanel2 tp){
        if(instance==null){
            instance=new Selector2(tp);
        }
        return instance;
    }
    
    public void setSelectedButton(String key){
        for(JButton b:buttonSet){
            b.setSelected(false);
            b.setBackground(backDefaultColor);
        }
        switch (key) {
            case KEY_SELECTOR:
                keyword.setSelected(true);
                keyword.setBackground(backSelectedColor);
                break;
            case FRIEND_EGO_SELECTOR:
                friendEgo.setSelected(true);
                friendEgo.setBackground(backSelectedColor);
                break;
            case FRIENDS_SELECTOR:
                friends.setSelected(true);
                friends.setBackground(backSelectedColor);
                break;
            case GROUP_EGO_SELECTOR:
                groupEgo.setSelected(true);
                groupEgo.setBackground(backSelectedColor);
                break;
            default:
                groups.setSelected(true);
                groups.setBackground(backSelectedColor);
                break;
        }
    }
    
    private void initAction(){
        keyword.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                card.show(panel, KEY_SELECTOR);
                //设置什么按钮被选中
                for(JButton b:buttonSet){
                    b.setSelected(false);
                    b.setBackground(backDefaultColor);
                }
                keyword.setSelected(true);
                keyword.setBackground(backSelectedColor);
            }
        });
        
        groups.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                card.show(panel, GROUPS_SELECTOR);
                //设置什么按钮被选中
                for(JButton b:buttonSet){
                    b.setSelected(false);
                    b.setBackground(backDefaultColor);
                }
                groups.setSelected(true);
                groups.setBackground(backSelectedColor);
            }
        });
        
        groupEgo.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                card.show(panel, GROUP_EGO_SELECTOR);
                //设置什么按钮被选中
                for(JButton b:buttonSet){
                    b.setSelected(false);
                    b.setBackground(backDefaultColor);
                }
                groupEgo.setSelected(true);
                groupEgo.setBackground(backSelectedColor);
            }
        });
        
        friendEgo.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                card.show(panel, FRIEND_EGO_SELECTOR);
                //设置什么按钮被选中
                for(JButton b:buttonSet){
                    b.setSelected(false);
                    b.setBackground(backDefaultColor);
                }
                friendEgo.setSelected(true);
                friendEgo.setBackground(backSelectedColor);
            }
        });
        
        friends.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                card.show(panel, FRIENDS_SELECTOR);
                //设置什么按钮被选中
                for(JButton b:buttonSet){
                    b.setSelected(false);
                    b.setBackground(backDefaultColor);
                }
                friends.setSelected(true);
                friends.setBackground(backSelectedColor);
            }
        });
        
        home.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                tp.showHomePage();
            }
        });
        
    }
    
    //返回主页
    public void returnHome(){
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                tp.showHomePage();
            }
        });
    }
    
    
    
    
    
    
    
    
    public void showPanel(String name){
        card.show(panel, name);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panel = new javax.swing.JPanel();
        keyword = new javax.swing.JButton();
        groups = new javax.swing.JButton();
        home = new javax.swing.JButton();
        friendEgo = new javax.swing.JButton();
        friends = new javax.swing.JButton();
        groupEgo = new javax.swing.JButton();

        panel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout panelLayout = new javax.swing.GroupLayout(panel);
        panel.setLayout(panelLayout);
        panelLayout.setHorizontalGroup(
            panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        panelLayout.setVerticalGroup(
            panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 564, Short.MAX_VALUE)
        );

        keyword.setFont(new java.awt.Font("微软雅黑", 0, 15)); // NOI18N
        keyword.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/seu/dataselector/button/5_16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(keyword, org.openide.util.NbBundle.getMessage(Selector2.class, "Selector2.keyword.text")); // NOI18N

        groups.setFont(new java.awt.Font("微软雅黑", 0, 15)); // NOI18N
        groups.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/seu/dataselector/button/4_16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(groups, org.openide.util.NbBundle.getMessage(Selector2.class, "Selector2.groups.text")); // NOI18N

        home.setFont(new java.awt.Font("微软雅黑", 0, 15)); // NOI18N
        home.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/seu/dataselector/Brandnew/image/home_16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(home, "主页"); // NOI18N
        home.setOpaque(false);

        friendEgo.setFont(new java.awt.Font("微软雅黑", 0, 15)); // NOI18N
        friendEgo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/seu/dataselector/button/1_16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(friendEgo, org.openide.util.NbBundle.getMessage(Selector2.class, "Selector2.friendEgo.text")); // NOI18N

        friends.setBackground(new java.awt.Color(153, 204, 255));
        friends.setFont(new java.awt.Font("微软雅黑", 0, 15)); // NOI18N
        friends.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/seu/dataselector/button/2_16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(friends, org.openide.util.NbBundle.getMessage(Selector2.class, "Selector2.friends.text")); // NOI18N

        groupEgo.setFont(new java.awt.Font("微软雅黑", 0, 15)); // NOI18N
        groupEgo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/seu/dataselector/button/3_16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(groupEgo, org.openide.util.NbBundle.getMessage(Selector2.class, "Selector2.groupEgo.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(home, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(friendEgo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(friends, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(groupEgo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(groups, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(keyword, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addComponent(panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(keyword)
                    .addComponent(groups)
                    .addComponent(home)
                    .addComponent(friendEgo)
                    .addComponent(friends)
                    .addComponent(groupEgo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton friendEgo;
    private javax.swing.JButton friends;
    private javax.swing.JButton groupEgo;
    private javax.swing.JButton groups;
    private javax.swing.JButton home;
    private javax.swing.JButton keyword;
    private javax.swing.JPanel panel;
    // End of variables declaration//GEN-END:variables
}
