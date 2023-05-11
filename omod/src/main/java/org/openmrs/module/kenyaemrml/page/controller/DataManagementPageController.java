package org.openmrs.module.kenyaemrml.page.controller;

import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrml.ModuleConstants;
import org.openmrs.module.kenyaui.KenyaUiUtils;
import org.openmrs.module.kenyaui.annotation.AppPage;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.util.PrivilegeConstants;

/**
 * Home pages controller
 */
@AppPage(ModuleConstants.APP_ML_PREDICTIONS)
public class DataManagementPageController {
	
	public void get(@SpringBean KenyaUiUtils kenyaUi, UiUtils ui, PageModel model) {
		// GET
	}
}
