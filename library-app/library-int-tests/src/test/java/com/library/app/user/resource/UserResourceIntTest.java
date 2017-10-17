package com.library.app.user.resource;

import java.net.URL;

import javax.ws.rs.core.Response;

import org.hamcrest.CoreMatchers;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.library.app.common.json.JsonReader;
import com.library.app.common.model.HTTPCode;
import com.library.app.commontests.user.UserForTestsRepository;
import com.library.app.commontests.user.UserTestUtils;
import com.library.app.commontests.utils.ArquillianTestUtils;
import com.library.app.commontests.utils.FileTestNameUtils;
import com.library.app.commontests.utils.IntTestUtils;
import com.library.app.commontests.utils.JsonTestUtils;
import com.library.app.commontests.utils.ResourceClient;
import com.library.app.commontests.utils.ResourceDefinitions;
import com.library.app.user.model.User;

@RunWith(Arquillian.class)
public class UserResourceIntTest {

	@ArquillianResource
	private URL deploymentUrl;

	private ResourceClient resourceClient;

	private static final String PATH_RESOURCE = ResourceDefinitions.USER.getResourceName();

	@Deployment
	public static WebArchive createDeployment() {
		return ArquillianTestUtils.createDeploymentArchive();
	}

	@Before
	public void initTestCase() {
		resourceClient = new ResourceClient(deploymentUrl);

		resourceClient.resourcePath("DB/").delete();
		resourceClient.resourcePath("DB/" + PATH_RESOURCE + "/admin").postWithContent("");
	}

	@Test
	@RunAsClient
	public void addValidCustomerAndFindIt() {
		final Long userId = addUserAndGetId("customerJohnDoe.json");

		findUserAndAssertResponseWithUser(userId, UserForTestsRepository.johnDoe());
	}

	@Test
	@RunAsClient
	public void addUserWithNullName() {
		addUserWithValidationError("customerWithNullName.json", "userErrorNullName.json");
	}

	@Test
	@RunAsClient
	public void addExistentUser() {
		addUserAndGetId("customerJohnDoe.json");
		addUserWithValidationError("customerJohnDoe.json", "userAlreadyExists.json");
	}

