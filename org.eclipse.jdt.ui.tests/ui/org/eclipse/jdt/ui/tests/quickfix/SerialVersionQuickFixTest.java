/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.ui.tests.quickfix;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.core.resources.IncrementalProjectBuilder;

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;

import org.eclipse.jdt.testplugin.JavaProjectHelper;
import org.eclipse.jdt.testplugin.TestOptions;

import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.tests.core.ProjectTestSetup;

import org.eclipse.jdt.internal.corext.template.java.CodeTemplateContextType;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.correction.SerialVersionDefaultProposal;
import org.eclipse.jdt.internal.ui.text.correction.SerialVersionHashProposal;

/**
 *  
 */
public class SerialVersionQuickFixTest extends QuickFixTest {

	private static final String FIELD_COMMENT= "/* Test */";

	private static final Class THIS= SerialVersionQuickFixTest.class;

	/**
	 * @return Test
	 */
	public static Test allTests() {
		return new ProjectTestSetup(new TestSuite(THIS));
	}

	/*
	 * @see org.eclipse.jdt.ui.tests.quickfix.QuickFixTest#suite()
	 */
	public static Test suite() {
		return allTests();
	}

	private IJavaProject fProject;

	private IPackageFragmentRoot fSourceFolder;

	/**
	 * @param name
	 */
	public SerialVersionQuickFixTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {

		Hashtable options= TestOptions.getFormatterOptions();

		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE, "1"); //$NON-NLS-1$
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4"); //$NON-NLS-1$
		options.put(JavaCore.COMPILER_PB_UNUSED_IMPORT, JavaCore.IGNORE);
		JavaCore.setOptions(options);

		IPreferenceStore store= JavaPlugin.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.CODEGEN_ADD_COMMENTS, false);

		fProject= JavaProjectHelper.createJavaProject("serialIdProject", "bin");
		Map preferences= fProject.getOptions(true);
//		JavaProjectHelper.set15CompilerOptions(preferences);
		fProject.setOptions(preferences);
		JavaProjectHelper.addRTJar(fProject);

		JavaPlugin.getDefault().getCodeTemplateStore().findTemplate(CodeTemplateContextType.NEWTYPE).setPattern(""); //$NON-NLS-1$
		JavaPlugin.getDefault().getCodeTemplateStore().findTemplate(CodeTemplateContextType.TYPECOMMENT).setPattern(""); //$NON-NLS-1$
		JavaPlugin.getDefault().getCodeTemplateStore().findTemplate(CodeTemplateContextType.FIELDCOMMENT).setPattern(FIELD_COMMENT); //$NON-NLS-1$

		fSourceFolder= JavaProjectHelper.addSourceContainer(fProject, "src"); //$NON-NLS-1$

