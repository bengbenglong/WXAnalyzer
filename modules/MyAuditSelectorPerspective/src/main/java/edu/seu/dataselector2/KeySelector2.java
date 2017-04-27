/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.dataselector2;

import edu.seu.dataselector.*;
import edu.seu.dataselector.drivers.SelectorDriver;
import edu.seu.dataselector.timeComponent.DateSetting;
import edu.seu.layout.LayoutImpl3;
import edu.seu.layout.MyLayoutController;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.perspective.api.PerspectiveController;
import org.gephi.perspective.spi.Perspective;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.utils.progress.ProgressTicketProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Cancellable;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

/**
 *
 * @author hp-6380
 */
public class KeySelector2 extends javax.swing.JPanel {

    private volatile static KeySelector2 instance;
    private DefaultTableModel tableModel=new DefaultTableModel();
    
    private Color defaultColor=new Color(0,150,255);
    
    //时间选择面板
    DateSetting dateSetting=new DateSetting();
    private boolean dateSetedFlag;
    
    /**
     * Creates new form KeySelector
     */
    private KeySelector2() {
        initLookAndFeel();
        initComponents();
        initAction();
        
//        new SwingWorker<Void,Void>(){
//            @Override
//            protected Void doInBackground() throws Exception {
//                getData();
//                return null;
//            }
//            
//            @Override
//            protected void done(){
//                resultNum.setText("结果数量："+tableModel.getRowCount());
//            }
//        }.execute();
    }
    
    public static synchronized KeySelector2 getInstance(){
        if(instance==null){
            instance=new KeySelector2();
        }
        return instance;
    }
    
    private void initAction(){
        timeSelectorButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                setTimeInteval();//设置时间间隔
            }
        });
        
