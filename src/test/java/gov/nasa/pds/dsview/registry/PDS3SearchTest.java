package gov.nasa.pds.dsview.registry;

import static org.junit.Assert.*;

import org.apache.solr.client.solrj.impl.HttpJdkSolrClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Regression tests for PDS3Search singleton HttpJdkSolrClient management.
 */
@RunWith(MockitoJUnitRunner.class)
public class PDS3SearchTest {

    private PDS3Search pds3Search;

    @Before
    public void setUp() {
        pds3Search = new PDS3Search("http://test-solr:8983/solr/data");
    }

    @After
    public void tearDown() {
        pds3Search.cleanup();
    }

    @Test
    public void testGetSolrClient_ReturnsSameInstance() throws Exception {
        HttpJdkSolrClient client1 = invokeGetSolrClient();
        HttpJdkSolrClient client2 = invokeGetSolrClient();
        assertSame("getSolrClient() must return the same instance on repeated calls", client1, client2);
    }

    @Test
    public void testCleanup_ReleasesClient() throws Exception {
        invokeGetSolrClient(); // initialize
        pds3Search.cleanup();
        AtomicReference<?> ref = getSolrClientField();
        assertNull("cleanup() must set the singleton reference to null", ref.get());
    }

    @Test
    public void testGetSolrClient_AfterCleanup_CreatesNewInstance() throws Exception {
        HttpJdkSolrClient first = invokeGetSolrClient();
        pds3Search.cleanup();
        HttpJdkSolrClient second = invokeGetSolrClient();
        assertNotNull(second);
        assertNotSame("After cleanup, getSolrClient() must return a new instance", first, second);
        pds3Search.cleanup();
    }

    private HttpJdkSolrClient invokeGetSolrClient() throws Exception {
        Method method = PDS3Search.class.getDeclaredMethod("getSolrClient");
        method.setAccessible(true);
        return (HttpJdkSolrClient) method.invoke(pds3Search);
    }

    @SuppressWarnings("unchecked")
    private AtomicReference<HttpJdkSolrClient> getSolrClientField() throws Exception {
        Field field = PDS3Search.class.getDeclaredField("solrClient");
        field.setAccessible(true);
        return (AtomicReference<HttpJdkSolrClient>) field.get(null);
    }
}
