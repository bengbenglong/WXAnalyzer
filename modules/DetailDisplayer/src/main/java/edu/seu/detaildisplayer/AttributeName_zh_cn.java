/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.detaildisplayer;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author hp-6380
 */
public class AttributeName_zh_cn {
    private Map<String,String> map=new HashMap<>();
    
    public AttributeName_zh_cn(){
        map.put("accountname", "账号");
        map.put("userid","关联人ID" );
        map.put("username", "用户姓名");
        map.put("imid", "QQ号");
        map.put("idno","证件号码" );
        map.put("email", "电子邮件");
        map.put("mobilephone", "手机号码");
        map.put("nickname","昵称" );
        map.put("regip","注册IP地址" );
        map.put("regtime", "注册时间");
        map.put("sex", "性别");
        map.put("idtype", "证件类型");
        map.put("telephone", "电话号码");
        map.put("country", "国家/地区");
        map.put("province", "省份/州");
        map.put("city", "城市");
        map.put("address", "地址");
        map.put("icon", "用户自定义头像");
        map.put("updatetime", "信息更新时间");
        map.put("friendremark", "好友备注");
        map.put("groupname", "群账号");
        map.put("creator", "创建者Id");
        map.put("creatornickname", "群主昵称");
        map.put("createtime", "创建时间");
        map.put("membernum", "成员数量");
        map.put("lastchattime", "最近一次聊天时间");
        map.put("groupermark", "成员备注");
        map.put("msgid", "消息ID");
        map.put("msgtime", "消息时间");
        map.put("msgtype", "消息类型");
        map.put("content", "消息内容");
        map.put("groupid", "群ID");
        map.put("friendid", "已关注关联人ID");
        map.put("defaultcolor","节点颜色" );
    }
            
    public Map<String,String> getAttributeNameZh(){
        return map;
    }
    
}
