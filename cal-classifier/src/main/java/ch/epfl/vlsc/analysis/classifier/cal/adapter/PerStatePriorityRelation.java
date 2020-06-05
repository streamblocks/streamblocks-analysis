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

package ch.epfl.vlsc.analysis.classifier.cal.adapter;

import ch.epfl.vlsc.analysis.core.air.Action;
import ch.epfl.vlsc.analysis.core.air.PriorityRelation;
import ch.epfl.vlsc.analysis.core.air.Transition;
import ch.epfl.vlsc.analysis.core.util.collections.FilteredSet;

import java.util.Set;


/**
 * PriorityRelation between Transitions of a single FSM state.
 * <p>
 * Implemented using the CaltoopiaActionPriorityRelation, this involves two things
 * (a) Map from a relation between Transitions to a relation between Actions
 * (b) Form the restriction of the relation to a single FSM state
 */
public class PerStatePriorityRelation implements PriorityRelation {

    private final Set<Transition> mTransitions;
    private final TychoActionPriorityRelation mSuperRelation;

    /**
     * @param transitions   Transitions of a particular FSM state
     * @param superRelation PriorityRelation between Actions
     */
    public PerStatePriorityRelation(Set<Transition> transitions,
                                    TychoActionPriorityRelation superRelation) {
        mTransitions = transitions;
        mSuperRelation = superRelation;
    }

    public Set<Transition> getDomain() {
        return mTransitions;
    }

    @Override
    public boolean isAncestorOf(Transition x, Transition y) {
        // Use priority relation between actions.
        // In addition, require the transitions to be part of the relation domain
        // (otherwise iteration won't work)
        return mSuperRelation.isAncestorOf(x.getAction(), y.getAction())
                && mTransitions.contains(x) && mTransitions.contains(y);
    }

    @Override
    public boolean areUnordered(Transition x, Transition y) {
        // Use priority relation between actions.
        // In addition, require the transitions to be part of the relation domain
        // (otherwise iteration won't work)
        return mSuperRelation.areUnordered(x.getAction(), y.getAction())
                && mTransitions.contains(x) && mTransitions.contains(y);
    }

    @Override
    public Set<Transition> getAncestors(Transition x) {
        return new FilteredTransitionSet(mSuperRelation.getAncestors(x.getAction()));
    }

    @Override
    public Set<Transition> getDescendants(Transition x) {
        return new FilteredTransitionSet(mSuperRelation.getDescendants(x.getAction()));
    }

    @Override
    public Set<Transition> getUnordered(Transition x) {
        return new FilteredTransitionSet(mSuperRelation.getUnordered(x.getAction()));
    }

    /**
     * The subset of Transitions, such that their action is in the given set of actions.
     * We use this to map priority between Transition onto priority
     */
    private class FilteredTransitionSet extends FilteredSet<Transition> {
        Set<Action> mActions;

        FilteredTransitionSet(Set<Action> actions) {
            mActions = actions;
        }

        @Override
        public boolean contains(Object obj) {
            return (obj instanceof Transition) && mActions.contains(((Transition) obj).getAction());
        }

        @Override
        protected Set<Transition> unfilteredElements() {
            return getDomain();
        }
    }
}
