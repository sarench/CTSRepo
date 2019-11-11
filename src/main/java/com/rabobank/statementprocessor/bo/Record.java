package com.rabobank.statementprocessor.bo;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="record")
public class Record {

	private String reference;
	private String accountNumber;
	private String description;
	private BigDecimal startBalance;
	private BigDecimal mutation;
	private BigDecimal endBalance;
	private String errorDesc;

	public String getReference() {
		return reference;
	}

	@XmlAttribute
	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	 @XmlElement(name = "accountNumber")
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getDescription() {
		return description;
	}

	 @XmlElement(name = "description")
	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getStartBalance() {
		return startBalance;
	}

	 @XmlElement(name = "startBalance")
	public void setStartBalance(BigDecimal startBalance) {
		this.startBalance = startBalance;
	}

	 
	public BigDecimal getMutation() {
		return mutation;
	}

	 @XmlElement(name = "mutation")
	public void setMutation(BigDecimal mutation) {
		this.mutation = mutation;
	}

	public BigDecimal getEndBalance() {
		return endBalance;
	}

	 @XmlElement(name = "endBalance")
	public void setEndBalance(BigDecimal endBalance) {
		this.endBalance = endBalance;
	}

	public String getErrorDesc() {
		return errorDesc;
	}

	public void setErrorDesc(String errorDesc) {
		this.errorDesc = errorDesc;
	}

}