	@Test
	@RunAsClient
	public void updateValidCustomerAsAdmin() {
		final Long userId = addUserAndGetId("customerJohnDoe.json");
		findUserAndAssertResponseWithUser(userId, UserForTestsRepository.johnDoe());

		final Response response = resourceClient.resourcePath(PATH_RESOURCE + "/" + userId).putWithFile(
				FileTestNameUtils.getPathFileRequest(PATH_RESOURCE, "updateCustomerJohnDoeWithNewName.json"));
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.OK.getCode())));

		final User expectedUser = UserForTestsRepository.johnDoe();
		expectedUser.setName("New name");
		findUserAndAssertResponseWithUser(userId, expectedUser);
	}

	@Test
	@RunAsClient
	public void updateValidLoggedCustomerAsCustomer() {
		final Long userId = addUserAndGetId("customerJohnDoe.json");
		findUserAndAssertResponseWithUser(userId, UserForTestsRepository.johnDoe());

		final Response response = resourceClient.user(UserForTestsRepository.johnDoe())
				.resourcePath(PATH_RESOURCE + "/" + userId)
				.putWithFile(
						FileTestNameUtils.getPathFileRequest(PATH_RESOURCE, "updateCustomerJohnDoeWithNewName.json"));
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.OK.getCode())));

		resourceClient.user(UserForTestsRepository.admin());
		final User expectedUser = UserForTestsRepository.johnDoe();
		expectedUser.setName("New name");
		findUserAndAssertResponseWithUser(userId, expectedUser);
	}

	@Test
	@RunAsClient
	public void updateCustomerButNotTheLoggedCustomer() {
		final Long userId = addUserAndGetId("customerJohnDoe.json");
		findUserAndAssertResponseWithUser(userId, UserForTestsRepository.johnDoe());
		addUserAndGetId("customerMary.json");

		final Response response = resourceClient.user(UserForTestsRepository.mary())
				.resourcePath(PATH_RESOURCE + "/" + userId).putWithFile(
						FileTestNameUtils.getPathFileRequest(PATH_RESOURCE, "updateCustomerJohnDoeWithNewName.json"));
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.FORBIDDEN.getCode())));
	}

	@Test
	@RunAsClient
	public void updateValidCustomerTryingToChangeType() {
		final Long userId = addUserAndGetId("customerJohnDoe.json");
		findUserAndAssertResponseWithUser(userId, UserForTestsRepository.johnDoe());

		final Response response = resourceClient.resourcePath(PATH_RESOURCE + "/" + userId).putWithFile(
				FileTestNameUtils.getPathFileRequest(PATH_RESOURCE, "updateCustomerJohnDoeWithNewType.json"));
		Assert.assertThat(response.getStatus(),
				CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.INTERNAL_ERROR.getCode())));
	}

	@Test
	@RunAsClient
	public void updateValidCustomerTryingToChangePassword() {
		final Long userId = addUserAndGetId("customerJohnDoe.json");
		findUserAndAssertResponseWithUser(userId, UserForTestsRepository.johnDoe());

		Assert.assertThat(
				authenticate(UserForTestsRepository.johnDoe().getEmail(),
						UserForTestsRepository.johnDoe().getPassword()),
				CoreMatchers.is(CoreMatchers.equalTo(true)));
		Assert.assertThat(authenticate(UserForTestsRepository.johnDoe().getEmail(), "111111"),
				CoreMatchers.is(CoreMatchers.equalTo(false)));

		final Response response = resourceClient.user(UserForTestsRepository.johnDoe())
				.resourcePath(PATH_RESOURCE + "/" + userId)
				.putWithFile(
						FileTestNameUtils.getPathFileRequest(PATH_RESOURCE,
								"updateCustomerJohnDoeWithNewPassword.json"));
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.OK.getCode())));

		Assert.assertThat(
				authenticate(UserForTestsRepository.johnDoe().getEmail(),
						UserForTestsRepository.johnDoe().getPassword()),
				CoreMatchers.is(CoreMatchers.equalTo(true)));
		Assert.assertThat(authenticate(UserForTestsRepository.johnDoe().getEmail(), "111111"),
				CoreMatchers.is(CoreMatchers.equalTo(false)));
	}

	@Test
	@RunAsClient
	public void updateUserWithEmailBelongingToOtherUser() {
		final Long userId = addUserAndGetId("customerJohnDoe.json");
		addUserAndGetId("customerMary.json");

		final Response responseUpdate = resourceClient.user(UserForTestsRepository.admin())
				.resourcePath(PATH_RESOURCE + "/" + userId)
				.putWithFile(
						FileTestNameUtils.getPathFileRequest(PATH_RESOURCE, "customerMary.json"));
		Assert.assertThat(responseUpdate.getStatus(),
				CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.VALIDATION_ERROR.getCode())));
		assertJsonResponseWithFile(responseUpdate, "userAlreadyExists.json");
	}

	@Test
	@RunAsClient
	public void updateUserNotFound() {
		final Response response = resourceClient.user(UserForTestsRepository.admin())
				.resourcePath(PATH_RESOURCE + "/" + 999).putWithFile(
						FileTestNameUtils.getPathFileRequest(PATH_RESOURCE, "customerJohnDoe.json"));
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.NOT_FOUND.getCode())));
	}

	@Test
	@RunAsClient
	public void updatePasswordAsAdmin() {
		final Long userId = addUserAndGetId("customerJohnDoe.json");

		Assert.assertThat(
				authenticate(UserForTestsRepository.johnDoe().getEmail(),
						UserForTestsRepository.johnDoe().getPassword()),
				CoreMatchers.is(CoreMatchers.equalTo(true)));
		Assert.assertThat(authenticate(UserForTestsRepository.johnDoe().getEmail(), "111111"),
				CoreMatchers.is(CoreMatchers.equalTo(false)));

		final Response response = resourceClient.user(UserForTestsRepository.admin())
				.resourcePath(PATH_RESOURCE + "/" + userId + "/password")
				.putWithContent(UserTestUtils.getJsonWithPassword("111111"));
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.OK.getCode())));

		Assert.assertThat(
				authenticate(UserForTestsRepository.johnDoe().getEmail(),
						UserForTestsRepository.johnDoe().getPassword()),
				CoreMatchers.is(CoreMatchers.equalTo(false)));
		Assert.assertThat(authenticate(UserForTestsRepository.johnDoe().getEmail(), "111111"),
				CoreMatchers.is(CoreMatchers.equalTo(true)));
	}

	@Test
	@RunAsClient
	public void updatePasswordLoggedCustomerAsCustomer() {
		final Long userId = addUserAndGetId("customerJohnDoe.json");

		Assert.assertThat(
				authenticate(UserForTestsRepository.johnDoe().getEmail(),
						UserForTestsRepository.johnDoe().getPassword()),
				CoreMatchers.is(CoreMatchers.equalTo(true)));
		Assert.assertThat(authenticate(UserForTestsRepository.johnDoe().getEmail(), "111111"),
				CoreMatchers.is(CoreMatchers.equalTo(false)));

		final Response response = resourceClient.user(UserForTestsRepository.johnDoe())
				.resourcePath(PATH_RESOURCE + "/" + userId + "/password")
				.putWithContent(UserTestUtils.getJsonWithPassword("111111"));
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.OK.getCode())));

		Assert.assertThat(
				authenticate(UserForTestsRepository.johnDoe().getEmail(),
						UserForTestsRepository.johnDoe().getPassword()),
				CoreMatchers.is(CoreMatchers.equalTo(false)));
		Assert.assertThat(authenticate(UserForTestsRepository.johnDoe().getEmail(), "111111"),
				CoreMatchers.is(CoreMatchers.equalTo(true)));
	}

	@Test
	@RunAsClient
	public void updatePasswordButNotTheLoggedCustomer() {
		final Long userId = addUserAndGetId("customerJohnDoe.json");
		addUserAndGetId("customerMary.json");

		final Response response = resourceClient.user(UserForTestsRepository.mary())
				.resourcePath(PATH_RESOURCE + "/" + userId + "/password")
				.putWithContent(UserTestUtils.getJsonWithPassword("111111"));
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.FORBIDDEN.getCode())));
	}

	@Test
	@RunAsClient
	public void findUserByIdNotFound() {
		final Response response = resourceClient.user(UserForTestsRepository.admin())
				.resourcePath(PATH_RESOURCE + "/" + 999).get();
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.NOT_FOUND.getCode())));
	}

	@Test
	@RunAsClient
	public void findByFilterPaginatingAndOrderingDescendingByName() {
		resourceClient.resourcePath("DB/").delete();
		resourceClient.resourcePath("DB/" + PATH_RESOURCE).postWithContent("");
		resourceClient.user(UserForTestsRepository.admin());

		// first page
		Response response = resourceClient.resourcePath(PATH_RESOURCE + "?page=0&per_page=2&sort=-name").get();
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.OK.getCode())));
		assertResponseContainsTheUsers(response, 3, UserForTestsRepository.mary(), UserForTestsRepository.johnDoe());

		// second page
		response = resourceClient.resourcePath(PATH_RESOURCE + "?page=1&per_page=2&sort=-name").get();
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.OK.getCode())));
		assertResponseContainsTheUsers(response, 3, UserForTestsRepository.admin());
	}

	private void addUserWithValidationError(final String requestFileName, final String responseFileName) {
		final Response response = resourceClient.user(null).resourcePath(PATH_RESOURCE)
				.postWithFile(FileTestNameUtils.getPathFileRequest(PATH_RESOURCE, requestFileName));
		Assert.assertThat(response.getStatus(),
				CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.VALIDATION_ERROR.getCode())));
		assertJsonResponseWithFile(response, responseFileName);
	}

	private void assertResponseContainsTheUsers(final Response response, final int expectedTotalRecords,
			final User... expectedUsers) {

		final JsonArray usersList = IntTestUtils.assertJsonHasTheNumberOfElementsAndReturnTheEntries(response,
				expectedTotalRecords, expectedUsers.length);

		for (int i = 0; i < expectedUsers.length; i++) {
			final User expectedUser = expectedUsers[i];
			Assert.assertThat(usersList.get(i).getAsJsonObject().get("name").getAsString(),
					CoreMatchers.is(CoreMatchers.equalTo(expectedUser.getName())));
		}
	}

	private boolean authenticate(final String email, final String password) {
		final Response response = resourceClient.user(null).resourcePath(PATH_RESOURCE + "/authenticate")
				.postWithContent(UserTestUtils.getJsonWithEmailAndPassword(email, password));
		return response.getStatus() == HTTPCode.OK.getCode();
	}

	private Long addUserAndGetId(final String fileName) {
		resourceClient.user(null);
		return IntTestUtils.addElementWithFileAndGetId(resourceClient, PATH_RESOURCE, PATH_RESOURCE, fileName);
	}

	private void findUserAndAssertResponseWithUser(final Long userIdToBeFound, final User expectedUser) {
		resourceClient.user(UserForTestsRepository.admin());
		final String bodyResponse = IntTestUtils.findById(resourceClient, PATH_RESOURCE, userIdToBeFound);
		assertResponseWithUser(bodyResponse, expectedUser);
	}

	private void assertResponseWithUser(final String bodyResponse, final User expectedUser) {
		final JsonObject userJson = JsonReader.readAsJsonObject(bodyResponse);
		Assert.assertThat(userJson.get("id").getAsLong(), CoreMatchers.is(CoreMatchers.notNullValue()));
		Assert.assertThat(userJson.get("name").getAsString(),
				CoreMatchers.is(CoreMatchers.equalTo(expectedUser.getName())));
		Assert.assertThat(userJson.get("email").getAsString(),
				CoreMatchers.is(CoreMatchers.equalTo(expectedUser.getEmail())));
		Assert.assertThat(userJson.get("type").getAsString(),
				CoreMatchers.is(CoreMatchers.equalTo(expectedUser.getUserType().toString())));
		Assert.assertThat(userJson.get("createdAt").getAsString(), CoreMatchers.is(CoreMatchers.notNullValue()));

		final JsonArray roles = userJson.getAsJsonArray("roles");
		Assert.assertThat(roles.size(), CoreMatchers.is(CoreMatchers.equalTo(expectedUser.getRoles().size())));
		for (int i = 0; i < roles.size(); i++) {
			final String actualRole = roles.get(i).getAsJsonPrimitive().getAsString();
			final String expectedRole = expectedUser.getRoles().get(i).toString();
			Assert.assertThat(actualRole, CoreMatchers.is(CoreMatchers.equalTo(expectedRole)));
		}
	}

	private void assertJsonResponseWithFile(final Response response, final String fileName) {
		JsonTestUtils.assertJsonMatchesFileContent(response.readEntity(String.class),
				FileTestNameUtils.getPathFileResponse(PATH_RESOURCE, fileName));
	}

}