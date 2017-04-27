/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.myviz.egoNetBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JOptionPane;

/**
 *
 * @author hp-6380
 */
public class FriendsNetSingleton {
    private static FriendsNetSingleton instance;
    
    private Map<String,Set<String>> friendsNet=new HashMap<>();
    private Map<String,Set<String>> usersNet=new HashMap<>();
    
    private FriendsNetSingleton(){
        initNet();
    }
    
    public synchronized static FriendsNetSingleton getInstance(){
        if(instance==null){
            instance=new FriendsNetSingleton();
        }
        return instance;
    }
    
    private void initNet(){
        try{
            SelectorDriver driver=new SelectorDriver();
            ResultSet rs=driver.getFriendsNet();
            while(rs.next()){
                String friendId=rs.getString("FRIENDID");
                String userId=rs.getString("USERID");
                
                //添加friends的朋友信息
                Set<String> targetUserSet=friendsNet.get(friendId);
                if(targetUserSet==null){
                    Set<String> newTargetUserSet=new HashSet<>();
                    newTargetUserSet.add(userId);
                    friendsNet.put(friendId, newTargetUserSet);
                }else if(targetUserSet.contains(userId)==false){
                    targetUserSet.add(userId);
                    friendsNet.put(friendId, targetUserSet);
                }
                
                //添加userId的对应节种子节点信息，方便做二阶查找
                Set<String> targetFriendSet=usersNet.get(userId);
                if(targetFriendSet==null){
                    Set<String> newTargetFriendSet=new HashSet<>();
                    newTargetFriendSet.add(friendId);
                    usersNet.put(userId, newTargetFriendSet);
                }else if(targetFriendSet.contains(friendId)==false){
                    targetFriendSet.add(friendId);
                    usersNet.put(userId, targetFriendSet);
                }
            }
        }catch(SQLException ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "出现错误！");
        }
    }
    
    public Map<String,Set<String>> getFriendNet(){
        return friendsNet;
    }
    
    public Map<String,Set<String>> getUserNet(){
        return usersNet;
    }
    
}