//		IPackageFragment package0= fSourceFolder.createPackageFragment("test0", false, null); //$NON-NLS-1$
//		StringBuffer buffer= new StringBuffer();
//
//		buffer.append("package test0;\n"); //$NON-NLS-1$
//		buffer.append("import java.io.Serializable;\n"); //$NON-NLS-1$
//		buffer.append("public class Test<T> implements Serializable {\n"); //$NON-NLS-1$
//		buffer.append("}\n"); //$NON-NLS-1$
//
//		package0.createCompilationUnit("Test.java", buffer.toString(), false, null); //$NON-NLS-1$
	}

	protected void tearDown() throws Exception {
		JavaProjectHelper.delete(fProject);
	}

	/**
	 * @throws Exception
	 */
	public void testAnonymousClass() throws Exception {

		IPackageFragment package3= fSourceFolder.createPackageFragment("test3", false, null); //$NON-NLS-1$
		StringBuffer buffer= new StringBuffer();

		buffer.append("package test3;\n"); //$NON-NLS-1$
		buffer.append("import java.io.Serializable;\n"); //$NON-NLS-1$
		buffer.append("public class Test3 {\n"); //$NON-NLS-1$
		buffer.append("    protected int var1;\n"); //$NON-NLS-1$
		buffer.append("    protected int var2;\n"); //$NON-NLS-1$
		buffer.append("    public void test() {\n"); //$NON-NLS-1$
		buffer.append("        Serializable var3= new Serializable() {\n"); //$NON-NLS-1$
		buffer.append("            int var4;\n"); //$NON-NLS-1$
		buffer.append("        };\n"); //$NON-NLS-1$
		buffer.append("    }\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$

		ICompilationUnit unit3= package3.createCompilationUnit("Test3.java", buffer.toString(), false, null); //$NON-NLS-1$
		fProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());

		CompilationUnit root3= getASTRoot(unit3);
		ArrayList proposals3= collectCorrections(unit3, root3);

		assertNumberOf("proposals3", proposals3.size(), 2); //$NON-NLS-1$
		assertCorrectLabels(proposals3);

		Object current= null;
		for (int index= 0; index < proposals3.size(); index++) {

			current= proposals3.get(index);
			if (current instanceof SerialVersionHashProposal) {

				SerialVersionHashProposal proposal= (SerialVersionHashProposal) current;
				String preview= getPreviewContent(proposal);

				buffer= new StringBuffer();
				buffer.append("package test3;\n"); //$NON-NLS-1$
				buffer.append("import java.io.Serializable;\n"); //$NON-NLS-1$
				buffer.append("public class Test3 {\n"); //$NON-NLS-1$
				buffer.append("    protected int var1;\n"); //$NON-NLS-1$
				buffer.append("    protected int var2;\n"); //$NON-NLS-1$
				buffer.append("    public void test() {\n"); //$NON-NLS-1$
				buffer.append("        Serializable var3= new Serializable() {\n"); //$NON-NLS-1$
				buffer.append("            " + FIELD_COMMENT + "\n");
				buffer.append("            private static final long serialVersionUID = 3258125877639328056L;\n"); //$NON-NLS-1$
				buffer.append("            int var4;\n"); //$NON-NLS-1$
				buffer.append("        };\n"); //$NON-NLS-1$
				buffer.append("    }\n"); //$NON-NLS-1$
				buffer.append("}\n"); //$NON-NLS-1$
				assertEqualString(preview, buffer.toString());

			} else if (current instanceof SerialVersionDefaultProposal) {

				SerialVersionDefaultProposal proposal= (SerialVersionDefaultProposal) current;
				String preview= getPreviewContent(proposal);

				buffer= new StringBuffer();
				buffer.append("package test3;\n"); //$NON-NLS-1$
				buffer.append("import java.io.Serializable;\n"); //$NON-NLS-1$
				buffer.append("public class Test3 {\n"); //$NON-NLS-1$
				buffer.append("    protected int var1;\n"); //$NON-NLS-1$
				buffer.append("    protected int var2;\n"); //$NON-NLS-1$
				buffer.append("    public void test() {\n"); //$NON-NLS-1$
				buffer.append("        Serializable var3= new Serializable() {\n"); //$NON-NLS-1$
				buffer.append("            " + FIELD_COMMENT + "\n");
				buffer.append("            private static final long serialVersionUID = 1L;\n"); //$NON-NLS-1$
				buffer.append("            int var4;\n"); //$NON-NLS-1$
				buffer.append("        };\n"); //$NON-NLS-1$
				buffer.append("    }\n"); //$NON-NLS-1$
				buffer.append("}\n"); //$NON-NLS-1$
				assertEqualString(preview, buffer.toString());
			}
		}
	}

