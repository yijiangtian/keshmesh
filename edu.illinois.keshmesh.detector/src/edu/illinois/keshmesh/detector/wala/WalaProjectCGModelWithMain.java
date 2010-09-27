/*******************************************************************************
 * Copyright (c) 2002 - 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package edu.illinois.keshmesh.detector.wala;

import java.io.IOException;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;

import com.ibm.wala.cast.java.ipa.callgraph.JavaSourceAnalysisScope;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.util.graph.InferGraphRoots;
import com.ibm.wala.util.warnings.WalaException;

/**
 * @see com.ibm.wala.eclipse.cg.model.WalaProjectCGModelWithMain
 * @author aying
 */
public class WalaProjectCGModelWithMain extends WalaProjectCGModel {

	public WalaProjectCGModelWithMain(IJavaProject project, String exclusionsFile) throws IOException, CoreException {
		super(project, exclusionsFile);
	}

	@Override
	protected Iterable<Entrypoint> getEntrypoints(AnalysisScope scope, IClassHierarchy cha) {
		return com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(JavaSourceAnalysisScope.SOURCE, cha);
	}

	@Override
	protected Collection inferRoots(CallGraph cg) throws WalaException {
		return InferGraphRoots.inferRoots(cg);
	}

}
