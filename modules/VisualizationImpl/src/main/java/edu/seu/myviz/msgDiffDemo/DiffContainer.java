/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.myviz.msgDiffDemo;

import java.util.HashMap;
import java.util.Map;
import org.gephi.graph.api.GraphModel;

/**
 *
 * @author hp-6380
 */
public class DiffContainer {
    private static Map<GraphModel,MsgDiff> map=new HashMap<>();
    private static Map<GraphModel,SpanningPathDiff> pathMap=new HashMap<>();
    
    public synchronized static MsgDiff getMsgDiff(GraphModel graphModel){
        if(map.get(graphModel)==null){
            map.put(graphModel, new MsgDiff());
        }
        return map.get(graphModel);
    }
    
    public synchronized static SpanningPathDiff getPathDiff(GraphModel graphModel){
        if(pathMap.get(graphModel)==null){
            pathMap.put(graphModel, new SpanningPathDiff());
        }
        return pathMap.get(graphModel);
    }
}