//	public void testGenericAnonymousClass() throws Exception {
//
//		IPackageFragment package4= fSourceFolder.createPackageFragment("test4", false, null); //$NON-NLS-1$
//		StringBuffer buffer= new StringBuffer();
//
//		buffer.append("package test4;\n"); //$NON-NLS-1$
//		buffer.append("import java.io.Serializable;\n"); //$NON-NLS-1$
//		buffer.append("import test0.Test;\n"); //$NON-NLS-1$
//		buffer.append("public class Test4<T> {\n"); //$NON-NLS-1$
//		buffer.append("    protected int var1;\n"); //$NON-NLS-1$
//		buffer.append("    protected int var2;\n"); //$NON-NLS-1$
//		buffer.append("    public void test() {\n"); //$NON-NLS-1$
//		buffer.append("        Serializable var3= new Test<T>() {\n"); //$NON-NLS-1$
//		buffer.append("            int var4;\n"); //$NON-NLS-1$
//		buffer.append("        };\n"); //$NON-NLS-1$
//		buffer.append("    }\n"); //$NON-NLS-1$
//		buffer.append("}\n"); //$NON-NLS-1$
//
//		ICompilationUnit unit4= package4.createCompilationUnit("Test4.java", buffer.toString(), false, null); //$NON-NLS-1$
//		fProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
//
//		CompilationUnit root4= getASTRoot(unit4);
//		ArrayList proposals4= collectCorrections(unit4, root4);
//
//		assertNumberOf("proposals4", proposals4.size(), 2); //$NON-NLS-1$
//		assertCorrectLabels(proposals4);
//
//		Object current= null;
//		for (int index= 0; index < proposals4.size(); index++) {
//
//			current= proposals4.get(index);
//			if (current instanceof SerialVersionHashProposal) {
//
//				SerialVersionHashProposal proposal= (SerialVersionHashProposal) current;
//				String preview= getPreviewContent(proposal);
//
//				buffer= new StringBuffer();
//				buffer.append("package test4;\n"); //$NON-NLS-1$
//				buffer.append("import java.io.Serializable;\n"); //$NON-NLS-1$
//				buffer.append("import test0.Test;\n"); //$NON-NLS-1$
//				buffer.append("public class Test4<T> {\n"); //$NON-NLS-1$
//				buffer.append("    protected int var1;\n"); //$NON-NLS-1$
//				buffer.append("    protected int var2;\n"); //$NON-NLS-1$
//				buffer.append("    public void test() {\n"); //$NON-NLS-1$
//				buffer.append("        Serializable var3= new Test<T>() {\n"); //$NON-NLS-1$
//				buffer.append("            " + FIELD_COMMENT + "\n");
//				buffer.append("            private static final long serialVersionUID = 3257288045601240375L;\n"); //$NON-NLS-1$
//				buffer.append("            int var4;\n"); //$NON-NLS-1$
//				buffer.append("        };\n"); //$NON-NLS-1$
//				buffer.append("    }\n"); //$NON-NLS-1$
//				buffer.append("}\n"); //$NON-NLS-1$
//				assertEqualString(preview, buffer.toString());
//
//			} else if (current instanceof SerialVersionDefaultProposal) {
//
//				SerialVersionDefaultProposal proposal= (SerialVersionDefaultProposal) current;
//				String preview= getPreviewContent(proposal);
//
//				buffer= new StringBuffer();
//				buffer.append("package test4;\n"); //$NON-NLS-1$
//				buffer.append("import java.io.Serializable;\n"); //$NON-NLS-1$
//				buffer.append("import test0.Test;\n"); //$NON-NLS-1$
//				buffer.append("public class Test4<T> {\n"); //$NON-NLS-1$
//				buffer.append("    protected int var1;\n"); //$NON-NLS-1$
//				buffer.append("    protected int var2;\n"); //$NON-NLS-1$
//				buffer.append("    public void test() {\n"); //$NON-NLS-1$
//				buffer.append("        Serializable var3= new Test<T>() {\n"); //$NON-NLS-1$
//				buffer.append("            " + FIELD_COMMENT + "\n");
//				buffer.append("            private static final long serialVersionUID = 1L;\n"); //$NON-NLS-1$
//				buffer.append("            int var4;\n"); //$NON-NLS-1$
//				buffer.append("        };\n"); //$NON-NLS-1$
//				buffer.append("    }\n"); //$NON-NLS-1$
//				buffer.append("}\n"); //$NON-NLS-1$
//				assertEqualString(preview, buffer.toString());
//			}
//		}
//	}

