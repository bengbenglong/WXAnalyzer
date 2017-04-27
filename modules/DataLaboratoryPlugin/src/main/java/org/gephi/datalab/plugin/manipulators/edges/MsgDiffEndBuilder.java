/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.datalab.plugin.manipulators.edges;

import org.gephi.datalab.spi.edges.EdgesManipulator;
import org.gephi.datalab.spi.edges.EdgesManipulatorBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author hp-6380
 */
//@ServiceProvider(service=EdgesManipulatorBuilder.class)
public class MsgDiffEndBuilder implements EdgesManipulatorBuilder{

    @Override
    public EdgesManipulator getEdgesManipulator() {
        return new MsgDiffEnd();
    }
    
}
