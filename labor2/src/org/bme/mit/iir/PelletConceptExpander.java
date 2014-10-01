package org.bme.mit.iir;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.OWLReasonerRuntimeException;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

public class PelletConceptExpander {

	public static final String PCSHOP_ONTOLOGY_FNAME = "data/pc_shop.owl";
	public static final String PCSHOP_BASE_URI = 
			"http://mit.bme.hu/ontologia/iir_labor/pc_shop.owl#";
	public static final IRI ANNOTATION_TYPE_IRI =
			OWLRDFVocabulary.RDFS_LABEL.getIRI();


	OWLOntologyManager manager;
	OWLOntology ontology;
	OWLReasoner reasoner;
	OWLDataFactory factory;


	public PelletConceptExpander(String ontologyFilename) {

		manager = OWLManager.createOWLOntologyManager();
		ontology = null;
		try {
			ontology = manager.loadOntologyFromOntologyDocument(
					new File(ontologyFilename));
		} catch (Exception e) {
			System.err.println("Hiba az ontológia betöltése közben:\n\t"
					+ e.getMessage());
			System.exit(-1);
		}
		OWLReasonerFactory reasonerFactory = new PelletReasonerFactory();

		reasoner = reasonerFactory.createReasoner(ontology);

		try {
			if (!reasoner.isConsistent()) {
				System.err.println("Az ontológia nem konzisztens!");

				Node<OWLClass> incClss = reasoner.getUnsatisfiableClasses();
				System.err.println("A következő osztályok nem konzisztensek: "
						+ Util.join(incClss.getEntities(), ", ") + ".");
				System.exit(-1);
			}
		} catch (OWLReasonerRuntimeException e) {
			System.err.println("Hiba a következtetőben: " + e.getMessage());
			System.exit(-1);
		}
		factory = manager.getOWLDataFactory();
	}

	private OWLClass getClassFromName(String className)
	{
		IRI clsIRI = IRI.create(PCSHOP_BASE_URI + className);
		if (!ontology.containsClassInSignature(clsIRI)) {
			return null;
		}
		return factory.getOWLClass(clsIRI);
	}

	private Set<OWLClass> getSubClasses(OWLClass cls, boolean direct) {

		NodeSet<OWLClass> subClss;
		try {
			subClss = reasoner.getSubClasses(cls, direct);
		} catch (OWLReasonerRuntimeException e) {
			System.err.println("Hiba az alosztályok következtetése közben: "
					+ e.getMessage());
			return Collections.emptySet();
		}
		return subClss.getFlattened();
	}



	private Set<String> getClassAnnotations(OWLEntity entity) {
		OWLAnnotationProperty label =
				factory.getOWLAnnotationProperty(ANNOTATION_TYPE_IRI);
		Set<String> result = new HashSet<String>();
		for (OWLAnnotation a : entity.getAnnotations(ontology, label)) {
			if (a.getValue() instanceof OWLLiteral) {
				OWLLiteral value = (OWLLiteral)a.getValue();
				result.add(value.getLiteral());
			}
		}
		return Collections.unmodifiableSet(result);
	}

	public Set<String> expandConcept(String concept, boolean inclAnnotations, boolean inclSubclasses)
	{
		Set<String> similarConcepts = new HashSet<String>();
		OWLClass conceptClass = getClassFromName(concept);
		if(conceptClass != null)
		{

			if(inclAnnotations)
			{
				similarConcepts.addAll(getClassAnnotations(conceptClass));
			}

			if(inclSubclasses)
			{
				Set<OWLClass> descendants  = getSubClasses(conceptClass, false);

				for (OWLClass cls : descendants) {
					if (!cls.isBuiltIn()) {
						
						similarConcepts.add(cls.getIRI().getFragment());
						if(inclAnnotations)
							similarConcepts.addAll(getClassAnnotations(cls));
					}
				}
			}
		}
		return similarConcepts;
	}

}
