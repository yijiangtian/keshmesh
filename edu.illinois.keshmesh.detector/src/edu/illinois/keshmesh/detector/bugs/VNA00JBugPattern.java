/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.keshmesh.detector.bugs;

import edu.illinois.keshmesh.detector.VNA00JBugDetector;

/**
 * 
 * @author Mohsen Vakilian
 * @author Stas Negara
 * 
 */
public class VNA00JBugPattern extends BugPattern {

	public VNA00JBugPattern() {
		super("VNA00J", "Ensure visibility when accessing shared primitive variables", new VNA00JBugDetector());
	}
}
