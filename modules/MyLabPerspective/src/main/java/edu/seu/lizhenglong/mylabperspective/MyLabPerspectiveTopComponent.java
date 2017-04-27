/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.lizhenglong.mylabperspective;

import edu.seu.database.drivers.DatabaseTableSelector;
import edu.seu.layout.LayoutImpl;
import edu.seu.lizhenglong.mylabperspective.buildNetwork.BuildNetworkSwingWorker;
import edu.seu.lizhenglong.mylabperspective.utils.MyManipulatePanel;
import edu.seu.lizhenglong.mylabperspective.utils.MyProcessBar;
import edu.seu.lizhenglong.mylabperspective.utils.RightClickMenu;
import edu.seu.lizhenglong.mylabperspective.utils.TableHeaderRightClick;
import edu.seu.mydatafilter.tablefilter.api.TableDataFilter;
import edu.seu.mydatafilter.tablefilter.api.FilterTypeEnum;
import edu.seu.networkbuild.api.BuildDiffNet;
import edu.seu.statistics.api.CalStatistics;
import java.awt.Component;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.utils.progress.ProgressTicketProvider;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;

/**
 * Top component which displays something.
 */
//@ConvertAsProperties(
//        dtd = "-//edu.seu.lizhenglong.mylabperspective//MyLabPerspective//EN",
//        autostore = false
//)
//@TopComponent.Description(
//        preferredID = "MyLabPerspectiveTopComponent",
//        //iconBase="SET/PATH/TO/ICON/HERE", 
//        persistenceType = TopComponent.PERSISTENCE_ALWAYS
//)
//@TopComponent.Registration(mode = "editor", openAtStartup = false, roles={"mydatalab"})
//@ActionID(category = "Window", id = "edu.seu.lizhenglong.mylabperspective.MyLabPerspectiveTopComponent")
//@ActionReference(path = "Menu/Window" /*, position = 333 */)
//@TopComponent.OpenActionRegistration(
//        displayName = "#CTL_MyLabPerspectiveAction",
//        preferredID = "MyLabPerspectiveTopComponent"
//)
//@Messages({
//    "CTL_MyLabPerspectiveAction=MyLabPerspective",
//    "CTL_MyLabPerspectiveTopComponent=MyLabPerspective Window",
//    "HINT_MyLabPerspectiveTopComponent=This is a MyLabPerspective window"
//})
public final class MyLabPerspectiveTopComponent extends TopComponent {

    private static volatile MyLabPerspectiveTopComponent instance;
    
    private final DefaultTableModel tableModel = new DefaultTableModel(new String[]{" "}, 0);
    private final MyProcessBar myProcessBar = new MyProcessBar(tableModel);
    private final MyManipulatePanel manipulatePanel = new MyManipulatePanel();
    
    private String tableContentName;//用来标记当前表格中显示的数据是哪张表格的，保存的是表格名称。
    
    private MySwingWorker swingWorker;
    
    public MyLabPerspectiveTopComponent() {
        initLookAndFeel();
        initComponents();
        setName("MyLabPerspectiveTop");
        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);

        processBarPanel.add(myProcessBar);
        optionPanel.add(manipulatePanel);
        
