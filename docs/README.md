# viadee Process Application Validator (vPAV)

The tool checks Camunda projects for consistency and discovers errors in process-driven applications.
Called as a Maven plugin or JUnit test, it discovers esp. inconsistencies of a given BPMN model in the classpath and the sourcecode of an underlying java project, 
such as a delegate reference to a non-existing java class or a non-existing Spring bean.

Find a list of the consistency checks below.

We recommend to integrate the consistency check in your CI builds - you can't find these inconsistencies early enough.

We forked the [Camunda BPM examples](https://github.com/viadee/camunda-bpm-examples/) to demonstrate the easy integration of vPAV.

# Features

## Model-Code Inconsistencies and Checkers
Consistency checks are performed by individual modules called checkers, which search for certain types of inconsistencies. Currently, the following checkers are implemented: 

| Checker                                                                              | Summary                                                                  | Status       |
| ------------------------------------------------------------------------------------ | ----------------------------------------------------------------------   | ------------ |
|[JavaDelegateChecker](JavaDelegateChecker.md)                                         | Is the implementation (or Spring bean reference) available and usable?   | Done         |
|[DmnTaskChecker](DmnTaskChecker.md)                                                   | Is the implementation available?                                         | Done         |
|[EmbeddedGroovyScriptChecker](EmbeddedGroovyScriptChecker.md)                         | Is the implementation available and does it look like a script?          | Done         |
|[ProcessVariablesModelChecker](ProcessVariablesModelChecker.md)                       | Are process variables in the model provided in the code for all paths?   | Experimental |
|[ProcessVariablesNameConventionChecker](ProcessVariablesNameConventionChecker.md)     | Do process variables in the model fit into a desired regex pattern?      | Done         |
|[TaskNamingConventionChecker](TaskNamingConventionChecker.md)                         | Do task names in the model fit into a desired regex pattern?             | Done         |
|[VersioningChecker](VersioningChecker.md)                                             | Do java classes implementing tasks fit  a version scheme?             | Done         |
|[XorNamingConventionChecker](XorNamingConventionChecker.md)                           | Are XOR gateways ending with "?"                                         | Done         |
|[NoScriptChecker](NoScriptChecker.md)                                                 | Is there any script in the model?                                        | Done         |
|[ElementIdConventionChecker](ElementIdConventionChecker.md)                           | Do task ids in the model fit into a desired regex pattern?           | Done         |
|[TimerExpressionChecker](TimerExpressionChecker.md)                                   | Are time events following the ISO 8601 scheme?                                        | Done         |
|[NoExpressionChecker](NoExpressionChecker.md)                                   | Are expressions used against common best-practices?                                        | Done         |

All of these can be switched on or off as required. Implementing further checkers is rather simple.
### Configuration
The viadee Process Application Validator comes with a default ruleSet.xml which provides some basic rules. In order to customize the plugin, we recommend creating your own ruleSet.xml and store it in **"src/test/resources"**. 
This allows you to use your own set of rules for naming conventions or to de-/activate certain checkers.

### One set of rules to rule them all
Furthermore you can use the plugin to manage multiple projects. Just create a blank maven project with only the parentRuleSet.xml stored in **"src/main/resources"** and run this project as maven install (make sure to package as jar). In your child projects you have to add the dependency to the parent project and to vPAV.

```xml
<dependency>
	<groupId>de.viadee</groupId>
	<artifactId>parent_config</artifactId>
	<version>1.0.0-SNAPSHOT</version>
</dependency>

<dependency>
	<groupId>de.viadee</groupId>
	<artifactId>viadeeProcessApplicationValidator</artifactId>
	<version>...</version>
</dependency>
```

The parentRuleSet.xml will provide a basic set of rules for all projects that "inherit". Local sets of rules will override inherited rules in order to allow for customization.

Make sure that inheritance is activated in the ruleSet.xml of your project.
```xml 
<rule>
	<name>HasParentRuleSet</name>
	<state>true</state>
</rule>
```
### Exclusion of false positives
An ignore file can be created to exclude false positives. The file has to be named **".ignoreIssues"** and has to be stored in **"src/test/resources"**. 
Here, you can list IDs of the issues which should be ignored in the next validation run. This must be done line by line. Line comments are initiated with "#".

**Example**
```
# Comment 
8d04f2e77a7d282c521098ab947ac060
```
## Output

The result of the check is first of all a direct one: if at least one inconsistency is 
found on the ERROR level, it will break your build or count as a failed unit 
test which will break your build too.

Further, the consistency check will provide an XML version, a JSON version and
an visual version based on  [BPMN.io](https://bpmn.io/) of all errors and warnings found.

### Visual output
The header contains the name of the current model. Below the heading, you can select a different model of the project to be displayed.
You can zoom in and out with the mouse wheel and move the model by click and hold.
In the BPMN model, the elements with errors are highlighted. Error categories are indicated by color. 
An overlay specifies the number of errors found on an element. Details can be seen by clicking on the overlay.
All errors are laid out in a table below the model. Clicking on the _rulename opens_ the corresponding documentation.
Clicking on the _Element-Id_ or _invalid sequenzflow_ marks the corresponding element(s) in the model.

### Example
<a href="img/output.PNG?raw=true" target="_blank"><img src="img/output.PNG" 
alt="Example HTML-Output" width="791" height="985" border="5" /></a>

## Requirements
- Camunda BPM Engine 7.4.0 and above

## Installation/Usage
There are two ways of installation. We recommend to use the JUnit approach as follows.

### Maven
You can start the validation as a Maven plugin. Therefore, add the dependency to your POM:

```xml
<dependency>
  <groupId>de.viadee</groupId>
  <artifactId>viadeeProcessApplicationValidator</artifactId>
  <version>...</version>
  <scope>test</scope>
</dependency>
```

Then, use the following maven goal to start the validation.  
```java
de.viadee:viadeeProcessApplicationValidator:{version}:check
```
Please note: This approach is not useful, if you use Spring managed java delegates in your processes.

### JUnit
Configure a JUnit-4 Test to fire up your usual Spring context - esp. delegates referenced in the process, 
if you use Spring in your application or a simple test case otherwise to call the consistency check.

The recommended name for this class is ModelConsistencyTest, where you 
call the ProcessApplicationValidator by simply using code like the following:

```java
import de.viadee.bpm.vPAV.ProcessApplicationValidator;
...
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SpringTestConfig.class })
public class ModelConsistencyTest{
        
    @Autowired
    private ApplicationContext ctx;   
    
    @Test
    public void validateModel() {
        assertTrue("Model inconsistency found. Please check target folder for validation output",
                ProcessApplicationValidator.findModelErrors(ctx).isEmpty());
    }
}

```
Note, that the Validator receives the Spring context. Thereby, the validation can
check delegate Beans and their names.


#### Methods
The `ctx` parameter is optional. If **no** Spring context is used, jUnit can also be started without the context parameter.

- `findModelErrors(ctx)` finds all model inconsistencies with **ERROR** status.

- `findModelInconsistencies(ctx)` finds **all** model inconsistencies (Error, Warning, Info).


#### SpringTestConfig

In order to evaluate beans in a Spring environment, you should specify a config class for your JUnit test

```java

import ServiceTaskOneDelegate;
import ServiceTaskTwoDelegate;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringTestConfig {

    public SpringTestConfig() {
        MockitoAnnotations.initMocks(this);
    }

    @InjectMocks
    private ServiceTaskOneDelegate serviceTaskOneDelegate;

    @InjectMocks
    private ServiceTaskTwoDelegate serviceTaskTwoDelegate;

    @Bean
    public ServiceTaskOneDelegate serviceTaskOneDelegate() {
        return serviceTaskOneDelegate;
    }

    @Bean
    public ServiceTaskTwoDelegate serviceTaskTwoDelegate() {
        return serviceTaskTwoDelegate;
    }

}
```


#### Additionally required dependencies 

```xml
<dependency>
	<groupId>org.mockito</groupId>
	<artifactId>mockito-all</artifactId>
	<version>1.10.19</version>
	<scope>test</scope>
</dependency>

<dependency>	
	<groupId>org.springframework</groupId>
	<artifactId>spring-test</artifactId>
	<version>4.3.11.RELEASE</version>
</dependency>
		
<dependency>
	<groupId>org.springframework</groupId>
	<artifactId>spring-beans</artifactId>
	<version>4.3.11.RELEASE</version>
</dependency>

<dependency>
	<groupId>javax.servlet</groupId>
	<artifactId>javax.servlet-api</artifactId>
	<version>4.0.0</version>
	<scope>provided</scope>
</dependency>

<dependency>
	<groupId>junit</groupId>
	<artifactId>junit</artifactId>
	<version>4.12</version>
</dependency>
```

## Commitments
This library will remain under an open source licence indefinately.

We follow the [semantic versioning](http://semver.org) scheme (2.0.0).

In the sense of semantic versioning, the resulting XML and JSON outputs are the _only public API_ provided here. 
We will keep these as stable as possible, in order to enable users to analyse and integrate results into the toolsets of their choice.

## Cooperation
Feel free to report issues, questions, ideas or patches. We are looking forward to it.

## Resources
Status of the development branch: [![Build Status](https://travis-ci.org/viadee/vPAV.svg?branch=development)](https://travis-ci.org/viadee/vPAV)

## Licenses
All licenses of reused components can be found on the [maven site](http://rawgit.com/viadee/vPAV/master/docs/MavenSite/project-info.html)
</br> Additionally we use the following third-party dependencies, that are not distributed via maven:
- [BPMN.io](https://bpmn.io/license/) tool under the bpmn.io license. 
- [Bootstrap](https://github.com/twbs/bootstrap/blob/v4-dev/LICENSE) licensed under MIT
- [jQuery](https://jquery.org/license/) licensed under MIT
- [PopperJS](https://github.com/FezVrasta/popper.js/blob/master/LICENSE.md) licensed under MIT


**License (BSD4)** <br/>
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
 1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
 3. All advertising materials mentioning features or use of this software
    must display the following acknowledgement:
    This product includes software developed by the viadee Unternehmensberatung GmbH.
 4. Neither the name of the viadee Unternehmensberatung GmbH nor the
    names of its contributors may be used to endorse or promote products
    derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
