package com.rabobank.StatementProcessor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import com.rabobank.statementprocessor.bo.Record;
import com.rabobank.statementprocessor.steps.RecordProcessor;

@RunWith(SpringRunner.class)
public class StatementProcessorApplicationTests {

	private Record record;
	private List<Record> records;

	private RecordProcessor recordProcessor = new RecordProcessor();

	@BeforeEach
	public void setUp() throws Exception {
		record = createMockRecord();
		records = createMockRecords();

	}

	private Record createMockRecord() {
		Record record = new Record();
		record.setReference("1234");
		record.setAccountNumber("987654");
		record.setStartBalance(new BigDecimal(10));
		record.setMutation(new BigDecimal(+1));
		record.setEndBalance(new BigDecimal(10));
		return record;
	}

	private List<Record> createMockRecords() {

		List<Record> records = new ArrayList<Record>();

		Record record1 = new Record();
		record1.setReference("1234");
		record1.setAccountNumber("987654");
		record1.setStartBalance(new BigDecimal(10));
		record1.setMutation(new BigDecimal(+1));
		record1.setEndBalance(new BigDecimal(10));

		records.add(record1);

		record1 = new Record();
		record1.setReference("1234");
		record1.setAccountNumber("345354");
		record1.setStartBalance(new BigDecimal(100));
		record1.setMutation(new BigDecimal(+5));
		record1.setEndBalance(new BigDecimal(105));
		
		records.add(record1);
		
		return records;
	}

	@Test
	public void testEndBalanceCheck() throws Exception {
		Record record = recordProcessor.process(this.record);
		Assert.assertNotNull(record);
		Assert.assertEquals("End Balance is wrong", record.getErrorDesc());
	}

	@Test
	public void testRefernceForUniqueCheck() throws Exception {
		Record outRecord = null;
		for (Record record : records) {
			outRecord = recordProcessor.process(record);
		}
		Assert.assertNotNull(outRecord);
		Assert.assertEquals("Duplicate ReferenceId", outRecord.getErrorDesc());
	}
}