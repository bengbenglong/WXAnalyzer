/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.desktop.banner;

import edu.seu.dataselector2.CardPanel2;
import edu.seu.dataselector2.Selector2;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import org.gephi.perspective.api.PerspectiveController;
import org.gephi.perspective.spi.Perspective;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class BannerComponent extends javax.swing.JPanel {

    private transient JToggleButton[] buttons;
    private transient final PerspectiveController pc;

    public BannerComponent() {
        initComponents();

        //Init perspective controller
        pc = Lookup.getDefault().lookup(PerspectiveController.class);

        addGroupTabs();

        //=============测试按钮=============
        logoButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
//                WindowManager.getDefault().setRole("overview");
                for(Perspective p:pc.getPerspectives()){
                    if(p.getDisplayName().equals("网络展示")){
                        pc.selectPerspective(p);
                    }
                }
            }
        });
        
        

        //This defines the height of the banner bar - with an extra height on MacOS X
//        mainPanel.setPreferredSize(new Dimension(100, 35 + (UIUtils.isAquaLookAndFeel() ? 10 : 0)));
//        setPreferredSize(new Dimension(300, 55 + (UIUtils.isAquaLookAndFeel() ? 10 : 0)));
    }

    private void addGroupTabs() {
        //互斥按钮组
        buttons = new JPerspectiveButton[pc.getPerspectives().length];
        int i = 0;

        //Add tabs
        for (final Perspective perspective : pc.getPerspectives()) {
            JPerspectiveButton toggleButton = new JPerspectiveButton(perspective.getDisplayName(), perspective.getIcon());
            toggleButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    pc.selectPerspective(perspective);
                }
            });
            perspectivesButtonGroup.add(toggleButton);
            buttonsPanel.add(toggleButton);
            buttons[i++] = toggleButton;
        }

        //添加数据选择和图表展示按钮

        //设置当前被选中的按钮
        perspectivesButtonGroup.setSelected(buttons[getSelectedPerspectiveIndex()].getModel(), true);
    }

    public int getSelectedPerspectiveIndex() {
        int i = 0;
        for (Perspective p : pc.getPerspectives()) {
            if (p.equals(pc.getSelectedPerspective())) {
                return i;
            }
            i++;
        }
        return -1;
    }

    //Not working
    /*public void reset() {
    refreshSelectedPerspective();
    for (final Perspective group : Lookup.getDefault().lookupAll(Perspective.class).toArray(new Perspective[0])) {
    TopComponentGroup tpg = WindowManager.getDefault().findTopComponentGroup(group.getName());
    if (group.getName().equals(selectedPerspective)) {
    tpg.open();
    } else {
    tpg.close();
    }
    }
    }*/
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        perspectivesButtonGroup = new javax.swing.ButtonGroup();
        mainPanel = new javax.swing.JPanel();
        logoButton = new javax.swing.JButton();
        groupsPanel = new javax.swing.JPanel();
        buttonsPanel = new javax.swing.JPanel();
        bannerBackground = new javax.swing.JLabel();
        workspacePanel = new org.gephi.desktop.banner.workspace.WorkspacePanel();

        setBackground(new java.awt.Color(255, 255, 255));

        mainPanel.setBackground(new java.awt.Color(255, 255, 255));
        mainPanel.setMinimumSize(new java.awt.Dimension(734, 70));
        mainPanel.setLayout(new java.awt.GridBagLayout());

        logoButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/banner/resources/logo_transparency_60.png"))); // NOI18N
        logoButton.setToolTipText(org.openide.util.NbBundle.getMessage(BannerComponent.class, "BannerComponent.logoButton.toolTipText")); // NOI18N
        logoButton.setBorderPainted(false);
        logoButton.setContentAreaFilled(false);
        logoButton.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        logoButton.setFocusPainted(false);
        logoButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        logoButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/banner/resources/logo_transparency_60_lomo.png"))); // NOI18N
        logoButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/banner/resources/logo_transparency_60_lomo.png"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        mainPanel.add(logoButton, gridBagConstraints);

        groupsPanel.setBackground(new java.awt.Color(255, 255, 255));
        groupsPanel.setLayout(new java.awt.GridBagLayout());

        buttonsPanel.setBackground(new java.awt.Color(255, 255, 255));
        buttonsPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        buttonsPanel.setFont(new java.awt.Font("微软雅黑", 0, 15)); // NOI18N
        buttonsPanel.setOpaque(false);
        buttonsPanel.setPreferredSize(new java.awt.Dimension(10, 50));
        buttonsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        groupsPanel.add(buttonsPanel, gridBagConstraints);

        bannerBackground.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/banner/resources/bannerback.png"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        groupsPanel.add(bannerBackground, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        mainPanel.add(groupsPanel, gridBagConstraints);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 884, Short.MAX_VALUE)
            .addComponent(workspacePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(workspacePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bannerBackground;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JPanel groupsPanel;
    private javax.swing.JButton logoButton;
    private javax.swing.JPanel mainPanel;
    private javax.swing.ButtonGroup perspectivesButtonGroup;
    private javax.swing.JPanel workspacePanel;
    // End of variables declaration//GEN-END:variables

    private static class JPerspectiveButton extends JToggleButton {

        /**
         * perspective按钮
         * @param text 按钮名称
         * @param icon 按钮logo，该图标绘制在背景图的左上角的（x,y）起始的位置上。
         */
        public JPerspectiveButton(String text, Icon icon) {
            initButton(text);
        }
        
        private void initButton(String text){
            setText(null);
            setBorder(null);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

            if(text.equals("查询选择")){
                setIcon(new ImageIcon(getClass().getResource("/edu/seu/desktop/banner/images/aud_normal.png")));
                setRolloverIcon(new ImageIcon(getClass().getResource("/edu/seu/desktop/banner/images/aud_hover.png")));
                setSelectedIcon(new ImageIcon(getClass().getResource("/edu/seu/desktop/banner/images/aud_selected.png")));
                
                //如果点击查询选择按钮，则默认返回查询选择主界面，这段代码就是为了返回主界面的。
                this.addActionListener(new ActionListener(){
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Selector2 selector=Selector2.getInstance(CardPanel2.getInstance());
                        selector.returnHome();
                    }
                });
            }else{
                setIcon(new ImageIcon(getClass().getResource("/edu/seu/desktop/banner/images/graph_normal.png")));
                setRolloverIcon(new ImageIcon(getClass().getResource("/edu/seu/desktop/banner/images/graph_hover.png")));
                setSelectedIcon(new ImageIcon(getClass().getResource("/edu/seu/desktop/banner/images/graph_selected.png")));
            }
        }
            
        
//        private MyAuditButton(){
//            setText(null);
//            setBorder(null);
//            setBorderPainted(false);
//            setContentAreaFilled(false);
//            setFocusPainted(false);
//            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//            setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
//
//            setIcon(new ImageIcon(getClass().getResource("/edu/seu/dataselector/Brandnew/image/chat_16.png")));
//            setRolloverIcon(new ImageIcon(getClass().getResource("/edu/seu/dataselector/Brandnew/image/chat_16.png")));
//            setSelectedIcon(new ImageIcon(getClass().getResource("/edu/seu/dataselector/Brandnew/image/chat_16.png")));
//        }
    }
}
