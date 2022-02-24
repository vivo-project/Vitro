package edu.cornell.mannlib.vitro.webapp.dynapi;

import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationImpl;
import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.SolrQuery;
import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.BaseSearchQuery;
import edu.cornell.mannlib.vitro.webapp.searchengine.solr.SolrSearchEngine;
import org.easymock.Mock;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import stubs.javax.servlet.http.HttpServletRequestStub;

@PowerMockIgnore("javax.management.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest(ApplicationUtils.class)
public class SolrQueryTest {

    public static SearchEngine mockSearchEngine;

    private SolrQuery solrQuery;

    @Mock
    private Parameter parameter1;


    @BeforeClass
    public static void setupSolrMock(){

    }

    @Before
    public void setupObjects() {
        mockSearchEngine = createMock(SolrSearchEngine.class);

        Application application =  createMock(ApplicationImpl.class);
        expect(application.getSearchEngine()).andReturn(mockSearchEngine).anyTimes();
        replay(application);

        mockStatic(ApplicationUtils.class);
        expect(ApplicationUtils.instance()).andReturn(application).anyTimes();
        replay(ApplicationUtils.class);

        this.solrQuery = new SolrQuery();
    }

    @Test
    public void requiredParameterMissingFromInput(){
        parameter1 = createMock(Parameter.class);
        expect(parameter1.getName()).andReturn("testParameter").times(1);
        replay(parameter1);
        solrQuery.addRequiredParameter(parameter1);
        assertTrue(solrQuery.run(new OperationData(new HttpServletRequestStub())).hasError());
        verify(parameter1);
    }

    @Test
    public void requiredParameterPresentButInvalid(){
        parameter1 = createMock(Parameter.class);
        expect(parameter1.getName()).andReturn("testParameter").times(1);
        expect(parameter1.isValid("testParameter", new String[]{"testValue"}))
            .andReturn(false).times(1);
        replay(parameter1);
        solrQuery.addRequiredParameter(parameter1);
        HttpServletRequestStub httpReq = new HttpServletRequestStub();
        httpReq.addParameter("testParameter","testValue");
        assertTrue(solrQuery.run(new OperationData(httpReq)).hasError());
        verify(parameter1);
    }

    //@Test
    public void parsingQueryTest(){
        expect(mockSearchEngine.createQuery()).andReturn(new BaseSearchQuery()).times(1);
        replay(mockSearchEngine);

        solrQuery.setQueryText("{query:\"http,test\", fluter: [\"prvo:true\",\"drugo:false\"]}");
        solrQuery.run(new OperationData(new HttpServletRequestStub()));
        verify(mockSearchEngine);
    }
}
