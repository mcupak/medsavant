package jannovar.common;


/**
 * This is a simple class of enumerated constants that describe the
 * type of filtering that was applied to a Gene/Variant. This class is
 * placed in the jannovar hierarchy for now because it is intertwined 
 * with Variant. TODO it should be put into the Exomizer hierarchy.
 * @author Peter Robinson
 * @version 0.01 (22 April, 2013)
 */
public enum FilterType {
    /**  VARIANT_QUALITY,Flag for output field representing the QUAL column of the VCF file. */
    
    /** Flag for output of field representing the Quality filter for the VCF entry */
    QUALITY_FILTER,
    /** Flag for filter type "interval" */
	INTERVAL_FILTER,
    /** Flag to output results of filtering against polyphen, SIFT, and mutation taster. */
	PATHOGENICITY_FILTER,
    /** Flag to output results of filtering against frequency with Thousand Genomes and ESP data. */
	FREQUENCY_FILTER,
    /** Flag to represent results of filtering against an inheritance pattern. */
	INHERITANCE_PATTERN_FILTER,
    /** Flag to represent results of filtering against MGI phenotype data (Phenodigm)*/
	PHENODIGM_FILTER,
    /** Flag to represent results of filtering against phenotype data (Uberpheno) */
	UBERPHENO_FILTER,
    /** Flag to represent results of filtering against ZFIN phenotype data (Phenodigm)*/
	ZFIN_PHENODIGM_FILTER,
    /** Flag to represent results of filtering against PPI-RandomWalk-proximity */
	GENEWANDERER_FILTER,
    /** Flag to represent results of annotating against OMIM data */
	OMIM_FILTER,
    /** Flag to represent exome-target filter */
	EXOME_TARGET_FILTER,
    /** Flag for dynamiic phenodigm filter. */
     DYNAMIC_PHENODIGM_FILTER;
   
}