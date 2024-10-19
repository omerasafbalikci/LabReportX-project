package com.lab.backend.report;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({
        "com.lab.backend.report.controller",
        "com.lab.backend.report.service.concretes"
})
class ReportServiceSuite {
}
