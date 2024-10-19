package com.lab.backend.patient;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({
        "com.lab.backend.patient.controller",
        "com.lab.backend.patient.service.concretes"
})
class PatientServiceSuite {
}