//        searchButton.addActionListener(new ActionListener(){
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                if(keywordField.getText()==null||keywordField.getText().equals("")){
//                    getData();
//                    resultNum.setText("结果数量："+table.getRowCount());
//                    return;
//                }
//                final String[] keywords=keywordField.getText().split(" ");
//                if(keywords.length==0){
//                    getData();
//                    resultNum.setText("结果数量："+table.getRowCount());
//                    return;
//                }
//                getData();//重新从数据库中提取数据
//                filterTableList(keywords);
//                resultNum.setText("关键词：\""+keywordField.getText()+"\"\t 结果数量："+table.getRowCount());
//            }
//        });

        searchButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                //判断有没有设置过时间
                if(dateSetedFlag==false){
                    Object[] options={"好","跳过","取消"};
                    int type=JOptionPane.showOptionDialog(null, "您还未选择时间区间，是否现在选择？", "时间区间未选择提示",JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE,null, options, options[0]);
                    switch (type) {
                        case JOptionPane.YES_OPTION:
                            setTimeInteval();
                            break;
                        case JOptionPane.NO_OPTION:
                            dateSetting.selectedStartTime=DateSetting.SELECTED_ALL;
                            getData();
                            break;
                        default:
                    }
                }else{
                    getData();
                }
            }
        });
        
        
        
        keywordField.addKeyListener(new KeyAdapter(){
            @Override
            public void keyPressed(KeyEvent e){
                if(e.getKeyChar()==KeyEvent.VK_ENTER ){   //按回车键执行相应操作; { 
                    searchButton.doClick();
                }
            }
        });
        
        //获得鼠标焦点时自动清除默认提示
        keywordField.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                if(keywordField.getText().equals("请输入关键字（多个关键字之间用空格隔开）")){
                    keywordField.setText("");
                }
            }
        });
        
        table.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                if(keywordField.getText().equals("")||keywordField.getText().equals(" ")){
                    keywordField.setText("请输入关键字（多个关键字之间用空格隔开）");
                }
            }
        });
        
        buildNetButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                
                
                new SwingWorker<Void,Void>(){
                    ProgressTicketProvider progressProvider = Lookup.getDefault().lookup(ProgressTicketProvider.class);
                    ProgressTicket ticket = null;
                    
                    @Override
                    protected Void doInBackground() throws Exception {
                        //判断数据量能不能支撑构建网络
                        int[] selectedRows=table.getSelectedRows();
                        if(selectedRows.length<1){
                            JOptionPane.showMessageDialog(null, "请选择数据！", "提示", JOptionPane.INFORMATION_MESSAGE);
                            return null;
                        }
                        
                        
                        SwingUtilities.invokeLater(new Runnable(){
                            @Override
                            public void run() {
                                buildNetButton.setEnabled(false);
                            }
                        });
                        
                        if(progressProvider!=null){
                            ticket=progressProvider.createTicket("构建网络中...", null);
                        }
                        ticket.start();
                        
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
                        
                        //开始构建网络
                        GraphModel graphModel= buildGroupMessageNet();
                        //设置默认的颜色和大小
                        setDefaultSizeAndPosition(graphModel);
                        
                        ticket.finish();
                        
                         //在开始布局之前便可以让“构建网络”按钮启用
                        SwingUtilities.invokeLater(new Runnable(){
                            @Override
                            public void run() {
                                buildNetButton.setEnabled(true);
                            }
                        });

                        //进行节点布局
                        graphModel=Lookup.getDefault().lookup(GraphController.class).getGraphModel();
                        final LayoutImpl3 layout=MyLayoutController.getLayout(graphModel);
                        ticket=progressProvider.createTicket("节点布局中...", new Cancellable(){
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
                        ticket.finish();
                        
                        return null;
                    }
                    
                    @Override
                    protected void done(){
                        if(ticket!=null){
                            ticket.finish();
                        }
                        buildNetButton.setEnabled(true);
                    }
                }.execute();
            }
        });
    }
    
    private GraphModel buildGroupMessageNet(){
        try{
            
            List<String> cmdids=new ArrayList<>();
            Set<String> keywords=new HashSet<>();

            int[] selectedRows=table.getSelectedRows();
            int columnIndex=-1;
            int applykeywordIndex=-1;
            for(int i=0;i<table.getColumnCount();i++){
                if(table.getColumnName(i).equals("CMDID")){
                    columnIndex=i;
                }
                if(table.getColumnName(i).equals("群内容检索关键字")){
                    applykeywordIndex=i;
                }
            }
            
            for(int i=0;i<selectedRows.length;i++){
                cmdids.add((String)tableModel.getValueAt(selectedRows[i], columnIndex));
                String account=(String)tableModel.getValueAt(selectedRows[i], applykeywordIndex);
                String[] keys=account.split(" ");
                for(int j=0;j<keys.length;j++){
                    keywords.add(keys[j]);
                }
            }

            //这个字段是图说明中需要适用的，表示当前是根据什么关键字构建的网络
            String keyword="";
            for(String s:keywords){
                keyword+=s+" ";
            }

            ProjectController pc=Lookup.getDefault().lookup(ProjectController.class);
            if(pc.getCurrentProject()==null){
                pc.newProject();
            }else{
                Workspace workspace=pc.newWorkspace(pc.getCurrentProject());
                pc.openWorkspace(workspace);
            }
            GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getGraphModel();
            Graph graph=graphModel.getDirectedGraph();
            
            //更改workspace名字
            Workspace w=pc.getCurrentWorkspace();
            pc.renameWorkspace(w, "关键词“"+keyword+"”的群组间消息传播网");

            //添加图的一些基本信息
            graph.setAttribute("GraphDes", "关键词“"+keyword+"”的群组间消息传播网");
            graph.setAttribute("FirstNodeDes", "群");
            graph.setAttribute("FirstNodeColor", "蓝色");
            graph.setAttribute("SecondNodeDes", "群成员");
            graph.setAttribute("SecondNodeColor", "红色");

            String[] columnName={"ACCOUNTNAME","MSGTIME","CONTENT","节点性质","defaultColor"};
            Column[] columns=new Column[5];
            columns[0]=initAttributeColumns(graphModel,columnName[0],String.class);
            columns[1]=initAttributeColumns(graphModel,columnName[1],String.class);
            columns[2]=initAttributeColumns(graphModel,columnName[2],String.class);
            columns[3]=initAttributeColumns(graphModel,columnName[3],String.class);
            columns[4]=initAttributeColumns(graphModel,columnName[4],String.class);
                    
            SelectorDriver driver=new SelectorDriver();
            ResultSet rs=driver.getGroupMessage(cmdids.toArray(new String[0]));

            Column edgeColumnAttrTime=initEdgeAttrColumns(graphModel,"发送时间",String.class);
            Column edgeColumnAttrContent=initEdgeAttrColumns(graphModel, "消息内容",String.class);

            try{
                while(rs.next()){
                       String groupId=rs.getString("GROUPID");
                       String userId=rs.getString("USERID");

                       String accountName=rs.getString("ACCOUNTNAME");
                       String msgTime=rs.getString("MSGTIME");
                       String content=rs.getString("CONTENT");

                       //添加节点，同时添加节点属性
                       Node n;
                       if(graph.getNode(userId)==null){
                            n=graphModel.factory().newNode(userId);
                            n.setColor(Color.RED);//红色为用户
                            n.setAttribute("defaultColor", "红色");
                            n.setAttribute(columns[0], accountName);
                            n.setAttribute(columns[3], "群成员");
                           
                            String message=msgTime+"△to:群“"+groupId+"” "+content;;
                            n.setAttribute(columns[2], message);
                            graph.addNode(n);
                       }else {
                            n=graph.getNode(userId);
                           
                            String c=(String)n.getAttribute(columns[2]);
                            String message="○"+msgTime+"△to:群“"+groupId+"” "+content;
                            c+=message;
                            n.setAttribute(columns[2], c);
                       }
                       
                        if(graph.getNode(groupId)==null){
                            n=graphModel.factory().newNode(groupId);
                            n.setColor(defaultColor);//蓝色为群组
                            n.setAttribute(columns[3], "群");
                            n.setAttribute("defaultColor", "蓝色");
                            String message=msgTime+"△from:群成员“"+accountName+"”  "+content;
                            n.setAttribute(columns[2], message);
                            graph.addNode(n);
                        }else{
                            n=graph.getNode(groupId);
                            String c=(String)n.getAttribute(columns[2]);
                            String message="○"+msgTime+"△from:群成员“"+accountName+"”  "+content;
                            c+=message;
                            n.setAttribute(columns[2], c);
                        }
//                       if(graph.getNode(userId)==null){
//                           Node n=graphModel.factory().newNode(userId);
//                           n.setColor(Color.RED);//红色为用户
//                           n.setAttribute("defaultColor", "红色");
//                           n.setAttribute(columns[0], accountName);
//                           n.setAttribute(columns[3], "用户");
//                           
//                           //=============这部分是边详情=========================
//                           if(n.getAttribute(columns[2])!=null){//如果之前已经添加过信息了
//                               String c=(String)n.getAttribute(columns[2]);
//                               String message="○"+msgTime+"△to:群“"+groupId+"” "+content;
////                               String message=content+" "+msgTime+"\"\t";
//                               c+=message;
//                               n.setAttribute(columns[2], c);
//                               System.out.println("=========进入这里===========");
//                           }else{
//                               String message=msgTime+"△to:群“"+groupId+"” "+content;;
//                               n.setAttribute(columns[2], message);
//                           }
//                           graph.addNode(n);
//
//                       }

                        
//                        if(graph.getNode(groupId)==null){
//                           Node n=graphModel.factory().newNode(groupId);
//                           n.setColor(defaultColor);//蓝色为群组
//                           n.setAttribute(columns[3], "群");
//                           n.setAttribute("defaultColor", "蓝色");
//                           
//                           //加入
//                           if(n.getAttribute(columns[2])==null){
//                               String message=msgTime+"△from:群成员“"+accountName+"”  "+content;
//                               n.setAttribute(columns[2], message);
//                           }else{//如果之前已经添加过信息了
//                               String c=(String)n.getAttribute(columns[2]);
//                               String message="○"+msgTime+"△from:群成员“"+accountName+"”  "+content;
//                               c+=message;
//                               n.setAttribute(columns[2], c);
//                           }
//                           graph.addNode(n);
//                       }
                       Edge e=graphModel.factory().newEdge(graph.getNode(userId), graph.getNode(groupId), true);
                       e.setAttribute(edgeColumnAttrTime, msgTime);
                       e.setAttribute(edgeColumnAttrContent, content);
                       if(graph.contains(e)==false){
                           graph.addEdge(e);
                       }
                }
                
            }catch(SQLException ex){
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "取TB_WX_GROUPMESSAGE表数据时出错");
            }finally{
                driver.close();
                if(graph.getNodeCount()==0){
                    WindowManager.getDefault().invokeWhenUIReady(new Runnable(){
                        @Override
                        public void run() {
                            JOptionPane.showMessageDialog(null, "网络中节点数量为0", "提示", JOptionPane.INFORMATION_MESSAGE);
                        }
                    });
                    pc.closeCurrentWorkspace();
                }
            }
            
            return graphModel;
            
        }catch(Exception ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "构建网络过程出现错误");
        }
        
       
        
        return null;
    }
    
    private void filterTableList(String[] keywords){
        Set<String> selectedKeywords=new HashSet<>();
        for(int i=0;i<keywords.length;i++){
            selectedKeywords.add(keywords[i]);
        }

        //找到keywork列表对应的列的序号
        int columnIndex=-1;
        for(int i=0;i<table.getColumnCount();i++){
            if(table.getColumnName(i).equals("群内容检索关键字")){
                columnIndex=i;
                break;
            }
        }
        //开始遍历表格
        for(int i=0;i<tableModel.getRowCount();){
            String rowData=(String)table.getModel().getValueAt(i, columnIndex);
            String[] keys=rowData.split(" ");
            int matchedNum=0;
            for(int j=0;j<keys.length;j++){
                if(selectedKeywords.contains(keys[j])){
                    matchedNum++;
                }
            }
            if(matchedNum==selectedKeywords.size()){
                i++;
            }else{
                tableModel.removeRow(i);
            }
        }
    }
    
    private void getData(){
        ProgressTicketProvider progressProvider = Lookup.getDefault().lookup(ProgressTicketProvider.class);
        ProgressTicket ticket = progressProvider.createTicket("正在从数据库中提取数据...", null);
        ticket.start();
        
        
        
        //设置一些按钮是不是被禁用
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                table.getTableHeader().setEnabled(false);//禁用表头功能
            }
        });
        
        SelectorDriver keywordSelector=new SelectorDriver();
        try{
            String startTime=dateSetting.selectedStartTime;
//            ResultSet rs=keywordSelector.getKeyworkList();
            ResultSet rs;
            if(startTime==null||startTime.equals(DateSetting.SELECTED_ALL)){
                rs=keywordSelector.getKeywordList();
            }else{
                rs=keywordSelector.getKeywodListWithTime(dateSetting.selectedStartTime, dateSetting.selectedEndTime);
            }
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);
            int numOfColumn = resetTableColumnName(rs);
            //读取表中数据到Jtable中
            while (rs.next()) {
                String[] rowData = new String[numOfColumn];
                //注意：遍历resultSet的下标得从1开始。
                for (int i = 1; i <= numOfColumn; i++) {
                    rowData[i - 1] = rs.getString(i);
                }
                tableModel.addRow(rowData);
            }
            //关闭数据库链接
            rs.close();
        }catch(SQLException e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "列表显示数据过程出错");
        }finally{
            keywordSelector.close();
            ticket.finish();
        }
        
        //重新启用一些按钮,以及显示一些信息
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                String keyword=keywordField.getText();
                String timeIntever;
                if(dateSetting.selectedStartTime.equals(DateSetting.SELECTED_ALL)){
                    timeIntever="已选择时间范围：不设限";
                }else{
                    timeIntever="已选择时间范围："+dateSetting.selectedStartTime+"-"+dateSetting.selectedEndTime;
                }
                if(keyword.equals("")||keyword.equals(" ")||keyword.equals("请输入关键字（多个关键字之间用空格隔开）")){
                    resultNum.setText(timeIntever+"   结果数量："+table.getRowCount());
                }else{
                    String[] keywords=keyword.split(" ");
                    filterTableList(keywords);
                    resultNum.setText(timeIntever+"   关键词：\""+keyword+"\"\t   结果数量："+table.getRowCount());
                }
                table.getTableHeader().setEnabled(true);//启用表头功能
                if(table.getRowCount()>0){
                    resetTableColumnWidth();
                }
            }
        });
        
        
    }
    
    //设置时间间隔
    private void setTimeInteval(){
        String oldStartTime=dateSetting.startTime;
        String oldEndTime=dateSetting.endTime;
        DialogDescriptor dd=new DialogDescriptor(dateSetting,"时间选择");
        DialogDisplayer.getDefault().notify(dd);
        Integer value=(Integer)dd.getValue();
        if(value==0){
            //判断用户自己选择的时间是不是符合要求
            if(dateSetting.jrb5SelectedFlag){
                if(dateSetting.getStartField().contains("----年--月--日")||dateSetting.getEndField().contains("----年--月--日")||dateSetting.startTime.compareTo(dateSetting.endTime)>0){
                    JOptionPane.showMessageDialog(null,"时间选择错误，请重新选择","警告",JOptionPane.ERROR_MESSAGE,ImageUtilities.loadImageIcon("edu/seu/lizhenglong/image/warning_32.png", false));
                    setTimeInteval();
                    return;
                }
            }
            dateSetting.selectedStartTime=dateSetting.startTime;
            dateSetting.selectedEndTime=dateSetting.endTime;
            dateSetedFlag=true;
            
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    if(dateSetting.jrb5SelectedFlag){
                        resultNum.setText("已选择时间范围："+dateSetting.getStartField()+"00:00:00 至 "+dateSetting.getEndField()+"23:59:59");
                    }else if(dateSetting.selectedStartTime.equals(DateSetting.SELECTED_ALL)){
                        resultNum.setText("已选择时间范围：不设限");
                    }else{
                        resultNum.setText("已选择时间范围："+dateSetting.selectedStartTime+" 至 "+dateSetting.selectedEndTime);
                    }
                }
            });
        }else{
            dateSetting.selectedStartTime=oldStartTime;
            dateSetting.selectedEndTime=oldEndTime;
        }
    }
    
    private int resetTableColumnName(ResultSet rs) {
        Map<String,String> ccn=new HashMap<>();
        ccn.put("APPLYKEYWORD", "群内容检索关键字");
        ccn.put("APPLYSTARTTIME", "开始日期");
        ccn.put("APPLYENDTIME", "结束日期");
        ccn.put("CMDID", "CMDID");
        int numOfColumn = 0;
        //设置列名
        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            numOfColumn = rsmd.getColumnCount();
            String[] newIdentiters = new String[numOfColumn];
            for (int i = 0; i < numOfColumn; i++) {
                newIdentiters[i] = ccn.get(rsmd.getColumnName(i + 1));//注意：rsmd中Column从下标1开始算起
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
    
    private Column initAttributeColumns(GraphModel graphModel,String attr,Class type) {
        Table nodeTable = graphModel.getNodeTable();
        Column eigenCol = nodeTable.getColumn(attr);
        if (eigenCol == null) {
            eigenCol=nodeTable.addColumn(attr, type);
        }
        
        return eigenCol;
    }
    
    private Column initEdgeAttrColumns(GraphModel graphModel, String attr, Class type){
        Table edgeTable=graphModel.getEdgeTable();
        Column eigenCol=edgeTable.getColumn(attr);
        if(eigenCol==null){
            eigenCol=edgeTable.addColumn(attr, type);
        }
        return eigenCol;
    }
    
    //给每个节点设置默认的节点位置，大小，颜色
    private void setDefaultSizeAndPosition(GraphModel graphModel){
        Node node;
        Iterator<Node> ite=graphModel.getGraph().getNodes().iterator();
        
        while(ite.hasNext()){
            node=ite.next();
            //设置节点大小
            int neighborCount=graphModel.getGraph().getDegree(node);
            if(neighborCount>20){
                neighborCount=20;
            }
            node.setSize(70f+neighborCount*2);
            
            //随机分配节点坐标
            float x=(float) ((0.01 + Math.random()) * 2000)-1000;
            float y=(float) ((0.01 + Math.random()) * 2000)-1000;
            node.setX(x);
            node.setY(y);
        }
    }

    private void initLookAndFeel(){
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DataSelectorTopComponent.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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

        keywordField = new javax.swing.JTextField();
        searchButton = new javax.swing.JButton();
        jScrollPane = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        resultNum = new javax.swing.JLabel();
        buildNetButton = new javax.swing.JButton();
        separator2 = new javax.swing.JToolBar.Separator();
        timeSelectorButton = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createEtchedBorder());

        keywordField.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        keywordField.setText("请输入关键字（多个关键字之间用空格隔开）"); // NOI18N
        keywordField.setToolTipText("请输入想要查询的关键词，多个关键字之间用空格分开"); // NOI18N

        searchButton.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(searchButton, "检索"); // NOI18N
        searchButton.setToolTipText("根据关键词，检索包含关键词的历史查询记录"); // NOI18N

        table.setModel(tableModel);
        table.setRowSorter(new TableRowSorter(tableModel));
        jScrollPane.setViewportView(table);
        if (table.getColumnModel().getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(KeySelector2.class, "KeySelector2.table.columnModel.title0")); // NOI18N
            table.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(KeySelector2.class, "KeySelector2.table.columnModel.title1")); // NOI18N
            table.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(KeySelector2.class, "KeySelector2.table.columnModel.title2")); // NOI18N
            table.getColumnModel().getColumn(3).setHeaderValue(org.openide.util.NbBundle.getMessage(KeySelector2.class, "KeySelector2.table.columnModel.title3")); // NOI18N
        }

        resultNum.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(resultNum, " "); // NOI18N

        buildNetButton.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(buildNetButton, "构建群聊消息传播网"); // NOI18N
        buildNetButton.setToolTipText(null);

        separator2.setOrientation(javax.swing.SwingConstants.VERTICAL);

        timeSelectorButton.setFont(new java.awt.Font("微软雅黑", 0, 16)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(timeSelectorButton, "选择时间"); // NOI18N
        timeSelectorButton.setToolTipText(org.openide.util.NbBundle.getMessage(KeySelector2.class, "KeySelector2.timeSelectorButton.toolTipText")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 994, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(keywordField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(timeSelectorButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(searchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(separator2, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buildNetButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(resultNum)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {searchButton, timeSelectorButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(keywordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(searchButton)
                        .addComponent(buildNetButton)
                        .addComponent(timeSelectorButton))
                    .addComponent(separator2, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 503, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resultNum)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buildNetButton;
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JTextField keywordField;
    private javax.swing.JLabel resultNum;
    private javax.swing.JButton searchButton;
    private javax.swing.JToolBar.Separator separator2;
    private javax.swing.JTable table;
    private javax.swing.JButton timeSelectorButton;
    // End of variables declaration//GEN-END:variables
}
