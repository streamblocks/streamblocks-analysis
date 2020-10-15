package ch.epfl.vlsc.analysis.core.visualization.controllers;

import ch.epfl.vlsc.analysis.core.air.Network;
import ch.epfl.vlsc.analysis.core.visualization.NetworkView;
import ch.epfl.vlsc.analysis.core.visualization.util.GuiAction;
import ch.epfl.vlsc.analysis.core.visualization.util.GuiActionKit;
import ch.epfl.vlsc.analysis.core.visualization.util.Zoomable.ZoomInAction;
import ch.epfl.vlsc.analysis.core.visualization.util.Zoomable.ZoomOutAction;
import ch.epfl.vlsc.analysis.core.visualization.util.Zoomable.ZoomToFitAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


/**
 * The controller of a model-view-controller "triad". The Model is an AIR Network object
 * and the view is a NetworkView.
 */
public class NetworkController {

    private final NetworkView mNetworkView;
    protected GuiActionKit mActions;
    private Network mNetworkModel;
    private JFrame mAppFrame;

    public NetworkController(NetworkView view) {
        mNetworkView = view;
    }

    /**
     * @param network Sets the network, creates the view and updates the rendering of the view
     */
    public void setNetwork(Network network) {
        // TODO: what if changing networks...
        if (mNetworkModel != null) {
        }

        mNetworkModel = network;

        if (mNetworkView != null) {
            mNetworkView.createNetwork(network);
            mNetworkView.doLayoutGraph(false);
            if (mNetworkView.getWidth() < 100 || mNetworkView.getHeight() < 100) {
                // Way to small viewport
                int newWidth = Math.min(mNetworkView.getWidth(), 400);
                int newHeight = Math.min(mNetworkView.getHeight(), 320);
                mNetworkView.setSize(new Dimension(newWidth, newHeight));
            }
            mNetworkView.zoomToFit();
        }

        if (mAppFrame != null) {
            mAppFrame.setTitle(getTitle());
        }

        if (mActions != null) {
            mActions.stateChanged();
        }
    }

    public Network getNetworkModel() {
        return mNetworkModel;
    }

    public NetworkView getNetworkView() {
        return mNetworkView;
    }

    public GuiActionKit getActions() {
        return mActions;
    }

    public JFrame createFrame() {
        mAppFrame = new JFrame(getTitle());
        mAppFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mAppFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
            }
        });
        mAppFrame.setContentPane(createGUI());
        mAppFrame.pack();
        mAppFrame.setVisible(true);

        return mAppFrame;
    }

    protected JPanel createGUI() {
        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout());

        pane.add(mNetworkView, BorderLayout.CENTER);

        mActions = new GuiActionKit(pane);
        createActions();

        JComponent toolbar = createToolbar();
        if (toolbar != null)
            pane.add(toolbar, BorderLayout.NORTH);

        JComponent statusLine = createStatusLine();
        if (statusLine != null)
            pane.add(createStatusLine(), BorderLayout.SOUTH);

        return pane;
    }


    protected void createActions() {
        GuiAction action = new ZoomInAction(mNetworkView) {
            private static final long serialVersionUID = 4667934474532319123L;

            @Override
            public void stateChanged() {
                mZoomable = mNetworkView;
                setEnabled(mZoomable != null);
            }
        };
        action.stateChanged();
        mActions.add(action);

        action = new ZoomOutAction(mNetworkView) {
            private static final long serialVersionUID = -3474663316738385290L;

            @Override
            public void stateChanged() {
                mZoomable = mNetworkView;
                setEnabled(mZoomable != null);
            }
        };
        action.stateChanged();
        mActions.add(action);

        action = new ZoomToFitAction(mNetworkView) {
            private static final long serialVersionUID = -4848527129152742414L;

            @Override
            public void stateChanged() {
                mZoomable = mNetworkView;
                setEnabled(mZoomable != null);
            }
        };
        action.stateChanged();
        mActions.add(action);

        // TODO: Move to caller
        mActions.addDefaultKeyBindings();
    }

    protected JComponent createToolbar() {
        JPanel toolbar = new JPanel();

        for (GuiAction action : mActions) {
            JButton button = new JButton(action);
            toolbar.add(button);
        }

        return toolbar;
    }

    public void classify() {

    }

    protected JComponent createStatusLine() {
        return null;
    }

    protected String getTitle() {
        return (mNetworkModel != null) ? mNetworkModel.getName() + " - Dataflow Network" : "Dataflow Network";
    }
}

