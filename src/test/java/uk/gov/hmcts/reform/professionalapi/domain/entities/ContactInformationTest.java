package uk.gov.hmcts.reform.professionalapi.domain.entities;

import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.meanbean.factories.FactoryCollection;
import org.meanbean.test.BeanTester;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Java6Assertions.assertThat;
import org.meanbean.lang.Factory;

public class ContactInformationTest extends AbstractEntityTest{

	@Test
	public void creates_contact_information_correctly() {

		Organisation organisation = mock(Organisation.class);

		ContactInformation contactInformation = new ContactInformation("some-address1", "some-address2",
				"some-address3", "some-town-city", "some-county", "some-country", "some-post-code", organisation);

		assertThat(contactInformation.getAddressLine1()).isEqualTo("some-address1");
		assertThat(contactInformation.getAddressLine2()).isEqualTo("some-address2");
		assertThat(contactInformation.getAddressLine3()).isEqualTo("some-address3");
		assertThat(contactInformation.getTownCity()).isEqualTo("some-town-city");
		assertThat(contactInformation.getCounty()).isEqualTo("some-county");
		assertThat(contactInformation.getCountry()).isEqualTo("some-country");
		assertThat(contactInformation.getPostCode()).isEqualTo("some-post-code");
		assertThat(contactInformation.getOrganisation()).isEqualTo(organisation);
		assertThat(contactInformation.getId()).isNull();
	}

	@Test
	public void adds_dx_address_correctly() {

		DXAddress dxAddress = mock(DXAddress.class);

		Organisation organisation = mock(Organisation.class);

		ContactInformation contactInformation = new ContactInformation("some-address1", "some-address2",
				"some-address3", "some-town-city", "some-county", "some-country", "some-post-code", organisation);

		contactInformation.addDXAddress(dxAddress);

		assertThat(contactInformation.getDxAddresses()).containsExactly(dxAddress);
	}


	@Override
	protected ContactInformation getBeanInstance() {
		//DXAddress dxAddress = mock(DXAddress.class);

		//Organisation organisation = mock(Organisation.class);
		return new ContactInformation();//"some-address1", "some-address2",
				//"some-address3", "some-town-city", "some-county", "some-country", "some-post-code", organisation);

	}

}
