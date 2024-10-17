package com.lab.backend.gateway;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({
        "com.lab.backend.gateway.config",
        "com.lab.backend.gateway.controller",
        "com.lab.backend.gateway.utilities"
})
class ApiGatewaySuite {
}
