package eu.fbk.irst.dkm.latex2owl;

import java.io.File;
import java.io.PrintStream;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLOntologyCreationIOException;
import org.semanticweb.owlapi.io.OWLParserFactoryRegistry;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

import eu.fbk.irst.dkm.owlapi.latexstyleparser.OWLLatexStyleSyntaxParserFactory;
import eu.fbk.irst.dkm.owlapi.latexstylerenderer.OWLLatexStyleSyntaxOntologyStorer;

/**
 * Author: Matteo Matassoni<br>
 * FBK-IRST<br>
 * Data & Knowledge Management Unit<br>
 * Date: 11-Nov-2013<br><br>
 */
public class Latex2OWL {
	
	static {
		// Register LatexStyle syntax parser
	    OWLParserFactoryRegistry registry = OWLParserFactoryRegistry.getInstance();
	    registry.registerParserFactory(new OWLLatexStyleSyntaxParserFactory());
	}	
	
	public static OWLOntologyManager create() {
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		
		// Register the LatexStyle syntax ontology storer with the manager
      	m.addOntologyStorer(new OWLLatexStyleSyntaxOntologyStorer());
		return m; 
	}

	public static void main(String[] args) {
		if (args.length != 2) {
            outputArgsError(System.out);
            System.exit(0);
        }
		String inputFilePath = args[0];
      	String outputFilePath = args[1];
      	
      	OWLOntologyManager m = create();
      	
      	OWLOntology o;
			try {
				o = m.loadOntologyFromOntologyDocument(new File(inputFilePath));
				OWLOntologyFormat inpuFormat = m.getOntologyFormat(o);
				PrefixOWLOntologyFormat outputFormat = new OWLXMLOntologyFormat();
				
				// copy prefixes from read format to the output one
				if (inpuFormat instanceof PrefixOWLOntologyFormat) {
					PrefixOWLOntologyFormat prefixFormat = (PrefixOWLOntologyFormat) inpuFormat;					
					for (String prefixName : prefixFormat.getPrefixNames()) {
						String prefix = prefixFormat.getPrefix(prefixName);
						outputFormat.setPrefix(prefixName, prefix);		                
		            }
				}
				
				// save the read ontology in the output format
	      		File output = new File(outputFilePath);
	      		IRI documentIRI2 = IRI.create(output); 
	      		m.saveOntology(o, outputFormat, documentIRI2);
			} catch (OWLOntologyCreationIOException e) {
				System.out.println("Could not load the ontology from input file: \"" + inputFilePath + "\" Cause: " + e.getMessage());
			} catch (OWLOntologyCreationException e) {
				System.out.println("Could not create and load the ontology from input file: \"" + inputFilePath + "\" Cause: " + e.getMessage());
			} catch (OWLOntologyStorageException e) {
				System.out.println("Could not save the ontology to target file: \"" + outputFilePath + "\" Cause: " + e.getMessage());
			}
	}
	
	private static void outputArgsError(PrintStream ps) {
        if (ps == null) return;
        ps.println("Latex2OWL");
        ps.println("--------------------------------------------------");
        ps.println("Version 2.0");
        ps.println("");
        ps.println("\tMatteo Matassoni");
    	ps.println("\tFBK-IRST, Data & Knowledge Management Unit");
    	ps.println("\tmmatassoni@fbk.eu");
    	ps.println("");
    	ps.println("Usage: latex2owl inputFilepath outputFilePath");
    	ps.println("");
    	ps.println("E.g., latex2owl examples/input.txt out/output.owl");
    }

}
