package ch.epfl.vlsc.analysis.core.visualization.util;

import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.swing.handler.mxRubberband;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxMorphing;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;

import java.awt.*;

public class CustomGraphComponent extends mxGraphComponent implements Zoomable {

    private static final long serialVersionUID = 1L;

    public CustomGraphComponent(mxGraph graph) {
        super(graph);

        getViewport().setOpaque(true);
        getViewport().setBackground(Color.white);

        setViewMode();
        this.setToolTips(true);

        getConnectionHandler().setEnabled(false);
        mxRubberband rubber = new mxRubberband(this);
    }

    public void setViewMode() {
        mxGraph graph = getGraph();

        graph.setCellsLocked(true);
        graph.setCellsBendable(false); /* doesn't stop edges from being bent... */

        // Implied by CellsLocked?
//		graph.setCellsEditable(false);
//		graph.setCellsResizable(false);
//		graph.setCellsCloneable(false);
//		graph.setCellsDisconnectable(false);
//		graph.setCellsDeletable(false);
    }

    public void setLayoutMode() {
        mxGraph graph = getGraph();

        graph.setCellsLocked(false);
        graph.setCellsBendable(true);
        graph.setCellsEditable(false);
        graph.setCellsResizable(false);
        graph.setCellsCloneable(false);
        graph.setCellsDisconnectable(false);
        graph.setCellsDeletable(false);
    }

    public void doLayoutGraph(mxIGraphLayout layout, boolean animate) {
//		mxGraph graph=getGraph();
//		Object cell = graph.getSelectionCell();
//
//		if (cell == null
//				|| graph.getModel().getChildCount(cell) == 0)
//		{
//			cell = graph.getDefaultParent();
//		}
        doLayoutCell(getGraph().getDefaultParent(), layout, animate);
    }

    public void doLayoutCell(Object cell, mxIGraphLayout layout, boolean animate) {
        mxGraph graph = getGraph();

        assert (cell != null);
        graph.getModel().beginUpdate();
        try {
            layout.execute(cell);
        } finally {
            if (animate) {
                animateAndEndUpdate();
            } else {
                graph.getModel().endUpdate();
            }
        }
    }

    public void animateAndEndUpdate() {
        mxMorphing morph = new mxMorphing(this, 20,
                1.2, 20);

        morph.addListener(mxEvent.DONE, new mxIEventListener() {
            public void invoke(Object sender, mxEventObject evt) {
                mxGraph graph = getGraph();
                graph.getModel().endUpdate();
                CustomGraphComponent.this.repaint(); // Seems to be necessary to paint properly
            }
        });

        morph.startAnimation();
    }

    public void zoomToFit() {
        mxRectangle r = getGraph().getGraphBounds();
        Dimension d = getSize();
        double factor = Math.min(d.getWidth() / r.getWidth(), d.getHeight() / r.getHeight());

        zoom(factor);
    }

//	protected String getToolTipForCell(Object cell) {
//		Object value=getGraph().getModel().getValue(cell);
//		return (value!=null)? value.toString() : "";
//	}
}
