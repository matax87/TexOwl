package eu.fbk.irst.dkm.latex2owl;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

/**
 * Author: Matteo Matassoni<br>
 * FBK-IRST<br>
 * Data & Knowledge Management Unit<br>
 * Date: 23-Sep-2014<br><br>
 */
public class OWLOntologyConverter {

	public static void convert(OWLOntology ontology, OWLOntologyFormat outputFormat, java.io.OutputStream outputStream) throws OWLOntologyStorageException {
		OWLOntologyManager manager = ontology.getOWLOntologyManager();
		OWLOntologyFormat inputFormat = manager.getOntologyFormat(ontology);
		
		// copy prefixes from read format to the output one
		if (inputFormat.isPrefixOWLOntologyFormat() && outputFormat.isPrefixOWLOntologyFormat()) {
			PrefixOWLOntologyFormat prefixFormat = inputFormat.asPrefixOWLOntologyFormat();
			outputFormat.asPrefixOWLOntologyFormat().copyPrefixesFrom(prefixFormat);
		}
		
		// save ontology
		manager.saveOntology(ontology, outputFormat, outputStream);
	}
}
