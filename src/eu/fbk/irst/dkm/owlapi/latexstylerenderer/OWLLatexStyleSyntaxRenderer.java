package eu.fbk.irst.dkm.owlapi.latexstylerenderer;

import java.io.IOException;
import java.io.Writer;

import org.semanticweb.owlapi.io.AbstractOWLRenderer;
import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.io.OWLRendererIOException;
import org.semanticweb.owlapi.model.OWLOntology;

public class OWLLatexStyleSyntaxRenderer extends AbstractOWLRenderer {

	@Override
	public void render(OWLOntology ontology, Writer writer) throws OWLRendererException {
		try {
			OWLLatexStyleSyntaxObjectRenderer ren = new OWLLatexStyleSyntaxObjectRenderer(ontology, writer);
			ontology.accept(ren);
			writer.flush();
		} catch (IOException e) {
			throw new OWLRendererIOException(e);
		}
	}
}
