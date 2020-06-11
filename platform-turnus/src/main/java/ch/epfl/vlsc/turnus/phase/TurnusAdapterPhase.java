package ch.epfl.vlsc.turnus.phase;

import ch.epfl.vlsc.turnus.adapter.TurnusModelAdapter;
import org.apache.log4j.BasicConfigurator;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.phase.Phase;
import se.lth.cs.tycho.reporting.CompilationException;
import se.lth.cs.tycho.settings.Setting;
import turnus.analysis.profiler.dynamic.DynamicProfiler;
import turnus.analysis.profiler.dynamic.util.ProfiledExecutionDataReader;
import turnus.common.TurnusException;
import turnus.common.configuration.Configuration;
import turnus.common.io.Logger;
import turnus.model.ModelsRegister;
import turnus.model.dataflow.Network;
import turnus.model.versioning.Versioner;
import turnus.model.versioning.impl.GitVersioner;

import java.io.File;
import java.util.List;

import static turnus.common.TurnusConstants.DEFAULT_VERSIONER;
import static turnus.common.TurnusOptions.CAL_PROJECT;
import static turnus.common.TurnusOptions.VERSIONER;

public class TurnusAdapterPhase implements Phase {
    @Override
    public String getDescription() {
        return "StreamBlocks Turnus Adapter Phase";
    }

    @Override
    public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
        Logger.info("===== STREAMBLOCKS-TURNUS DYNAMIC EXECUTION ANALYSIS ====");

        ModelsRegister.init();
        BasicConfigurator.configure();

        Logger.info("Configuring the project");

        Configuration configuration = new Configuration();
        configuration.setValue(CAL_PROJECT, task.getIdentifier().getLast().toString());
        configuration.setValue(VERSIONER, DEFAULT_VERSIONER);


        Logger.info("* QID: %s", task.getIdentifier().toString());

        String profiledExecutionFile = "";

        try {
            Versioner versioner = new GitVersioner();
            // -- Model Adapter
            Logger.info("Network and profiler building");
            TurnusModelAdapter modelAdapter = new TurnusModelAdapter(task, versioner);
            Network network = modelAdapter.getNetwork();

            // -- Dynamic profiling
            DynamicProfiler profiler = new DynamicProfiler(network);
            profiler.setConfiguration(configuration);

            File jsonFile = new File(profiledExecutionFile);
            ProfiledExecutionDataReader reader = new ProfiledExecutionDataReader(profiler, jsonFile);
            reader.start();
            reader.join();

        } catch (TurnusException e) {
            e.printStackTrace();
        }

        Logger.info("Parsing the execution profiling data");

        return null;
    }

    @Override
    public List<Setting<?>> getPhaseSettings() {
        return null;
    }
}
