package com.rabobank.statementprocessor.steps;

import java.util.HashSet;
import java.util.Set;

import org.springframework.batch.item.ItemProcessor;

import com.rabobank.statementprocessor.bo.Record;

public class RecordProcessor implements ItemProcessor<Record, Record> {
	
	private Set<String> transReferences = new HashSet<String>();

	@Override
	public Record process(final Record record) throws Exception {
		
		if(record.getStartBalance().add(record.getMutation()).compareTo(record.getEndBalance())!=0) {
			record.setErrorDesc("End Balance is wrong");
		}
		
		if(transReferences.contains(record.getReference())) {
			System.out.println("Duplicate Reference Id : "+ record.getAccountNumber());
			if(record.getErrorDesc() != null) {
				record.setErrorDesc(record.getErrorDesc()+"; "+"Duplicate ReferenceId");
			} else {
				record.setErrorDesc("Duplicate ReferenceId");
			}
        }
		transReferences.add(record.getReference());
		System.out.println(record.toString());
		
		if(record.getErrorDesc() != null)
			return record;
		else
			return null;
	}

}
