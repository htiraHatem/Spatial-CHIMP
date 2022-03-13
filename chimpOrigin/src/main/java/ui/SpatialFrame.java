package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.commons.collections15.Transformer;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.Variable;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;

public class SpatialFrame extends JFrame {

	private static final long serialVersionUID = -5124219242305675891L;


	static Graph<Variable,Constraint> graph;

	VisualizationViewer<Variable,Constraint> vv;
	
	
	public SpatialFrame(Graph<Variable, Constraint> g, int distx) {

		this.graph = g;
		
		Dimension preferredGraphSize=new Dimension(600,600);
		//Layout<V,E> layout=new FRLayout<>(graph, preferredGraphSize);
		Layout<Variable, Constraint> hLayout = new FRLayout<>(graph, preferredGraphSize);
		vv =  new VisualizationViewer<Variable, Constraint>(hLayout, new Dimension(600,600));

		vv.setBackground(Color.white);
		vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line<Variable, Constraint>());
		vv.getRenderContext().setVertexLabelTransformer(new Transformer<Variable, String>() {
			@Override
			public String transform(Variable arg0) {
				return arg0.toString();
			}
		});

		vv.setVertexToolTipTransformer(new Transformer<Variable, String>() {
			@Override
			public String transform(Variable arg0) {
				return arg0.toString();
			}
		});

		vv.getRenderContext().setArrowFillPaintTransformer(new Transformer<Constraint, Paint>() {
			@Override
			public Paint transform(Constraint arg0) {
				return Color.lightGray;
			}
		});
		
		
		//draw edge labels
		Transformer<Constraint,String> stringer = new Transformer<Constraint,String>(){
			@Override
			public String transform(Constraint e) {
				try { return e.getEdgeLabel(); }
				catch (NullPointerException ex) { return ""; }
			}
		};

		Transformer<Variable,Paint> vertexPaint = new Transformer<Variable,Paint>() {
			public Paint transform(Variable v) {
				return v.getColor();
			}
		};
		
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		vv.getRenderContext().setEdgeLabelTransformer(stringer);

		Container content = getContentPane();
		final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
		content.add(panel);

		final DefaultModalGraphMouse<?, ?> graphMouse = new DefaultModalGraphMouse<Object, Object>();

		vv.setGraphMouse(graphMouse);

		JComboBox modeBox = graphMouse.getModeComboBox();
		modeBox.addItemListener(graphMouse.getModeListener());
		graphMouse.setMode(Mode.TRANSFORMING);

		final ScalingControl scaler = new CrossoverScalingControl();

		JButton plus = new JButton("+");
		plus.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				scaler.scale(vv, 1.1f, vv.getCenter());
			}
		});
		JButton minus = new JButton("-");
		minus.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				scaler.scale(vv, 1/1.1f, vv.getCenter());
			}
		});

		JPanel scaleGrid = new JPanel(new GridLayout(1,0));
		scaleGrid.setBorder(BorderFactory.createTitledBorder("Zoom"));

		JPanel controls = new JPanel();
		scaleGrid.add(plus);
		scaleGrid.add(minus);
		controls.add(scaleGrid);
		controls.add(modeBox);

		content.add(controls, BorderLayout.SOUTH);
	}
	
	public static SpatialFrame drawRec(Graph<Variable, Constraint> g, int distx) {
		SpatialFrame stf = new SpatialFrame(g, 100);
		stf.setTitle(PlanHierarchyFrame.class.getSimpleName());
		stf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		stf.pack();
		stf.setVisible(true);
		return stf;
	}
}
