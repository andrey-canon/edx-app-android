package org.proversity.edx.mobile.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ NotificationPreferenceTest.class, CourseManagerTest.class,
    WeakListTest.class, ViewPagerDownloadManagerTest.class, CourseComponentTest.class})
public class AllTest {

}
