package com.lab.backend.auth;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({
        "com.lab.backend.auth.controller",
        "com.lab.backend.auth.service.concretes"
})
class AuthServiceSuite {
}
