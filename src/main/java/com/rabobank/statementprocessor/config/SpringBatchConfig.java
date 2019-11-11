package com.rabobank.statementprocessor.config;

import java.io.IOException;
import java.io.Writer;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import com.rabobank.statementprocessor.bo.Record;
import com.rabobank.statementprocessor.steps.RecordProcessor;
import com.rabobank.statementprocessor.steps.ReportDeleteTasklet;

@Configuration
@EnableBatchProcessing
public class SpringBatchConfig {

	private static final String PROPERTY_INPUT_CSV_FILE = "input.csv.file";
	private static final String PROPERTY_INPUT_XML_FILE = "input.xml.file";

	@Value("${report.file.header}")
	private String reportHeader;

	@Value("${report.file}")
	private String reportFile;

	// Job Defn
	@Bean
	public Job sequentialStepsJob(JobBuilderFactory jobBuilderFactory, Step xmlProcessingStep,
			Step csvProcessingStep, Step deleteReportStep) {
		return jobBuilderFactory.get("sequentialStepsJob")
				.incrementer(new RunIdIncrementer())
				.start(deleteReportStep)
				.next(xmlProcessingStep)
				.next(csvProcessingStep)
				.build();
	}
	
	@Bean
	public Job parallelStepsJob(JobBuilderFactory jobBuilderFactory, Step xmlProcessingStep,
			Step csvProcessingStep , Step deleteReportStep) {
		
		Flow deleteReportFlow = new FlowBuilder<Flow>("deleteReportFlow")
				.start(deleteReportStep)
				.build();
		
		Flow sceondFlow = new FlowBuilder<Flow>("sceondFlow")
				.start(csvProcessingStep)
				.build();
		
		Flow parallelFlow = new FlowBuilder<Flow>("parallelFlow")
				.start(xmlProcessingStep)
				.split(new SimpleAsyncTaskExecutor())
				.add(sceondFlow)
				.build();
		
		return jobBuilderFactory.get("parallelStepsJob")
				.start(deleteReportFlow)
				.next(parallelFlow)
				.end()
				.build();
	}

	// Step Defn
	
	@Bean
    protected Step deleteReportStep(StepBuilderFactory deleteReportStep) {
        return deleteReportStep
          .get("deleteReportStep")
          .tasklet(new ReportDeleteTasklet(reportFile))
          .build();
    }
	
	@Bean
	public Step xmlProcessingStep(ItemReader<Record> xmlReader, StepBuilderFactory stepBuilderFactory) {

		return stepBuilderFactory.get("xmlProcessingStep").<Record, Record>chunk(5)
				.reader(xmlReader)
				.processor(processor())
				.writer(writer())
				.build();
	}

	@Bean
	public Step csvProcessingStep(ItemReader<Record> csvReader, StepBuilderFactory stepBuilderFactory) {

		return stepBuilderFactory.get("csvProcessingStep").<Record, Record>chunk(5)
				.reader(csvReader)
				.processor(processor())
				.writer(writer())
				.build();
	}
	
	// Processor Defn
	@Bean
	public ItemProcessor<Record, Record> processor() {
		return new RecordProcessor();
	}

	// XML Reader Defn
	@Bean
	ItemReader<Record> xmlReader(Environment environment) {
		StaxEventItemReader<Record> xmlFileReader = new StaxEventItemReader<>();
		xmlFileReader.setResource((new FileSystemResource(environment.getRequiredProperty(PROPERTY_INPUT_XML_FILE))));
		xmlFileReader.setFragmentRootElementName("record");

		Jaxb2Marshaller unMarshaller = new Jaxb2Marshaller();
		unMarshaller.setClassesToBeBound(Record.class);

		xmlFileReader.setUnmarshaller(unMarshaller);
		return xmlFileReader;
	}

	// CSV Reader Defn

	@Bean
	public FlatFileItemReader<Record> csvReader(Environment environment) {
		FlatFileItemReader<Record> itemReader = new FlatFileItemReader<Record>();
		itemReader.setLineMapper(lineMapper());
		itemReader.setLinesToSkip(1);
		itemReader.setResource(new FileSystemResource(environment.getRequiredProperty(PROPERTY_INPUT_CSV_FILE)));
		return itemReader;
	}

	@Bean
	public LineMapper<Record> lineMapper() {
		DefaultLineMapper<Record> lineMapper = new DefaultLineMapper<Record>();
		DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
		lineTokenizer.setNames(new String[] { "Reference", "AccountNumber", "Description", "Start Balance", "Mutation",
				"End Balance" });
		BeanWrapperFieldSetMapper<Record> fieldSetMapper = new BeanWrapperFieldSetMapper<Record>();
		fieldSetMapper.setTargetType(Record.class);
		lineMapper.setLineTokenizer(lineTokenizer);
		lineMapper.setFieldSetMapper(fieldSetMapper);
		return lineMapper;
	}

	// CSV Writer Defn
	@Bean()
	@Scope(value = "prototype")
	public FlatFileItemWriter<Record> writer() {
		FlatFileItemWriter<Record> itemWriter = new FlatFileItemWriter<Record>();

		itemWriter.setHeaderCallback(new FlatFileHeaderCallback() {

			@Override
			public void writeHeader(Writer writer) throws IOException {
				writer.write(reportHeader);

			}
		});

		itemWriter.setResource(new FileSystemResource(reportFile));
		itemWriter.setAppendAllowed(true);
		itemWriter.setLineAggregator(new DelimitedLineAggregator<Record>() {
			{
				setDelimiter(",");
				setFieldExtractor(new BeanWrapperFieldExtractor<Record>() {
					{
						setNames(new String[] { "Reference", "ErrorDesc" });
					}
				});
			}
		});

		return itemWriter;
	}
}