//	public void testGenericInnerClass() throws Exception {
//
//		IPackageFragment package5= fSourceFolder.createPackageFragment("test5", false, null); //$NON-NLS-1$
//		StringBuffer buffer= new StringBuffer();
//
//		buffer.append("package test5;\n"); //$NON-NLS-1$
//		buffer.append("import java.io.Serializable;\n"); //$NON-NLS-1$
//		buffer.append("import test0.Test;\n"); //$NON-NLS-1$
//		buffer.append("public class Test5 {\n"); //$NON-NLS-1$
//		buffer.append("    protected int var1;\n"); //$NON-NLS-1$
//		buffer.append("    protected int var2;\n"); //$NON-NLS-1$
//		buffer.append("    protected static class Test1<T> extends Test<T> {\n"); //$NON-NLS-1$
//		buffer.append("        public long var3;\n"); //$NON-NLS-1$
//		buffer.append("    }\n"); //$NON-NLS-1$
//		buffer.append("}\n"); //$NON-NLS-1$
//
//		ICompilationUnit unit5= package5.createCompilationUnit("Test5.java", buffer.toString(), false, null); //$NON-NLS-1$
//		fProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
//
//		CompilationUnit root5= getASTRoot(unit5);
//		ArrayList proposals5= collectCorrections(unit5, root5);
//
//		assertNumberOf("proposals5", proposals5.size(), 2); //$NON-NLS-1$
//		assertCorrectLabels(proposals5);
//
//		Object current= null;
//		for (int index= 0; index < proposals5.size(); index++) {
//
//			current= proposals5.get(index);
//			if (current instanceof SerialVersionHashProposal) {
//
//				SerialVersionHashProposal proposal= (SerialVersionHashProposal) current;
//				String preview= getPreviewContent(proposal);
//
//				buffer= new StringBuffer();
//				buffer.append("package test5;\n"); //$NON-NLS-1$
//				buffer.append("import java.io.Serializable;\n"); //$NON-NLS-1$
//				buffer.append("import test0.Test;\n"); //$NON-NLS-1$
//				buffer.append("public class Test5 {\n"); //$NON-NLS-1$
//				buffer.append("    protected int var1;\n"); //$NON-NLS-1$
//				buffer.append("    protected int var2;\n"); //$NON-NLS-1$
//				buffer.append("    protected static class Test1<T> extends Test<T> {\n"); //$NON-NLS-1$
//				buffer.append("        " + FIELD_COMMENT + "\n");
//				buffer.append("        private static final long serialVersionUID = 3256720676126538041L;\n"); //$NON-NLS-1$
//				buffer.append("        public long var3;\n"); //$NON-NLS-1$
//				buffer.append("    }\n"); //$NON-NLS-1$
//				buffer.append("}\n"); //$NON-NLS-1$
//				assertEqualString(preview, buffer.toString());
//
//			} else if (current instanceof SerialVersionDefaultProposal) {
//
//				SerialVersionDefaultProposal proposal= (SerialVersionDefaultProposal) current;
//				String preview= getPreviewContent(proposal);
//
//				buffer= new StringBuffer();
//				buffer.append("package test5;\n"); //$NON-NLS-1$
//				buffer.append("import java.io.Serializable;\n"); //$NON-NLS-1$
//				buffer.append("import test0.Test;\n"); //$NON-NLS-1$
//				buffer.append("public class Test5 {\n"); //$NON-NLS-1$
//				buffer.append("    protected int var1;\n"); //$NON-NLS-1$
//				buffer.append("    protected int var2;\n"); //$NON-NLS-1$
//				buffer.append("    protected static class Test1<T> extends Test<T> {\n"); //$NON-NLS-1$
//				buffer.append("        " + FIELD_COMMENT + "\n");
//				buffer.append("        private static final long serialVersionUID = 1L;\n"); //$NON-NLS-1$
//				buffer.append("        public long var3;\n"); //$NON-NLS-1$
//				buffer.append("    }\n"); //$NON-NLS-1$
//				buffer.append("}\n"); //$NON-NLS-1$
//				assertEqualString(preview, buffer.toString());
//			}
//		}
//	}

