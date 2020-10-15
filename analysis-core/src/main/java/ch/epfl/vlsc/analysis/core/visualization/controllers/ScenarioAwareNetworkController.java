package ch.epfl.vlsc.analysis.core.visualization.controllers;

import ch.epfl.vlsc.analysis.core.actor.ScenarioAwareActorAnalysis;
import ch.epfl.vlsc.analysis.core.air.ActorInstance;
import ch.epfl.vlsc.analysis.core.air.Network;
import ch.epfl.vlsc.analysis.core.network.ScenarioAwareNetworkAnalysis;
import ch.epfl.vlsc.analysis.core.network.SneakyNetworkAnalyzer;
import ch.epfl.vlsc.analysis.core.visualization.NetworkView;
import ch.epfl.vlsc.analysis.core.visualization.util.ColorCodeLegend;
import ch.epfl.vlsc.analysis.core.visualization.util.GuiAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ScenarioAwareNetworkController extends NetworkController {

    private final Color mSingleRateStaticColor = new Color(0xC0, 0xA0, 0xC0);
    private final Color mMultiRateStaticColor = new Color(0xFF, 0xC0, 0x40);
    private final Color mCycloStaticColor = new Color(0xF0, 0xE0, 0x40);
    private final Color mSADynamicColor = new Color(0x40, 0xC0, 0x40);
    private final Color mDetectorColor = new Color(0xC3, 0xD9, 0x00);
    private final Color mUnclassifiedColor = new Color(0xFF, 0x40, 0x40);
    private final Color mNoImplementationColor = new Color(0xFF, 0xFF, 0xFF);

    private final String mSingleRateStaticStyle = "#C0A0C0";
    private final String mMultiRateStaticStyle = "#FFC040";
    private final String mCycloStaticStyle = "#F0E040";
    private final String mSADynamicStyle = "#40C040";
    private final String mDetectorStyle = "#C3D900";
    private final String mUnclassifiedStyle = "#FF4040";
    private final String mNoImplementationStyle = "#FFFFFF";

    public ScenarioAwareNetworkController(NetworkView view) {
        super(view);
    }

    public void classify() {
        Network network = getNetworkModel();
        NetworkView view = getNetworkView();
        SneakyNetworkAnalyzer mNetworkAnalyzer = new SneakyNetworkAnalyzer();
        ScenarioAwareNetworkAnalysis scenarioAwareAnalysis =
                new ScenarioAwareNetworkAnalysis(network, mNetworkAnalyzer.analyze(network));

        for (ActorInstance actor : network.getActors()) {
            ScenarioAwareActorAnalysis actorAnalysis = scenarioAwareAnalysis.getScenarioAwareActorAnalysis(actor);
            // Set style here depending on the classification of actor
            String style = mNoImplementationStyle;
            if (actor.hasImplementation()) {
                switch (actorAnalysis.getScenarioAwareActorInstanceType()) {
                    case SA_STATIC:
                        switch (actorAnalysis.getActorInstanceType()) {
                            case SINGLE_RATE_STATIC:
                                style = mSingleRateStaticStyle;
                                break;
                            case MULTI_RATE_STATIC:
                                style = mMultiRateStaticStyle;
                                break;
                            case CYCLO_STATIC:
                                style = mCycloStaticStyle;
                                break;
                        }
                        break;
                    case SA_DYNAMIC:
                        style = mSADynamicStyle;
                        break;
                    case SA_DETECTOR:
                        style = mDetectorStyle;
                        break;
                    case UNCLASSIFIED:
                        style = mUnclassifiedStyle;
                        break;
                }
            }
            view.setActorFillColor(actor, style);
        }
    }

    @Override
    protected void createActions() {
        GuiAction action = new GuiAction("Classify") {
            private static final long serialVersionUID = 1L;

            @Override
            public void stateChanged() {
                setEnabled(ScenarioAwareNetworkController.this.getNetworkView() != null);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                ScenarioAwareNetworkController.this.classify();
            }
        };
        action.stateChanged();
        getActions().add(action);

        super.createActions();
    }

    @Override
    protected JComponent createStatusLine() {
        ColorCodeLegend legend = new ColorCodeLegend();

        legend.add(mSingleRateStaticColor, "Single-rate-static");
        legend.add(mMultiRateStaticColor, "Multi-rate-static");
        legend.add(mCycloStaticColor, "Cyclo-static");
        legend.add(mSADynamicColor, "Scenario-aware-dynamic");
        legend.add(mDetectorColor, "Detector");
        legend.add(mUnclassifiedColor, "Unclassified");
        legend.add(mNoImplementationColor, "No Implementation");

        return legend;
    }
}
