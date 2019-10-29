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
package ch.epfl.vlsc.analysis.core.trace;

import ch.epfl.vlsc.analysis.core.adapter.VanillaNetwork;
import ch.epfl.vlsc.analysis.core.air.Action;
import ch.epfl.vlsc.analysis.core.air.ActorInstance;
import ch.epfl.vlsc.analysis.core.air.Connection;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class ArtNetworkDescription extends VanillaNetwork {

    private Map<Integer, Action> mActionMap;
    private Map<Action, ActorInstance> mAction2ActorMap;

    public ArtNetworkDescription(String name,
                                 Collection<ActorInstance> actors,
                                 Set<Connection> connections,
                                 Map<Integer, Action> actionMap,
                                 Map<Action, ActorInstance> action2ActorMap) {
        super(name, actors, connections);

        mActionMap = actionMap;
        mAction2ActorMap = action2ActorMap;
    }

    public Action getAction(int actionIndex) {
        return mActionMap.get(actionIndex);
    }

    public Map<Integer, Action> getActionMap() {
        return mActionMap;
    }

    public ActorInstance getActor(Action action) {
        return mAction2ActorMap.get(action);
    }
}
