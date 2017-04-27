/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalab.plugin.manipulators.nodes;

import java.awt.Color;
import javax.swing.Icon;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/**
 * Nodes manipulator that selects in nodes table all neighbours of a node.
 * @author Eduardo Ramos
 */
public class SelectNeighboursOnTable extends BasicNodesManipulator{

    private Node[] nodes;

    @Override
    public void setup(Node[] nodes, Node clickedNode) {
        this.nodes = nodes;
    }
    
    @Override
    public void execute() {
        resetColor();
        for(Node node:nodes){
            node.setColor(new Color(50,255,0));
        }
    }

    @Override
    public String getName() {
        return "高亮节点";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public boolean canExecute() {
        return nodes.length>0;
    }

    @Override
    public ManipulatorUI getUI() {
        return null;
    }

    @Override
    public int getType() {
        return 100;
    }

    @Override
    public int getPosition() {
        return 0;
    }

    @Override
    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/datalab/plugin/manipulators/resources/items/highlight.png", false);
    }
    
    private void resetColor(){
        ProjectController pc=Lookup.getDefault().lookup(ProjectController.class);
        if(pc.getCurrentProject()!=null){
            GraphController gc = Lookup.getDefault().lookup(GraphController.class);
            GraphModel gm = gc.getGraphModel();
            Graph graph = gm.getGraphVisible();
            for (Node n : graph.getNodes()) {
                String color=(String)n.getAttribute("defaultColor");
                switch(color){
                    case("红色"):
                        n.setColor(Color.RED);
                        break;
                    case("蓝色"):
                        n.setColor(new Color(0,150,255));
                        break;
                    case("紫色"):
                        n.setColor(new Color(204,0,153));
                        break;
                    case("橙色"):
                        n.setColor(new Color(255,100,0));
                        break;
                    case("淡黄"):
                        n.setColor(new Color(255,204,51));
                        break;

                    default:n.setColor(Color.GRAY);
                }

            }
        }
    }

}
