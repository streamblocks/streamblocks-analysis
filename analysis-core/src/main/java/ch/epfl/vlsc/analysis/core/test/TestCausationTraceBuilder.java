/*
 * Copyright (c) Ericsson AB, 2013
 * All rights reserved.
 *
 * License terms:
 *
 * Redistribution and use in source and binary forms,
 * with or without modification, are permitted provided
 * that the following conditions are met:
 *     * Redistributions of source code must retain the above
 *       copyright notice, this list of conditions and the
 *       following disclaimer.
 *     * Redistributions in binary form must reproduce the
 *       above copyright notice, this list of conditions and
 *       the following disclaimer in the documentation and/or
 *       other materials provided with the distribution.
 *     * Neither the name of the copyright holder nor the names
 *       of its contributors may be used to endorse or promote
 *       products derived from this software without specific
 *       prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ch.epfl.vlsc.analysis.core.test;

import ch.epfl.vlsc.analysis.core.trace.*;
import ch.epfl.vlsc.analysis.core.util.graph.DiGraph;
import ch.epfl.vlsc.analysis.core.util.io.StdErrorConsole;

import java.io.File;

public class TestCausationTraceBuilder extends TestArtTraceBundle {

    ArtTraceBundle mBundle;
    SchedulingConstraints mStateDep;

    public static void main(String[] args) {
        TestCausationTraceBuilder test = new TestCausationTraceBuilder();
        DiGraph<ArtTraceEvent> causationTrace = test.buildCausationTrace(args);

        if (causationTrace != null) {
            test.printCausationTrace(causationTrace);
        }
    }

    DiGraph<ArtTraceEvent> buildCausationTrace(String[] args) {
        if (args.length < 3) {
            printSynopsis();
            return null;
        }

        return buildCausationTrace(args, 0);
    }

    private DiGraph<ArtTraceEvent> buildCausationTrace(String[] inputFiles, int first) {
        mBundle = readTraceBundle(inputFiles, first + 1);
        if (mBundle != null) {
            mStateDep = readStateDependence(new File(inputFiles[first]), mBundle.getNetwork());
            return (mStateDep != null) ? CausationTraceBuilder.build(mBundle, mStateDep) : null;
        } else
            return null;
    }

    private SchedulingConstraints readStateDependence(File input, ArtNetworkDescription network) {
        StateDependenceReader reader = new StateDependenceReader(new StdErrorConsole());
        return reader.readNetwork(input, network);
    }

    private void printCausationTrace(DiGraph<ArtTraceEvent> causationTrace) {
        System.out.println("timestamp step cpu exec.time action                          depends on");
        for (ArtTraceEvent e : causationTrace.getVertices()) {
            System.out.printf("%9d %4d %3d %9d %3d (%s)",
                    e.getTimeStamp(),
                    e.getSequenceIndex(),
                    e.getCPU(),
                    e.getExecutionTime(),
                    e.getAction(),
                    mBundle.getAction(e).getName());

            for (ArtTraceEvent pred : causationTrace.getPredecessors(e)) {
                System.out.print(" " + pred.getSequenceIndex());
            }
            System.out.println();
        }
    }

    protected void printSynopsis() {
        System.err.println("Usage: TestCausationTraceBuilder state_depend.xml net_trace.xml <trace-file>...");
    }
}
