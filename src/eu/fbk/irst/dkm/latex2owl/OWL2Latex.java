package eu.fbk.irst.dkm.latex2owl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.semanticweb.owlapi.io.OWLOntologyCreationIOException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import eu.fbk.irst.dkm.owlapi.apibinding.OWLEnhancedManager;
import eu.fbk.irst.dkm.owlapi.io.OWLLatexStyleSyntaxOntologyFormat;
import eu.fbk.irst.dkm.owlapi.latexstylerenderer.OWLLatexStyleSyntaxOntologyStorer;

/**
 * Author: Matteo Matassoni<br>
 * FBK-IRST<br>
 * Data & Knowledge Management Unit<br>
 * Date: 11-Nov-2013<br><br>
 */
public class OWL2Latex {

	public static OWLOntologyManager create() {
		OWLOntologyManager m = OWLEnhancedManager.createOWLOntologyManager();

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

		try {
			OWLOntologyManager manager = create();
			OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(inputFilePath));

			OWLOntologyFormat outputFormat = new OWLLatexStyleSyntaxOntologyFormat();
			OWLOntologyConverter.convert(ontology, outputFormat, new FileOutputStream(outputFilePath, false));
		} catch (OWLOntologyCreationIOException e) {
			System.out.println("Could not load the ontology from input file: \""  + inputFilePath + "\" Cause: " + e.getMessage());
		} catch (OWLOntologyCreationException e) {
			System.out.println("Could not create and load the ontology from input file: \"" + inputFilePath + "\" Cause: " + e.getMessage());
		} catch (OWLOntologyStorageException e) {
			System.out.println("Could not save the ontology to target file: \"" + outputFilePath + "\" Cause: " + e.getMessage());
		} catch (FileNotFoundException e) {
			System.out.println("Could not save the ontology to target file: \"" + outputFilePath + "\" Cause: " + e.getMessage());
		}
	}

	private static void outputArgsError(PrintStream ps) {
        if (ps == null) return;
        ps.println("OWL2Latex");
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
