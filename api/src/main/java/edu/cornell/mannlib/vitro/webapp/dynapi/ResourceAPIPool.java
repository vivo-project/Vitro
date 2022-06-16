package edu.cornell.mannlib.vitro.webapp.dynapi;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.DefaultResourceAPI;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ResourceAPI;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ResourceAPIKey;
import edu.cornell.mannlib.vitro.webapp.dynapi.validator.ModelValidator;
import edu.cornell.mannlib.vitro.webapp.dynapi.validator.SHACLActionBeanValidator;
import edu.cornell.mannlib.vitro.webapp.dynapi.validator.SHACLBeanValidator;
import edu.cornell.mannlib.vitro.webapp.dynapi.validator.SHACLResourceAPIBeanValidator;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoader;
import org.apache.jena.rdf.model.Model;

import javax.servlet.ServletContext;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.FULL_UNION;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.TBOX_ASSERTIONS;

public class ResourceAPIPool extends VersionableAbstractPool<ResourceAPIKey, ResourceAPI, ResourceAPIPool> {

    private static ResourceAPIPool INSTANCE = new ResourceAPIPool();

    public static ResourceAPIPool getInstance() {
        return INSTANCE;
    }

    @Override
    public ResourceAPIPool getPool() {
        return getInstance();
    }

    @Override
    public ResourceAPI getDefault() {
        return new DefaultResourceAPI();
    }

    @Override
    public Class<ResourceAPI> getType() {
        return ResourceAPI.class;
    }

    @Override
    public ModelValidator getValidator(Model data, Model scheme) {
        return new SHACLResourceAPIBeanValidator(data, scheme);
    }
}
