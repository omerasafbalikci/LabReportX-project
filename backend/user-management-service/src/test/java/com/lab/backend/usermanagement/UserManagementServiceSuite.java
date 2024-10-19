package com.lab.backend.usermanagement;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({
        "com.lab.backend.usermanagement.controller",
        "com.lab.backend.usermanagement.service.concretes"
})
class UserManagementServiceSuite {
}
