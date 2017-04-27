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
package org.gephi.visualization.component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ButtonModel;
import javax.swing.JCheckBox;
import net.miginfocom.swing.MigLayout;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphController;
import org.gephi.visualization.text.TextModelImpl;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class LabelAttributesPanel extends javax.swing.JPanel {

    //Settings
    private ButtonModel selectedModel;
    private boolean showProperties = true;
    //Model
    private TextModelImpl textModel;
    private AttributesCheckBox[] nodeCheckBoxs;
    private AttributesCheckBox[] edgeCheckBoxs;

    /**
     * Creates new form LabelAttributesPanel
     */
    public LabelAttributesPanel() {
        initComponents();
//        selectedModel = nodesToggleButton.getModel();
//        elementButtonGroup.setSelected(selectedModel, true);
//        nodesToggleButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                if (nodesToggleButton.isSelected()) {
//                    selectedModel = nodesToggleButton.getModel();
//                    refresh();
//                }
//            }
//        });
//        edgesToggleButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                if (edgesToggleButton.isSelected()) {
//                    selectedModel = edgesToggleButton.getModel();
//                    refresh();
//                }
//            }
//        });
//        showPropertiesCheckbox.setSelected(showProperties);
//        showPropertiesCheckbox.addItemListener(new ItemListener() {
//            @Override
//            public void itemStateChanged(ItemEvent e) {
//                showProperties = showPropertiesCheckbox.isSelected();
//                refresh();
//            }
//        });
    }

    public void setup(TextModelImpl model) {
        this.textModel = model;
        refresh();
    }
    
    

    private void refresh() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);

        List<Column> availableColumns = new ArrayList<>();
        List<Column> selectedColumns = new ArrayList<>();
        AttributesCheckBox[] target;
//        if (elementButtonGroup.getSelection() == nodesToggleButton.getModel()) {
            for (Column c : graphController.getGraphModel().getNodeTable()) {
                if (!c.isProperty()&&AttributesCheckBox.neededProperties(c.getTitle())!=null) {//后面的判断句是我加的
                    availableColumns.add(c);
                } else if (showProperties && c.isProperty() && !c.getId().equals("timeset")&&AttributesCheckBox.neededProperties(c.getTitle())!=null) {//后面的判断句是我加的
                    availableColumns.add(c);
                }
            }

            if (textModel.getNodeTextColumns() != null) {
                selectedColumns = Arrays.asList(textModel.getNodeTextColumns());
            }
            nodeCheckBoxs = new AttributesCheckBox[availableColumns.size()];
            target = nodeCheckBoxs;
//        } else {
//            for (Column c : graphController.getGraphModel().getEdgeTable()) {
//                if (!c.isProperty()&&AttributesCheckBox.neededProperties(c.getTitle())!=null) {//后面的判断句是我加的
//                    availableColumns.add(c);
//                } else if (showProperties) {
//                    if (showProperties && c.isProperty() && !c.getId().equals("timeset")&&AttributesCheckBox.neededProperties(c.getTitle())!=null) {//后面的判断句是我加的
//                        availableColumns.add(c);
//                    }
//                }
//            }
//
//            if (textModel.getEdgeTextColumns() != null) {
//                selectedColumns = Arrays.asList(textModel.getEdgeTextColumns());
//            }
//            edgeCheckBoxs = new AttributesCheckBox[availableColumns.size()];
//            target = edgeCheckBoxs;
//        }
        contentPanel.removeAll();
        contentPanel.setLayout(new MigLayout("", "[pref!]"));
        for (int i = 0; i < availableColumns.size(); i++) {
            Column column = availableColumns.get(i);
            AttributesCheckBox c = new AttributesCheckBox(column, selectedColumns.contains(column));
            target[i] = c;
            contentPanel.add(c.getCheckBox(), "wrap");
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void unsetup() {
        List<Column> nodeColumnsList = new ArrayList<Column>();
        List<Column> edgeColumnsList = new ArrayList<Column>();
        if (nodeCheckBoxs != null) {
            for (AttributesCheckBox c : nodeCheckBoxs) {
                if (c.isSelected()) {
                    nodeColumnsList.add(c.getColumn());
                }
            }
        }
        if (edgeCheckBoxs != null) {
            for (AttributesCheckBox c : edgeCheckBoxs) {
                if (c.isSelected()) {
                    edgeColumnsList.add(c.getColumn());
                }
            }
        }
        if (edgeColumnsList.size() > 0 || nodeColumnsList.size() > 0) {
            textModel.setTextColumns(nodeColumnsList.toArray(new Column[0]), edgeColumnsList.toArray(new Column[0]));
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

        elementButtonGroup = new javax.swing.ButtonGroup();
        contentScrollPane = new javax.swing.JScrollPane();
        contentPanel = new javax.swing.JPanel();
        labelComment = new javax.swing.JLabel();

        contentPanel.setLayout(new java.awt.GridLayout(1, 0));
        contentScrollPane.setViewportView(contentPanel);

        labelComment.setText(org.openide.util.NbBundle.getMessage(LabelAttributesPanel.class, "LabelAttributesPanel.labelComment.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(contentScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 387, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelComment)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelComment)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(contentScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel contentPanel;
    private javax.swing.JScrollPane contentScrollPane;
    private javax.swing.ButtonGroup elementButtonGroup;
    private javax.swing.JLabel labelComment;
    // End of variables declaration//GEN-END:variables

    private static class AttributesCheckBox {

        private JCheckBox checkBox;
        private Column column;

        public AttributesCheckBox(Column column, boolean selected) {
            checkBox = new JCheckBox(neededProperties(column.getTitle()), selected);
            this.column = column;
        }
        
        //============我的代码===================
        private static String neededProperties(String column){
            Map<String,String> map=new HashMap<>();
            map.put("Id", "Id");
            map.put("USERID", "关联人ID");
            map.put("ACCOUNTNAME", "账号");
            map.put("FRIENDID", "已关注关联人ID");
            map.put("USERNAME", "姓名");
            map.put("MOBILEPHONE", "手机");
            map.put("NICKNAME", "昵称");
            map.put("SEX", "性别");
            map.put("COUNTRY", "国家");
            map.put("PROVINCE", "省份");
            map.put("CITY","城市");
            map.put("发送时间", "发送时间");
            map.put("消息内容", "消息内容");
            return map.get(column);
        }
        //======================================

        public void setSelected(boolean selected) {
            checkBox.setSelected(selected);
        }

        public boolean isSelected() {
            return checkBox.isSelected();
        }

        public JCheckBox getCheckBox() {
            return checkBox;
        }

        public Column getColumn() {
            return column;
        }
    }
}
