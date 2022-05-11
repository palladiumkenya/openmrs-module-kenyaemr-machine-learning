package org.openmrs.module.kenyaemrml.metadata;

import org.openmrs.module.metadatadeploy.bundle.AbstractMetadataBundle;
import org.openmrs.module.metadatadeploy.bundle.Requires;
import org.springframework.stereotype.Component;

import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.idSet;
import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.privilege;
import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.role;

import org.openmrs.module.kenyaemrml.MLinKenyaEMRConfig;

/**
 * Implementation of access control to the app.
 */
@Component
@Requires(org.openmrs.module.kenyaemr.metadata.SecurityMetadata.class)
public class MLSeculityMetadata extends AbstractMetadataBundle {
    public static class _Privilege {
		
		public static final String APP_ML_ADMIN = "App: kenyaemrml.predictions";
	}
	
	public static final class _Role {
		
		public static final String APPLICATION_ML_ADMIN = "ML administration";
		
		public static final String APPLICATION_ML_PULL_RISK_SCORES = "ML pull risk scores from NDWH";
	}
	
	/**
	 * @see AbstractMetadataBundle#install()
	 */
	@Override
	public void install() {
		
		// install privileges
		install(privilege(_Privilege.APP_ML_ADMIN, "Able to view Machine Learning"));
		install(privilege(MLinKenyaEMRConfig.MODULE_PRIVILEGE, "Able to execute actions in Machine Learning"));

		// install roles
		install(role(_Role.APPLICATION_ML_ADMIN, "Can access ML app",
		    idSet(org.openmrs.module.kenyaemr.metadata.SecurityMetadata._Role.API_PRIVILEGES_VIEW_AND_EDIT),
		    idSet(_Privilege.APP_ML_ADMIN)));
        install(role(_Role.APPLICATION_ML_PULL_RISK_SCORES, "Can administer ML risk scores from NDWH",
		    idSet(org.openmrs.module.kenyaemr.metadata.SecurityMetadata._Role.API_PRIVILEGES_VIEW_AND_EDIT),
		    idSet(MLinKenyaEMRConfig.MODULE_PRIVILEGE)));
	}
}
