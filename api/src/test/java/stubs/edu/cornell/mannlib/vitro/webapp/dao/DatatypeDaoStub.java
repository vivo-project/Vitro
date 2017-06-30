package stubs.edu.cornell.mannlib.vitro.webapp.dao;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Objects;

import edu.cornell.mannlib.vitro.webapp.beans.Datatype;
import edu.cornell.mannlib.vitro.webapp.dao.DatatypeDao;

public class DatatypeDaoStub implements DatatypeDao {

	private final List<Datatype> dtList = new ArrayList<>();

	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	public void addDatatype(Datatype dt) {
		dtList.add(dt);
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public Datatype getDatatypeByURI(String uri) {
		for (Datatype dt : dtList) {
			if (Objects.equal(dt.getUri(), uri)) {
				return dt;
			}
		}
		return null;
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	public void updateDatatype(Datatype dtp) {
		throw new RuntimeException(
				"DatatypeDaoStub.updateDatatype() not implemented.");
	}

	@Override
	public void deleteDatatype(Datatype dtp) {
		throw new RuntimeException(
				"DatatypeDaoStub.deleteDatatype() not implemented.");
	}

	@Override
	public void deleteDatatype(int id) {
		throw new RuntimeException(
				"DatatypeDaoStub.deleteDatatype() not implemented.");
	}

	@Override
	public Datatype getDatatypeById(int id) {
		throw new RuntimeException(
				"DatatypeDaoStub.getDatatypeById() not implemented.");
	}

	@Override
	public int getDatatypeIdByURI(String uri) {
		throw new RuntimeException(
				"DatatypeDaoStub.getDatatypeIdByURI() not implemented.");
	}

	@Override
	public List<Datatype> getAllDatatypes() {
		throw new RuntimeException(
				"DatatypeDaoStub.getAllDatatypes() not implemented.");
	}

}
