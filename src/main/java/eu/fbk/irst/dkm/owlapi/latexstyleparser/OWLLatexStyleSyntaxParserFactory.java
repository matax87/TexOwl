package eu.fbk.irst.dkm.owlapi.latexstyleparser;

import org.semanticweb.owlapi.io.OWLParser;
import org.semanticweb.owlapi.io.OWLParserFactory;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * Author: Matteo Matassoni<br>
 * FBK-IRST<br>
 * Data & Knowlegde Management Unit<br>
 * Date: 11-Nov-2013<br><br>
 */
public class OWLLatexStyleSyntaxParserFactory implements OWLParserFactory {
	@Override
    public OWLParser createParser(OWLOntologyManager owlOntologyManager) {
        return new OWLLatexStyleSyntaxOWLParser();
    }
}
