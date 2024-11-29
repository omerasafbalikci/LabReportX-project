package com.lab.backend.analytics;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({
		"com.lab.backend.analytics.controller",
		"com.lab.backend.analytics.service.concretes"
})
class AnalyticsServiceSuite {
}
