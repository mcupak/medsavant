package jannovar.annotation;

import jannovar.common.Constants;
import jannovar.common.VariantType;
import jannovar.exception.AnnotationException;
import jannovar.exception.JannovarException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class collects all the information about a variant and its annotations and 
 * calculates the final annotations for a given variant. The 
 * {@link jannovar.reference.Chromosome Chromosome} objects each use an instance of
 * this class to assemble a list of {@link jannovar.annotation.Annotation Annotation} objects
 * for each {@link jannovar.exome.Variant Variant}. Each  {@link jannovar.exome.Variant Variant} should
 * receive at least one {@link jannovar.annotation.Annotation Annotation}, but variants that affect
 * multiple transcripts will have multiple annotations. 
 * <P>
 * This class creates one {@link jannovar.annotation.AnnotationList AnnotationList} object for each
 * variant (with one or more {@link jannovar.annotation.Annotation Annotation} objects), 
 * that can return both an ArrayList of all annotations, a list of all annotations of the highest
 * priority level for the variant, and a single representative Annotation.
 * <P>
 * The default preference for annotations is thus
 * <OL>
 * <LI><B>exonic</B>: variant overlaps a coding exon (does not include 5' or 3' UTR, and also does not include synonymous).
 * <LI><B>splicing</B>: variant is within 2-bp of a splicing junction (same precedence as exonic).
 * <LI><B>ncRNA</B>: variant overlaps a transcript without coding annotation in the gene definition 
 * <LI><B>UTR5</B>: variant overlaps a 5' untranslated region 
 * <LI><B>UTR3</B>: variant overlaps a 3' untranslated region 
 * <LI><B>synonymous</B> synonymous substitution
 * <LI><B>intronic</B>:	variant overlaps an intron 
 * <LI><B>upstream</B>: variant overlaps 1-kb region upstream of transcription start site
 * <LI><B>downstream</B>: variant overlaps 1-kb region downtream of transcription end site (use -neargene to change this)
 * <LI><B>intergenic</B>: variant is in intergenic region 
 * </OL>
 * Note that the class of <B>exonic</B> and <B>splicing</B> mutations as defined here comprises the class of "obvious candidates"
 * for pathogenic mutations, i.e., NS/SS/I, nonsynonymous, splice site, indel.
 * <P>
 * One object of this class is created for each variant we want to annotate. The {@link jannovar.reference.Chromosome Chromosome}
 * class goes through a list of genes in the vicinity of the variant and adds one {@link jannovar.annotation.Annotation Annotation}
 * object for each gene. These are essentially candidates for the actual correct annotation of the variant, but we can
 * only decide what the correct annotation is once we have seen enough candidates. Therefore, once we have gone
 * through the candidates, this class decides what the best annotation is and returns the corresponding 
 * {@link jannovar.annotation.Annotation Annotation} object (in some cases, this class may modify the 
 * {@link jannovar.annotation.Annotation Annotation} object before returning it).
 * <P>
 * For each class of Variant, there is a function that returns a single {@link jannovar.annotation.Annotation Annotation} object.
 * These functions are called summarizeABC(), where ABC is Intronic, Exonic, etc., representing the precedence classes.
 * @version 0.24 (3 August, 2013)
 * @author Peter N Robinson
 */

public class AnnotatedVariantFactory implements Constants {
   
   private static final Log LOG = LogFactory.getLog(AnnotatedVariantFactory.class);

    /** List of all {@link jannovar.annotation.Annotation Annotation} objects found for exonic variation. */
    private ArrayList<Annotation> annotationLst =null;


    /** Set of all gene symbols used for the current annotation (usually one, but if the size of this set
	is greater than one, then there qare annotations to multiple genes and we will need to use
	special treatment).*/
    private HashSet<String> geneSymbolSet=null;


    /** Flag to state that we have at least one exonic variant. */
    private boolean hasExonic;
    /** Flag to state we have at least one splicing variant  */
    private boolean hasSplicing;
    /** Flag to state that we have at least one noncoding RNA variant. */
    private boolean hasNcRna;
    /** Flag to state that we have at least one UTR5 variant. */
    private boolean hasUTR5;
    /** Flag to state that we have at least one UTR3 variant. */
    private boolean hasUTR3;
    /** Flag to state that we have at least one nonsynonymous exonic variant. */
    private boolean hasSynonymous;
    /** Flag to state that we have at least one intronic variant. */
    private boolean hasIntronic;
    /** Flag to state that we have at least one noncoding RNA intronic variant. */
    private boolean hasNcrnaIntronic;
    /** Flag to state that we have at least one upstream variant. */
    private boolean hasUpstream;
    /** Flag to state that we have at least one downstream variant. */
    private boolean hasDownstream;
    /** Flag to state that we have at least one intergenic variant. */
    private boolean hasIntergenic;
    /** Flag to state that we have at least one error annotation. */
    private boolean hasError;
    /**
     * True if we have at least one annotation for the classes ncRNA_EXONIC
     * SPLICING, UTR5, UTR3, EXONIC, INTRONIC
     */
    private boolean hasGenicMutation;

