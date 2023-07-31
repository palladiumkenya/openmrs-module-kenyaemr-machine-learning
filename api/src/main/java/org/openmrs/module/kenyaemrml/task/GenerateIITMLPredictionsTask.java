package org.openmrs.module.kenyaemrml.task;

import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrml.util.MLDataExchange;
import org.openmrs.scheduler.tasks.AbstractTask;

/**
 * this task Generates IIT ML Predictions For patients with new greencard encounters
 *
 */

public class GenerateIITMLPredictionsTask extends AbstractTask {

    @Override
    public void execute() {
        Context.openSession();
		// Run the generate function
        MLDataExchange mlDataExchange = new MLDataExchange();
		mlDataExchange.generateIITScoresTask();
		Context.closeSession();
    }
    
}
