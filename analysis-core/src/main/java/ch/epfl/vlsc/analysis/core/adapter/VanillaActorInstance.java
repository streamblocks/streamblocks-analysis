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

package ch.epfl.vlsc.analysis.core.adapter;

import ch.epfl.vlsc.analysis.core.air.ActorImplementation;
import ch.epfl.vlsc.analysis.core.air.ActorInstance;
import ch.epfl.vlsc.analysis.core.air.PortInstance;
import ch.epfl.vlsc.analysis.core.util.collections.UnionCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class VanillaActorInstance implements ActorInstance {

    private final String mInstanceName;
    private final Collection<PortInstance> mInputPorts;
    private final Collection<PortInstance> mOutputPorts;
    private final Collection<PortInstance> mPorts;

    public VanillaActorInstance(String instanceName) {
        mInstanceName = instanceName;
        mInputPorts = new ArrayList<>();
        mOutputPorts = new ArrayList<PortInstance>();
        mPorts = new UnionCollection<>(mInputPorts, mOutputPorts);
    }

    @Override
    public String getName() {
        return mInstanceName;
    }

    @Override
    public Collection<? extends PortInstance> getPorts() {
        return mPorts;
    }

    @Override
    public Collection<PortInstance> getInputPorts() {
        return mInputPorts;
    }

    @Override
    public Collection<PortInstance> getOutputPorts() {
        return mOutputPorts;
    }

    @Override
    public PortInstance getPort(String name) {
        for (PortInstance port : mInputPorts) {
            if (port.getName().equals(name)) {
                return port;
            }
        }
        for (PortInstance port : mOutputPorts) {
            if (port.getName().equals(name)) {
                return port;
            }
        }

        return null;
    }

    @Override
    public boolean hasImplementation() {
        return false;
    }

    @Override
    public ActorImplementation getImplementation() {
        return null;
    }

    @Override
    public boolean hasAnnotation(String annotation) {
        return false;
    }

    @Override
    public Map<String, String> getAnnotationArguments(String annotation) {
        return null;
    }

    @Override
    public String getAnnotationArgumentValue(String annotation, String argId) {
        return null;
    }

    @Override
    public String toString() {
        return "VanillaActorInstance{" +
                "mInstanceName='" + mInstanceName + '\'' +
                '}';
    }
}