    /** The current number of annotations for the variant being annotated */
    private int annotationCount;

    
    /**
     * The constructor initializes an ArrayList of 
     * {@link jannovar.annotation.Annotation Annotation} objects as well as
     * a HashSet of Gene symbols (Strings).
     * @param initialCapacity The initial capacity of the arraylist and hashset.
     */
    public AnnotatedVariantFactory(int initialCapacity) {
	this.annotationLst = new ArrayList<Annotation>(initialCapacity);
	this.geneSymbolSet = new HashSet<String>();
    }

    /**
     * This function should be called before a new variant is annotation
     * in order to clear the lists used to store Annotations.
     */
    public void clearAnnotationLists() {
	this.annotationLst.clear();
	this.geneSymbolSet.clear();
	this.hasExonic=false;
	this.hasSplicing=false;
	this.hasNcRna=false;
	this.hasUTR5=false;
	this.hasUTR3=false;
	this.hasIntronic=false;
	this.hasSynonymous=false;
	this.hasNcrnaIntronic=false;
	this.hasUpstream=false;
	this.hasDownstream=false;
	this.hasIntergenic=false;
	this.hasError=false;
	this.hasGenicMutation=false;
	this.annotationCount=0;
	
    }

    /**
     * @return The number of {@link jannovar.annotation.Annotation Annotation} 
     * objects for the current variant.
     */
    public int getAnnotationCount() { return this.annotationCount; }

    /**
     * Note that this function is used by {@link jannovar.reference.Chromosome Chromosome}
     * during the construction of an {@link jannovar.annotation.AnnotationList AnnotationList}
     * for a given {@link jannovar.exome.Variant Variant}.
     * @return true if there are currently no annotations. 
     */
    public boolean isEmpty() { return this.annotationCount == 0; }

    /**
     * @return true if there is a nonsynonymous, splice site, or insertion/deletion variant
     */
    public boolean isNS_SS_I() { return hasExonic || hasSplicing; }

    /**
     * @return True if we have at least one annotation for the classes ncRNA_EXONIC
     * SPLICING, UTR5, UTR3, EXONIC, INTRONIC
     */
    public boolean hasGenic() { return this.hasGenicMutation; }

    /**
     * After the {@link jannovar.reference.Chromosome Chromosome} object
     * has added annotations for all of the transcripts that intersect with 
     * the current variant (or a DOWNSTREAM, UPSTREAM, or INTERGENIC annotation
     * if the variant does not intersect with any transcript), it calls
     * this function to return the list of annotations in form of an
     * {@link jannovar.annotation.AnnotationList AnnotationList} object.
     * <P>
     * The strategy is to return all variants that affect coding exons (and only these)
     * if such variants exist, as they are the best candidates. Otherwise, return 
     * all variants that affect other exonic sequences (UTRs, ncRNA). Otherwise,
     * return UPSTREAM and DOWNSTREAM annotations if they exist. Otherwise, return
     * an intergenic Annotation.
     * @return returns the {@link AnnotationList} with all associated {@link Annotation}s
     * @throws jannovar.exception.AnnotationException
     */
    public AnnotationList getAnnotationList() throws AnnotationException {
	Collections.sort(this.annotationLst);
	AnnotationList annL = new AnnotationList(this.annotationLst);
	if (this.geneSymbolSet.size() > 1) {
	    annL.setHasMultipleGeneSymbols();
	}
	annL.sortAnnotations();
	VariantType vt = getMostPathogenicVariantType();
	annL.setMostPathogenicVariantType(vt);
	return annL;
    }

    
     
    /**
     * This function goes through all of the Annotations that have been 
     * entered for the current variant and enters the type of 
     * variant that is deemed to be the most pathogenic. The function
     * follows the priority as set out by annovar.
     * <P>
     * The strategy of the function is to start out with the least
     * pathogenic type (INTERGENIC), and to workthrough all types
     * towards the most pathogenic. After this is finished, the variant
     * type with the most pathogenic annotation is returned.
     * <P>
     * There should always be at least one annotation type. If not
     * return ERROR (should never happen).
     * @return most pathogenic variant type for current variant.
     */
    private VariantType getMostPathogenicVariantType() {
	VariantType vt;
	Collections.sort(this.annotationLst);
	Annotation a = this.annotationLst.get(0);
	//debugPrint();
	vt = a.getVariantType();
	return vt;
    }

    
  


    /**
     * The {@link jannovar.reference.Chromosome Chromosome} class calls this
     * function to add a non-coding RNA exon variant.
     * From the program
     * logic, only one such Annotation should be added per variant.
     * @param ann A noncoding RNA exonic annotation object.
     */
     public void addNonCodingRNAExonicAnnotation(Annotation ann){
	this.annotationLst.add(ann);
	this.hasNcRna=true;
	this.annotationCount++;
     } 

    /**
     * The {@link jannovar.reference.Chromosome Chromosome} class calls this
     * function to add a 5' UTR  variant.
     * @param ann A 5' UTR annotation object.
     */
    public void addUTR5Annotation(Annotation ann){
	this.annotationLst.add(ann);
	this.hasUTR5=true;
	this.hasGenicMutation=true;
	this.annotationCount++;
    }