//	public void testGenericOuterClass() throws Exception {
//
//		IPackageFragment package6= fSourceFolder.createPackageFragment("test6", false, null); //$NON-NLS-1$
//		StringBuffer buffer= new StringBuffer();
//
//		buffer.append("package test6;\n"); //$NON-NLS-1$
//		buffer.append("import java.io.Serializable;\n"); //$NON-NLS-1$
//		buffer.append("public class Test6<T> implements Serializable {\n"); //$NON-NLS-1$
//		buffer.append("    protected int var1;\n"); //$NON-NLS-1$
//		buffer.append("    protected int var2;\n"); //$NON-NLS-1$
//		buffer.append("}\n"); //$NON-NLS-1$
//
//		ICompilationUnit unit6= package6.createCompilationUnit("Test6.java", buffer.toString(), false, null); //$NON-NLS-1$
//		fProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
//
//		CompilationUnit root6= getASTRoot(unit6);
//		ArrayList proposals6= collectCorrections(unit6, root6);
//
//		assertNumberOf("proposals6", proposals6.size(), 2); //$NON-NLS-1$
//		assertCorrectLabels(proposals6);
//
//		Object current= null;
//		for (int index= 0; index < proposals6.size(); index++) {
//
//			current= proposals6.get(index);
//			if (current instanceof SerialVersionHashProposal) {
//
//				SerialVersionHashProposal proposal= (SerialVersionHashProposal) current;
//				String preview= getPreviewContent(proposal);
//
//				buffer= new StringBuffer();
//				buffer.append("package test6;\n"); //$NON-NLS-1$
//				buffer.append("import java.io.Serializable;\n"); //$NON-NLS-1$
//				buffer.append("public class Test6<T> implements Serializable {\n"); //$NON-NLS-1$
//				buffer.append("    " + FIELD_COMMENT + "\n");
//				buffer.append("    private static final long serialVersionUID = 3257853198738667576L;\n"); //$NON-NLS-1$
//				buffer.append("    protected int var1;\n"); //$NON-NLS-1$
//				buffer.append("    protected int var2;\n"); //$NON-NLS-1$
//				buffer.append("}\n"); //$NON-NLS-1$
//				assertEqualString(preview, buffer.toString());
//
//			} else if (current instanceof SerialVersionDefaultProposal) {
//
//				SerialVersionDefaultProposal proposal= (SerialVersionDefaultProposal) current;
//				String preview= getPreviewContent(proposal);
//
//				buffer= new StringBuffer();
//				buffer.append("package test6;\n"); //$NON-NLS-1$
//				buffer.append("import java.io.Serializable;\n"); //$NON-NLS-1$
//				buffer.append("public class Test6<T> implements Serializable {\n"); //$NON-NLS-1$
//				buffer.append("    " + FIELD_COMMENT + "\n");
//				buffer.append("    private static final long serialVersionUID = 1L;\n"); //$NON-NLS-1$
//				buffer.append("    protected int var1;\n"); //$NON-NLS-1$
//				buffer.append("    protected int var2;\n"); //$NON-NLS-1$
//				buffer.append("}\n"); //$NON-NLS-1$
//				assertEqualString(preview, buffer.toString());
//			}
//		}
//	}

	/**
	 * @throws Exception
	 */
	public void testInnerClass() throws Exception {

		IPackageFragment package2= fSourceFolder.createPackageFragment("test2", false, null); //$NON-NLS-1$
		StringBuffer buffer= new StringBuffer();

		buffer.append("package test2;\n"); //$NON-NLS-1$
		buffer.append("import java.io.Serializable;\n"); //$NON-NLS-1$
		buffer.append("public class Test2 {\n"); //$NON-NLS-1$
		buffer.append("    protected int var1;\n"); //$NON-NLS-1$
		buffer.append("    protected int var2;\n"); //$NON-NLS-1$
		buffer.append("    protected class Test1 implements Serializable {\n"); //$NON-NLS-1$
		buffer.append("        public long var3;\n"); //$NON-NLS-1$
		buffer.append("    }\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$

		ICompilationUnit unit2= package2.createCompilationUnit("Test2.java", buffer.toString(), false, null); //$NON-NLS-1$
		fProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());

		CompilationUnit root2= getASTRoot(unit2);
		ArrayList proposals2= collectCorrections(unit2, root2);

		assertNumberOf("proposals2", proposals2.size(), 2); //$NON-NLS-1$
		assertCorrectLabels(proposals2);

		Object current= null;
		for (int index= 0; index < proposals2.size(); index++) {

			current= proposals2.get(index);
			if (current instanceof SerialVersionHashProposal) {

				SerialVersionHashProposal proposal= (SerialVersionHashProposal) current;
				String preview= getPreviewContent(proposal);

				buffer= new StringBuffer();
				buffer.append("package test2;\n"); //$NON-NLS-1$
				buffer.append("import java.io.Serializable;\n"); //$NON-NLS-1$
				buffer.append("public class Test2 {\n"); //$NON-NLS-1$
				buffer.append("    protected int var1;\n"); //$NON-NLS-1$
				buffer.append("    protected int var2;\n"); //$NON-NLS-1$
				buffer.append("    protected class Test1 implements Serializable {\n"); //$NON-NLS-1$
				buffer.append("        " + FIELD_COMMENT + "\n");
				buffer.append("        private static final long serialVersionUID = 3257281422661137200L;\n"); //$NON-NLS-1$
				buffer.append("        public long var3;\n"); //$NON-NLS-1$
				buffer.append("    }\n"); //$NON-NLS-1$
				buffer.append("}\n"); //$NON-NLS-1$
				assertEqualString(preview, buffer.toString());

			} else if (current instanceof SerialVersionDefaultProposal) {

				SerialVersionDefaultProposal proposal= (SerialVersionDefaultProposal) current;
				String preview= getPreviewContent(proposal);

				buffer= new StringBuffer();
				buffer.append("package test2;\n"); //$NON-NLS-1$
				buffer.append("import java.io.Serializable;\n"); //$NON-NLS-1$
				buffer.append("public class Test2 {\n"); //$NON-NLS-1$
				buffer.append("    protected int var1;\n"); //$NON-NLS-1$
				buffer.append("    protected int var2;\n"); //$NON-NLS-1$
				buffer.append("    protected class Test1 implements Serializable {\n"); //$NON-NLS-1$
				buffer.append("        " + FIELD_COMMENT + "\n");
				buffer.append("        private static final long serialVersionUID = 1L;\n"); //$NON-NLS-1$
				buffer.append("        public long var3;\n"); //$NON-NLS-1$
				buffer.append("    }\n"); //$NON-NLS-1$
				buffer.append("}\n"); //$NON-NLS-1$
				assertEqualString(preview, buffer.toString());
			}
		}
	}

	/**
	 * @throws Exception
	 */
	public void testOuterClass() throws Exception {

		IPackageFragment package1= fSourceFolder.createPackageFragment("test1", false, null); //$NON-NLS-1$
		StringBuffer buffer= new StringBuffer();

		buffer.append("package test1;\n"); //$NON-NLS-1$
		buffer.append("import java.io.Serializable;\n"); //$NON-NLS-1$
		buffer.append("public class Test1 implements Serializable {\n"); //$NON-NLS-1$
		buffer.append("    protected int var1;\n"); //$NON-NLS-1$
		buffer.append("    protected int var2;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$

		ICompilationUnit unit1= package1.createCompilationUnit("Test1.java", buffer.toString(), false, null); //$NON-NLS-1$
		fProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());

		CompilationUnit root1= getASTRoot(unit1);
		ArrayList proposals1= collectCorrections(unit1, root1);

		assertNumberOf("proposals1", proposals1.size(), 2); //$NON-NLS-1$
		assertCorrectLabels(proposals1);

		Object current= null;
		for (int index= 0; index < proposals1.size(); index++) {

			current= proposals1.get(index);
			if (current instanceof SerialVersionHashProposal) {

				SerialVersionHashProposal proposal= (SerialVersionHashProposal) current;
				String preview= getPreviewContent(proposal);

				buffer= new StringBuffer();
				buffer.append("package test1;\n"); //$NON-NLS-1$
				buffer.append("import java.io.Serializable;\n"); //$NON-NLS-1$
				buffer.append("public class Test1 implements Serializable {\n"); //$NON-NLS-1$
				buffer.append("    " + FIELD_COMMENT + "\n");
				buffer.append("    private static final long serialVersionUID = 3256720680304458040L;\n"); //$NON-NLS-1$
				buffer.append("    protected int var1;\n"); //$NON-NLS-1$
				buffer.append("    protected int var2;\n"); //$NON-NLS-1$
				buffer.append("}\n"); //$NON-NLS-1$
				assertEqualString(preview, buffer.toString());

			} else if (current instanceof SerialVersionDefaultProposal) {

				SerialVersionDefaultProposal proposal= (SerialVersionDefaultProposal) current;
				String preview= getPreviewContent(proposal);

				buffer= new StringBuffer();
				buffer.append("package test1;\n"); //$NON-NLS-1$
				buffer.append("import java.io.Serializable;\n"); //$NON-NLS-1$
				buffer.append("public class Test1 implements Serializable {\n"); //$NON-NLS-1$
				buffer.append("    " + FIELD_COMMENT + "\n");
				buffer.append("    private static final long serialVersionUID = 1L;\n"); //$NON-NLS-1$
				buffer.append("    protected int var1;\n"); //$NON-NLS-1$
				buffer.append("    protected int var2;\n"); //$NON-NLS-1$
				buffer.append("}\n"); //$NON-NLS-1$
				assertEqualString(preview, buffer.toString());
			}
		}
	}
}
