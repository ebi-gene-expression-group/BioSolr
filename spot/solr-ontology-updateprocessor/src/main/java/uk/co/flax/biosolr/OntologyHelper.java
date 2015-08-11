/**
 * Copyright (c) 2015 Lemur Consulting Ltd.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.co.flax.biosolr;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for loading an ontology and making its properties easily
 * accessible.
 *
 * @author mlp
 */
public class OntologyHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(OntologyHelper.class);

	private final OWLOntology ontology;
	private final OWLReasoner reasoner;
//	private final ShortFormProvider shortFormProvider;
	private final IRI owlNothingIRI;

	private final Map<IRI, OWLClass> owlClassMap = new HashMap<>();

	private Map<IRI, Collection<String>> labels = new HashMap<>();
	
	/**
	 * Construct a new ontology helper instance with a string representing
	 * the ontology URI.
	 * @param ontologyUriString the URI.
	 * @throws OWLOntologyCreationException if the ontology cannot be read
	 * for some reason - internal inconsistencies, etc.
	 * @throws URISyntaxException if the URI cannot be parsed.
	 */
	public OntologyHelper(String ontologyUriString) throws OWLOntologyCreationException, URISyntaxException {
		this(new URI(ontologyUriString));
	}

	/**
	 * Construct a new ontology helper instance.
	 * @param ontologyUri the URI giving the location of the ontology.
	 * @throws OWLOntologyCreationException if the ontology cannot be read
	 * for some reason - internal inconsistencies, etc.
	 * @throws URISyntaxException if the URI cannot be parsed.
	 */
	public OntologyHelper(URI ontologyUri) throws OWLOntologyCreationException, URISyntaxException {
		if (!ontologyUri.isAbsolute()) {
			// Try to read as a file from the resource path
			LOGGER.debug("Ontology URI {} is not absolute - loading from classpath", ontologyUri);
			ontologyUri = this.getClass().getClassLoader().getResource(ontologyUri.toString()).toURI();
		}
		LOGGER.info("Loading ontology from " + ontologyUri + "...");
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		IRI iri = IRI.create(ontologyUri);
		this.ontology = manager.loadOntologyFromOntologyDocument(iri);
		// Use a buffering reasoner - not interested in ongoing changes
		this.reasoner = new StructuralReasonerFactory().createReasoner(ontology);
//		this.shortFormProvider = new SimpleShortFormProvider();
		this.owlNothingIRI = manager.getOWLDataFactory().getOWLNothing().getIRI();

		// Initialise the class map
		initialiseClassMap();
	}

	private void initialiseClassMap() {
		for (OWLClass clazz : ontology.getClassesInSignature()) {
			owlClassMap.put(clazz.getIRI(), clazz);
		}
	}

	/**
	 * Explicitly dispose of the helper class, closing down any resources in 
	 * use.
	 */
	public void dispose() {
		reasoner.dispose();
	}

	/**
	 * Get the OWL class for an IRI.
	 * @param iri the IRI of the required class.
	 * @return the class from the ontology, or <code>null</code> if no such
	 * class can be found, or the IRI string is null.
	 */
	public OWLClass getOwlClass(String iri) {
		OWLClass ret = null;
		
		if (StringUtils.isNotBlank(iri)) {
			ret = owlClassMap.get(IRI.create(iri));
		}
		
		return ret;
	}

	/**
	 * Find the labels for a single OWL class.
	 * @param owlClass the class whose labels are required.
	 * @return a collection of labels for the class.
	 */
	public Collection<String> findLabels(OWLClass owlClass) {
		return findLabels(owlClass.getIRI());
	}
	
	/**
	 * Find all of the labels for a collection of OWL class IRIs.
	 * @param iris the IRIs whose labels should be looked up.
	 * @return a collection of labels. Never <code>null</code>.
	 */
	public Collection<String> findLabelsForIRIs(Collection<String> iris) {
		Set<String> labels = new HashSet<>();
		iris.stream().map(iri -> findLabels(IRI.create(iri))).forEach(labels::addAll);
		return labels;
	}

	private Collection<String> findLabels(IRI iri) {
		Set<String> classNames = new HashSet<>();

		if (!labels.containsKey(iri)) {
			// get label annotation property
			OWLAnnotationProperty labelAnnotationProperty = ontology.getOWLOntologyManager().getOWLDataFactory()
					.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());

			classNames = new HashSet<>(findAnnotationNames(iri, labelAnnotationProperty));
			labels.put(iri, classNames);
		}

		return labels.get(iri);
	}

	private Collection<String> findAnnotationNames(IRI iri, OWLAnnotationProperty annotationType) {
		Collection<String> classNames = new HashSet<String>();

		// get all literal annotations
		for (OWLAnnotationAssertionAxiom axiom : ontology.getAnnotationAssertionAxioms(iri)) {
			if (axiom.getAnnotation().getProperty().equals(annotationType)) {
				OWLAnnotationValue value = axiom.getAnnotation().getValue();
				if (value instanceof OWLLiteral) {
					classNames.add(((OWLLiteral) value).getLiteral());
				}
			}
		}

		return classNames;
	}

	/**
	 * Get the direct child URIs for an OWL class.
	 * @param owlClass
	 * @return the child URIs, as strings. Never <code>null</code>.
	 */
	public Collection<String> getChildUris(OWLClass owlClass) {
    	return getSubclassUris(owlClass, true);
    }

	/**
	 * Get all descendent URIs for an OWL class, including direct children.
	 * @param owlClass
	 * @return the descendent URIs, as strings. Never <code>null</code>.
	 */
	public Collection<String> getDescendentUris(OWLClass owlClass) {
    	return getSubclassUris(owlClass, false);
    }
	
	/**
	 * Get direct parent URIs for an OWL class.
	 * @param owlClass
	 * @return the parent URIs, as strings. Never <code>null</code>.
	 */
    public Collection<String> getParentUris(OWLClass owlClass) {
    	return getSuperclassUris(owlClass, true);
    }
    
    /**
     * Get all ancestor URIs for an OWL class, including direct parents.
     * @param owlClass
     * @return the ancestor URIs, as strings. Never <code>null</code>.
     */
    public Collection<String> getAncestorUris(OWLClass owlClass) {
    	return getSuperclassUris(owlClass, false);
    }
    
	private Collection<String> getSubclassUris(OWLClass owlClass, boolean direct) {
    	return getUrisFromNodeSet(reasoner.getSubClasses(owlClass, direct));
	}

	private Collection<String> getSuperclassUris(OWLClass owlClass, boolean direct) {
    	return getUrisFromNodeSet(reasoner.getSuperClasses(owlClass, direct));
	}

    private Collection<String> getUrisFromNodeSet(NodeSet<OWLClass> nodeSet) {
    	Set<String> uris = new HashSet<>();
    	
    	for (Node<OWLClass> node : nodeSet) {
    		for (OWLClass expr : node.getEntities()) {
    			if (isClassSatisfiable(expr)) {
    				uris.add(expr.getIRI().toURI().toString());
    			}
    		}
    	}
    	
    	return uris;
    }
    
    private boolean isClassSatisfiable(OWLClass owlClass) {
    	return !owlClass.isAnonymous() && !owlClass.getIRI().equals(owlNothingIRI);
    }
    
    /**
     * Retrieve a map of related classes for a particular class.
     * @param owlClass
     * @return a map of relation type to a list of IRIs for nodes with that relationship.
     */
    public Map<String, List<String>> getRestrictions(OWLClass owlClass) {
    	RestrictionVisitor visitor = new RestrictionVisitor(Collections.singleton(ontology));
		for (OWLSubClassOfAxiom ax : ontology.getSubClassAxiomsForSubClass(owlClass)) {
			OWLClassExpression superCls = ax.getSuperClass();
			// Ask our superclass to accept a visit from the RestrictionVisitor
			// - if it is an existential restriction then our restriction visitor
			// will answer it - if not our visitor will ignore it
			superCls.accept(visitor);
		}
		
		Map<String, List<String>> restrictions = new HashMap<>();
		for (OWLObjectSomeValuesFrom val : visitor.getSomeValues()) {
			OWLClassExpression exp = val.getFiller();
			
			// Get the shortname of the property expression
			String shortForm = null;
			Set<OWLObjectProperty> signatureProps = val.getProperty().getObjectPropertiesInSignature();
			for (OWLObjectProperty sigProp : signatureProps) {
				Collection<String> labels = findLabels(sigProp.getIRI());
				if (labels.size() > 0) {
					shortForm = new ArrayList<String>(labels).get(0);
				}
			}

			if (shortForm != null && !exp.isAnonymous()) {
				IRI iri = exp.asOWLClass().getIRI();
				
				if (!restrictions.containsKey(shortForm)) {
					restrictions.put(shortForm, new ArrayList<String>());
				}
				restrictions.get(shortForm).add(iri.toString());
			}
		}
		
		return restrictions;
    }
    
}