package ch.epfl.vlsc.analysis.core.visualization.controllers;

import ch.epfl.vlsc.analysis.core.actor.McdfActorAnalysis;
import ch.epfl.vlsc.analysis.core.air.ActorInstance;
import ch.epfl.vlsc.analysis.core.air.Network;
import ch.epfl.vlsc.analysis.core.network.McdfNetworkAnalysis;
import ch.epfl.vlsc.analysis.core.network.SneakyNetworkAnalyzer;
import ch.epfl.vlsc.analysis.core.visualization.view.NetworkView;
import ch.epfl.vlsc.analysis.core.visualization.util.ColorCodeLegend;
import ch.epfl.vlsc.analysis.core.visualization.util.GuiAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class McdfNetworkController extends NetworkController {

    private final Color mAmodalColor = new Color(0xC0, 0xA0, 0xC0);
    private final Color mMcColor = new Color(0xFF, 0xC0, 0x40);
    private final Color mDataDependentColor = new Color(0xF0, 0xE0, 0x40);
    private final Color mModalColor = new Color(0x40, 0xC0, 0x40);
    private final Color mNAColor = new Color(0xC3, 0xD9, 0xFF);
    private final Color mOtherColor = new Color(0xFF, 0x40, 0x40);

    private final String mAmodalStyle = "#C0A0C0";
    private final String mMcStyle = "#FFC040";
    private final String mDataDependentStyle = "#F0E040";
    private final String mModalStyle = "#40C040";
    private final String mNAStyle = "#C3D9FF";
    private final String mOtherStyle = "#FF4040";
    
    public McdfNetworkController(NetworkView view) {
        super(view);
    }

    public void classify() {
        Network network = getNetworkModel();
        NetworkView view = getNetworkView();
        SneakyNetworkAnalyzer mNetworkAnalyzer = new SneakyNetworkAnalyzer();
        McdfNetworkAnalysis mcdfAnalysis =
                new McdfNetworkAnalysis(network, mNetworkAnalyzer.analyze(network));

        mcdfAnalysis.getModeControlActors();

        for (ActorInstance actor : network.getActors()) {
            McdfActorAnalysis actorAnalysis = mcdfAnalysis.getMcdfActorAnalysis(actor);
            // Set style here depending on the classification of actor
            String style = mNAStyle;

            switch (actorAnalysis.getMcdfActorInstanceType()) {
                case MC:
                    style = mMcStyle;
                    break;
                case SWITCH:
                case SELECT:
                case TUNNEL:
                    style = mDataDependentStyle;
                    break;
                case AMODAL:
                    style = mAmodalStyle;
                    break;
                case MODAL:
                    style = mModalStyle;
                    break;
                case UNCLASSIFIED:
                    style = mOtherStyle;
                    break;
            }

            view.setActorFillColor(actor, style);
        }
    }

    @Override
    protected void createActions() {
        GuiAction action = new GuiAction("Classify") {
            @Override
            public void stateChanged() {
                setEnabled(McdfNetworkController.this.getNetworkView() != null);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                McdfNetworkController.this.classify();
            }
        };
        action.stateChanged();
        getActions().add(action);

        super.createActions();
    }

    @Override
    protected JComponent createStatusLine() {
        ColorCodeLegend legend = new ColorCodeLegend();

        legend.add(mMcColor, "Mode Controller");
        legend.add(mAmodalColor, "Amodal Actors");
        legend.add(mDataDependentColor, "Data-dependent Actors");
        legend.add(mModalColor, "Modal Actors");
        legend.add(mNAColor, "Not Analyzed");
        legend.add(mOtherColor, "Other MoC");

        return legend;
    }
}