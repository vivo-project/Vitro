/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.indexing;

import java.util.Collections;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Statement;

import edu.cornell.mannlib.vitro.webapp.search.beans.StatementToURIsToUpdate;

public class AdditionalURIsForDataProperties  implements StatementToURIsToUpdate{

    @Override
    public List<String> findAdditionalURIsToIndex(Statement stmt) {
        if( stmt != null && stmt.getObject().isLiteral() && stmt.getSubject().getURI() != null )
          return Collections.singletonList( stmt.getSubject().getURI() );
        else
            return Collections.emptyList();
    }

    @Override
    public void startIndexing() { /* nothing to prepare */ }

    @Override
    public void endIndxing() { /* nothing to do */ }
}
