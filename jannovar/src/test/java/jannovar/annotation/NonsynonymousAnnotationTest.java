package jannovar.annotation;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.HashMap;

/* serialization */
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import jannovar.exception.JannovarException;
import jannovar.io.SerializationManager;
import jannovar.io.UCSCKGParser;
import jannovar.common.Constants;
import jannovar.common.VariantType;
import jannovar.reference.TranscriptModel;
import jannovar.reference.Chromosome;
import jannovar.annotation.Annotation;
import jannovar.annotation.AnnotationList;
import jannovar.exome.Variant;
import jannovar.exception.AnnotationException;


import org.junit.Test;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Assert;


/**
 * This class is intended to perform unuit testing on variants that
 * are intergenic. 
 */
public class NonsynonymousAnnotationTest implements Constants {

    
   
    private static HashMap<Byte,Chromosome> chromosomeMap = null;

  
    @BeforeClass 
	public static void setUp() throws IOException, JannovarException  {
	ArrayList<TranscriptModel> kgList=null;
	java.net.URL url = NonsynonymousAnnotationTest.class.getResource(UCSCserializationTestFileName);
	String path = url.getPath();
	SerializationManager manager = new SerializationManager();
	kgList = manager.deserializeKnownGeneList(path);
	chromosomeMap = Chromosome.constructChromosomeMapWithIntervalTree(kgList);   
    }