    /**
     * The {@link jannovar.reference.Chromosome Chromosome} class calls this
     * function to add a 3' UTR  variant.
     * @param ann A 3' UTR annotation object.
     */
    public void addUTR3Annotation(Annotation ann){
	this.annotationLst.add(ann);
	this.hasUTR3=true;
	this.hasGenicMutation=true;
	this.annotationCount++;
    }

    /** The {@link jannovar.reference.Chromosome Chromosome} class calls this
     * function  to register an Annotation for
     * a variant that is located between two genes. From the program
     * logic, only one such Annotation should be added per variant.
     * @param ann An Annotation with type INTERGENIC
     */
     public void addIntergenicAnnotation(Annotation ann){
	this.annotationLst.add(ann);
	this.hasIntergenic=true;
	this.annotationCount++;
    }


   

  

    /** The {@link jannovar.reference.Chromosome Chromosome} class calls this
     * function  to register an Annotation for
     * a variant that affects the coding sequence of an exon. Many different
     * variant types are summarized (NONSYNONYMOUS, DELETION etc.).
      * @param ann An Annotation to be added.
     */
    public void addExonicAnnotation(Annotation ann){
	this.annotationLst.add(ann);
	if (ann.getVariantType() == VariantType.SYNONYMOUS) {
	    this.hasSynonymous = true;
	} else if (ann.getVariantType() == VariantType.SPLICING) {
	    this.hasSplicing = true;
	} else {
	    this.hasExonic=true;
	}
	this.geneSymbolSet.add(ann.getGeneSymbol());
	this.hasGenicMutation=true;
	this.annotationCount++;
    }

    /**  The {@link jannovar.reference.Chromosome Chromosome} class calls this
     * function  to register an annotation for a noncoding RNA transcript that 
     * is affected by a splice mutation.
     *
     * @param ann {@link Annotation} to be registered
     */
    public void addNcRNASplicing(Annotation ann) {
	String s = String.format("%s",ann.getVariantAnnotation());
	this.hasNcRna=true;
	ann.setVariantAnnotation(s);
	this.annotationLst.add(ann);
    }

   
    /**  The {@link jannovar.reference.Chromosome Chromosome} class calls this
     * function to add an annotation for an intronic variant. Note that if the
     * same intronic annotation already exists, nothing is done, i.e.,
     * this method avoids duplicate annotations. 
     * @param ann the Intronic annotation to be added.    
     */
    public void addIntronicAnnotation(Annotation ann){
	this.geneSymbolSet.add(ann.getGeneSymbol());
	if (ann.getVariantType() == VariantType.INTRONIC || 
	    ann.getVariantType() == VariantType.ncRNA_INTRONIC) {
	    for (Annotation a: this.annotationLst) {
		if (a.equals(ann)) return; /* already have identical annotation */
	    }
	    this.annotationLst.add(ann);
	} 
	if (ann.getVariantType() == VariantType.INTRONIC) {
	    this.hasIntronic = true;
	} else if (ann.getVariantType() == VariantType.ncRNA_INTRONIC) {
	    this.hasNcrnaIntronic=true;
	} 
        this.hasGenicMutation=true;
	this.annotationCount++;
    }


    


    /**
     * An error annotation is created in a few cases where there
     * data seem to be inconsistent.
     * @param ann An Annotation object that contains a String representing the error.
     */
    public void addErrorAnnotation(Annotation ann){
       	this.annotationLst.add(ann);
	this.hasError=true;
	this.annotationCount++;
     }

   

    

    /**
     * Adds an annotation for an upstream or downstream variant. Note
     * that currently, we add only one such annotation for each gene, that is,
     * we do not add a separate annotation for each isoform of a gene. This
     * method avaoid such duplicate annotations. 
     * @param ann The annotation that is to be added to the list of annotations for the current sequence variant.
     */
    public void addUpDownstreamAnnotation(Annotation ann) throws JannovarException{
	for (Annotation a: annotationLst) {
	    if (a.equals(ann)) return;
	}
	this.annotationLst.add(ann);
	VariantType type = ann.getVariantType();
	if (type == VariantType.DOWNSTREAM) {
	    this.hasDownstream=true;
	} else if (type == VariantType.UPSTREAM) {
	    this.hasUpstream=true;
	} else {
            String msg = "Warning [AnnotatedVar.java]: Was expecting UPSTREAM or DOWNSTREAM" +
			       " type of variant but got " + type;
	    LOG.error(msg);
	    /* TODO -- Add Exception! */
	    throw new JannovarException(msg);
	}
	this.annotationCount++;
    }

    /**
     * Print out all annotations we have for debugging purposes (before summarization)
     */
    public void debugPrint() {
	LOG.info("[AnnotatedVariantFactory]:debugPrint");
	LOG.info("Total annotations: " + annotationCount);
	for (Annotation a : this.annotationLst) {
	    LOG.info("\t[" + a.getVariantTypeAsString() + "] \"" + a.getGeneSymbol() + "\" -> " + a.getVariantAnnotation());
	}
    }



    
}
