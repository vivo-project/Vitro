/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.reasoner;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;

/**
 * We're using a simple OntModel as the TBox in the SimpleReasoner tests, so we
 * don't get tied to a particular TBox reasoner (like Pellet).
 * 
 * But the SimpleReasoner expects certain elementary reasoning, so these methods
 * impose that reasoning on the model.
 * 
 * On the model: Thing is a class.
 * 
 * On classes: Every class is equivalent to itself, a subclass of itself, and a
 * subclass of Thing. Every class is a subclass of all its ancestors and a
 * superclass of all its descendants. Every class has the same superclasses and
 * subclasses as do all of its equivalent classes.
 * 
 * On object properties: Every object property is equivalent to itself and a
 * subproperty of itself. Every object property has the same inverses as do all
 * of its equivalent properties.
 * 
 * ----------------------
 * 
 * It's a little silly to implement this as a parent class of the unit tests. It
 * would have been nicer to find a way that is more object-oriented but still
 * explicit in what "reasoning" is performed. This will do for now.
 */
public class SimpleReasonerTBoxHelper extends AbstractTestClass {
	private static String URI_THING = "http://www.w3.org/2002/07/owl#Thing";

	// ----------------------------------------------------------------------
	// The model
	// ----------------------------------------------------------------------

	protected OntModel createTBoxModel() {
		OntModel tBox = ModelFactory
				.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		createClass(tBox, URI_THING, "OWL Thing");
		return tBox;
	}

	// ----------------------------------------------------------------------
	// Classes
	// ----------------------------------------------------------------------

	protected OntClass createClass(OntModel tBox, String uri, String label) {
		OntClass s = tBox.createClass(uri);
		s.setLabel(label, "en-US");
		s.addEquivalentClass(s);
		s.addSubClass(s);
		thing(tBox).addSubClass(s);
		return s;
	}

	private OntClass thing(OntModel tBox) {
		return tBox.getOntClass(URI_THING);
	}

	/**
	 * Make sure that you establish subclass relationships before setting
	 * equivalent classes.
	 */
	protected void setEquivalent(OntClass c1, OntClass c2) {
		setEquivalentClasses(equivalences(c1), equivalences(c2));
		setEquivalentClasses(equivalences(c2), equivalences(c1));
		c1.addEquivalentClass(c2);
		c2.addEquivalentClass(c1);
		copySubClasses(c1, c2);
		copySubClasses(c2, c1);
		copySuperClasses(c1, c2);
		copySuperClasses(c2, c1);
	}

	private void setEquivalentClasses(Set<OntClass> equivalences1,
			Set<OntClass> equivalences2) {
		for (OntClass c1 : equivalences1) {
			for (OntClass c2 : equivalences2) {
				c1.addEquivalentClass(c2);
			}
		}
	}

	private void copySubClasses(OntClass c1, OntClass c2) {
		for (OntClass sub : c1.listSubClasses().toList()) {
			c2.addSubClass(sub);
		}
	}

	private void copySuperClasses(OntClass c1, OntClass c2) {
		for (OntClass sup : c1.listSuperClasses().toList()) {
			c2.addSuperClass(sup);
		}
	}

	private Set<OntClass> equivalences(OntClass c1) {
		return new HashSet<OntClass>(c1.listEquivalentClasses().toList());
	}

	protected void addSubclass(OntClass parent, OntClass child) {
		addSubclass(equivalences(parent), equivalences(child));
	}

	private void addSubclass(Set<OntClass> equivalentParents,
			Set<OntClass> equivalentChildren) {
		for (OntClass parent : equivalentParents) {
			for (OntClass child : equivalentChildren) {
				parent.addSubClass(child);

				for (OntClass ancestor : parent.listSuperClasses().toList()) {
					ancestor.addSubClass(child);
				}
				for (OntClass descendant : child.listSubClasses().toList()) {
					parent.addSubClass(descendant);
				}
			}
		}
	}

	protected void removeSubclass(OntClass parent, OntClass child) {
		removeSubclass(equivalences(parent), equivalences(child));
	}

	/**
	 * This has the potential for problems if we set this up:
	 * 
	 * <pre>
	 * A -> B -> C
	 * 
	 * explicit add A -> C
	 * 
	 * remove A -> B
	 * </pre>
	 * 
	 * But why would we do that?
	 */
	private void removeSubclass(Set<OntClass> equivalentParents,
			Set<OntClass> equivalentChildren) {
		for (OntClass parent : equivalentParents) {
			for (OntClass child : equivalentChildren) {
				parent.removeSubClass(child);

				for (OntClass ancestor : parent.listSuperClasses().toList()) {
					ancestor.removeSubClass(child);
				}
				for (OntClass descendant : child.listSubClasses().toList()) {
					parent.removeSubClass(descendant);
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	// Object properties
	// ----------------------------------------------------------------------

	protected ObjectProperty createObjectProperty(OntModel tBox, String uri,
			String label) {
		ObjectProperty p = tBox.createObjectProperty(uri);
		p.setLabel(label, "en-US");
		p.addEquivalentProperty(p);
		p.addSubProperty(p);
		return p;
	}

	protected void setEquivalent(OntProperty p1, OntProperty p2) {
		setEquivalentProperty(equivalences(p1), equivalences(p2));
		setEquivalentProperty(equivalences(p2), equivalences(p1));
		copyInverses(p1, p2);
		copyInverses(p2, p1);
	}

	private void setEquivalentProperty(Set<OntProperty> equivalences1,
			Set<OntProperty> equivalences2) {
		for (OntProperty p1 : equivalences1) {
			for (OntProperty p2 : equivalences2) {
				p1.addEquivalentProperty(p2);
			}
		}
	}

	private void copyInverses(OntProperty p1, OntProperty p2) {
		for (OntProperty inv : p1.listInverse().toList()) {
			p2.addInverseOf(inv);
		}
	}

	protected void setInverse(OntProperty p1, OntProperty p2) {
		setInverse(equivalences(p1), equivalences(p2));
		setInverse(equivalences(p2), equivalences(p1));
	}

	private void setInverse(Set<OntProperty> equivalences1,
			Set<OntProperty> equivalences2) {
		for (OntProperty p1 : equivalences1) {
			for (OntProperty p2 : equivalences2) {
				p1.addInverseOf(p2);
			}
		}
	}

	protected void removeInverse(OntProperty p1, OntProperty p2) {
		removeInverse(equivalences(p1), equivalences(p2));
		removeInverse(equivalences(p2), equivalences(p1));
	}

	private void removeInverse(Set<OntProperty> equivalences1,
			Set<OntProperty> equivalences2) {
		for (OntProperty p1 : equivalences1) {
			for (OntProperty p2 : equivalences2) {
				p1.removeInverseProperty(p2);
			}
		}
	}

	private Set<OntProperty> equivalences(OntProperty p) {
		return new HashSet<OntProperty>(p.listEquivalentProperties().toSet());
	}

}
