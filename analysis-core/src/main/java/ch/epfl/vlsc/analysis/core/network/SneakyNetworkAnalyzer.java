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

package ch.epfl.vlsc.analysis.core.network;


import ch.epfl.vlsc.analysis.core.actor.*;
import ch.epfl.vlsc.analysis.core.actor.GenericActorAnalysis.ActorInstanceType;
import ch.epfl.vlsc.analysis.core.air.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SneakyNetworkAnalyzer implements NetworkAnalyzer {

    public SneakyNetworkAnalyzer() {
    }

    public NetworkAnalysis analyze(Network network) {
        SneakyNetworkAnalysis result = new SneakyNetworkAnalysis(network);
        return result;
    }
}

class SneakyNetworkAnalysis implements NetworkAnalysis {
    private final Map<ActorInstance, GenericActorAnalysis> mActorMap = new HashMap<ActorInstance, GenericActorAnalysis>();
    private final Map<Action, ActorInstance> mActionMap = new HashMap<Action, ActorInstance>();
    protected Network network;
    protected Map<Connection, ConnectionAnalysis> mConnectionMap = new HashMap<Connection, ConnectionAnalysis>();

    public SneakyNetworkAnalysis(Network n) {
        network = n;
        ActorAnalyzer actorAnalyzer = new SneakyActorAnalyzer();
        for (ActorInstance actor : network.getActors()) {
            GenericActorAnalysis analysis = null;
            if (actor.hasImplementation()) {
                ActorImplementation actorImpl = actor.getImplementation();
                analysis = new GenericActorAnalysis(actor, actorAnalyzer.analyze(actorImpl));
            } else {
                analysis = new GenericActorAnalysis(actor, null);
            }
            mActorMap.put(actor, analysis);
        }

        for (Connection connection : network.getConnections()) {
            ConnectionAnalysis connectionAnalysis = null;
            if (connection.getProducerPort().getActor().hasImplementation()) {
                connectionAnalysis = new ConnectionAnalysis(connection,
                        getGenericActorAnalysis(connection.getProducerPort().getActor()));
            } else {
                connectionAnalysis = new ConnectionAnalysis(connection, null);
            }
            mConnectionMap.put(connection, connectionAnalysis);
        }

    }

    public ConnectionAnalysis getConnectionAnalysis(Connection c) {
        assert (mConnectionMap.get(c) != null);
        return mConnectionMap.get(c);
    }

    public GenericActorAnalysis getGenericActorAnalysis(ActorInstance a) {
        assert (mActorMap.get(a) != null);
        return mActorMap.get(a);
    }

    public PortAnalysis getPortAnalysis(PortInstance p) {
        return getGenericActorAnalysis(p.getActor()).getPortAnalysis(p);
    }

    public ActionAnalysis getActionAnalysis(Action a) {
        ActorInstance actor = mActionMap.get(a);
        assert (actor != null);
        return getGenericActorAnalysis(actor).getActionAnalysis(a);
    }

    public List<ActorInstance> getSingleRateStaticActors() {
        List<ActorInstance> sdfActors = new ArrayList<ActorInstance>();
        for (ActorInstance actor : network.getActors()) {
            if (getGenericActorAnalysis(actor).getActorInstanceType()
                    == ActorInstanceType.SINGLE_RATE_STATIC) {
                sdfActors.add(actor);
            }
        }
        return sdfActors;
    }

    public List<ActorInstance> getMultiRateStaticActors() {
        List<ActorInstance> sdfActors = new ArrayList<ActorInstance>();
        for (ActorInstance actor : network.getActors()) {
            if (getGenericActorAnalysis(actor).getActorInstanceType()
                    == ActorInstanceType.MULTI_RATE_STATIC) {
                sdfActors.add(actor);
            }
        }
        return sdfActors;
    }

    public List<ActorInstance> getCycloStaticActors() {
        List<ActorInstance> csdfActors = new ArrayList<ActorInstance>();
        for (ActorInstance actor : network.getActors()) {
            if (getGenericActorAnalysis(actor).getActorInstanceType()
                    == ActorInstanceType.CYCLO_STATIC) {
                csdfActors.add(actor);
            }
        }
        return csdfActors;
    }

    public List<ActorInstance> getQuasiStaticActors() {
        List<ActorInstance> quasiStaticActors = new ArrayList<ActorInstance>();
        for (ActorInstance actor : network.getActors()) {
            if (getGenericActorAnalysis(actor).getActorInstanceType()
                    == ActorInstanceType.QUASI_STATIC) {
                quasiStaticActors.add(actor);
            }
        }
        return quasiStaticActors;
    }


    public List<ActorInstance> getDynamicActors() {
        List<ActorInstance> dynamicActors = new ArrayList<ActorInstance>();
        for (ActorInstance actor : network.getActors()) {
            if (getGenericActorAnalysis(actor).getActorInstanceType()
                    == ActorInstanceType.DYNAMIC) {
                dynamicActors.add(actor);
            }
        }
        return dynamicActors;
    }

    public List<ActorInstance> getUnclassifiedActors() {
        List<ActorInstance> unclassifiedActors = new ArrayList<ActorInstance>();
        for (ActorInstance actor : network.getActors()) {
            if (getGenericActorAnalysis(actor).getActorInstanceType()
                    == ActorInstanceType.UNCLASSIFIED) {
                unclassifiedActors.add(actor);
            }
        }
        return unclassifiedActors;
    }
}