        initAction();
        instance=this;
    }
    
    public JTable getTable(){
        return table;
    }
    
    public String getTableContentName(){
        return tableContentName;
    }
    
    public static synchronized MyLabPerspectiveTopComponent getInstance(){
        if(instance==null){
            instance=new MyLabPerspectiveTopComponent();
        }
        return instance;
    }
    
    public void dispalyAuditPerspectiveData(final ResultSet rs){
        new SwingWorker<Integer,Integer>(){
            @Override
            protected Integer doInBackground() throws Exception {
            //绘制processBar
            myProcessBar.startloadLabelChange();
            //
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);
            int numOfColumn = resetTableColumnName(rs);
            table.getTableHeader().setEnabled(false);//在加载数据过程中禁用表头功能
            //读取表中数据到Jtable中
            while (rs.next() && !isCancelled()) {
                String[] rowData = new String[numOfColumn];
                //注意：遍历resultSet的下标得从1开始。
                for (int i = 1; i <= numOfColumn; i++) {
                    rowData[i - 1] = rs.getString(i);
                }
                tableModel.addRow(rowData);
            }
            //关闭数据库链接
            rs.close();

            publish(tableModel.getRowCount());
            return tableModel.getRowCount();
            }
            
            @Override
            protected void done() {
                if (isCancelled() == true) {
                    loadButton.setEnabled(true);
                    loadButton.setText("加载");
                    //启用OptionsPanel中数据表选择组合框
                    manipulatePanel.setComboBoxEnabledT();
                } else {
                    myProcessBar.endLodaLabelChange();
                }
                //启用表头功能
                table.getTableHeader().setEnabled(true);
                if(table.getRowCount()>0){
                    resetTableColumnWidth();
                }
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                //更新GUI
                loadButton.setEnabled(true);
                loadButton.setText("加载");
                //启用OptionsPanel中数据表选择组合框
                manipulatePanel.setComboBoxEnabledT();
                myProcessBar.cancelLoadLabelChange();
            }
            
        }.execute();
    }
    
    private int resetTableColumnName(ResultSet rs) {
        int numOfColumn = 0;
        //设置列名
        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            numOfColumn = rsmd.getColumnCount();
            String[] newIdentiters = new String[numOfColumn];
            for (int i = 0; i < numOfColumn; i++) {
                newIdentiters[i] = rsmd.getColumnName(i + 1);//注意：rsmd中Column从下标1开始算起
            }
            tableModel.setColumnIdentifiers(newIdentiters);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        //设置默认列宽
        for(int i=0; i<numOfColumn; i++){
            table.getColumnModel().getColumn(i).setPreferredWidth(150);
        }
        return numOfColumn;
    }
    
    /**
     * 根据列表内容长度来重新设置列宽.
     */
    private synchronized void resetTableColumnWidth(){
        int columnNum=table.getColumnCount();
        int[] columnWidths=new int[columnNum];
        
        //计算列宽
        for(int col=0; col<columnNum; col++){
            TableColumn column=table.getColumnModel().getColumn(col);
            int maxWidth=0;
            int countNum=table.getRowCount()<=100 ? table.getRowCount():100;//若行数小于等于100行，则按照行数计算，若行数多于100行，则只计算100行
            for(int row=0; row<countNum; row++){
                TableCellRenderer renderer = table.getCellRenderer (row, col);  
                Object value = table.getValueAt (row, col);  
                Component comp =renderer.getTableCellRendererComponent (table, value, false, false, row, col);  
                maxWidth = Math.max (comp.getPreferredSize().width,  maxWidth);  
            }
            //为了避免列名不能完全显示，设置列宽至少等于列名宽度。
            TableCellRenderer headerRenderer=column.getHeaderRenderer();
            if (headerRenderer == null){  
                headerRenderer = table.getTableHeader().getDefaultRenderer();  
                Object headerValue = column.getHeaderValue();  
                Component headerComp =headerRenderer.getTableCellRendererComponent (table, headerValue, false, false, 0, col);  
                maxWidth = Math.max (maxWidth, headerComp.getPreferredSize().width);  
            }
            //过滤某些单元格尺寸，使之不能太宽。设置单元格最大宽度为500.
            if(maxWidth<=500){
                columnWidths[col] = maxWidth+5;//+5是为了使添加列与列之间的宽度
            }else{
                columnWidths[col] = 500;
            }
        }
        
        //根据表的宽度调整列宽，使列表宽度能铺满列表内容
        int sumWidth=0,offset=0;
        int tableWidth=jScrollPane.getWidth();
                
        for(int i=0; i<columnWidths.length; i++){
            sumWidth+=columnWidths[i];
        }
        if(tableWidth>sumWidth){
            offset=(tableWidth-sumWidth)/columnNum;
        }
        
        //设置每一列列宽
        for(int i=0; i<columnNum; i++){
            table.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]+offset);
        }
    }

    class MySwingWorker extends SwingWorker<Integer, Integer> {
        
        @Override
        protected Integer doInBackground() throws Exception {
            //显示加载数据进度条动画
            myProcessBar.startloadLabelChange();
            //读取数据库
            DatabaseTableSelector dbTableSelector = new DatabaseTableSelector(manipulatePanel.getSelectedTable(), manipulatePanel.getSelectedTime());
            ResultSet resultSet = dbTableSelector.getData();
            //根据取出的数据表重置表头列数和列名.
            tableContentName=manipulatePanel.getSelectedTable();
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);
            int numOfColumn = resetTableColumnName(resultSet);
            table.getTableHeader().setEnabled(false);//在加载数据过程中禁用表头功能
            //读取表中数据到Jtable中
            while (resultSet.next() && !isCancelled()) {
                String[] rowData = new String[numOfColumn];
                //注意：遍历resultSet的下标得从1开始。
                for (int i = 1; i <= numOfColumn; i++) {
                    rowData[i - 1] = resultSet.getString(i);
                }
                tableModel.addRow(rowData);
            }
            
            //关闭数据库链接
            dbTableSelector.close();
            publish(tableModel.getRowCount());
            return tableModel.getRowCount();
        }

        @Override
        protected void done() {
            if (isCancelled() == true) {
                loadButton.setEnabled(true);
                loadButton.setText("加载");
                //启用OptionsPanel中数据表选择组合框
                manipulatePanel.setComboBoxEnabledT();
            } else {
                myProcessBar.endLodaLabelChange();
            }
            //启用表头功能
            table.getTableHeader().setEnabled(true);
            if(table.getRowCount()>0){
                resetTableColumnWidth();
            }
        }

        @Override
        protected void process(java.util.List<Integer> chunks) {
            //更新GUI
            loadButton.setEnabled(true);
            loadButton.setText("加载");
            //启用OptionsPanel中数据表选择组合框
            manipulatePanel.setComboBoxEnabledT();
            
            myProcessBar.cancelLoadLabelChange();
        }
    }
    
    /**
     * 为组件添加事件监听器.
     */
    private void initAction(){
        
        typeTestButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                TableDataFilter tableFilter=Lookup.getDefault().lookup(TableDataFilter.class);
                
                tableFilter.setFilterType(FilterTypeEnum.FRIENDS_RELATION);
                tableFilter.execute();
            }
        });
        
        loadButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                swingWorker = new MySwingWorker();
                swingWorker.execute();
                //禁用OptionsPanel中的数据表选择组合框
                manipulatePanel.setComboBoxEnabledF();
                buildNetworkButton.setEnabled(true);
            }
        });
        
        cancelButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if (swingWorker != null && !swingWorker.isDone()) {
                    swingWorker.cancel(true);
                    swingWorker = null;
                }
            }
        });
        
        buildNetworkButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                
            
                int[] selectedRowIndex=table.getSelectedRows();
                //若用户还未选择待构建网络的数据，则弹窗提醒其是否默认选择表格中所有数据并用之构建网络
                if(selectedRowIndex.length==0){
                    int optionKey=JOptionPane.showConfirmDialog(
                            bottomPanel,
                            "您还未选择特定数据，是否根据表格中的所有信息构建网络？", 
                            "提示",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE,
                            new ImageIcon(getClass().getResource("/edu/seu/lizhenglong/image/warning_32.png"))
                    );
                    if(optionKey!=JOptionPane.NO_OPTION){
                        //设置选中表格中所有数据
                        table.selectAll();
                        BuildNetworkSwingWorker buildNetSwingWorker=new BuildNetworkSwingWorker(table,tableContentName,buildNetworkButton,manipulatePanel.getBuildType());
                        buildNetSwingWorker.execute();
                    }
                }else{
                    BuildNetworkSwingWorker buildNetSwingWorker=new BuildNetworkSwingWorker(table, tableContentName,buildNetworkButton,manipulatePanel.getBuildType());
                    buildNetSwingWorker.execute();
                    
                }
                    
            }
        });
        
        buildDiffButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                
                //设置进度栏，显示正在构建网络
