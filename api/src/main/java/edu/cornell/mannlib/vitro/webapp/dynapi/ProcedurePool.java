package edu.cornell.mannlib.vitro.webapp.dynapi;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Procedure;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.NullProcedure;

public class ProcedurePool extends AbstractPool<String, Procedure, ProcedurePool> {

    private static ProcedurePool INSTANCE = new ProcedurePool();

    public static ProcedurePool getInstance() {
        return INSTANCE;
    }

    @Override
    public ProcedurePool getPool() {
        return getInstance();
    }

    @Override
    public Procedure getDefault() {
        return NullProcedure.getInstance();
    }

    @Override
    public Class<Procedure> getType() {
        return Procedure.class;
    }

}
