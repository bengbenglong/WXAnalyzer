/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.seu.myviz.component;

import edu.seu.layout.MyLayoutController;
import edu.seu.layout.LayoutImpl3;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.ui.components.JColorButton;
import org.gephi.ui.utils.UIUtils;
import org.gephi.visualization.VizController;
import org.gephi.visualization.VizModel;
import org.gephi.visualization.component.LabelAttributesPanel;
import org.gephi.visualization.text.TextModelImpl;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/**
 *
 * @author Li Zhenglong
 */
public class MyVizToolbar extends JToolBar {
    
    private Color blue=new Color(0,150,255);
    private ActionListener action;
    private Timer timer;

    public MyVizToolbar() {
        initDesign();
        JComponent[] components=addComponents();
        for (JComponent c : components) {
            addSeparator();
            add(c);
        }
    }
    
    
    /**
     * 添加字体大小，标签，边的粗细等一些列控件
     * @return 
     */
    private JComponent[] addComponents(){
        TextModelImpl model=new TextModelImpl();
//        JComponent[] components=new JComponent[3];
        List<JComponent> components=new ArrayList<>();
        
        //添加显示标签按钮
        VizModel vizModel = VizController.getInstance().getVizModel();
        final JToggleButton showLabelsButton = new JToggleButton();
        showLabelsButton.setSelected(vizModel.getTextModel().isShowNodeLabels());
        showLabelsButton.setToolTipText("显示/隐藏节点标签");

//      showLabelsButton.setIcon(new ImageIcon(getClass().getResource("/edu/seu/viz/image/showNodename.png")));
        showLabelsButton.setIcon(ImageUtilities.loadImageIcon("edu/seu/viz/image/eye.png", false));
        showLabelsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                VizModel vizModel = VizController.getInstance().getVizModel();
                vizModel.getTextModel().setShowNodeLabels(showLabelsButton.isSelected());
            }
        });
        vizModel.getTextModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                TextModelImpl textModel = VizController.getInstance().getVizModel().getTextModel();
                if (showLabelsButton.isSelected() != textModel.isShowNodeLabels()) {
                    showLabelsButton.setSelected(textModel.isShowNodeLabels());
                }
            }
        });
        components.add(showLabelsButton);
        
        //节点标签属性可见性
        final JButton attributesButton = new JButton();
//        attributesButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/visualization/component/configureLabels.png")));
        attributesButton.setIcon(ImageUtilities.loadImageIcon("edu/seu/viz/image/tag.png", false));
        attributesButton.setToolTipText("设置需要显示的节点属性");
        attributesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProjectController pc=Lookup.getDefault().lookup(ProjectController.class);
                if(pc.getCurrentProject()!=null){
                    TextModelImpl model = VizController.getInstance().getVizModel().getTextModel();
                    LabelAttributesPanel panel = new LabelAttributesPanel();
                    panel.setup(model);
                    DialogDescriptor dd = new DialogDescriptor(panel, "选择需要显示的标签", true, NotifyDescriptor.OK_CANCEL_OPTION, null, null);
                    if (DialogDisplayer.getDefault().notify(dd).equals(NotifyDescriptor.OK_OPTION)) {
                        panel.unsetup();
                        return;
                    }
                }
            }
        });
        components.add(attributesButton);
        
        //字体大小滑条
        final JSlider fontSizeSlider = new JSlider(0, 500, (int) (model.getNodeSizeFactor() * 100f));
        fontSizeSlider.setToolTipText("拖动更改字体大小");
        fontSizeSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                TextModelImpl model = VizController.getInstance().getVizModel().getTextModel();
                model.setNodeSizeFactor(fontSizeSlider.getValue() / 100f);
            }
        });
        fontSizeSlider.setPreferredSize(new Dimension(100, 20));
        fontSizeSlider.setMaximumSize(new Dimension(100, 20));
        model.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                TextModelImpl model = VizController.getInstance().getVizModel().getTextModel();
                if (((int) (model.getNodeSizeFactor() * 100f)) != fontSizeSlider.getValue()) {
                    fontSizeSlider.setValue((int) (model.getNodeSizeFactor() * 100f));
                }
            }
        });
        components.add(fontSizeSlider);
        
        //边粗细滑条
        final JSlider edgeScaleSlider = new JSlider(0, 500, (int) ((vizModel.getEdgeScale() - 0.1f) * 10));
        edgeScaleSlider.setToolTipText("拖动更改边的粗细");
        edgeScaleSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                VizModel vizModel = VizController.getInstance().getVizModel();
                if (vizModel.getEdgeScale() != (edgeScaleSlider.getValue() / 10f + 0.1f)) {
                    vizModel.setEdgeScale(edgeScaleSlider.getValue() / 10f + 0.1f);
                }
            }
        });
        edgeScaleSlider.setPreferredSize(new Dimension(100, 20));
        edgeScaleSlider.setMaximumSize(new Dimension(100, 20));
        vizModel.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("edgeScale")) {
                    VizModel vizModel = VizController.getInstance().getVizModel();
                    if (vizModel.getEdgeScale() != (edgeScaleSlider.getValue() / 10f + 0.1f)) {
                        edgeScaleSlider.setValue((int) ((vizModel.getEdgeScale() - 0.1f) * 10));
                    }
                }
            }
        });
        components.add(edgeScaleSlider);
        
        //图中心
        final JButton centerOnGraphButton = new JButton();
        centerOnGraphButton.setToolTipText("将图置于画板中心位置");
//        centerOnGraphButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/visualization/component/centerOnGraph.png")));
        centerOnGraphButton.setIcon(ImageUtilities.loadImageIcon("org/gephi/visualization/component/centerOnGraph.png", false));
        centerOnGraphButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                VizController.getInstance().getGraphIO().centerOnGraph();
            }
        });
        components.add(centerOnGraphButton);
        
        
        //开始/停止节点布局按钮
        final JButton startAndStopLayout=new JButton();
        startAndStopLayout.setIcon(ImageUtilities.loadImageIcon("edu/seu/viz/image/layoutRun.png", false));
        startAndStopLayout.setToolTipText("开始/停止节点布局");
        
        startAndStopLayout.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getGraphModel();
                LayoutImpl3 layout=MyLayoutController.getLayout(graphModel);
                final MyLayoutController layoutContainer=new MyLayoutController(layout);
                if(graphModel==null||layout==null){
                   
                }else if(layout.isRunning()){
                    layoutContainer.stopLayout();
                }else{
                    layoutContainer.startLayout();
                }
            }
        });
        components.add(startAndStopLayout);
        
        //重置节点颜色
        final JColorButton resetColorButton = new JColorButton(new Color(0,150,255), true, false);
        resetColorButton.setIcon(ImageUtilities.loadImageIcon("edu/seu/viz/image/resetColor.png", false));
        resetColorButton.setToolTipText("重置节点颜色");
        resetColorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
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
                                n.setColor(blue);
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
        });
        components.add(resetColorButton);
        
        
       
        
        
        
        return components.toArray(new JComponent[0]);
    }
    

    private void initDesign() {
        setFloatable(false);
        putClientProperty("JToolBar.isRollover", Boolean.TRUE); //NOI18N
        setBorder(BorderFactory.createEmptyBorder(2, 0, 4, 0));
        setBackground(Color.WHITE);
    }

    public void setEnable(final boolean enabled) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                for (Component c : getComponents()) {
                    c.setEnabled(enabled);
                }
            }
        });
    }

    @Override
    public Component add(Component comp) {
        if (comp instanceof JButton) {
            UIUtils.fixButtonUI((JButton) comp);
        }

        return super.add(comp);
    }
}
