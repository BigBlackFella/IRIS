package com.temenos.interaction.sdk;

/*
 * #%L
 * interaction-sdk-test
 * %%
 * Copyright (C) 2012 - 2013 Temenos Holdings N.V.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */


import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class InteractionSDKTest {

    public static final File ROOT = new File("target/test-classes/");

    public static final String ARCHETYPE_GROUP_ID = "com.temenos.interaction";
    public static final String ARCHETYPE_ARTEFACT_ID = "interaction-sdk-archetype";

    public static final String TEST_GROUP_ID = "com.mycorp.airtraffic";
    
    public static final String ARTIFACT_ID_FLIGHT_RESPONDER = "FlightResponder";
    public static final String ARTIFACT_ID_FLIGHT_RESPONDER_NON_STRICT_ODATA = "FlightResponderNonStrictOData";
    
    private Verifier verifier;
    private String localRepo;

    @Before
    public void setUp() throws VerificationException, IOException {
	/* Local Repo issues:
	 * 
	 * The Verifier tests the SDK by invoking Maven. This is a separate process from
	 * the maven that is building and running the test in the first place. Certain settings,
	 * however, need to be consistent between the build maven and this test maven, notably
	 * the local repository directory, which if not specified will default to being in the
	 * user's settings directory.
	 * 
	 * The pom file for this project sets the repo directory into the localRepo environment
	 * variable, so it can be extracted here and set on the Verifier.
	 *
	 * The code here at some points adds the "localRepo" system property as well as the 
	 * localRepository Verifier property, just in case this is behaving recursively in some
	 * way (which it probably isn't)
	 */
	
	localRepo = System.getProperty("localRepo");
	File localRepoDir = new File(localRepo);
	if ( localRepoDir.isDirectory() ) {
	    System.out.println("Local Repo: " + localRepo);
	} else {
	    System.err.println("Invalid Local Repo:" + localRepo);
	    Assert.fail("Invalid Local Repo:" + localRepo);
	}

        verifier = new Verifier(ROOT.getAbsolutePath());
	verifier.setLocalRepo(localRepo);
    }

    private Properties getSystemProperties(String artifactId) {
        Properties props = new Properties(System.getProperties());
        props.put("archetypeGroupId", ARCHETYPE_GROUP_ID);
        props.put("archetypeArtifactId", ARCHETYPE_ARTEFACT_ID);
        props.put("archetypeVersion", System.getProperty("archetypeVersion"));
        props.put("groupId", TEST_GROUP_ID);
        props.put("artifactId", artifactId);
        props.put("version", System.getProperty("flightResponderVersion"));
        props.put("interactiveMode", "false");

        return props;
    }

    @Ignore  // jdk8 upgrade issue - java.lang.ClassNotFoundException: org.eclipse.xtext.serializer.ISerializationContext
    public void testCreateFlightResponder() throws VerificationException, IOException, InterruptedException {
    	String artifactId = ARTIFACT_ID_FLIGHT_RESPONDER;
		verifier.displayStreamBuffers();
        verifier.deleteArtifact(TEST_GROUP_ID, artifactId, System.getProperty("flightResponderVersion"), null);		//Remove archetype output artefacts
        verifier.deleteDirectory(artifactId);											//Remove the maven project

        createFlightResponder(artifactId, true);
    }

    @Ignore // jdk8 upgrade issue - java.lang.ClassNotFoundException: org.eclipse.xtext.serializer.ISerializationContext
    public void testCreateFlightResponderNonStrictOData() throws VerificationException, IOException, InterruptedException {
    	String artifactId = ARTIFACT_ID_FLIGHT_RESPONDER_NON_STRICT_ODATA;
		verifier.displayStreamBuffers();
        verifier.deleteArtifact(TEST_GROUP_ID, artifactId, System.getProperty("flightResponderVersion"), null);		//Remove archetype output artefacts
        verifier.deleteDirectory(artifactId);											//Remove the maven project

        createFlightResponder(artifactId, false);
    }
    
    private void createFlightResponder(String artifactId, boolean strictOdata) throws VerificationException, InterruptedException {
    	System.out.println("Creating project " + artifactId);
    	
        //Generate the archetype
        Verifier verifier = new Verifier(ROOT.getAbsolutePath());
	verifier.displayStreamBuffers();
        verifier.setSystemProperties(getSystemProperties(artifactId));
	verifier.setLocalRepo(localRepo);
        verifier.setAutoclean(false);
        verifier.executeGoal("archetype:generate");
        verifier.verifyErrorFreeLog();

        //Verify the archetype
        verifier = new Verifier(ROOT.getAbsolutePath() + "/" + artifactId);
        verifier.setAutoclean(true);
        // the RIM file has not been generated yet (see interaction-sdk:gen target)
        Properties props = new Properties();
        props.put("strictOdata", strictOdata ? "true" : "false");
        props.put("skipRIMGeneration", "true");
	props.put("localRepo", localRepo);
        verifier.setSystemProperties(props);
	verifier.setLocalRepo(localRepo);

        verifier.executeGoal("verify");
        verifier.verifyErrorFreeLog();
        
        //Run the Interaction SDK to generate the Flight responder project
        props.put("skipRIMGeneration", "false");
        verifier.setSystemProperties(props);
        verifier.executeGoal("interaction-sdk:gen");
        verifier.verifyErrorFreeLog();
        
        //Overwrite responder insert file
        try {
        	FileUtils.copyFileToDirectory( System.getProperty("insertFile"), ROOT.getAbsolutePath() + "/" + artifactId + "/src/main/resources/META-INF");
        }
        catch(IOException ioe) {
        	new VerificationException("Failed to copy INSERT file.");
        }

        // Verify the Flight responder project (this target builds the war)
        verifier.executeGoal("verify");
        verifier.verifyErrorFreeLog();
        System.out.println("Finished responder generate");
        
        // create a thread to run the integration tests
        Verifier airlineIntTests = new Verifier(new File("../interaction-examples/interaction-odata-airline").getAbsolutePath());
	airlineIntTests.setLocalRepo(localRepo);
        Helper helper = new Helper(airlineIntTests, artifactId);
        helper.start();

        // start the Flight responder jetty server
        startFlightResponder(verifier);

        // wait for the tests to finish
        helper.join();
        assertFalse(helper.hasFailed());
    }
    
    
    private class Helper extends Thread {
    	private Verifier verifier;
    	private boolean failed = false;
    	private String artifactId;
    	
    	Helper(Verifier verifier, String artifactId) {
    		this.verifier = verifier;
    		this.artifactId = artifactId;
    	}
    	public boolean hasFailed() {
    		return failed;
    	}
    	public void run() {
    		try {
    	        // wait a little while until started
    	        System.out.println("Waiting 10 seconds for server start..");
    	        Thread.sleep(10000);
    	        // run the integration tests
    	        verifier.displayStreamBuffers();
    	        Properties airlineProps = new Properties();
    	        airlineProps.put("TEST_ENDPOINT_URI", "http://localhost:8080/responder/" + artifactId + ".svc/");
    	        verifier.setSystemProperties(airlineProps);
    	        List<String> goals = new ArrayList<String>();
    	        goals.add("package");
    	        goals.add("failsafe:integration-test");
    	        goals.add("failsafe:verify");
    	        verifier.executeGoals(goals);
    	        verifier.verifyErrorFreeLog();
			} catch (InterruptedException e) {
				failed = true;
			} catch (VerificationException e) {
				e.printStackTrace();
				failed = true;
			}
            try {
				// stop the Flight responder jetty server
				stopFlightResponder(artifactId);
			} catch (VerificationException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
    	}
    }

	void stopFlightResponder(String artifactId) throws VerificationException {
        Verifier verifier = new Verifier(ROOT.getAbsolutePath() + "/" + artifactId);
	verifier.setLocalRepo(localRepo);
        verifier.displayStreamBuffers();
        verifier.setAutoclean(false);
//        Properties props = new Properties();
//        props.put("argLine", "-DstopPort=8005 -DstopKey=STOP");
//        verifier.setSystemProperties(props);
        verifier.executeGoal("jetty:stop");
	}
	
    void startFlightResponder(Verifier verifier) throws VerificationException {
    	verifier.setAutoclean(false);
        verifier.executeGoal("jetty:run");
    }

}
