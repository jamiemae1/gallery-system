package com.mycompany.myapp.cucumber;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.ConfigurationParameters;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("com/mycompany/myapp/cucumber")
@ConfigurationParameters(
    {
        @ConfigurationParameter(key = "cucumber.plugin", value = "pretty"),
        @ConfigurationParameter(key = "cucumber.glue", value = "com.mycompany.myapp.cucumber"),
    }
)
class CucumberIT {}
