package com.temenos.interaction.commands.odata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OEntityKey.KeyType;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmType;
import org.odata4j.producer.EntityQueryInfo;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.ODataProducer;

import com.temenos.interaction.core.MultivaluedMapImpl;
import com.temenos.interaction.core.command.InteractionCommand;
import com.temenos.interaction.core.command.InteractionContext;
import com.temenos.interaction.core.hypermedia.ResourceState;
import com.temenos.interaction.core.resource.EntityResource;

public class TestJUnitGETEntityCommand {

	class MyEdmType extends EdmType {
		public MyEdmType(String name) {
			super(name);
		}
		public boolean isSimple() { return false; }
	}

	@Test(expected = AssertionError.class)
	public void testEntitySetNotFound() {
		ODataProducer mockProducer = createMockODataProducer("MyEntity", "Edm.String");
		GETEntityCommand gec = new GETEntityCommand(mockProducer);
		
		// test assertion for entity set not found
        InteractionContext ctx = createInteractionContext("DOESNOTMATCH", "1");
		InteractionCommand.Result result = gec.execute(ctx);
		assertEquals(InteractionCommand.Result.SUCCESS, result);
	}

	@Test
	public void testEntityKeyTypeString() {
		ODataProducer mockProducer = createMockODataProducer("MyEntity", "Edm.String");
		GETEntityCommand gec = new GETEntityCommand(mockProducer);
		
		// test our method
        InteractionContext ctx = createInteractionContext("MyEntity", "1");
		InteractionCommand.Result result = gec.execute(ctx);
		assertEquals(InteractionCommand.Result.SUCCESS, result);
		assertTrue(ctx.getResource() instanceof EntityResource);
		// check the key was constructed correctly
		class StringOEntityKey extends ArgumentMatcher<OEntityKey> {
		      public boolean matches(Object obj) {
		    	  OEntityKey ek = (OEntityKey) obj;
		    	  assertNotNull(ek);
		    	  assertEquals(KeyType.SINGLE, ek.getKeyType());
		    	  assertEquals("1", (String) ek.asSingleValue());
		          return true;
		      }
		   }
		verify(mockProducer).getEntity(eq("MyEntity"), argThat(new StringOEntityKey()), isNull(EntityQueryInfo.class));
	}

	@Test
	public void testEntityKeyInt64() {
		ODataProducer mockProducer = createMockODataProducer("MyEntity", "Edm.Int64");
		GETEntityCommand gec = new GETEntityCommand(mockProducer);
		
		// test our method
        InteractionContext ctx = createInteractionContext("MyEntity", "1");
		InteractionCommand.Result result = gec.execute(ctx);
		assertEquals(InteractionCommand.Result.SUCCESS, result);
		assertTrue(ctx.getResource() instanceof EntityResource);
		// check the key was constructed correctly
		class Int64OEntityKey extends ArgumentMatcher<OEntityKey> {
		      public boolean matches(Object obj) {
		    	  OEntityKey ek = (OEntityKey) obj;
		    	  assertNotNull(ek);
		    	  assertEquals(KeyType.SINGLE, ek.getKeyType());
		    	  assertEquals(new Long(1), (Long) ek.asSingleValue());
		          return true;
		      }
		   }
		verify(mockProducer).getEntity(eq("MyEntity"), argThat(new Int64OEntityKey()), isNull(EntityQueryInfo.class));
	}

	@Test
	public void testEntityKeyInt64Error() {
		ODataProducer mockProducer = createMockODataProducer("MyEntity", "Edm.Int64");
		GETEntityCommand gec = new GETEntityCommand(mockProducer);
		
		// test our method
        InteractionContext ctx = createInteractionContext("MyEntity", "A1");
		InteractionCommand.Result result = gec.execute(ctx);
		// command should return FAILURE
		assertEquals(InteractionCommand.Result.FAILURE, result);
		assertTrue(ctx.getResource() == null);
	}
	
