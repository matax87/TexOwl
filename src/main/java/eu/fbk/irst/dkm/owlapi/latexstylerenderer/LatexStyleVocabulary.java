package eu.fbk.irst.dkm.owlapi.latexstylerenderer;

/**
 * Author: Matteo Matassoni<br>
 * FBK-IRST<br>
 * Data & Knowledge Management Unit<br>
 * Date: 05-Dic-2013<br><br>
 */
public enum LatexStyleVocabulary {
	
	PREFIX("\\ns"),
	
	START_ONTOLOGY("\\begin{ontology}"),
	
	END_ONTOLOGY("\\end{ontology}"),
	
	IMPORT("\\import"),

	CLASS("\\c"),

    DATA_PROPERTY("\\d"),

    OBJECT_PROPERTY("\\o"),

    NAMED_INDIVIDUAL("\\i"),

    ANNOTATION_PROPERTY("\\a"),

    DATATYPE("\\dt"),

    ANNOTATION("\\a"),

    OBJECT_INVERSE_OF("\\oinvof"),

    DATA_COMPLEMENT_OF("\\drnot"),

    DATA_ONE_OF("\\droneof"),

    DATATYPE_RESTRICTION("\\drres"),

    DATA_UNION_OF("\\dror"),
    
    SEQ_DATA_UNION_OF("\\drorof"),

    DATA_INTERSECTION_OF("\\drand"),
    
    SEQ_DATA_INTERSECTION_OF("\\drandof"),

    OBJECT_INTERSECTION_OF("\\cand"),
    
    SEQ_OBJECT_INTERSECTION_OF("\\candof"),

    OBJECT_UNION_OF("\\cor"),
    
    SEQ_OBJECT_UNION_OF("\\corof"),

    OBJECT_COMPLEMENT_OF("\\cnot"),

    OBJECT_ONE_OF("\\ooneof"),

    OBJECT_SOME_VALUES_FROM("\\oexists"),

    OBJECT_ALL_VALUES_FROM("\\oforall"),

    OBJECT_HAS_SELF("\\ohasself"),

    OBJECT_HAS_VALUE("\\ohasvalue"),

    OBJECT_MIN_CARDINALITY("\\o[>="),

    OBJECT_EXACT_CARDINALITY("\\o[="),

    OBJECT_MAX_CARDINALITY("\\o[<="),

    DATA_SOME_VALUES_FROM("\\dexists"),

    DATA_ALL_VALUES_FROM("\\dforall"),

    DATA_HAS_VALUE("\\dhasvalue"),

    DATA_MIN_CARDINALITY("\\d[>="),

    DATA_EXACT_CARDINALITY("\\d[="),

    DATA_MAX_CARDINALITY("\\d[<="),

    SUB_CLASS_OF("\\cisa"),

    EQUIVALENT_CLASSES("\\ceq"),
    
    SEQ_EQUIVALENT_CLASSES("\\calleq"),

    DISJOINT_CLASSES("\\cdisjoint"),
    
    SEQ_DISJOINT_CLASSES("\\calldisjoint"),

    DISJOINT_UNION("\\cdisjunion"),

    SUB_OBJECT_PROPERTY_OF("\\oisa"),

    OBJECT_PROPERTY_CHAIN("\\ochain"),

    EQUIVALENT_OBJECT_PROPERTIES("\\oeq"),
    
    SEQ_EQUIVALENT_OBJECT_PROPERTIES("\\oalleq"),

    DISJOINT_OBJECT_PROPERTIES("\\odisjoint"),

    SEQ_DISJOINT_OBJECT_PROPERTIES("\\oalldisjoint"),

    OBJECT_PROPERTY_DOMAIN("\\odomain"),

    OBJECT_PROPERTY_RANGE("\\orange"),

    INVERSE_OBJECT_PROPERTIES("\\oinv"),

    FUNCTIONAL_OBJECT_PROPERTY("\\ofunc"),

    INVERSE_FUNCTIONAL_OBJECT_PROPERTY("\\oinvfunc"),

    SYMMETRIC_OBJECT_PROPERTY("\\osym"),

    ASYMMETRIC_OBJECT_PROPERTY("\\oasym"),

    REFLEXIVE_OBJECT_PROPERTY("\\oreflex"),

    IRREFLEXIVE_OBJECT_PROPERTY("\\oirreflex"),

    TRANSITIVE_OBJECT_PROPERTY("\\otrans"),

    SUB_DATA_PROPERTY_OF("\\disa"),

    EQUIVALENT_DATA_PROPERTIES("\\deq"),
    
    SEQ_EQUIVALENT_DATA_PROPERTIES("\\dalleq"),

    DISJOINT_DATA_PROPERTIES("\\ddisjoint"),
    
    SEQ_DISJOINT_DATA_PROPERTIES("\\dalldisjoint"),

    DATA_PROPERTY_DOMAIN("\\ddomain"),

    DATA_PROPERTY_RANGE("\\drange"),

    FUNCTIONAL_DATA_PROPERTY("\\dfunc"),

    SAME_INDIVIDUAL("\\ieq"),
    
    SEQ_SAME_INDIVIDUAL("\\ialleq"),

    DIFFERENT_INDIVIDUALS("\\idiff"),
    
    SEQ_DIFFERENT_INDIVIDUALS("\\ialldiff"),

    HAS_KEY("\\key"),

    ANNOTATION_ASSERTION("\\a"),

    ANNOTATION_PROPERTY_DOMAIN("\\adomain"),

    ANNOTATION_PROPERTY_RANGE("\\arange"),

    SUB_ANNOTATION_PROPERTY_OF("\\aisa"),

    DATATYPE_DEFINITION("\\dtdef"),
    ;
	
	private final String shortName;
	
	
	
	
	LatexStyleVocabulary(String name) {
		shortName = name;
	}
	
	
	public String getShortName() {
        return shortName;
    }
}
