package uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderMethodName = "dxAddressCreationRequest")
public class DXAddressCreationRequest {

	private final String dxNumber;
	private final String dxExchange;

	@JsonCreator
	public DXAddressCreationRequest(

			@JsonProperty("dxNumber") String dxNumber,
			@JsonProperty("dxExchange") String dxExchange) {

		this.dxNumber = dxNumber;
		this.dxExchange = dxExchange;
	}
}