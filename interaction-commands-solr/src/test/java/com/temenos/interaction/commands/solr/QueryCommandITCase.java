package com.temenos.interaction.commands.solr;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.io.File;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.util.TestHarness;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.temenos.interaction.core.MultivaluedMapImpl;
import com.temenos.interaction.core.command.InteractionCommand;
import com.temenos.interaction.core.command.InteractionContext;
import com.temenos.interaction.core.command.InteractionCommand.Result;
import com.temenos.interaction.core.entity.Entity;
import com.temenos.interaction.core.entity.Metadata;
import com.temenos.interaction.core.hypermedia.ResourceState;
import com.temenos.interaction.core.resource.CollectionResource;


public class QueryCommandITCase {

    private SolrServer server;

    public String getSolrHome() {
    	return "solr";
    }
    
    public String getSchemaFile() {
        return "solr/universalsearch/conf/schema.xml";
    }

    public String getSolrConfigFile() {
        return "solr/universalsearch/conf/solrconfig.xml";
    }

    @Before
    public void setUp() throws Exception {
    	System.setProperty("solr.solr.home", getSolrHome());
    	SolrConfig solrConfig = TestHarness.createConfig(getSolrHome(), "universalsearch", getSolrConfigFile());
    	TestHarness h = new TestHarness( new File("./target/solr-test/data").getAbsolutePath(),
                solrConfig,
                getSchemaFile());
    	server = new EmbeddedSolrServer(h.getCoreContainer(), h.getCore().getName());
    	
    	initialiseTestData();
    }   
    
    @After
    public void tearDown() {
    	server.shutdown();
    }
    
    private void initialiseTestData() throws Exception {
    	server.add(create("1", "JOHN1", "John Jones", "123 Somewhere", "W1 IWSH", "UK"), 0);
    	server.add(create("2", "JOHN1", "John Jillian", "123 Somewhere", "W1 IWSH", "UK"), 0);
    	server.add(create("3", "JOHN1", "John Jake", "123 Somewhere", "W1 IWSH", "UK"), 0);
    	server.add(create("4", "JOHN1", "John Jones", "123 Somewhere", "W1 IWSH", "UK"), 0);
    	server.add(create("5", "JOHN1", "John Joker", "123 Somewhere", "W1 IWSH", "UK"), 0);
    	server.commit(true, true);
    }
    
    private SolrInputDocument create(String id, String mnemonic, String name, String address, String postcode, String country) {
    	SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", id, 1.0f);
        doc.addField("mnemonic", mnemonic, 1.0f);
        doc.addField("name", name, 1.0f);
        doc.addField("address", address, 1.0f);
        doc.addField("postcode", postcode, 1.0f);
        doc.addField("country", country, 1.0f);
    	return doc;
    }
    
	@SuppressWarnings("unchecked")
	@Test
	public void testSelect() {
		SelectCommand command = new SelectCommand(server);
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl<String>();
		queryParams.add("q", "John*");
		InteractionContext ctx = new InteractionContext(new MultivaluedMapImpl<String>(), queryParams, mock(ResourceState.class), mock(Metadata.class));
		InteractionCommand.Result result = command.execute(ctx);
		assertEquals(Result.SUCCESS, result);
		
		CollectionResource<Entity> cr = (CollectionResource<Entity>) ctx.getResource();
		assertEquals(5, cr.getEntities().size());
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testTerms() {
		TermsCommand command = new TermsCommand(server);
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl<String>();
		queryParams.add("q", "j");
		InteractionContext ctx = new InteractionContext(new MultivaluedMapImpl<String>(), queryParams, mock(ResourceState.class), mock(Metadata.class));
		InteractionCommand.Result result = command.execute(ctx);
		assertEquals(Result.SUCCESS, result);
		
		CollectionResource<Entity> cr = (CollectionResource<Entity>) ctx.getResource();
		assertEquals(5, cr.getEntities().size());
		
	}

}
