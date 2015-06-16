package edu.buffalo.cse.blueseal.networkflow.summary;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import soot.SootMethod;
import soot.Unit;
import soot.jimple.FieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.ParameterRef;
import soot.jimple.StaticFieldRef;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JReturnStmt;
import soot.jimple.internal.JimpleLocal;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.NetworkFlowBackwardFlowAnalysis;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.UnitValue;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.UnitWrapper;
import edu.uci.ics.jung.graph.DelegateTree;

public class SootString {

	
	public static String URL_TYPE = "URL";
	public static String REFLECTION_TYPE = "REFELCTION";
	public static String INTENT_TYPE = "INTENT";
	
	static Logger logger = Logger.getLogger(SootString.class);

	private DelegateTree<UnitWrapper, String> tree;
	private SootMethod sootMethod;
	
	private String type;
	
	// Master list of classifications
	public static final String[] CLASSIFICATIONS = new String[]{"Obfuscation","Provenance","Semantic","Structural","Construction","Class or Static"};

	
	// If the string is statically defined or dynamically assembled
	public static final String STATIC_STRING_TYPE = "Static";
	public static final String DYNAMIC_STRING_TYPE = "Static";
	
	// If the string uses class or static fields
	public static final String CLASS_FIELD_TYPE = "Class Field";
	public static final String STATIC_FIELD_TYPE = "Static Field";
	
	// Obfuscated types
	public static HashMap<String, Set<String>> OBFUSCATION_MAP = new HashMap<String, Set<String>>();
	
	// Provenance types
	public static HashMap<String, Set<String>> PROVENANCE_MAP = new HashMap<String, Set<String>>();
	
	// Semantic types
	public static HashMap<String, Set<String>> SEMANTIC_MAP = new HashMap<String, Set<String>>();

	// Structural types
	public static HashMap<String, Set<String>> STRUCTURAL_MAP = new HashMap<String, Set<String>>();

	private ArrayList<String> obfuscationMatches = new ArrayList<String>();
	private ArrayList<String> provenanceMatches = new ArrayList<String>();
	private ArrayList<String> semanticMatches = new ArrayList<String>();
	private ArrayList<String> structuralMatches = new ArrayList<String>();
	private ArrayList<String> classFieldMatches = new ArrayList<String>();
	private ArrayList<String> staticFieldMatches = new ArrayList<String>();
	private ArrayList<String> stringFieldMatches = new ArrayList<String>();
	private ArrayList<String> methodSignatures = new ArrayList<String>();
	private boolean hasIncompleteParameter;

	
	public boolean isHasIncompleteParameter() {
		return hasIncompleteParameter;
	}

	private int nesting = 0;
	
	private boolean classificationPopulated = false;
	
	public static String getMatchesAsStringFromList(ArrayList<String> matchesList, boolean pipeDelimeted, boolean whiteSpaceToUnderscore){
		StringBuffer content = new StringBuffer("");
		int size = matchesList.size();
		for (int i = 0; i < size; i++) {
			String match = matchesList.get(i);
			match = match.replaceAll(",", "_");
			if(whiteSpaceToUnderscore){
				match = match.replaceAll(" ", "_");
			}
			content.append(match);
			if(i != size - 1){
				if(pipeDelimeted){
					content.append(" | ");					
				}
				else{
					content.append(" ");										
				}
			}			
		}
		return content.toString();
	}

	public static String getMatchesAsStringFromSet(ArrayList<String> matchesList){
		Set<String> matches = new HashSet<String>(matchesList);
		StringBuffer content = new StringBuffer("");
		for (String string : matches) {
			content.append(string).append(" | ");
		}
		return content.toString();
	}
	
	
	public boolean isUrlType(){
		return type.equals(URL_TYPE);
	}
	
	public boolean isReflectionType(){
		return type.equals(REFLECTION_TYPE);
	}
	
	//Feng: added for Intent analysis
	public boolean isIntentType(){
		return type.equals(INTENT_TYPE);
	}
	
	public SootString(DelegateTree<UnitWrapper, String> newTree, SootMethod sootMethod, String newType){
		try {
			setupClassifiers();
		} catch (IOException e) {
			logger.error("No Classifier files found!!  Probably a resource issue");
			e.printStackTrace();
		}
		this.type = newType;
		this.tree = newTree;
		StringExtractionUtils.prettyPrint(tree, sootMethod);
		this.sootMethod = sootMethod;
		StringExtractionUtils.prettyPrint(tree, sootMethod);
		hasIncompleteParameter = false;
		populateTypes();
	}
	
