package edu.buffalo.cse.blueseal.networkflow.summary;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

public class GraphExporter {
	
	private VisualizationImageServer vs;

	public GraphExporter(Graph g){
		//http://www.programcreek.com/java-api-examples/index.php?api=edu.uci.ics.jung.algorithms.layout.CircleLayout
		//http://www.vainolo.com/2011/02/14/learning-jung-java-universal-networkgraph-framework/
		DAGLayout dagLayout = new DAGLayout(g);
		dagLayout.setLocation(g, 0, 0);
		vs = new VisualizationImageServer(dagLayout, new Dimension(800, 800));
	    vs.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
	    vs.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
	    JFrame frame=new JFrame("Graph");
	    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    frame.getContentPane().add(vs);
	    frame.pack();
	}
	
	public void setNewGrap(Graph g){
		vs.getGraphLayout().setGraph(g);
	    vs.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
	    vs.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
	}

	
	public void saveFile(String filePath){
		BufferedImage image = (BufferedImage) vs.getImage(
			    new Point2D.Double(vs.getGraphLayout().getSize().getWidth() / 2,
			    vs.getGraphLayout().getSize().getHeight() / 2),
			    new Dimension(vs.getGraphLayout().getSize()));
	
		// Write image to a png file
		File outputfile = new File(filePath + ".png");
	
		try {
		    ImageIO.write(image, "png", outputfile);
		} catch (IOException e) {
		    // Exception handling
		}
	}
}