    @AfterClass public static void releaseResources() { 
	chromosomeMap = null;
	System.gc();
    }

/**
 *<P>
 * annovar: RNF207:uc001amg.3:exon17:c.1718A>G:p.N573S,
 * chr1:6278414A>G
 *</P>
 */
@Test public void testNonSyn2ahand() throws AnnotationException, JannovarException  {
	byte chr = X_CHROMOSOME;
	int pos = 154009588;
	String ref = "T";
	String alt = "A";
	Chromosome c = chromosomeMap.get(chr); 
	if (c==null) {
	    Assert.fail("Could not identify chromosome \"" + chr + "\"");
	} else {
	    AnnotationList ann =c.getAnnotationList(pos,ref,alt); 
	    VariantType varType = ann.getVariantType();
	    Assert.assertEquals(VariantType.MISSENSE,varType);
	    String annot = ann.getVariantAnnotation();
	    Assert.assertEquals("MPP1(uc011mzv.2:exon12:c.1060A>T:p.T354S,uc010nvg.2:exon11:c.1090A>T:p.T364S,uc011mzw.2:exon11:c.1099A>T:p.T367S,uc004fmp.2:exon11:c.1150A>T:p.T384S)",annot);
	}
}
/**
 *<P>
 * annovar: LOC654433
 * chr2:114017029A>G
 expected:<...xon4:c.166A>G:p.T56A[])> but was:
 <...xon4:c.166A>G:p.T56A[,LOC654433,LOC654433)PAX8(PAX8])>

 *</P>
 */
@Test public void testNcRnaExonicVar76() throws AnnotationException, JannovarException  {
	byte chr = 2;
	int pos = 114017029;
	String ref = "A";
	String alt = "G";
	Chromosome c = chromosomeMap.get(chr); 
	if (c==null) {
	    Assert.fail("Could not identify chromosome \"" + chr + "\"");
	} else {
	    AnnotationList ann = c.getAnnotationList(pos,ref,alt);
	    VariantType varType = ann.getVariantType();
	    Assert.assertEquals(VariantType.MISSENSE,varType);
	    String annot = ann.getVariantAnnotation();
	    Assert.assertEquals("LOC654433(uc010fks.4:exon3:c.166A>G:p.T56A,uc002tjq.5:exon4:c.166A>G:p.T56A)",annot);
	}
}


/**
 *<P>
 * annovar: SND1-IT1
 * chr7:127637816A>G
 expected:<SND1[]-IT1(uc003vmj.2:exon...> but was:
          <SND1[(SND1)SND1]-IT1(uc003vmj.2:exon...>

 *</P>
 */
@Test public void testNcRnaExonicVar215() throws AnnotationException, JannovarException  {
	byte chr = 7;
	int pos = 127637816;
	String ref = "A";
	String alt = "G";
	Chromosome c = chromosomeMap.get(chr); 
	if (c==null) {
	    Assert.fail("Could not identify chromosome \"" + chr + "\"");
	} else {
	    AnnotationList ann = c.getAnnotationList(pos,ref,alt);
	    VariantType varType = ann.getVariantType();
	    Assert.assertEquals(VariantType.MISSENSE,varType);
	    String annot = ann.getVariantAnnotation();
	    Assert.assertEquals("SND1-IT1(uc003vmj.2:exon1:c.70A>G:p.T24A)",annot);
	}
}


/**
 *<P>
 * annovar: RNF207:uc001amg.3:exon17:c.1718A>G:p.N573S,
 * chr1:6278414A>G
 *</P>
 */
@Test public void testNonsynVar1hand() throws AnnotationException, JannovarException  {
	byte chr = 1;
	int pos = 6278414;
	String ref = "A";
	String alt = "G";
	Chromosome c = chromosomeMap.get(chr); 
	if (c==null) {
	    Assert.fail("Could not identify chromosome \"" + chr + "\"");
	} else {
	    AnnotationList ann =c.getAnnotationList(pos,ref,alt); 
	    VariantType varType = ann.getVariantType();
	    Assert.assertEquals(VariantType.MISSENSE,varType);
	    String annot = ann.getVariantAnnotation();
	    Assert.assertEquals("RNF207(uc001amg.3:exon17:c.1718A>G:p.N573S)",annot);
	}
}

/**
 *<P>
 * annovar: PRAMEF11:uc001auk.2:exon3:c.308A>G:p.E103G,
 * chr1:12887549T>C
 *</P>
 */
@Test public void testNonsynVar2() throws AnnotationException, JannovarException  {
	byte chr = 1;
	int pos = 12887549;
	String ref = "T";
	String alt = "C";
	Chromosome c = chromosomeMap.get(chr); 
	if (c==null) {
	    Assert.fail("Could not identify chromosome \"" + chr + "\"");
	} else {
	    AnnotationList ann =c.getAnnotationList(pos,ref,alt); 
	    VariantType varType = ann.getVariantType();
	    Assert.assertEquals(VariantType.MISSENSE,varType);
	    String annot = ann.getVariantAnnotation();
	    Assert.assertEquals("PRAMEF11(uc001auk.2:exon3:c.308A>G:p.E103G)",annot);
	}
}



/**
 *<P>
 * annovar: LOC440563:uc010obg.2:exon2:c.434A>G:p.H145R,
 * chr1:13183439T>C
 *</P>
 */
@Test public void testNonsynVar3() throws AnnotationException, JannovarException  {
	byte chr = 1;
	int pos = 13183439;
	String ref = "T";
	String alt = "C";
	Chromosome c = chromosomeMap.get(chr); 
	if (c==null) {
	    Assert.fail("Could not identify chromosome \"" + chr + "\"");
	} else {
	    AnnotationList ann =c.getAnnotationList(pos,ref,alt); 
	    VariantType varType = ann.getVariantType();
	    Assert.assertEquals(VariantType.MISSENSE,varType);
	    String annot = ann.getVariantAnnotation();
	    Assert.assertEquals("LOC440563(uc010obg.2:exon2:c.434A>G:p.H145R)",annot);
	}
}



/**
 *<P>
 * annovar: FHAD1:uc001awb.2:exon21:c.2756A>G:p.E919G,FHAD1:uc001awf.3:exon2:c.197A>G:p.E66G,FHAD1:uc010obl.1:exon5:c.515A>G:p.E172G,FHAD1:uc001awd.1:exon6:c.515A>G:p.E172G,FHAD1:uc001awe.1:exon4:c.320A>G:p.E107G,
 * chr1:15687059A>G
 *</P>
 */
@Test public void testNonsynVar4() throws AnnotationException, JannovarException  {
	byte chr = 1;
	int pos = 15687059;
	String ref = "A";
	String alt = "G";
	Chromosome c = chromosomeMap.get(chr); 
	if (c==null) {
	    Assert.fail("Could not identify chromosome \"" + chr + "\"");
	} else {
	    AnnotationList ann =c.getAnnotationList(pos,ref,alt); 
	    VariantType varType = ann.getVariantType();
	    Assert.assertEquals(VariantType.MISSENSE,varType);
	    String annot = ann.getVariantAnnotation();
	    Assert.assertEquals("FHAD1(uc001awf.3:exon2:c.197A>G:p.E66G,uc001awe.1:exon4:c.320A>G:p.E107G,uc010obl.1:exon5:c.515A>G:p.E172G,uc001awd.1:exon6:c.515A>G:p.E172G,uc001awb.2:exon21:c.2756A>G:p.E919G)",annot);
	}
}


/**
 *<P>
 * annovar: CASP9:uc001awm.2:exon5:c.662A>G:p.Q221R,CASP9:uc009voi.3:exon5:c.194A>G:p.Q65R,CASP9:uc010obm.2:exon5:c.413A>G:p.Q138R,CASP9:uc001awp.3:exon5:c.194A>G:p.Q65R,CASP9:uc001awn.3:exon5:c.662A>G:p.Q221R,
 * chr1:15832543T>C
 *</P>
 * expected:<CASP9(uc001awp.[3:exon5:c.194A>G:p.Q65R,uc009voi.3:exon5:c.194A>G:p.Q65R,uc010obm.2:exon5:c.413A>G:p.Q138R,uc001awn.3]:exon5:c.662A>G:p.Q2...> but was:
 * <CASP9(uc001awp.[4:exon5:c.194A>G:p.Q65R,uc009voi.4:exon5:c.194A>G:p.Q65R,uc010obm.3:exon5:c.413A>G:p.Q138R,uc001awn.4]:exon5:c.662A>G:p.Q2...>
 *
 */
@Test public void testNonsynVar5() throws AnnotationException, JannovarException  {
	byte chr = 1;
	int pos = 15832543;
	String ref = "T";
	String alt = "C";
	Chromosome c = chromosomeMap.get(chr); 
	if (c==null) {
	    Assert.fail("Could not identify chromosome \"" + chr + "\"");
	} else {
	    AnnotationList ann =c.getAnnotationList(pos,ref,alt); 
	    VariantType varType = ann.getVariantType();
	    Assert.assertEquals(VariantType.MISSENSE,varType);
	    String annot = ann.getVariantAnnotation();
	    Assert.assertEquals("CASP9(uc001awp.4:exon5:c.194A>G:p.Q65R,uc009voi.4:exon5:c.194A>G:p.Q65R,uc010obm.3:exon5:c.413A>G:p.Q138R,uc001awn.4:exon5:c.662A>G:p.Q221R,uc001awm.2:exon5:c.662A>G:p.Q221R)",annot);
	}
}





/**
 *<P>
 * annovar: AKR7L:uc021ohn.1:exon6:c.253G>A:p.A85T,AKR7L:uc021oho.1:exon4:c.229G>A:p.A77T,
 * chr1:19595137C>T
 *</P>
 */
@Test public void testNonsynVar6() throws AnnotationException, JannovarException  {
	byte chr = 1;
	int pos = 19595137;
	String ref = "C";
	String alt = "T";
	Chromosome c = chromosomeMap.get(chr); 
	if (c==null) {
	    Assert.fail("Could not identify chromosome \"" + chr + "\"");
	} else {
	    AnnotationList ann =c.getAnnotationList(pos,ref,alt); 
	    VariantType varType = ann.getVariantType();
	    Assert.assertEquals(VariantType.MISSENSE,varType);
	    String annot = ann.getVariantAnnotation();
	    Assert.assertEquals("AKR7L(uc021oho.1:exon4:c.229G>A:p.A77T,uc021ohn.1:exon6:c.253G>A:p.A85T)",annot);
	}
}




/**
 *<P>
 * annovar: AKR7L:uc021ohn.1:exon4:c.47G>A:p.C16Y,
 * chr1:19596124C>T
 *</P>
 */
@Test public void testNonsynVar7() throws AnnotationException, JannovarException  {
	byte chr = 1;
	int pos = 19596124;
	String ref = "C";
	String alt = "T";
	Chromosome c = chromosomeMap.get(chr); 
	if (c==null) {
	    Assert.fail("Could not identify chromosome \"" + chr + "\"");
	} else {
	    AnnotationList ann =c.getAnnotationList(pos,ref,alt); 
	    VariantType varType = ann.getVariantType();
	    Assert.assertEquals(VariantType.MISSENSE,varType);
	    String annot = ann.getVariantAnnotation();
	    Assert.assertEquals("AKR7L(uc021ohn.1:exon4:c.47G>A:p.C16Y)",annot);
	}
}



/**
 *<P>
 * annovar: ECE1:uc001bem.2:exon9:c.974C>T:p.T325I,ECE1:uc001bek.2:exon9:c.1022C>T:p.T341I,ECE1:uc001bej.2:exon7:c.986C>T:p.T329I,ECE1:uc009vqa.1:exon9:c.1022C>T:p.T341I,ECE1:uc010odl.1:exon9:c.1022C>T:p.T341I,ECE1:uc001bei.2:exon8:c.1013C>T:p.T338I,
 * chr1:21573855G>A
 *
expected:<....1013C>T:p.T338I,uc0[09vqa.1:exon9:c.1022C>T:p.T341I,uc010odl.1:exon9:c.1022C>T:p.T341I,uc001bek.2]:exon9:c.1022C>T:p.T...> 
but was:<....1013C>T:p.T338I,uc0[10odl.1:exon9:c.1022C>T:p.T341I,uc001bek.2:exon9:c.1022C>T:p.T341I,uc009vqa.1]:exon9:c.1022C>T:p.T...>

 *</P>
 */
@Test public void testNonsynVar8() throws AnnotationException, JannovarException  {
	byte chr = 1;
	int pos = 21573855;
	String ref = "G";
	String alt = "A";
	Chromosome c = chromosomeMap.get(chr); 
	if (c==null) {
	    Assert.fail("Could not identify chromosome \"" + chr + "\"");
	} else {
	    AnnotationList ann =c.getAnnotationList(pos,ref,alt); 
	    VariantType varType = ann.getVariantType();
	    Assert.assertEquals(VariantType.MISSENSE,varType);
	    String annot = ann.getVariantAnnotation();
	    Assert.assertEquals("ECE1(uc001bem.2:exon9:c.974C>T:p.T325I,uc001bej.2:exon7:c.986C>T:p.T329I,uc001bei.2:exon8:c.1013C>T:p.T338I,uc010odl.1:exon9:c.1022C>T:p.T341I,uc001bek.2:exon9:c.1022C>T:p.T341I,uc009vqa.1:exon9:c.1022C>T:p.T341I)",annot);
	}
}



/**
 *<P>
 * annovar: USP48:uc001bfa.3:exon1:c.7G>T:p.V3L,
 * chr1:22050649C>A
 *</P>
 */
@Test public void testNonsynVar9() throws AnnotationException, JannovarException  {
	byte chr = 1;
	int pos = 22050649;
	String ref = "C";
	String alt = "A";
	Chromosome c = chromosomeMap.get(chr); 
	if (c==null) {
	    Assert.fail("Could not identify chromosome \"" + chr + "\"");
	} else {
	    AnnotationList ann =c.getAnnotationList(pos,ref,alt); 
	    VariantType varType = ann.getVariantType();
	    Assert.assertEquals(VariantType.MISSENSE,varType);
	    String annot = ann.getVariantAnnotation();
	    Assert.assertEquals("USP48(uc001bfa.3:exon1:c.7G>T:p.V3L)",annot);
	}
}


/**
 *<P>
 * annovar: ZBTB40:uc001bfu.2:exon14:c.2989G>A:p.V997M,ZBTB40:uc009vqi.1:exon12:c.2653G>A:p.V885M,ZBTB40:uc001bft.2:exon15:c.2989G>A:p.V997M,
 * chr1:22846709G>A
 *</P>
 */
@Test public void testNonsynVar10() throws AnnotationException, JannovarException  {
	byte chr = 1;
	int pos = 22846709;
	String ref = "G";
	String alt = "A";
	Chromosome c = chromosomeMap.get(chr); 
	if (c==null) {
	    Assert.fail("Could not identify chromosome \"" + chr + "\"");
	} else {
	    AnnotationList ann =c.getAnnotationList(pos,ref,alt); 
	    VariantType varType = ann.getVariantType();
	    Assert.assertEquals(VariantType.MISSENSE,varType);
	    String annot = ann.getVariantAnnotation();
	    Assert.assertEquals("ZBTB40(uc009vqi.1:exon12:c.2653G>A:p.V885M,uc001bfu.2:exon14:c.2989G>A:p.V997M,uc001bft.2:exon15:c.2989G>A:p.V997M)",annot);
	}
}

/**
 *<P>
 * annovar: FUCA1:uc001bie.3:exon5:c.857A>G:p.Q286R,
 * chr1:24180962T>C
 *</P>
 */
@Test public void testNonsynVar12() throws AnnotationException, JannovarException  {
	byte chr = 1;
	int pos = 24180962;
	String ref = "T";
	String alt = "C";
	Chromosome c = chromosomeMap.get(chr); 
	if (c==null) {
	    Assert.fail("Could not identify chromosome \"" + chr + "\"");
	} else {
	    AnnotationList ann =c.getAnnotationList(pos,ref,alt); 
	    VariantType varType = ann.getVariantType();
	    Assert.assertEquals(VariantType.MISSENSE,varType);
	    String annot = ann.getVariantAnnotation();
	    Assert.assertEquals("FUCA1(uc001bie.3:exon5:c.857A>G:p.Q286R)",annot);
	}
}


/**
 *<P>
 * annovar: CATSPER4:uc010oez.2:exon2:c.230A>G:p.Q77R,
 * chr1:26517794A>G
 *</P>
 */
@Test public void testNonsynVar13() throws AnnotationException, JannovarException  {
	byte chr = 1;
	int pos = 26517794;
	String ref = "A";
	String alt = "G";
	Chromosome c = chromosomeMap.get(chr); 
	if (c==null) {
	    Assert.fail("Could not identify chromosome \"" + chr + "\"");
	} else {
	    AnnotationList ann =c.getAnnotationList(pos,ref,alt); 
	    VariantType varType = ann.getVariantType();
	    Assert.assertEquals(VariantType.MISSENSE,varType);
	    String annot = ann.getVariantAnnotation();
	    Assert.assertEquals("CATSPER4(uc010oez.2:exon2:c.230A>G:p.Q77R)",annot);
	}
}


/**
 *<P>
 * annovar: ADC:uc001bwx.1:exon1:c.17A>G:p.H6R,
 * chr1:33549535A>G
 *</P>
 */
@Test public void testNonsynVar14() throws AnnotationException, JannovarException  {
	byte chr = 1;
	int pos = 33549535;
	String ref = "A";
	String alt = "G";
	Chromosome c = chromosomeMap.get(chr); 
	if (c==null) {
	    Assert.fail("Could not identify chromosome \"" + chr + "\"");
	} else {
	    AnnotationList ann =c.getAnnotationList(pos,ref,alt); 
	    VariantType varType = ann.getVariantType();
	    Assert.assertEquals(VariantType.MISSENSE,varType);
	    String annot = ann.getVariantAnnotation();
	    Assert.assertEquals("ADC(uc001bwx.1:exon1:c.17A>G:p.H6R)",annot);
	}
}


/**
 *<P>
 * annovar: NBAS:uc002rcc.1:exon9:c.727A>G:p.I243V,
 * chr2:15674686T>C
 *</P>
 */
@Test public void testNonsynVar59() throws AnnotationException, JannovarException  {
	byte chr = 2;
	int pos = 15674686;
	String ref = "T";
	String alt = "C";
	Chromosome c = chromosomeMap.get(chr); 
	if (c==null) {
	    Assert.fail("Could not identify chromosome \"" + chr + "\"");
	} else {
	    AnnotationList ann =c.getAnnotationList(pos,ref,alt); 
	    VariantType varType = ann.getVariantType();
	    Assert.assertEquals(VariantType.MISSENSE,varType);
	    String annot = ann.getVariantAnnotation();
	    Assert.assertEquals("NBAS(uc002rcc.2:exon9:c.727A>G:p.I243V)",annot);
	}
}




/**
 *<P>
 * annovar: LOC375190:uc002rew.3:exon10:c.542G>A:p.G181D,
 * chr2:24390517G>A
 * Note that LOC375190 was changed to FAM228B (GeneID:375190)
 *</P>
 */
@Test public void testNonsynVar60() throws AnnotationException, JannovarException  {
	byte chr = 2;
	int pos = 24390517;
	String ref = "G";
	String alt = "A";
	Chromosome c = chromosomeMap.get(chr); 
	if (c==null) {
	    Assert.fail("Could not identify chromosome \"" + chr + "\"");
	} else {
	    AnnotationList ann =c.getAnnotationList(pos,ref,alt); 
	    VariantType varType = ann.getVariantType();
	    Assert.assertEquals(VariantType.MISSENSE,varType);
	    String annot = ann.getVariantAnnotation();
	    Assert.assertEquals("FAM228B(uc002rew.3:exon10:c.542G>A:p.G181D)",annot);
	}
}

/**
 *<P>
 * annovar: EMILIN1:uc002rii.4:exon3:c.446A>G:p.Q149R,EMILIN1:uc010eyq.2:exon3:c.446A>G:p.Q149R,
 * chr2:27303755A>G
Jannovar reveals the following:
Total annotiation: 2
[Nonsynonymous] "EMILIN1" -> uc010eyq.2:exon3:c.446A>G:p.Q149R
[Synonymous] "EMILIN1" -> uc002rii.4:exon3:c.446G>G [WARNING: mRNA/genome discrepancy: "A"/"G" strand=+]:p.R149R
Therefore, the "nonsynonymous" variant "wins", togehther with an error message. This is the desired behaviour

 *</P>
 */
@Test public void testNonsynVar61() throws AnnotationException, JannovarException  {
	byte chr = 2;
	int pos = 27303755;
	String ref = "A";
	String alt = "G";
	Chromosome c = chromosomeMap.get(chr); 
	if (c==null) {
	    Assert.fail("Could not identify chromosome \"" + chr + "\"");
	} else {
	    AnnotationList ann =c.getAnnotationList(pos,ref,alt); 
	    VariantType varType = ann.getVariantType();
	    Assert.assertEquals(VariantType.MISSENSE,varType);
	    String annot = ann.getVariantAnnotation();
	    Assert.assertEquals("EMILIN1(uc010eyq.2:exon3:c.446A>G:p.Q149R)",annot);
	}
}



}
/* eof */