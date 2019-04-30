package uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;

@Getter
public class DXAddressCreationRequest {

	@NotNull
	private final String dxNumber;
	@NotNull
	private final String dxExchange;

	@JsonIgnore
	private Boolean isDXRequestValid = false;

	@Builder(builderMethodName = "dxAddressCreationRequest")
	@JsonCreator
	public DXAddressCreationRequest(

			@JsonProperty("dxNumber") String dxNumber, @JsonProperty("dxExchange") String dxExchange) {

		this.dxNumber = dxNumber;
		this.dxExchange = dxExchange;
		this.isDXRequestValid = isDXRequestValid;
	}

	public String getDxNumber() {
		return dxNumber;
	}

	public String getDxExchange() {
		return dxExchange;
	}

	public void setIsDXRequestValid(Boolean isDXRequestValid) {
		this.isDXRequestValid = isDXRequestValid;
	}

	public Boolean getIsDXRequestValid() {
		return isDXRequestValid;
	}
}