//                myProcessBar.setCustomContent("正在构建网络");
                new SwingWorker<Void,Void>(){
                    @Override
                    protected Void doInBackground() throws Exception {
                        ProgressTicketProvider progressProvider = Lookup.getDefault().lookup(ProgressTicketProvider.class);
                        ProgressTicket ticket = null;
                        if(progressProvider!=null){
                            ticket=progressProvider.createTicket("开始构建网络...", null);
                        }
                        
                        try{
                            BuildDiffNet diffBuilder=Lookup.getDefault().lookup(BuildDiffNet.class);
                            diffBuilder.build();
                            
                            ticket.setDisplayName("计算节点中心性参数...");
                            ticket.start(5);
                            ticket.progress(4);
                            CalStatistics calSta=Lookup.getDefault().lookup(CalStatistics.class);
                            calSta.execute();
                            
                            ticket.setDisplayName("网络节点布局...");
                            ticket.progress(4);
                            LayoutImpl layout=new LayoutImpl();
                            GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getGraphModel();
                            layout.forceAtlasLayout2(graphModel);
                            ticket.finish("网络构建成功");
                            
                        }catch(Exception e){
                            e.printStackTrace();
                            ticket.finish();
                            JOptionPane.showMessageDialog(null, "构建消息传播网络中出现错误");
                        }
                        return null;
                    }
                    
                    @Override
                    protected void done(){
                        myProcessBar.endLodaLabelChange();
                    }
                }.execute();
            }
        });
        
        
        
        table.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(final MouseEvent e){
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (e.getButton() == MouseEvent.BUTTON3) {
                            RightClickMenu pMenu = new RightClickMenu(table);
                            pMenu.showPMenu(e);
                        }
                    }
                });
            }
        });
        
        //添加列头的
        table.getTableHeader().addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(final MouseEvent e){
                final int column=table.columnAtPoint(e.getPoint());
                if( table.getTableHeader().isEnabled() && e.getButton()==MouseEvent.BUTTON3 ){
                    SwingUtilities.invokeLater(new Runnable(){
                        @Override
                        public void run() {
                            TableHeaderRightClick pMenu = new TableHeaderRightClick(table,column);
                            pMenu.showMenu(e);
                            
                        }
                    });
                }
            }
        });
        
        //监听jScrollPane窗口大小改变，则动态更改列表中的列宽。
        jScrollPane.addComponentListener(new ComponentAdapter(){
            @Override
            public void componentResized(ComponentEvent e) {
                if(table.getRowCount()>0){
                    resetTableColumnWidth();
                }
            }
        });
        
    }
    
    
    
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bottomPanel = new javax.swing.JPanel();
        optionPanel = new javax.swing.JPanel();
        jScrollPane = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        processBarPanel = new javax.swing.JPanel();
        loadButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        buildNetworkButton = new javax.swing.JButton();
        typeTestButton = new javax.swing.JButton();
        buildDiffButton = new javax.swing.JButton();

        optionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        optionPanel.setLayout(new java.awt.BorderLayout());

        table.setModel(tableModel);
        table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        table.setRowSorter(new TableRowSorter<>(tableModel));
        jScrollPane.setViewportView(table);

        javax.swing.GroupLayout processBarPanelLayout = new javax.swing.GroupLayout(processBarPanel);
        processBarPanel.setLayout(processBarPanelLayout);
        processBarPanelLayout.setHorizontalGroup(
            processBarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        processBarPanelLayout.setVerticalGroup(
            processBarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        loadButton.setFont(new java.awt.Font("微软雅黑", 0, 15)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(loadButton, org.openide.util.NbBundle.getMessage(MyLabPerspectiveTopComponent.class, "MyLabPerspectiveTopComponent.loadButton.text")); // NOI18N

        cancelButton.setFont(new java.awt.Font("微软雅黑", 0, 15)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, org.openide.util.NbBundle.getMessage(MyLabPerspectiveTopComponent.class, "MyLabPerspectiveTopComponent.cancelButton.text")); // NOI18N

        buildNetworkButton.setFont(new java.awt.Font("微软雅黑", 0, 15)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(buildNetworkButton, org.openide.util.NbBundle.getMessage(MyLabPerspectiveTopComponent.class, "MyLabPerspectiveTopComponent.buildNetworkButton.text")); // NOI18N

        typeTestButton.setFont(new java.awt.Font("微软雅黑", 0, 15)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(typeTestButton, org.openide.util.NbBundle.getMessage(MyLabPerspectiveTopComponent.class, "MyLabPerspectiveTopComponent.typeTestButton.text")); // NOI18N

        buildDiffButton.setFont(new java.awt.Font("微软雅黑", 0, 15)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(buildDiffButton, org.openide.util.NbBundle.getMessage(MyLabPerspectiveTopComponent.class, "MyLabPerspectiveTopComponent.buildDiffButton.text")); // NOI18N

        javax.swing.GroupLayout bottomPanelLayout = new javax.swing.GroupLayout(bottomPanel);
        bottomPanel.setLayout(bottomPanelLayout);
        bottomPanelLayout.setHorizontalGroup(
            bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bottomPanelLayout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 817, Short.MAX_VALUE))
            .addComponent(processBarPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(bottomPanelLayout.createSequentialGroup()
                .addComponent(optionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(loadButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(typeTestButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(bottomPanelLayout.createSequentialGroup()
                        .addComponent(cancelButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buildNetworkButton))
                    .addComponent(buildDiffButton, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13))
        );

        bottomPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {buildNetworkButton, cancelButton, loadButton});

        bottomPanelLayout.setVerticalGroup(
            bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bottomPanelLayout.createSequentialGroup()
                .addGroup(bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(bottomPanelLayout.createSequentialGroup()
                        .addGroup(bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(loadButton)
                            .addComponent(cancelButton)
                            .addComponent(buildNetworkButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(typeTestButton)
                            .addComponent(buildDiffButton)))
                    .addComponent(optionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(processBarPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bottomPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bottomPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JButton buildDiffButton;
    private javax.swing.JButton buildNetworkButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JButton loadButton;
    private javax.swing.JPanel optionPanel;
    private javax.swing.JPanel processBarPanel;
    private javax.swing.JTable table;
    private javax.swing.JButton typeTestButton;
    // End of variables declaration//GEN-END:variables

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    private static class MyJFrameTest extends PopupMenu {

        public MyJFrameTest() {
        }
    }
    
    
    private void initLookAndFeel(){
       //========look&feel========
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MyLabPerspectiveTopComponent.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //========look&feel========

    }
}
