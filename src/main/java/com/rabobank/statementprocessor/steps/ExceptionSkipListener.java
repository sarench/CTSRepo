package com.rabobank.statementprocessor.steps;

import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.annotation.OnSkipInProcess;
import org.springframework.batch.core.annotation.OnSkipInRead;
import org.springframework.batch.core.annotation.OnSkipInWrite;
import org.springframework.stereotype.Component;

import com.rabobank.statementprocessor.bo.Record;

@Component
public class ExceptionSkipListener implements SkipListener<Record, Record> {

	@Override
	public void onSkipInRead(Throwable t) {
		System.out.println("From onSkipInRead -> " + t.getMessage());

	}

	@Override
	public void onSkipInWrite(Record item, Throwable t) {
		System.out.println("From onSkipInWrite: " + item + " -> " + t.getMessage());

	}

	@Override
	public void onSkipInProcess(Record item, Throwable t) {
		System.out.println("From onSkipInProcess: " + item + " -> " + t.getMessage());

	}

}