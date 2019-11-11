package com.rabobank.statementprocessor.steps;

import java.io.File;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

public class ReportDeleteTasklet implements Tasklet {

	private String reportFile;

	public ReportDeleteTasklet(String reportFile) {
		this.reportFile = reportFile;
	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		try {
			File fileToDelete = new File(reportFile);
			if (fileToDelete.exists()) {
				System.out.println(fileToDelete.delete());
				System.out.println("File deletion successful : " + reportFile);
			} else {
				System.out.println("File deletion failed : " + reportFile);
			}
		} catch (Exception e) {
			System.out.println("Exception msg: " + e.getMessage() + reportFile);
		}
		return RepeatStatus.FINISHED;
	}

}
