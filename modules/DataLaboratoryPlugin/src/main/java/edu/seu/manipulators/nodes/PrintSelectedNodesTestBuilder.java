/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.manipulators.nodes;

import org.gephi.datalab.spi.nodes.NodesManipulator;
import org.gephi.datalab.spi.nodes.NodesManipulatorBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author hp-6380
 */
@ServiceProvider(service=NodesManipulatorBuilder.class)
public class PrintSelectedNodesTestBuilder implements NodesManipulatorBuilder{

    @Override
    public NodesManipulator getNodesManipulator() {
        return new PrintSelectedNodesTest();
    }

}
