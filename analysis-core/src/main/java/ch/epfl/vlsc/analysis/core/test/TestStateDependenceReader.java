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

import ch.epfl.vlsc.analysis.core.air.Action;
import ch.epfl.vlsc.analysis.core.air.ActorInstance;
import ch.epfl.vlsc.analysis.core.trace.ArtNetworkDescription;
import ch.epfl.vlsc.analysis.core.trace.ArtNetworkDescriptionReader;
import ch.epfl.vlsc.analysis.core.trace.SchedulingConstraints;
import ch.epfl.vlsc.analysis.core.trace.StateDependenceReader;
import ch.epfl.vlsc.analysis.core.util.io.ErrorConsole;
import ch.epfl.vlsc.analysis.core.util.io.StdErrorConsole;

import java.io.File;

public class TestStateDependenceReader {

    public static void main(String[] args) {
        ErrorConsole errorConsole = new StdErrorConsole();
        ArtNetworkDescriptionReader networkReader = new ArtNetworkDescriptionReader(errorConsole);
        StateDependenceReader stateDepReader = new StateDependenceReader(errorConsole);

        if (args.length != 2) {
            System.err.println("Usage: TestArtNetworkDescriptorReader <network-file> <state-dependence-file>");
            System.exit(1);
        }


        ArtNetworkDescription network = networkReader.readNetworkDescription(new File(args[0]));
        SchedulingConstraints dep = stateDepReader.readNetwork(new File(args[1]), network);
        ActorInstance lastActor = null;

        System.out.println("\nCPUs:       " + dep.getCpus());
        System.out.println("Network: " + network.getName());
        for (Action action : network.getActionMap().values()) {
            ActorInstance actor = network.getActor(action);
            if (actor != lastActor) {
                Object cpu = dep.getAffinity(actor);
                String affinity = (cpu != null) ? " (affinity=" + cpu + ")" : "";
                System.out.println("actor " + actor.getName() + affinity);
                lastActor = actor;
            }
            System.out.println("  action " + action.getName());
            System.out.println("    use: " + dep.getUses(action));
            System.out.println("    def: " + dep.getDefinitions(action));
        }
    }
}
