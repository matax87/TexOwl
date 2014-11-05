package eu.fbk.irst.dkm.owlapi.latexstylerenderer;

import static eu.fbk.irst.dkm.owlapi.latexstylerenderer.LatexStyleVocabulary.*;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.EscapeUtils;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

import eu.fbk.irst.dkm.owlapi.latexstylerenderer.LatexStyleVocabulary;

public class OWLLatexStyleSyntaxObjectRenderer implements OWLObjectVisitor {
	private static final Set<String> default_prefix_names = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("owl:", "rdf:", "rdfs:", "xml:", "xsd:")));
	private DefaultPrefixManager prefixManager;
    protected OWLOntology ontology;
    private Writer writer;
    private int pos;
    int lastNewLinePos;
    private boolean writeEnitiesAsURIs;
    private OWLObject focusedObject;
	
	public OWLLatexStyleSyntaxObjectRenderer(OWLOntology ontology, Writer writer) {
		this.ontology = ontology;
        this.writer = writer;
        prefixManager = new DefaultPrefixManager();
        writeEnitiesAsURIs = true;
        OWLOntologyFormat ontologyFormat = ontology.getOWLOntologyManager()
                .getOntologyFormat(ontology);
        if (ontologyFormat instanceof PrefixOWLOntologyFormat) {
        	PrefixOWLOntologyFormat prefixFormat = (PrefixOWLOntologyFormat) ontologyFormat;
        	for (String prefixName : prefixFormat.getPrefixNames()) {
                String prefix = prefixFormat.getPrefix(prefixName);
                prefixManager.setPrefix(prefixName, prefix);
            }
        }
        if (!ontology.isAnonymous()) {
            String defPrefix = ontology.getOntologyID().getOntologyIRI() + "#";
            prefixManager.setDefaultPrefix(defPrefix);
        }
        focusedObject = ontology.getOWLOntologyManager().getOWLDataFactory()
                .getOWLThing();
	}

	public void setPrefixManager(DefaultPrefixManager prefixManager) {
        this.prefixManager = prefixManager;
    }
	
	public void setFocusedObject(OWLObject focusedObject) {
        this.focusedObject = focusedObject;
    }
	
    public void writePrefix(String prefix, String namespace) {
    	write("\\ns");
		writeSpace();
        write(prefix);
        writeSpace();
        write("<");
        write(namespace);
        write(">");
        write("\n");
    }
    
    public void writeDefaultPrefix(String namespace) {
    	write("\\ns");
		writeSpace();
        write("<");
        write(namespace);
        write(">");
        write("\n");
    }

    private Set<String> writePrefixes() {    	    	
    	Set<String> prefixes = new HashSet<String>();
        for (String prefixName : prefixManager.getPrefixName2PrefixMap().keySet()) {
        	String prefix = prefixManager.getPrefix(prefixName);        	
        	if (!default_prefix_names.contains(prefixName)) {
        		prefixes.add(prefixName);
        		if (prefixName.equals(":")) {
            		writeDefaultPrefix(prefix);
            	} else {
            		writePrefix(prefixName, prefix);
            	}
        	}            
        }
        return prefixes;
    }
    
    private void write(LatexStyleVocabulary v) {
        write(v.getShortName());
    }

    private void write(String s) {
        try {
            int newLineIndex = s.indexOf('\n');
            if (newLineIndex != -1) {
                lastNewLinePos = pos + newLineIndex;
            }
            pos += s.length();
            writer.write(s);
        } catch (IOException e) {
            throw new OWLRuntimeException(e);
        }
    }
    
    private void write(IRI iri) {
        String iriString = iri.toString();
        String qname = prefixManager.getPrefixIRI(iri);   
        if (qname != null && !qname.equals(iriString)) {
        	if (qname.startsWith(":")) {
        		qname = qname.substring(1);
        	}
            write(qname);
        } else {
        	writeFullIRI(iri);
        }
    }
    
    private void writeFullIRI(IRI iri) {
        write("<");
        write(iri.toString());
        write(">");
    }
	
	@Override
	public void visit(OWLOntology ontology1) {
		if (!writePrefixes().isEmpty()) {
			write("\n");
		}
		write(START_ONTOLOGY);
		if (!ontology1.isAnonymous()) {
			writeOpenSquarePar();
			writeFullIRI(ontology1.getOntologyID().getOntologyIRI());
			if (ontology1.getOntologyID().getVersionIRI() != null) {
				writeComma();
				writeSpace();
                writeFullIRI(ontology1.getOntologyID().getVersionIRI());
            }
			writeCloseSquarePar();
		}
		write("\n");
		for (OWLImportsDeclaration decl : ontology1.getImportsDeclarations()) {
            write(IMPORT);
            writeSpace();
            writeFullIRI(decl.getIRI());
            write("\n");
        }
		for (OWLAnnotation ontologyAnnotation : ontology1.getAnnotations()) {
            ontologyAnnotation.accept(this);
            write("\n");
        }
		write("\n");
		Set<OWLAxiom> writtenAxioms = new HashSet<OWLAxiom>();
		List<OWLEntity> signature = new ArrayList<OWLEntity>(ontology1.getSignature());
		Collections.sort(signature);
		for (OWLEntity ent : signature) {
            writeDeclarations(ent, writtenAxioms);
        }
		write("\n");
		for (OWLEntity ent : signature) {
            writeAxioms(ent, writtenAxioms);
        }
		write("\n");
		List<OWLAxiom> remainingAxioms = new ArrayList<OWLAxiom>(ontology1.getAxioms());
        remainingAxioms.removeAll(writtenAxioms);
        for (OWLAxiom ax : remainingAxioms) {
            ax.accept(this);
            write("\n");
        }
		write(END_ONTOLOGY);
	}
	
	/** Writes out the axioms that define the specified entity
     * 
     * @param entity
     *            The entity
     * @return The set of axioms that was written out */
    public Set<OWLAxiom> writeAxioms(OWLEntity entity) {
        Set<OWLAxiom> writtenAxioms = new HashSet<OWLAxiom>();
        return writeAxioms(entity, writtenAxioms);
    }

    private Set<OWLAxiom>
            writeAxioms(OWLEntity entity, Set<OWLAxiom> alreadyWrittenAxioms) {
        Set<OWLAxiom> writtenAxioms = new HashSet<OWLAxiom>();
        setFocusedObject(entity);
        writtenAxioms.addAll(writeDeclarations(entity, alreadyWrittenAxioms));
        writtenAxioms.addAll(writeAnnotations(entity));
        List<OWLAxiom> axs = new ArrayList<OWLAxiom>();
        axs.addAll(entity.accept(new OWLEntityVisitorEx<Set<? extends OWLAxiom>>() {
            @Override
            public Set<? extends OWLAxiom> visit(OWLClass cls) {
                return ontology.getAxioms(cls);
            }

            @Override
            public Set<? extends OWLAxiom> visit(OWLObjectProperty property) {
                return ontology.getAxioms(property);
            }

            @Override
            public Set<? extends OWLAxiom> visit(OWLDataProperty property) {
                return ontology.getAxioms(property);
            }

            @Override
            public Set<? extends OWLAxiom> visit(OWLNamedIndividual individual) {
                return ontology.getAxioms(individual);
            }

            @Override
            public Set<? extends OWLAxiom> visit(OWLDatatype datatype) {
                return ontology.getAxioms(datatype);
            }

            @Override
            public Set<? extends OWLAxiom> visit(OWLAnnotationProperty property) {
                return ontology.getAxioms(property);
            }
        }));
        // Collections.sort(axs);
        for (OWLAxiom ax : axs) {
            if (alreadyWrittenAxioms.contains(ax)) {
                continue;
            }
            if (ax.getAxiomType().equals(AxiomType.DIFFERENT_INDIVIDUALS)) {
                continue;
            }
            if (ax.getAxiomType().equals(AxiomType.DISJOINT_CLASSES)
                    && ((OWLDisjointClassesAxiom) ax).getClassExpressions().size() > 2) {
                continue;
            }
            ax.accept(this);
            writtenAxioms.add(ax);
            write("\n");
        }
        alreadyWrittenAxioms.addAll(writtenAxioms);
        return writtenAxioms;
    }
	
	private Set<OWLAxiom> writeDeclarations(OWLEntity entity, Set<OWLAxiom> alreadyWrittenAxioms) {
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		for (OWLAxiom ax : ontology.getDeclarationAxioms(entity)) {
			if (!alreadyWrittenAxioms.contains(ax)) {
				ax.accept(this);
				axioms.add(ax);
				write("\n");
			}
		}
		alreadyWrittenAxioms.addAll(axioms);
		return axioms;
	}
	
	/** Writes of the annotation for the specified entity
     * 
     * @param entity
     *            The entity
     * @return The set of axioms that were written out */
    public Set<OWLAxiom> writeAnnotations(OWLEntity entity) {
        Set<OWLAxiom> annotationAssertions = new HashSet<OWLAxiom>();
        for (OWLAnnotationAxiom ax : entity.getAnnotationAssertionAxioms(ontology)) {
            ax.accept(this);
            annotationAssertions.add(ax);
            write("\n");
        }
        return annotationAssertions;
    }
    
    private void writeAnnotations(Collection<OWLAnnotation> annotations) {
    	if (!annotations.isEmpty()) {
    		writeSpace();
			writeOpenSquarePar();
		}
		for (Iterator<OWLAnnotation> it = annotations.iterator(); it.hasNext();) {
			it.next().accept(this);
			if (it.hasNext()) {
            	writeComma();
                writeSpace();
            }            
        }
		if (!annotations.isEmpty()) {
			writeCloseSquarePar();
		}
    }
    
    private void writeOpenBracket() {
    	write("(");
    }
    
    private void writeCloseBracket() {
    	write(")");
    }
    
    private void writeOpenBrace() {
    	write("{");
    }
    
    private void writeCloseBrace() {
    	write("}");
    }
    
    private void writeOpenSquarePar() {
    	write("[");
    }
    
    private void writeCloseSquarePar() {
    	write("]");
    }
    
    private void writeSpace() {
        write(" ");
    }
    
    private void writeComma() {
        write(",");
    }
    
    public void writeAnnotations(OWLAxiom ax) {
    	writeAnnotations(ax.getAnnotations());
    }
    
    public void writeAxiomEnd(OWLAxiom axiom) {
    	writeAnnotations(axiom);
    }

    public void writePropertyCharacteristic(LatexStyleVocabulary v, OWLAxiom ax,
            OWLPropertyExpression<?, ?> prop) {
        prop.accept(this);
        writeSpace();
        write(v);
        writeAxiomEnd(ax);
    }
    
    
	@Override
	public void visit(IRI iri) {
		write(iri);
	}
	
	
	@Override
	public void visit(OWLAnnotation node) {
		write(ANNOTATION);
		writeOpenBrace();
		node.getProperty().accept(this);
		writeComma();
		writeSpace();
		node.getValue().accept(this);
		writeCloseBrace();
		writeAnnotations(node.getAnnotations());
	}
    
    
    @Override
	public void visit(OWLClass desc) {
		desc.getIRI().accept(this);
        if (!writeEnitiesAsURIs) {
        	writeSpace();
            write(CLASS);
        }
	}
    
    @Override
	public void visit(OWLDatatype node) {
		node.getIRI().accept(this);
        if (!writeEnitiesAsURIs) {
        	writeSpace();
            write(DATATYPE);
        }
	}    
    
	@Override
	public void visit(OWLObjectProperty property) {
		property.getIRI().accept(this);
        if (!writeEnitiesAsURIs) {
        	writeSpace();
            write(OBJECT_PROPERTY);
        }
	}
	
	@Override
	public void visit(OWLDataProperty property) {
		property.getIRI().accept(this);
        if (!writeEnitiesAsURIs) {
        	writeSpace();
            write(DATA_PROPERTY);
        }
	}
	
	@Override
	public void visit(OWLAnnotationProperty property) {
        property.getIRI().accept(this);
        if (!writeEnitiesAsURIs) {
        	writeSpace();
            write(ANNOTATION_PROPERTY);
        }
	}
	
	@Override
	public void visit(OWLNamedIndividual individual) {
		individual.getIRI().accept(this);
	    if (!writeEnitiesAsURIs) {
	    	writeSpace();
	    	write(NAMED_INDIVIDUAL);
	    }		
	}
	
	@Override
	public void visit(OWLAnonymousIndividual individual) {
		write(individual.getID().toString());
	}
    
	@Override
	public void visit(OWLLiteral node) {
		write("\"");
        write(EscapeUtils.escapeString(node.getLiteral()));
        write("\"");
        if (node.hasLang()) {
        	writeOpenSquarePar();
            write("@");
            write(node.getLang());
            writeCloseSquarePar();
        } else if (!node.isRDFPlainLiteral()) {
        	writeOpenSquarePar();
            write(node.getDatatype().getIRI());
            writeCloseSquarePar();
        }
	}
	
	@Override
	public void visit(OWLDeclarationAxiom axiom) {
		writeEnitiesAsURIs = false;
		axiom.getEntity().accept(this);
		writeEnitiesAsURIs = true;
		writeAxiomEnd(axiom);
	}
	
	
	private void writeDataRanges(Collection<OWLDataRange> drs) {
		writeOpenBrace();
		for (Iterator<OWLDataRange> it = drs.iterator(); it.hasNext();) {
            it.next().accept(this);
            if (it.hasNext()) {
            	writeComma();
                writeSpace();
            }
        }
		writeCloseBrace();
	}
	
	private void writeDataRangeBinaryOp(Collection<OWLDataRange> drs, LatexStyleVocabulary v) {
		if (drs.size() == 2){
			Iterator<OWLDataRange> it = drs.iterator();
			OWLDataRange drA = it.next();
			OWLDataRange drB = it.next();
			OWLDataRange left, right;
            if (drA.equals(focusedObject)) {
            	left = drA;
            	right = drB;
            } else {
            	left = drB;
            	right = drA;
            }
            left.accept(this);
            writeSpace();
            write(v);
            writeSpace();
            right.accept(this);
		}
	}
	
	@Override
	public void visit(OWLDataIntersectionOf node) {
		Set<OWLDataRange> drs = node.getOperands();
		if (drs.size() > 2) {
			write(SEQ_DATA_INTERSECTION_OF);
			writeDataRanges(drs);
        } else if (drs.size() == 2) {
        	writeOpenBracket();
        	writeDataRangeBinaryOp(drs, DATA_INTERSECTION_OF);
        	writeCloseBracket();
        }
	}

	@Override
	public void visit(OWLDataUnionOf node) {
		Set<OWLDataRange> drs = node.getOperands();
		if (drs.size() > 2) {
			write(SEQ_DATA_UNION_OF);
			writeDataRanges(drs);
        } else if (drs.size() == 2) {
        	writeOpenBracket();
        	writeDataRangeBinaryOp(drs, DATA_UNION_OF);
        	writeCloseBracket();
        }
	}
	
	private void writeLiterals(Collection<OWLLiteral> literals) {
		writeOpenBrace();
		for (Iterator<OWLLiteral> it = literals.iterator(); it.hasNext();) {
            it.next().accept(this);
            if (it.hasNext()) {
            	writeComma();
                writeSpace();
            }
        }
		writeCloseBrace();
	}

	@Override
	public void visit(OWLDataOneOf node) {
		write(DATA_ONE_OF);
		writeLiterals(node.getValues());
	}

	@Override
	public void visit(OWLDataComplementOf node) {
		writeOpenBracket();
		write(DATA_COMPLEMENT_OF);
		writeSpace();
		node.getDataRange().accept(this);
		writeCloseBracket();
	}
	
	private void writeFacetRestrictions(Collection<OWLFacetRestriction> facets) {
		writeOpenBrace();
		for (Iterator<OWLFacetRestriction> it = facets.iterator(); it.hasNext();) {
            it.next().accept(this);
            if (it.hasNext()) {
            	writeComma();
                writeSpace();
            }
        }
		writeCloseBrace();
	}
	
	@Override
	public void visit(OWLFacetRestriction node) {
		write(node.getFacet().getIRI());
        writeSpace();
        node.getFacetValue().accept(this);
	}

	@Override
	public void visit(OWLDatatypeRestriction node) {
		writeOpenBracket();
		node.getDatatype().accept(this);
		writeSpace();
		write(DATATYPE_RESTRICTION);
		writeFacetRestrictions(node.getFacetRestrictions());
		writeCloseBracket();
	}
    
    	
	private void writeClassExpressions(Collection<OWLClassExpression> descs) {
		writeOpenBrace();
		for (Iterator<OWLClassExpression> it = descs.iterator(); it.hasNext();) {
            it.next().accept(this);
            if (it.hasNext()) {
            	writeComma();
                writeSpace();
            }
        }
		writeCloseBrace();
	}
	
	private void writeClassExpressionBinaryOp(Collection<OWLClassExpression> descs, LatexStyleVocabulary v) {
		if (descs.size() == 2){
			Iterator<OWLClassExpression> it = descs.iterator();
            OWLClassExpression descA = it.next();
            OWLClassExpression descB = it.next();
            OWLClassExpression left, right;
            if (descA.equals(focusedObject)) {
            	left = descA;
            	right = descB;
            } else {
            	left = descB;
            	right = descA;
            }
            left.accept(this);
            writeSpace();
            write(v);
            writeSpace();
            right.accept(this);
		}
	}

	@Override
	public void visit(OWLObjectIntersectionOf desc) {
		Set<OWLClassExpression> operands = desc.getOperands();
		if (operands.size() > 2) {
			write(SEQ_OBJECT_INTERSECTION_OF);
			writeClassExpressions(operands);
        } else if (operands.size() == 2) {
        	writeOpenBracket();
        	writeClassExpressionBinaryOp(operands, OBJECT_INTERSECTION_OF);
        	writeCloseBracket();
        }
	}

	@Override
	public void visit(OWLObjectUnionOf desc) {
		Set<OWLClassExpression> operands = desc.getOperands();
		if (operands.size() > 2) {
			write(SEQ_OBJECT_UNION_OF);
			writeClassExpressions(operands);
        } else if (operands.size() == 2) {
        	writeOpenBracket();
        	writeClassExpressionBinaryOp(operands, OBJECT_UNION_OF);
        	writeCloseBracket();
        }
	}

	@Override
	public void visit(OWLObjectComplementOf desc) {
		writeOpenBracket();
		write(OBJECT_COMPLEMENT_OF);
		writeSpace();
		desc.getOperand().accept(this);
		writeCloseBracket();
	}
	
	@Override
	public void visit(OWLObjectOneOf desc) {
		write(OBJECT_ONE_OF);
		writeIndividuals(desc.getIndividuals());
	}

	@Override
	public void visit(OWLObjectSomeValuesFrom desc) {
		write(OBJECT_SOME_VALUES_FROM);
		writeOpenBrace();
		desc.getProperty().accept(this);
		writeCloseBrace();
		writeOpenBrace();
		desc.getFiller().accept(this);
		writeCloseBrace();
	}

	@Override
	public void visit(OWLObjectAllValuesFrom desc) {
		write(OBJECT_ALL_VALUES_FROM);
		writeOpenBrace();
		desc.getProperty().accept(this);
		writeCloseBrace();
		writeOpenBrace();
		desc.getFiller().accept(this);
		writeCloseBrace();
	}

	@Override
	public void visit(OWLObjectHasValue desc) {
		write(OBJECT_HAS_VALUE);
		writeOpenBrace();
		desc.getProperty().accept(this);
		writeCloseBrace();
		writeOpenBrace();
		desc.getValue().accept(this);
		writeCloseBrace();
	}
	
	@Override
	public void visit(OWLObjectHasSelf desc) {
		writeOpenBracket();
		write(OBJECT_HAS_SELF);
		writeSpace();
		desc.getProperty().accept(this);
		writeCloseBracket();
	}

	@Override
	public void visit(OWLObjectMinCardinality desc) {
		write(OBJECT_MIN_CARDINALITY);
		write(Integer.toString(desc.getCardinality()));
		writeCloseSquarePar();
		writeOpenBrace();
		desc.getProperty().accept(this);
		writeCloseBrace();
		if (desc.isQualified()) {
			writeOpenBrace();
			desc.getFiller().accept(this);
			writeCloseBrace();
		}
	}
	
	@Override
	public void visit(OWLObjectMaxCardinality desc) {
		write(OBJECT_MAX_CARDINALITY);
		write(Integer.toString(desc.getCardinality()));
		writeCloseSquarePar();
		writeOpenBrace();
		desc.getProperty().accept(this);
		writeCloseBrace();
		if (desc.isQualified()) {
			writeOpenBrace();
			desc.getFiller().accept(this);
			writeCloseBrace();
		}
	}

	@Override
	public void visit(OWLObjectExactCardinality desc) {
		write(OBJECT_EXACT_CARDINALITY);
		write(Integer.toString(desc.getCardinality()));
		writeCloseSquarePar();
		writeOpenBrace();
		desc.getProperty().accept(this);
		writeCloseBrace();
		if (desc.isQualified()) {
			writeOpenBrace();
			desc.getFiller().accept(this);
			writeCloseBrace();
		}
	}

	@Override
	public void visit(OWLDataSomeValuesFrom desc) {
		write(DATA_SOME_VALUES_FROM);
		writeOpenBrace();
		desc.getProperty().accept(this);
		writeCloseBrace();
		writeOpenBrace();
		desc.getFiller().accept(this);
		writeCloseBrace();
	}

	@Override
	public void visit(OWLDataAllValuesFrom desc) {
		write(DATA_ALL_VALUES_FROM);
		writeOpenBrace();
		desc.getProperty().accept(this);
		writeCloseBrace();
		writeOpenBrace();
		desc.getFiller().accept(this);
		writeCloseBrace();
	}

	@Override
	public void visit(OWLDataHasValue desc) {
		write(DATA_HAS_VALUE);
		writeOpenBrace();
		desc.getProperty().accept(this);
		writeCloseBrace();
		writeOpenBrace();
		desc.getValue().accept(this);
		writeCloseBrace();
	}

	@Override
	public void visit(OWLDataMinCardinality desc) {
		write(DATA_MIN_CARDINALITY);
		write(Integer.toString(desc.getCardinality()));
		writeCloseSquarePar();
		writeOpenBrace();
		desc.getProperty().accept(this);
		writeCloseBrace();
		if (desc.isQualified()) {
			writeOpenBrace();
			desc.getFiller().accept(this);
			writeCloseBrace();
		}
	}
	
	@Override
	public void visit(OWLDataMaxCardinality desc) {
		write(DATA_MAX_CARDINALITY);
		write(Integer.toString(desc.getCardinality()));
		writeCloseSquarePar();
		writeOpenBrace();
		desc.getProperty().accept(this);
		writeCloseBrace();
		if (desc.isQualified()) {
			writeOpenBrace();
			desc.getFiller().accept(this);
			writeCloseBrace();
		}
	}

	@Override
	public void visit(OWLDataExactCardinality desc) {
		write(DATA_EXACT_CARDINALITY);
		write(Integer.toString(desc.getCardinality()));
		writeCloseSquarePar();
		writeOpenBrace();
		desc.getProperty().accept(this);
		writeCloseBrace();
		if (desc.isQualified()) {
			writeOpenBrace();
			desc.getFiller().accept(this);
			writeCloseBrace();
		}
	}
	

	@Override
	public void visit(OWLSubClassOfAxiom axiom) {
        axiom.getSubClass().accept(this);
        writeSpace();
        write(SUB_CLASS_OF);
        writeSpace();
        axiom.getSuperClass().accept(this);
        writeAxiomEnd(axiom);
	}
	
	@Override
	public void visit(OWLEquivalentClassesAxiom axiom) {
		Set<OWLClassExpression> operands = axiom.getClassExpressions();
		if (operands.size() > 2) {
			write(SEQ_EQUIVALENT_CLASSES);
			writeClassExpressions(operands);
        } else if (operands.size() == 2) {
        	writeClassExpressionBinaryOp(operands, EQUIVALENT_CLASSES);
        }
		writeAxiomEnd(axiom);
	}
	
	@Override
	public void visit(OWLDisjointClassesAxiom axiom) {
		Set<OWLClassExpression> operands = axiom.getClassExpressions();
		if (operands.size() > 2) {
			write(SEQ_DISJOINT_CLASSES);
			writeClassExpressions(operands);
        } else if (operands.size() == 2) {
        	writeClassExpressionBinaryOp(operands, DISJOINT_CLASSES);
        }
		writeAxiomEnd(axiom);
	}
	
	@Override
	public void visit(OWLDisjointUnionAxiom axiom) {
		axiom.getOWLClass().accept(this);
		writeSpace();		
		write(DISJOINT_UNION);
		writeClassExpressions(axiom.getClassExpressions());
		writeAxiomEnd(axiom);
	}
	
	@Override
	public void visit(OWLObjectInverseOf property) {
		writeOpenBracket();
		write(OBJECT_INVERSE_OF);
		writeSpace();
		property.getInverse().accept(this);
		writeCloseBracket();
	}
	
	@Override
	public void visit(OWLSubObjectPropertyOfAxiom axiom) {
		axiom.getSubProperty().accept(this);
		writeSpace();
		write(SUB_OBJECT_PROPERTY_OF);
		writeSpace();
		axiom.getSuperProperty().accept(this);
		writeAxiomEnd(axiom);
	}
	
	private void writeObjectProperties(Collection<OWLObjectPropertyExpression> opes) {
		writeOpenBrace();
		for (Iterator<OWLObjectPropertyExpression> it = opes.iterator(); it.hasNext();) {
            it.next().accept(this);
            if (it.hasNext()) {
            	writeComma();
                writeSpace();
            }
        }
		writeCloseBrace();
	}
	
	private void writeObjectPropertyBinaryOp(Collection<OWLObjectPropertyExpression> opes, LatexStyleVocabulary v) {
		if (opes.size() == 2){
			Iterator<OWLObjectPropertyExpression> it = opes.iterator();
			OWLObjectPropertyExpression opeA = it.next();
			OWLObjectPropertyExpression opeB = it.next();
			OWLObjectPropertyExpression left, right;
            if (opeA.equals(focusedObject)) {
            	left = opeA;
            	right = opeB;
            } else {
            	left = opeB;
            	right = opeA;
            }
            left.accept(this);
            writeSpace();
            write(v);
            writeSpace();
            right.accept(this);
		}
	}
	
	@Override
	public void visit(OWLSubPropertyChainOfAxiom axiom) {
		write(OBJECT_PROPERTY_CHAIN);
		writeObjectProperties(axiom.getPropertyChain());
		writeSpace();
		write(SUB_OBJECT_PROPERTY_OF);
		writeSpace();
		axiom.getSuperProperty().accept(this);
		writeAxiomEnd(axiom);
	}
	
	@Override
	public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
		Set<OWLObjectPropertyExpression> properties = axiom.getProperties();
		if (properties.size() > 2) {
			write(SEQ_EQUIVALENT_OBJECT_PROPERTIES);
			writeObjectProperties(properties);
        } else if (properties.size() == 2) {
        	writeObjectPropertyBinaryOp(properties, EQUIVALENT_OBJECT_PROPERTIES);
        }
		writeAxiomEnd(axiom);
	}
	
	@Override
	public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
		Set<OWLObjectPropertyExpression> properties = axiom.getProperties();
		if (properties.size() > 2) {
			write(SEQ_DISJOINT_OBJECT_PROPERTIES);
			writeObjectProperties(properties);
        } else if (properties.size() == 2) {
        	writeObjectPropertyBinaryOp(properties, DISJOINT_OBJECT_PROPERTIES);
        }
		writeAxiomEnd(axiom);
	}
	
	@Override
	public void visit(OWLInverseObjectPropertiesAxiom axiom) {		
        axiom.getFirstProperty().accept(this);
        writeSpace();
        write(INVERSE_OBJECT_PROPERTIES);
        writeSpace();
        axiom.getSecondProperty().accept(this);
		writeAxiomEnd(axiom);
	}
	
	@Override
	public void visit(OWLObjectPropertyDomainAxiom axiom) {
		axiom.getProperty().accept(this);
        writeSpace();
        write(OBJECT_PROPERTY_DOMAIN);
        writeSpace();
        axiom.getDomain().accept(this);
        writeAxiomEnd(axiom);
	}
	
	@Override
	public void visit(OWLObjectPropertyRangeAxiom axiom) {
        axiom.getProperty().accept(this);
        writeSpace();
        write(OBJECT_PROPERTY_RANGE);
        writeSpace();
        axiom.getRange().accept(this);
        writeAxiomEnd(axiom);
	}
	
	@Override
	public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
		writePropertyCharacteristic(FUNCTIONAL_OBJECT_PROPERTY, axiom,
                axiom.getProperty());
	}
	
	@Override
	public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
		writePropertyCharacteristic(INVERSE_FUNCTIONAL_OBJECT_PROPERTY, axiom,
                axiom.getProperty());
	}
	
	@Override
	public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
		writePropertyCharacteristic(REFLEXIVE_OBJECT_PROPERTY, axiom, axiom.getProperty());
	}
	
	@Override
	public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
		writePropertyCharacteristic(IRREFLEXIVE_OBJECT_PROPERTY, axiom,
                axiom.getProperty());
	}

	@Override
	public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
		writePropertyCharacteristic(SYMMETRIC_OBJECT_PROPERTY, axiom, axiom.getProperty());
	}
	
	@Override
	public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
		writePropertyCharacteristic(ASYMMETRIC_OBJECT_PROPERTY, axiom,
                axiom.getProperty());
	}
	
	@Override
	public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
		writePropertyCharacteristic(TRANSITIVE_OBJECT_PROPERTY, axiom,
                axiom.getProperty());
	}
	
	
	@Override
	public void visit(OWLSubDataPropertyOfAxiom axiom) {		
        axiom.getSubProperty().accept(this);
        writeSpace();
        write(SUB_DATA_PROPERTY_OF);
        writeSpace();
        axiom.getSuperProperty().accept(this);        
		writeAxiomEnd(axiom);
	}	
	
	private void writeDataProperties(Collection<OWLDataPropertyExpression> dpes) {
		writeOpenBrace();
		for (Iterator<OWLDataPropertyExpression> it = dpes.iterator(); it.hasNext();) {
            it.next().accept(this);
            if (it.hasNext()) {
            	writeComma();
                writeSpace();
            }
        }
		writeCloseBrace();
	}
	
	private void writeDataPropertyBinaryOp(Collection<OWLDataPropertyExpression> dpes, LatexStyleVocabulary v) {
		if (dpes.size() == 2){
			Iterator<OWLDataPropertyExpression> it = dpes.iterator();
			OWLDataPropertyExpression dpeA = it.next();
			OWLDataPropertyExpression dpeB = it.next();
			OWLDataPropertyExpression left, right;
            if (dpeA.equals(focusedObject)) {
            	left = dpeA;
            	right = dpeB;
            } else {
            	left = dpeB;
            	right = dpeA;
            }
            left.accept(this);
            writeSpace();
            write(v);
            writeSpace();
            right.accept(this);
		}
	}
	
	@Override
	public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
		Set<OWLDataPropertyExpression> properties = axiom.getProperties();
		if (properties.size() > 2) {
			write(SEQ_EQUIVALENT_DATA_PROPERTIES);
			writeDataProperties(properties);
        } else if (properties.size() == 2) {
        	writeDataPropertyBinaryOp(properties, EQUIVALENT_DATA_PROPERTIES);
        }
		writeAxiomEnd(axiom);
	}
	
	@Override
	public void visit(OWLDisjointDataPropertiesAxiom axiom) {
		Set<OWLDataPropertyExpression> properties = axiom.getProperties();
		if (properties.size() > 2) {
			write(SEQ_DISJOINT_DATA_PROPERTIES);
			writeDataProperties(properties);
        } else if (properties.size() == 2) {
        	writeDataPropertyBinaryOp(properties, DISJOINT_DATA_PROPERTIES);
        }
		writeAxiomEnd(axiom);
	}
	
	@Override
	public void visit(OWLDataPropertyDomainAxiom axiom) {
        axiom.getProperty().accept(this);
        writeSpace();
        write(DATA_PROPERTY_DOMAIN);
        writeSpace();
        axiom.getDomain().accept(this);
        writeAxiomEnd(axiom);
	}
	
	@Override
	public void visit(OWLDataPropertyRangeAxiom axiom) {
		axiom.getProperty().accept(this);
        writeSpace();
        write(DATA_PROPERTY_RANGE);
        writeSpace();
        axiom.getRange().accept(this);
        writeAxiomEnd(axiom);
	}
	
	@Override
	public void visit(OWLFunctionalDataPropertyAxiom axiom) {
		writePropertyCharacteristic(FUNCTIONAL_DATA_PROPERTY, axiom, axiom.getProperty());
	}
	
	
	@Override
	public void visit(OWLDatatypeDefinitionAxiom axiom) {
		axiom.getDatatype().accept(this);
		writeSpace();
		write(DATATYPE_DEFINITION);        
        writeSpace();
        axiom.getDataRange().accept(this);		
		writeAxiomEnd(axiom);
	}
	
	
	@Override
	public void visit(OWLHasKeyAxiom axiom) {
		axiom.getClassExpression().accept(this);
		writeSpace();
		write(HAS_KEY);
		writeObjectProperties(axiom.getObjectPropertyExpressions());
		writeDataProperties(axiom.getDataPropertyExpressions());
		writeAxiomEnd(axiom);
	}	
	
	
	private void writeIndividuals(Collection<OWLIndividual> individuals) {
		writeOpenBrace();
		for (Iterator<OWLIndividual> it = individuals.iterator(); it.hasNext();) {
            it.next().accept(this);
            if (it.hasNext()) {
            	writeComma();
                writeSpace();
            }
        }
		writeCloseBrace();
	}
	
	private void writeIndividualBinaryOp(Collection<OWLIndividual> individuals, LatexStyleVocabulary v) {
		if (individuals.size() == 2){
			Iterator<OWLIndividual> it = individuals.iterator();
			OWLIndividual indA = it.next();
			OWLIndividual indB = it.next();
			OWLIndividual left, right;
            if (indA.equals(focusedObject)) {
            	left = indA;
            	right = indB;
            } else {
            	left = indB;
            	right = indA;
            }
            left.accept(this);
            writeSpace();
            write(v);
            writeSpace();
            right.accept(this);
		}
	}	
	
	@Override
	public void visit(OWLSameIndividualAxiom axiom) {
		Set<OWLIndividual> individuals = axiom.getIndividuals();
		if (individuals.size() > 2) {
			write(SEQ_SAME_INDIVIDUAL);
			writeIndividuals(individuals);
        } else if (individuals.size() == 2) {
        	writeIndividualBinaryOp(individuals, SAME_INDIVIDUAL);
        }
		writeAxiomEnd(axiom);
	}
	
	@Override
	public void visit(OWLDifferentIndividualsAxiom axiom) {
		Set<OWLIndividual> individuals = axiom.getIndividuals();
		if (individuals.size() > 2) {
			write(SEQ_DIFFERENT_INDIVIDUALS);
			writeIndividuals(individuals);
        } else if (individuals.size() == 2) {
        	writeIndividualBinaryOp(individuals, DIFFERENT_INDIVIDUALS);
        }
		writeAxiomEnd(axiom);
	}	
	
	@Override
	public void visit(OWLClassAssertionAxiom axiom) {
        axiom.getClassExpression().accept(this);
        writeOpenBracket();
        axiom.getIndividual().accept(this);
        writeCloseBracket();
        writeAxiomEnd(axiom);
	}
	
	@Override
	public void visit(OWLObjectPropertyAssertionAxiom axiom) {
        axiom.getProperty().accept(this);
        writeOpenBracket();
        axiom.getSubject().accept(this);
        writeComma();
        writeSpace();       
        axiom.getObject().accept(this);
        writeCloseBracket();
        writeAxiomEnd(axiom);
	}
	
	@Override
	public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
		write("!");
        axiom.getProperty().accept(this);
        writeOpenBracket();
        axiom.getSubject().accept(this);
        writeComma();
        writeSpace();        
        axiom.getObject().accept(this);
        writeCloseBracket();
        writeAxiomEnd(axiom);
	}
	
	@Override
	public void visit(OWLDataPropertyAssertionAxiom axiom) {
        axiom.getProperty().accept(this);
        writeOpenBracket();
        axiom.getSubject().accept(this);
        writeComma();
        writeSpace();
        axiom.getObject().accept(this);
        writeCloseBracket();
		writeAxiomEnd(axiom);
	}
	
	@Override
	public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
		write("!");
		axiom.getProperty().accept(this);
        writeOpenBracket();
        axiom.getSubject().accept(this);
        writeComma();
        writeSpace();
        axiom.getObject().accept(this);
        writeCloseBracket();
		writeAxiomEnd(axiom);
	}
	
	@Override
	public void visit(OWLAnnotationAssertionAxiom axiom) {
		axiom.getSubject().accept(this);
		writeSpace();
		write(ANNOTATION_ASSERTION);
		writeOpenBrace();
		axiom.getProperty().accept(this);
		writeComma();
		writeSpace();
		axiom.getValue().accept(this);
		writeCloseBrace();
		writeAxiomEnd(axiom);
	}
	
	@Override
	public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {
		axiom.getSubProperty().accept(this);
		writeSpace();
		write(SUB_ANNOTATION_PROPERTY_OF);
		writeSpace();
        axiom.getSuperProperty().accept(this);
        writeAxiomEnd(axiom);
	}
	
	@Override
	public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
		axiom.getProperty().accept(this);
        writeSpace();
        write(ANNOTATION_PROPERTY_DOMAIN);
        writeSpace();
        axiom.getDomain().accept(this);
        writeAxiomEnd(axiom);
	}
	
	@Override
	public void visit(OWLAnnotationPropertyRangeAxiom axiom) {
		axiom.getProperty().accept(this);
        writeSpace();
        write(ANNOTATION_PROPERTY_RANGE);
        writeSpace();
        axiom.getRange().accept(this);
        writeAxiomEnd(axiom);
	}
	

	@Override
	public void visit(SWRLRule rule) {
		throw new OWLRuntimeException("NOT IMPLEMENTED!");
	}

	@Override
	public void visit(SWRLClassAtom node) {
		throw new OWLRuntimeException("NOT IMPLEMENTED!");
	}

	@Override
	public void visit(SWRLDataRangeAtom node) {
		throw new OWLRuntimeException("NOT IMPLEMENTED!");
	}

	@Override
	public void visit(SWRLObjectPropertyAtom node) {
		throw new OWLRuntimeException("NOT IMPLEMENTED!");
	}

	@Override
	public void visit(SWRLDataPropertyAtom node) {
		throw new OWLRuntimeException("NOT IMPLEMENTED!");
	}

	@Override
	public void visit(SWRLBuiltInAtom node) {
		throw new OWLRuntimeException("NOT IMPLEMENTED!");
	}

	@Override
	public void visit(SWRLVariable node) {
		throw new OWLRuntimeException("NOT IMPLEMENTED!");
	}

	@Override
	public void visit(SWRLIndividualArgument node) {
		throw new OWLRuntimeException("NOT IMPLEMENTED!");
	}

	@Override
	public void visit(SWRLLiteralArgument node) {
		throw new OWLRuntimeException("NOT IMPLEMENTED!");
	}

	@Override
	public void visit(SWRLSameIndividualAtom node) {
		throw new OWLRuntimeException("NOT IMPLEMENTED!");
	}

	@Override
	public void visit(SWRLDifferentIndividualsAtom node) {
		throw new OWLRuntimeException("NOT IMPLEMENTED!");
	}
}
