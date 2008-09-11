/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
 package org.eclipse.jdt.ui.tests.refactoring.all;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.jdt.ui.tests.refactoring.reorg.AllReorgPerformanceTests;
import org.eclipse.jdt.ui.tests.refactoring.type.AllTypeConstraintsPerformanceTests;

public class AllRefactoringPerformanceTests extends TestCase {

	public static Test suite() {
		TestSuite suite= new TestSuite("All Refactoring Performance Tests"); //$NON-NLS-1$
		suite.addTest(AllReorgPerformanceTests.suite());
		suite.addTest(AllTypeConstraintsPerformanceTests.suite());
		return suite;
	}

}
