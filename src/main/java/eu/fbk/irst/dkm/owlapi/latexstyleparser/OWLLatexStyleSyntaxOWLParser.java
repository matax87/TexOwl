package eu.fbk.irst.dkm.owlapi.latexstyleparser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.semanticweb.owlapi.io.AbstractOWLParser;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.UnloadableImportException;

/**
 * Author: Matteo Matassoni<br>
 * FBK-IRST<br>
 * Data & Knowledge Management Unit<br>
 * Date: 11-Nov-2013<br><br>
 */
public class OWLLatexStyleSyntaxOWLParser extends AbstractOWLParser {

    @Override
    public OWLOntologyFormat parse(OWLOntologyDocumentSource documentSource, OWLOntology ontology) throws OWLParserException, IOException, UnloadableImportException {
        return parse(documentSource, ontology, new OWLOntologyLoaderConfiguration());
    }

    @Override
    public OWLOntologyFormat parse(OWLOntologyDocumentSource documentSource, OWLOntology ontology, OWLOntologyLoaderConfiguration configuration) throws OWLParserException, IOException, OWLOntologyChangeException, UnloadableImportException {
        Reader reader = null;
        InputStream is = null;
        try {
            OWLLatexStyleSyntaxParser parser;
            if(documentSource.isReaderAvailable()) {
                reader = documentSource.getReader();
                parser = new OWLLatexStyleSyntaxParser(reader);
            }
            else if(documentSource.isInputStreamAvailable()) {
                is = documentSource.getInputStream();
                parser = new OWLLatexStyleSyntaxParser(is);
            }
            else {
                is = getInputStream(documentSource.getDocumentIRI(), configuration);
                parser = new OWLLatexStyleSyntaxParser(is);
            }
            parser.setUp(ontology, configuration);
            return parser.parse();
        }
        catch (ParseException e) {
            throw new OWLParserException(e.getMessage(), e, e.currentToken.beginLine,
                    e.currentToken.beginColumn);
        } finally {
            if (is != null) {
                is.close();
            } else if (reader != null) {
                reader.close();
            }
        }
    }
}