	public String getType(){
		return type;
	}

	private void setupClassifiers() throws IOException {
		if(! classificationPopulated){
			ClassLoader classLoader = getClass().getClassLoader();
			URL resource = classLoader.getResource("resources/classifiers");
			File directory = new File(resource.getFile());
			if(directory.exists()  && directory.isDirectory()){
				File[] files = directory.listFiles();
				for (File file : files) {
					if(file.getName().equals(CLASSIFICATIONS[0])){
						populateClassifierHashMap(file, OBFUSCATION_MAP);
					}
					else if(file.getName().equals(CLASSIFICATIONS[1])){
						populateClassifierHashMap(file, PROVENANCE_MAP);							
					}
					else if(file.getName().equals(CLASSIFICATIONS[2])){
						populateClassifierHashMap(file, SEMANTIC_MAP);
					}
					else if(file.getName().equals(CLASSIFICATIONS[3])){
						populateClassifierHashMap(file, STRUCTURAL_MAP);
					}
				}
			}
			else{
				logger.debug("Classifier files not found");				
			}
			classificationPopulated = true;
		}
	}

	public ArrayList<String> getObfuscationMatches(){ return obfuscationMatches;};
	public ArrayList<String> getProvenanceMatches(){ return provenanceMatches;};
	public ArrayList<String> getSemanticMatches(){ return semanticMatches;};
	public ArrayList<String> getStructuralMatches(){ return structuralMatches;};
	public ArrayList<String> getClassFieldMatches(){ return classFieldMatches;};
	public ArrayList<String> getStaticFieldMatches(){ return staticFieldMatches;};
	public ArrayList<String> getStringFieldMatches(){ return stringFieldMatches;};
	public ArrayList<String> getMethodSignatures(){ return methodSignatures;};
	
	private void populateClassifierHashMap(File file, HashMap<String, Set<String>> map)
			throws IOException {
		File[] classifierFiles = file.listFiles();
		for (File classifierType : classifierFiles) {
			List<String> lines = FileUtils.readLines(classifierType, "UTF-8");
			Set<String> values = new HashSet<String>();
			for (String string : lines) {
				values.add(string);
			}
			map.put(classifierType.getName().replaceAll("txt", ""), values);
		}
	}
	
	public int getNesting(){
		return nesting;
	}
	
	private void populateTypes() {
		evaluateViaBfs(tree.getRoot(), tree);
		//classifyConstruction();
	}
	
	private void classifyConstruction() throws Exception{
		throw new Exception("make me work!");
	}

	private void evaluateViaBfs(UnitWrapper unit, DelegateTree<UnitWrapper, String> delegateTree) {
		if(unit.getUnit() instanceof JReturnStmt ||
				unit.getUnit() instanceof JIdentityStmt){
			nesting++;
			if(unit.getUnit() instanceof JIdentityStmt && delegateTree.getChildCount(unit) == 0){
				hasIncompleteParameter = true;
			}
		}
		harvestString(unit);
		classifyType(unit);
		classifyClassOrStatic(unit);
		captureMethodSignatures(unit);
		Collection<UnitWrapper> units = delegateTree.getChildren(unit);
		int children = units.size();
		for (UnitWrapper childUnit : units) {
			evaluateViaBfs(childUnit, delegateTree);
		}
	}

	private void captureMethodSignatures(UnitWrapper unitWrapper) {
		Unit unit = unitWrapper.getUnit();
		if(unit instanceof JInvokeStmt){
			JInvokeStmt invokeStmt = (JInvokeStmt)unit;
			String signature = invokeStmt.getInvokeExpr().getMethod().getSignature();
			methodSignatures.add(signature);
		}
		else if(unit instanceof JAssignStmt){
			JAssignStmt assignStmt = (JAssignStmt)unit;
			if(assignStmt.getRightOpBox().getValue() instanceof InvokeExpr){
				InvokeExpr invokeExpr = (InvokeExpr)assignStmt.getRightOp();
				String signature = invokeExpr.getMethod().getSignature();
				methodSignatures.add(signature);
				if(assignStmt.getLeftOp() instanceof FieldRef){
					FieldRef fieldRef = (FieldRef)assignStmt.getLeftOp(); 
					ArrayList<DelegateTree<UnitWrapper, String>> classFieldValues = NetworkFlowBackwardFlowAnalysis.getClassFieldAssignedValues().get(fieldRef);
					for (DelegateTree<UnitWrapper, String> delegateTree : classFieldValues) {
						Collection<UnitWrapper> verticies = delegateTree.getVertices();
						for (UnitWrapper classFieldUnitWrappper : verticies) {
							if(classFieldUnitWrappper.getUnit() instanceof JInvokeStmt){
								JInvokeStmt classInvokeStmt = (JInvokeStmt)classFieldUnitWrappper.getUnit();
								String classFieldSignature = classInvokeStmt.getInvokeExpr().getMethod().getSignature();
								methodSignatures.add(classFieldSignature);
							}
							else if(classFieldUnitWrappper.getUnit() instanceof JAssignStmt){
								JAssignStmt classAssignStmt = (JAssignStmt)classFieldUnitWrappper.getUnit();
								if(classAssignStmt.getRightOpBox().getValue() instanceof InvokeExpr){
									InvokeExpr classInvokeExpr = (InvokeExpr)classAssignStmt.getRightOp();
									String classFieldSignature = classInvokeExpr.getMethod().getSignature();
									methodSignatures.add(classFieldSignature);
								}
							}
						}
					}
				}
				logger.debug("major tom");
			}
		}		
	}

