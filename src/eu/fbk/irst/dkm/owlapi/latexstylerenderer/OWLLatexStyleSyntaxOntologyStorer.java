package eu.fbk.irst.dkm.owlapi.latexstylerenderer;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.AbstractOWLOntologyStorer;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

import eu.fbk.irst.dkm.owlapi.io.OWLLatexStyleSyntaxOntologyFormat;

public class OWLLatexStyleSyntaxOntologyStorer extends AbstractOWLOntologyStorer {
	private static final long serialVersionUID = 30402L;

	@Override
	public boolean canStoreOntology(OWLOntologyFormat ontologyFormat) {
		return ontologyFormat.equals(new OWLLatexStyleSyntaxOntologyFormat());
	}
	
	@Override
	protected void storeOntology(OWLOntologyManager manager, OWLOntology ontology,
			Writer writer, OWLOntologyFormat format) throws OWLOntologyStorageException {
		storeOntology(ontology, writer, format);
	}

	@Override
	protected void storeOntology(OWLOntology ontology, Writer writer,
			OWLOntologyFormat format) throws OWLOntologyStorageException {
		try {
			OWLLatexStyleSyntaxObjectRenderer ren = new OWLLatexStyleSyntaxObjectRenderer(ontology, writer);
			if (format instanceof PrefixOWLOntologyFormat) {
				PrefixOWLOntologyFormat prefixFormat = (PrefixOWLOntologyFormat) format;
				DefaultPrefixManager man = new DefaultPrefixManager();
				Map<String, String> map = prefixFormat.getPrefixName2PrefixMap();
				for (Map.Entry<String, String> e : map.entrySet()) {
                    man.setPrefix(e.getKey(), e.getValue());
                }				
				ren.setPrefixManager(man);
			}
            ontology.accept(ren);
            writer.flush();
        } catch (IOException e) {
            throw new OWLOntologyStorageException(e);
        }
	}
}
