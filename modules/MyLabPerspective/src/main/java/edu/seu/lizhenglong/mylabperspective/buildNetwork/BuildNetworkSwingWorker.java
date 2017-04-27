/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.lizhenglong.mylabperspective.buildNetwork;

import edu.seu.layout.LayoutImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import org.openide.util.Lookup;
import edu.seu.networkbuild.api.BuildNet;
import edu.seu.networkbuild.api.MyTableAttr;
import edu.seu.networkbuild.impl.FriendsNetBuilder;
import edu.seu.networkbuild.impl.GroupMemberNetBuilder;
import edu.seu.statistics.api.CalStatistics;
import edu.seu.statistics.api.CalStatisticsImpl;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.utils.progress.ProgressTicketProvider;
import org.openide.util.Cancellable;

/**
 *
 * @author hp-6380
 */
public class BuildNetworkSwingWorker extends SwingWorker<Void,Void>{
    private boolean exception=false;
    private String buildType;
    
    private JTable jTable;
    private JButton createNetworkButton;
    private Map<String,Integer> network;
    private String tableName;
    
    private BuildNet buildNet;
    
    public BuildNetworkSwingWorker( JTable jTable, String tableName, JButton button, String buildType){
        this.jTable=jTable;
        this.tableName=tableName;
//        this.buildType=buildType;
        this.createNetworkButton=button;
        this.buildType=buildType;
    }
    
    
    public Map<String,Integer> getGeneratedNetwork(){
        return network;
    }

    @Override
    protected Void doInBackground() throws Exception {
        
        ProgressTicketProvider progressProvider = Lookup.getDefault().lookup(ProgressTicketProvider.class);
        ProgressTicket ticket = null;
        
        try{
            //暂时停用“构建网络”按钮
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                   createNetworkButton.setEnabled(false);
                }
            });
            //选择适当构建网络的类，并用类信息来选择jTable中需要的信息来构建网络
            
            if (progressProvider != null) {
                ticket = progressProvider.createTicket("开始构建网络...", null);
            }
            ticket.start(3);
            ticket.progress(0);
            buildNet=selectProperClass2();
            //只选择构建网络所必须的列信息以及所选择的行数据(这个属于过滤，是不是可以把这部分功能单独提取到另一个模块中比较好？）
            String[] columnNames=buildNet.getNeedfulColumnNames();
            List<String[]> filteredData=selectNeedfulColumnData(columnNames);
            //获得table中选中列的列信息TableAttr
            MyTableAttr tableAttr=generateTableAttr();
            //初始化构建buildNet类信息
            buildNet.init(filteredData,buildType,tableAttr);
            buildNet.build();
            
            //计算各种中心性参数
            ticket.setDisplayName("计算节点中心性参数...");
            ticket.progress(1);
            CalStatistics calSta=new CalStatisticsImpl();
            calSta.execute();
            //布局