	private void classifyType(UnitWrapper unit) {
		matchOnClassifier(unit, " ", "(", OBFUSCATION_MAP, obfuscationMatches);
		matchOnClassifier(unit, "", "", PROVENANCE_MAP, provenanceMatches);
		matchOnClassifier(unit, "", "", SEMANTIC_MAP, semanticMatches);
		matchOnClassifier(unit, "", "", STRUCTURAL_MAP, structuralMatches);		
	}

	private void matchOnClassifier(UnitWrapper unit, String prefix, String postfix, HashMap<String, Set<String>> map, ArrayList<String> arrayList) {
		Set<String> obfuscations = map.keySet();
		for (String key : obfuscations) {
			Set<String> values = map.get(key);
			for (String match : values) {
				String matchCriteria = prefix + match + postfix;
				if(unit.toString().contains(matchCriteria)){
					arrayList.add(match);
				}
			}
		}
	}

	private void classifyClassOrStatic(UnitWrapper unit) {
		unit.toString();
		if(unit.getUnit() instanceof JAssignStmt){
			JAssignStmt jAssignStmt = (JAssignStmt)unit.getUnit();
			if(jAssignStmt.getRightOp() instanceof JInstanceFieldRef){
				classFieldMatches.add(unit.getUnit().toString());
			}
			else if(jAssignStmt.getRightOp() instanceof StaticFieldRef){
				staticFieldMatches.add(unit.getUnit().toString());
			}
		}
		logger.debug("here we are");
	}


	private void harvestString(UnitWrapper unit) {
		if(unit.toString().contains("com.geinimi.AdService: java.lang.String g")){
			logger.debug("What are we doing with classes ...");
		}
		//if(!unit.toString().contains("java.lang.String replace")){
			//Pattern p = Pattern.compile(".*\\\"(.*)\\\".*");
			Pattern p = Pattern.compile("\"([^\"]*)\"");
			
			Matcher m = p.matcher(unit.toString());
			while (m.find()){
				logger.debug("Start index: " + m.start());
				logger.debug(" End index: " + m.end());
				logger.debug(" group size: " + m.group(1).length());
				int groupCount = m.groupCount();
				if(m.group(1).length() > 0){
					String match = m.group(1);
					stringFieldMatches.add(match);					
				}
			}			
		//}
		if(unit.getUnit() instanceof JAssignStmt){
			JAssignStmt assignStmt = (JAssignStmt)unit.getUnit();
			if(assignStmt.getRightOp() instanceof FieldRef){
				FieldRef jInstanceFieldRef = (FieldRef)assignStmt.getRightOp();
				ArrayList<DelegateTree<UnitWrapper, String>> assignments = NetworkFlowBackwardFlowAnalysis.getClassFieldAssignedValues().get(jInstanceFieldRef.toString());
				logger.debug("assignments");
				if(assignments != null){
					for (DelegateTree<UnitWrapper, String> tree : assignments) {
						ArrayList<String> classAssignStrings = StringExtractionUtils.harvestString(tree);
						stringFieldMatches.add("<<CL: " + getMatchesAsStringFromList(classAssignStrings, true, false) +" >>");																
					}					
				}
			}
		}
	}

	
	
	public String getUrl(){
		return StringExtractionUtils.harvestString(tree).get(0);
	}
	
	public int getTreeSize(){
		return tree.getVertexCount();
	}

	public DelegateTree<UnitWrapper, String> getDelegateTree() {
		return tree;
	}

	public SootMethod getSootMethod() {
		return sootMethod;
	}
	
}
