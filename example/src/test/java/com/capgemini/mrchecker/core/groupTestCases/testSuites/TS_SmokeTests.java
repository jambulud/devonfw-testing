package com.capgemini.mrchecker.core.groupTestCases.testSuites;

import com.capgemini.mrchecker.core.groupTestCases.testSuites.tags.TestsSmoke;
import com.googlecode.junittoolbox.ExcludeCategories;
import com.googlecode.junittoolbox.IncludeCategories;
import com.googlecode.junittoolbox.SuiteClasses;
import com.googlecode.junittoolbox.WildcardPatternSuite;
import org.junit.runner.RunWith;

@RunWith(WildcardPatternSuite.class)
@IncludeCategories({ TestsSmoke.class })
@ExcludeCategories({})
@SuiteClasses({ "../**/*Test.class" })
public class TS_SmokeTests {

}