//            ticket.setDisplayName("网络节点布局...");
            
            final LayoutImpl layout=new LayoutImpl();
            ticket=progressProvider.createTicket("节点布局中...", new Cancellable(){
                @Override
                public boolean cancel() {
                    layout.cancel(true);
                    return true;
                }
            });
            ticket.progress(2);
            //建好之后布局
            GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getGraphModel();
            layout.forceAtlasLayout(graphModel);
            ticket.finish();
            
            
        }catch(Exception e){
            e.printStackTrace();
            if(ticket!=null){
                ticket.finish();
            }
            JOptionPane.showMessageDialog(null, "网络构建过程中出错");
        }
        
        return null;
    }
    
    
    @Override
    protected void done(){
        createNetworkButton.setEnabled(true);
        if(exception){
            JOptionPane.showMessageDialog(null, "构建网络过程中发生错误", "构建网络过程中发生错误", JOptionPane.ERROR_MESSAGE);
        }else{
            //启用“构建网络”按钮
            JOptionPane.showMessageDialog(
                    null,
                    "网络构建成功！", 
                    "消息", 
                    JOptionPane.PLAIN_MESSAGE, 
                    new ImageIcon(getClass().getResource("/edu/seu/lizhenglong/image/success32.png"))
            );
        }
    }
    
    
    public BuildNet selectProperClass2(){
        BuildNet buildNet=null;
        if(tableName.equals("TB_WX_ACCOUNT")){
            JOptionPane.showMessageDialog(null, "该功能还未实现！","提示",JOptionPane.OK_OPTION);
            throw new UnsupportedOperationException("not supported yet!");
        }else if(tableName.equals("TB_WX_FRIENDS")){
            buildNet=new FriendsNetBuilder();
        }else if(tableName.equals("TB_WX_GROUP")){
            JOptionPane.showMessageDialog(null, "该功能还未实现！","提示",JOptionPane.OK_OPTION);
            throw new UnsupportedOperationException("not supported yet!");
        }else if(tableName.equals("TB_WX_GROUPMEMBER")){
            buildNet=new GroupMemberNetBuilder();
        }else if(tableName.equals("TB_WX_MSGMONITOR")){
            JOptionPane.showMessageDialog(null, "该功能还未实现！","提示",JOptionPane.OK_OPTION);
            throw new UnsupportedOperationException("not supported yet!");
        }
        return buildNet;
    }
    
    /**
     * 根据当前表格名称来动态确定该用什么类来构建网络
     * @return 
     */
    private BuildNet selectProperClass(){
        
        BuildNet buildNet=null;
        
//        buildNet=Lookup.getDefault().lookup(BuildNet.class);
        
        Lookup.Template template=new Lookup.Template<>(BuildNet.class);
        Lookup.Result result=Lookup.getDefault().lookup(template);
        Collection c=result.allInstances();//改变了
        
        if(tableName.equals("TB_WX_ACCOUNT")){
            for(Iterator i=c.iterator(); i.hasNext();){
                BuildNet temp=(BuildNet)i.next();
                if(temp.getClass().getName().contains("Account")){
                    buildNet=temp;
                    break;
                }
            }
        }else if(tableName.equals("TB_WX_FRIENDS")){
            for(Iterator i=c.iterator(); i.hasNext(); ){
                BuildNet temp=(BuildNet)i.next();
                if(temp.getClass().getName().contains("Friends")){
                    buildNet=temp;
                    break;
                }
            }
        }else if(tableName.equals("TB_WX_GROUP")){
            for(Iterator i=c.iterator(); i.hasNext(); ){
                BuildNet temp=(BuildNet)i.next();
                if(temp.getClass().getName().contains("Group")){
                    buildNet=temp;
                    break;
                }
            }
        }else if(tableName.equals("TB_WX_GROUPMEMBER")){
            for(Iterator i=c.iterator(); i.hasNext(); ){
                BuildNet temp=(BuildNet)i.next();
                if(temp.getClass().getName().contains("GroupMember")){
                    buildNet=temp;
                    break;
                }
            }
        }else if(tableName.equals("TB_WX_GROUPMESSAGE")){
            for(Iterator i=c.iterator(); i.hasNext(); ){
                BuildNet temp=(BuildNet)i.next();
                if(temp.getClass().getName().contains("GroupMessage")){
                    buildNet=temp;
                    break;
                }
            }
        }else if(tableName.equals("TB_WX_MSGMONITOR")){
            for(Iterator i=c.iterator(); i.hasNext(); ){
                BuildNet temp=(BuildNet)i.next();
                if(temp.getClass().getName().contains("MsgMonitor")){
                    buildNet=temp;
                    break;
                }
            }
        }
        return buildNet;
    }
    
    /**
     * 根据列名称确定构建网络时所需要的列，其他列的内容对构建网络没有关系
     * @param columnNames
     * @return 
     */
    private List<String[]> selectNeedfulColumnData(String[] columnNames){
        
        List<String[]> filteredData=new ArrayList<>();
        
        //根据列名确定所需列的列号,其他不相关的列的数据不用
        int[] selectedRowIndex=jTable.getSelectedRows();
        int[] selectedColumnIndex=new int[columnNames.length];
        for(int i=0; i<columnNames.length; i++)
            for(int j=0; j<jTable.getColumnCount(); j++){
                if(columnNames[i].equals(jTable.getColumnName(j))){
                    selectedColumnIndex[i]=j;
                }
            }

        //根据所选定的行号和列号，提取待构建数据
        for(int i=0; i<selectedRowIndex.length; i++){
            String[] rowData=new String[selectedColumnIndex.length];
            for(int j=0; j<selectedColumnIndex.length; j++){
                rowData[j]=(String) jTable.getValueAt(selectedRowIndex[i], selectedColumnIndex[j]);
            }
            filteredData.add(rowData);
        }
        
        
        //将表格中的数据全部重新存储起来，以便
        return filteredData;
    }
    
    //将被选中用来构建网络的内容复制一份来保存
    //用来做查询时候使用。
    private MyTableAttr generateTableAttr(){
        String[] columnNameAll=new String[jTable.getColumnCount()];
        
        //先获取列名
        int idColumnNameIndex=-1;
        for(int i=0; i<jTable.getColumnCount(); i++){
            columnNameAll[i]=jTable.getModel().getColumnName(i);
            if(jTable.getModel().getColumnName(i).equals(buildNet.getIdColumnName())){
                idColumnNameIndex=i;
            }
        }
        
        MyTableAttr tableAttr=new MyTableAttr(columnNameAll);
        int[] selectedRowIndex=jTable.getSelectedRows();
        for(int i=0; i<selectedRowIndex.length; i++){
            String[] s=new String[jTable.getColumnCount()];
            for(int j=0; j<s.length; j++){
                s[j]=(String)jTable.getValueAt(selectedRowIndex[i], j);
            }
            tableAttr.addAttributes(s[idColumnNameIndex], s);
        }
        return tableAttr;
    }
      
}