	@Test
	public void testEntityKeyDateTime() {
		ODataProducer mockProducer = createMockODataProducer("MyEntity", "Edm.DateTime");
		GETEntityCommand gec = new GETEntityCommand(mockProducer);
		
		// test our method
        InteractionContext ctx = createInteractionContext("MyEntity", "datetime'2012-02-06T18:05:53'");
		InteractionCommand.Result result = gec.execute(ctx);
		assertEquals(InteractionCommand.Result.SUCCESS, result);
		assertTrue(ctx.getResource() instanceof EntityResource);
		// check the key was constructed correctly
		class TimestampOEntityKey extends ArgumentMatcher<OEntityKey> {
		      public boolean matches(Object obj) {
		    	  OEntityKey ek = (OEntityKey) obj;
		    	  assertNotNull(ek);
		    	  assertEquals(KeyType.SINGLE, ek.getKeyType());
		    	  assertEquals(new LocalDateTime(1328551553000L), (LocalDateTime) ek.asSingleValue());
		          return true;
		      }
		   }
		verify(mockProducer).getEntity(eq("MyEntity"), argThat(new TimestampOEntityKey()), isNull(EntityQueryInfo.class));
	}

	@Test
	public void testEntityKeyTime() {
		ODataProducer mockProducer = createMockODataProducer("MyEntity", "Edm.Time");
		GETEntityCommand gec = new GETEntityCommand(mockProducer);
		
		// test our method
        InteractionContext ctx = createInteractionContext("MyEntity", "time'PT18H05M53S'");
		InteractionCommand.Result result = gec.execute(ctx);
		assertEquals(InteractionCommand.Result.SUCCESS, result);
		assertTrue(ctx.getResource() instanceof EntityResource);
		// check the key was constructed correctly
		class DateOEntityKey extends ArgumentMatcher<OEntityKey> {
		      public boolean matches(Object obj) {
		    	  OEntityKey ek = (OEntityKey) obj;
		    	  assertNotNull(ek);
		    	  assertEquals(KeyType.SINGLE, ek.getKeyType());
		    	  assertEquals(new LocalTime(1332954353000L), (LocalTime) ek.asSingleValue());
		          return true;
		      }
		   }
		verify(mockProducer).getEntity(eq("MyEntity"), argThat(new DateOEntityKey()), isNull(EntityQueryInfo.class));
	}

	private ODataProducer createMockODataProducer(String entityName, String keyTypeName) {
		ODataProducer mockProducer = mock(ODataProducer.class);
		List<String> keys = new ArrayList<String>();
		keys.add("MyId");
		List<EdmProperty.Builder> properties = new ArrayList<EdmProperty.Builder>();
		EdmProperty.Builder ep = EdmProperty.newBuilder("MyId").setType(new MyEdmType(keyTypeName));
		properties.add(ep);
		EdmEntityType.Builder eet = EdmEntityType.newBuilder().setNamespace("MyNamespace").setAlias("MyAlias").setName(entityName).addKeys(keys).addProperties(properties);
		EdmEntitySet.Builder ees = EdmEntitySet.newBuilder().setName(entityName).setEntityType(eet);

		List<EdmEntityType> mockEntityTypes = new ArrayList<EdmEntityType>();
		mockEntityTypes.add(eet.build());

		EdmDataServices mockEDS = mock(EdmDataServices.class);
		when(mockEDS.getEdmEntitySet(anyString())).thenReturn(ees.build());
		when(mockEDS.getEntityTypes()).thenReturn(mockEntityTypes);
		when(mockProducer.getMetadata()).thenReturn(mockEDS);

		EntityResponse mockEntityResponse = mock(EntityResponse.class);
		when(mockEntityResponse.getEntity()).thenReturn(mock(OEntity.class));
		when(mockProducer.getEntity(anyString(), any(OEntityKey.class), any(EntityQueryInfo.class))).thenReturn(mockEntityResponse);
				        
        return mockProducer;
	}

	@SuppressWarnings("unchecked")
	private InteractionContext createInteractionContext(String entity, String id) {
		ResourceState resourceState = mock(ResourceState.class);
		when(resourceState.getEntityName()).thenReturn(entity);
		MultivaluedMap<String, String> pathParams = new MultivaluedMapImpl<String>();
		pathParams.add("id", id);
        InteractionContext ctx = new InteractionContext(pathParams, mock(MultivaluedMap.class), resourceState);
        return ctx;
	}

}
