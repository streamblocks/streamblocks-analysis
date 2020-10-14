package ch.epfl.vlsc.analysis.core.visualization;

import ch.epfl.vlsc.analysis.core.actor.GenericActorAnalysis;
import ch.epfl.vlsc.analysis.core.air.ActorInstance;
import ch.epfl.vlsc.analysis.core.air.Network;
import ch.epfl.vlsc.analysis.core.network.GenericNetworkAnalysis;
import ch.epfl.vlsc.analysis.core.network.SneakyNetworkAnalyzer;
import ch.epfl.vlsc.analysis.core.visualization.util.ColorCodeLegend;
import ch.epfl.vlsc.analysis.core.visualization.util.GuiAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class GenericNetworkController extends NetworkController {

    private final Color mSingleRateStaticColor = new Color(0xC0, 0xA0, 0xC0);
    private final Color mMultiRateStaticColor = new Color(0xFF, 0xC0, 0x40);
    private final Color mCycloStaticColor = new Color(0xF0, 0xE0, 0x40);
    private final Color mQuasiStaticColor = new Color(0x40, 0xC0, 0x40);
    private final Color mDynamicColor = new Color(0xC3, 0xD9, 0xFF);
    private final Color mOtherColor = new Color(0xFF, 0x40, 0x40);

    private final String mSingleRateStaticStyle = "#C0A0C0";
    private final String mMultiRateStaticStyle = "#FFC040";
    private final String mCycloStaticStyle = "#F0E040";
    private final String mQuasiStaticStyle = "#40C040";
    private final String mDynamicStyle = "#C3D9FF";
    private final String mOtherStyle = "#FF4040";


    public GenericNetworkController(NetworkView view) {
        super(view);
    }

    public void classify() {
        Network network = getNetworkModel();
        NetworkView view = getNetworkView();
        SneakyNetworkAnalyzer mNetworkAnalyzer = new SneakyNetworkAnalyzer();
        GenericNetworkAnalysis genericAnalysis =
                new GenericNetworkAnalysis(network, mNetworkAnalyzer.analyze(network));

        for (ActorInstance actor : network.getActors()) {
            GenericActorAnalysis actorAnalysis = genericAnalysis.getGenericActorAnalysis(actor);
            // Set style here depending on the classification of actor
            String style = mOtherStyle;

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
                case QUASI_STATIC:
                    style = mQuasiStaticStyle;
                    break;
                case DYNAMIC:
                    style = mDynamicStyle;
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
                setEnabled(GenericNetworkController.this.getNetworkView() != null);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                GenericNetworkController.this.classify();
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
        legend.add(mQuasiStaticColor, "Quasi-static");
        legend.add(mDynamicColor, "Dynamic");
        legend.add(mOtherColor, "Other MoC");

        return legend;
    }
}
