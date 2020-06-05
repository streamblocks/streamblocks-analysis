package ch.epfl.vlsc.analysis.classifier.cal.adapter;

import ch.epfl.vlsc.analysis.core.air.StateVariable;
import se.lth.cs.tycho.ir.decl.VarDecl;

public class TychoStateVariable implements StateVariable {

    private final VarDecl decl;

    public TychoStateVariable(VarDecl decl) {
        this.decl = decl;
    }

    @Override
    public String getName() {
        return decl.getName();
    }
}
