/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrml;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.ModuleException;
import org.openmrs.util.OpenmrsUtil;

/**
 * This class contains the logic that is run every time this module is either started or shutdown
 */
public class MLinKenyaEMRActivator extends BaseModuleActivator {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	public static final String DEFAULT_ML_DIRECTORY_NAME = "kenyaemrML";
	
	/**
	 * @see #started()
	 */
	public void started() {
		
		createMachineLearningDirectory();
		log.info("Started ML in KenyaEMR");
	}
	
	/**
	 * @see #shutdown()
	 */
	public void shutdown() {
		log.info("Shutdown ML in KenyaEMR");
	}
	
	/**
	 * Creates a directory to hold machine learning models if none exists. The default directory
	 * name is kenyaemrML By default, the directory is created in the OpenMRS app directory If the
	 * default value is edited, and a full path provided, the directory is created as per user
	 * specification
	 */
	public void createMachineLearningDirectory() {
		
		// try to load the repository folder straight away.
		File folder = new File(DEFAULT_ML_DIRECTORY_NAME);
		
		// if the property wasn't a full path already, assume it was intended to be a folder in the
		// application directory
		if (!folder.exists()) {
			folder = new File(OpenmrsUtil.getApplicationDataDirectory(), DEFAULT_ML_DIRECTORY_NAME);
		}
		
		// now create the modules folder if it doesn't exist
		if (!folder.exists()) {
			log.warn("KenyaEMR ML directory " + folder.getAbsolutePath() + " doesn't exist.  Creating it now.");
			folder.mkdirs();
		}
		
		if (!folder.isDirectory()) {
			throw new ModuleException("KenyaEMR ML is not a directory at: " + folder.getAbsolutePath());
		}
	}
}
