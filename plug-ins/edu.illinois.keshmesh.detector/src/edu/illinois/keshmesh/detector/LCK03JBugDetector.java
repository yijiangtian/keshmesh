/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAMonitorInstruction;

import edu.illinois.keshmesh.detector.bugs.BugInstance;
import edu.illinois.keshmesh.detector.bugs.BugInstances;
import edu.illinois.keshmesh.detector.bugs.BugPatterns;
import edu.illinois.keshmesh.detector.bugs.CodePosition;
import edu.illinois.keshmesh.detector.bugs.LCK03JBugPattern;
import edu.illinois.keshmesh.detector.bugs.LCK03JFixInformation;
import edu.illinois.keshmesh.detector.util.AnalysisUtils;
import edu.illinois.keshmesh.util.Logger;

/**
 * 
 * @author Samira Tasharofi
 * 
 */
public class LCK03JBugDetector extends BugPatternDetector {

	@Override
	public IntermediateResults getIntermediateResults() {
		return null;
	}

	@Override
	public BugInstances doPerformAnalysis(IJavaProject javaProject, BasicAnalysisData basicAnalysisData) {
		BugInstances bugInstances = new BugInstances();
		Collection<InstructionInfo> unsafeSynchronizedBlocks = new HashSet<InstructionInfo>();
		Iterator<CGNode> cgNodesIterator = basicAnalysisData.callGraph.iterator();
		while (cgNodesIterator.hasNext()) {
			final CGNode cgNode = cgNodesIterator.next();
			IMethod method = cgNode.getMethod();
			if (!isIgnoredClass(method.getDeclaringClass())) {
				populateBugInstances(unsafeSynchronizedBlocks, cgNode, bugInstances);
			}
		}
		return bugInstances;
	}

	private Set<String> getJavaNames(Set<IClass> monitorExpressionTypes) {
		Set<String> monitorExpressionTypeNames = new HashSet<String>();
		for (IClass type : monitorExpressionTypes) {
			monitorExpressionTypeNames.add(AnalysisUtils.walaTypeNameToJavaName(type.getName()));
		}
		return monitorExpressionTypeNames;
	}

	private void populateBugInstances(Collection<InstructionInfo> synchronizedBlocks, final CGNode cgNode, final BugInstances bugInstances) {
		AnalysisUtils.collect(javaProject, synchronizedBlocks, cgNode, new InstructionFilter() {

			@Override
			public boolean accept(InstructionInfo instructionInfo) {
				SSAInstruction instruction = instructionInfo.getInstruction();
				if (AnalysisUtils.isMonitorEnter(instruction)) {
					SSAMonitorInstruction monitorEnterInstruction = (SSAMonitorInstruction) instruction;
					Set<IClass> monitorExpressionTypes = getMonitorExpressionTypes(cgNode, monitorEnterInstruction);
					boolean isLock = isLock(monitorExpressionTypes);
					boolean isCondition = isCondition(monitorExpressionTypes);
					if (isLock || isCondition) {
						CodePosition instructionPosition = instructionInfo.getPosition();
						Logger.log("Detected an instance of LCK03-J in class " + instructionPosition.getFullyQualifiedClassName() + ", line number=" + instructionPosition.getFirstLine()
								+ ", instructionIndex= " + instructionInfo.getInstructionIndex());
						bugInstances.add(new BugInstance(BugPatterns.LCK03J, instructionPosition, new LCK03JFixInformation(getJavaNames(monitorExpressionTypes), isLock)));
					}
				}
				return false;
			}
		});
	}

	boolean isLock(Set<IClass> instanceTypes) {
		return anyClassImplementsInterface(instanceTypes, LCK03JBugPattern.LOCK);
	}

	boolean isCondition(Set<IClass> instanceTypes) {
		return anyClassImplementsInterface(instanceTypes, LCK03JBugPattern.CONDITION);
	}

	private boolean anyClassImplementsInterface(Set<IClass> instanceTypes, String interfaceName) {
		for (IClass instanceType : instanceTypes) {
			Collection<IClass> implementedInterfaces = instanceType.getAllImplementedInterfaces();
			for (IClass implementedInterface : implementedInterfaces) {
				String interfaceType = AnalysisUtils.walaTypeNameToJavaName(implementedInterface.getName());
				if (interfaceType.equals(interfaceName)) {
					return true;
				}
			}
		}
		return false;
	}

	Set<IClass> getMonitorExpressionTypes(CGNode cgNode, SSAMonitorInstruction monitorInstruction) {
		if (!monitorInstruction.isMonitorEnter()) {
			throw new AssertionError("Expected a monitor enter instruction.");
		}
		PointerKey lockPointer = getPointerForValueNumber(cgNode, monitorInstruction.getRef());
		Collection<InstanceKey> lockPointedInstances = getPointedInstances(lockPointer);
		Set<IClass> instancesTypes = new HashSet<IClass>();
		for (InstanceKey instanceKey : lockPointedInstances) {
			instancesTypes.add(instanceKey.getConcreteType());
		}
		return instancesTypes;

	}

	private boolean isIgnoredClass(IClass klass) {
		//TODO: Should we look for bugs in JDK usage as well?
		//TODO: !!!What about other bytecodes, e.g. from the libraries, which will not allow to get the source position?
		return AnalysisUtils.isJDKClass(klass);
	}

}
