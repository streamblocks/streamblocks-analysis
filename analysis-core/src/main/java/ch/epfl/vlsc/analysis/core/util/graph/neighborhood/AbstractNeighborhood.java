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
package ch.epfl.vlsc.analysis.core.util.graph.neighborhood;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Represents the "neighborhood" of a vertex:
 * (a) adjacent vertices, divided into two sets reverse/forward links according to an orientation of the edges
 * (b) a possible vertex label
 * (c) a possible edge label associated with each forward link
 *
 * @param <V>  vertex type
 * @param <VL> type of vertex label
 * @param <EL> type of edge label
 */
public abstract class AbstractNeighborhood<V, VL, EL> implements Neighborhood<V, VL, EL> {

    private Set<V> mReverseLinks = new LinkedHashSet<>();

    /**
     * @return the set of links (neighbors) with forward orientation
     * <p>
     * Note: updating the set (adding/removing forward links) does not result in
     * a consistent update of the adjacency relation. Use AdjacencyRelation instead!
     */
    public abstract Set<V> getForwardLinks();

    /**
     * @return the set of links (neighbors) with reverse orientation
     * <p>
     * Note: updating the set (adding/removing forward links) does not result in
     * a consistent update of the adjacency relation. Use AdjacencyRelation instead!
     */
    public Set<V> getReverseLinks() {
        return mReverseLinks;
    }

    /**
     * @return the vertex label
     */
    public VL getVertexLabel() {
        // Default implementation for unlabeled neighborhoods
        return null;
    }

    /**
     * @param vertexLabel
     */
    public void setVertexLabel(VL vertexLabel) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param neighbor a vertex
     * @return the label of the forward link (v,neighbor)
     */
    public EL getForwardLinkLabel(Object neighbor) {
        // Default implementation for unlabeled neighborhoods
        return null;
    }

    /**
     * @param neighbor  a neighbor among the forward links
     * @param edgeLabel
     * @return true successful (neighbor is among the forward links)
     */
    public boolean setForwardLinkLabel(V neighbor, EL edgeLabel) {
        throw new UnsupportedOperationException();
    }
}