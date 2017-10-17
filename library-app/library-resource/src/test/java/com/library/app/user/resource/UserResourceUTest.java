package com.library.app.user.resource;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.library.app.common.exception.FieldNotValidException;
import com.library.app.common.model.HTTPCode;
import com.library.app.common.model.PaginatedData;
import com.library.app.commontests.user.UserArgumentMatcher;
import com.library.app.commontests.user.UserForTestsRepository;
import com.library.app.commontests.user.UserTestUtils;
import com.library.app.commontests.utils.FileTestNameUtils;
import com.library.app.commontests.utils.JsonTestUtils;
import com.library.app.commontests.utils.ResourceDefinitions;
import com.library.app.user.exception.UserExistentException;
import com.library.app.user.exception.UserNotFoundException;
import com.library.app.user.model.User;
import com.library.app.user.model.User.Roles;
import com.library.app.user.model.filter.UserFilter;
import com.library.app.user.services.UserServices;

public class UserResourceUTest {
	private UserResource userResource;

	@Mock
	private UserServices userServices;

	@Mock
	private UriInfo uriInfo;

	@Mock
	private SecurityContext securityContext;

	private static final String PATH_RESOURCE = ResourceDefinitions.USER.getResourceName();

	@Before
	public void initTestCase() {
		MockitoAnnotations.initMocks(this);

		userResource = new UserResource();

		userResource.userJsonConverter = new UserJsonConverter();
		userResource.userServices = userServices;
		userResource.uriInfo = uriInfo;
		userResource.securityContext = securityContext;
	}

	@Test
	public void addValidCustomer() {
		Mockito.when(userServices.add(UserArgumentMatcher.userEq(UserForTestsRepository.johnDoe())))
				.thenReturn(UserForTestsRepository.userWithIdAndCreatedAt(UserForTestsRepository.johnDoe(), 1L));

		final Response response = userResource
				.add(JsonTestUtils.readJsonFile(FileTestNameUtils.getPathFileRequest(PATH_RESOURCE,
						"customerJohnDoe.json")));
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.CREATED.getCode())));
		JsonTestUtils.assertJsonMatchesExpectedJson(response.getEntity().toString(), "{\"id\": 1}");
	}

	@Test
	public void addValidEmployee() {
		final Response response = userResource
				.add(JsonTestUtils
						.readJsonFile(FileTestNameUtils.getPathFileRequest(PATH_RESOURCE, "employeeAdmin.json")));
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.FORBIDDEN.getCode())));
	}

	@Test
	public void addExistentUser() {
		Mockito.when(userServices.add(UserArgumentMatcher.userEq(UserForTestsRepository.johnDoe())))
				.thenThrow(new UserExistentException());

		final Response response = userResource
				.add(JsonTestUtils.readJsonFile(FileTestNameUtils.getPathFileRequest(PATH_RESOURCE,
						"customerJohnDoe.json")));
		Assert.assertThat(response.getStatus(),
				CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.VALIDATION_ERROR.getCode())));
		assertJsonResponseWithFile(response, "userAlreadyExists.json");
	}

	@Test
	public void addUserWithNullName() {
		Mockito.when(userServices.add((User) Mockito.anyObject()))
				.thenThrow(new FieldNotValidException("name", "may not be null"));

		final Response response = userResource
				.add(JsonTestUtils.readJsonFile(
						FileTestNameUtils.getPathFileRequest(PATH_RESOURCE, "customerWithNullName.json")));
		Assert.assertThat(response.getStatus(),
				CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.VALIDATION_ERROR.getCode())));
		assertJsonResponseWithFile(response, "userErrorNullName.json");
	}

	@Test
	public void updateValidCustomer() {
		Mockito.when(securityContext.isUserInRole(Roles.ADMINISTRATOR.name())).thenReturn(true);

		final Response response = userResource.update(1L,
				JsonTestUtils.readJsonFile(
						FileTestNameUtils.getPathFileRequest(PATH_RESOURCE, "updateCustomerJohnDoe.json")));
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.OK.getCode())));
		Assert.assertThat(response.getEntity().toString(), CoreMatchers.is(CoreMatchers.equalTo("")));

		final User expectedUser = UserForTestsRepository.userWithIdAndCreatedAt(UserForTestsRepository.johnDoe(), 1L);
		expectedUser.setPassword(null);
		Mockito.verify(userServices).update(UserArgumentMatcher.userEq(expectedUser));
	}

	@Test
	public void updateValidCustomerLoggedAsCustomerToBeUpdated() {
		setUpPrincipalUser(UserForTestsRepository.userWithIdAndCreatedAt(UserForTestsRepository.johnDoe(), 1L));
		Mockito.when(securityContext.isUserInRole(Roles.ADMINISTRATOR.name())).thenReturn(false);

		final Response response = userResource.update(1L,
				JsonTestUtils.readJsonFile(
						FileTestNameUtils.getPathFileRequest(PATH_RESOURCE, "updateCustomerJohnDoe.json")));
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.OK.getCode())));
		Assert.assertThat(response.getEntity().toString(), CoreMatchers.is(CoreMatchers.equalTo("")));

		final User expectedUser = UserForTestsRepository.userWithIdAndCreatedAt(UserForTestsRepository.johnDoe(), 1L);
		expectedUser.setPassword(null);
		Mockito.verify(userServices).update(UserArgumentMatcher.userEq(expectedUser));
	}

	@Test
	public void updateValidCustomerLoggedAsOtherCustomer() {
		setUpPrincipalUser(UserForTestsRepository.userWithIdAndCreatedAt(UserForTestsRepository.mary(), 2L));
		Mockito.when(securityContext.isUserInRole(Roles.ADMINISTRATOR.name())).thenReturn(false);

		final Response response = userResource.update(1L,
				JsonTestUtils.readJsonFile(
						FileTestNameUtils.getPathFileRequest(PATH_RESOURCE, "updateCustomerJohnDoe.json")));
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.FORBIDDEN.getCode())));
	}

	@Test
	public void updateValidEmployee() {
		Mockito.when(securityContext.isUserInRole(Roles.ADMINISTRATOR.name())).thenReturn(true);

		final Response response = userResource.update(1L,
				JsonTestUtils
						.readJsonFile(FileTestNameUtils.getPathFileRequest(PATH_RESOURCE, "updateEmployeeAdmin.json")));
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.OK.getCode())));
		Assert.assertThat(response.getEntity().toString(), CoreMatchers.is(CoreMatchers.equalTo("")));

		final User expectedUser = UserForTestsRepository.userWithIdAndCreatedAt(UserForTestsRepository.admin(), 1L);
		expectedUser.setPassword(null);
		Mockito.verify(userServices).update(UserArgumentMatcher.userEq(expectedUser));
	}

	@Test
	public void updateUserWithEmailBelongingToOtherUser() {
		Mockito.when(securityContext.isUserInRole(Roles.ADMINISTRATOR.name())).thenReturn(true);

		Mockito.doThrow(new UserExistentException()).when(userServices)
				.update(UserForTestsRepository.userWithIdAndCreatedAt(UserForTestsRepository.johnDoe(), 1L));

		final Response response = userResource.update(1L,
				JsonTestUtils.readJsonFile(
						FileTestNameUtils.getPathFileRequest(PATH_RESOURCE, "updateCustomerJohnDoe.json")));
		Assert.assertThat(response.getStatus(),
				CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.VALIDATION_ERROR.getCode())));
		assertJsonResponseWithFile(response, "userAlreadyExists.json");
	}

	@Test
	public void updateUserWithNullName() {
		Mockito.when(securityContext.isUserInRole(Roles.ADMINISTRATOR.name())).thenReturn(true);

		Mockito.doThrow(new FieldNotValidException("name", "may not be null")).when(userServices).update(
				UserForTestsRepository.userWithIdAndCreatedAt(UserForTestsRepository.johnDoe(), 1L));

		final Response response = userResource.update(1L,
				JsonTestUtils.readJsonFile(
						FileTestNameUtils.getPathFileRequest(PATH_RESOURCE, "customerWithNullName.json")));
		Assert.assertThat(response.getStatus(),
				CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.VALIDATION_ERROR.getCode())));
		assertJsonResponseWithFile(response, "userErrorNullName.json");
	}

	@Test
	public void updateUserNotFound() {
		Mockito.when(securityContext.isUserInRole(Roles.ADMINISTRATOR.name())).thenReturn(true);
		Mockito.doThrow(new UserNotFoundException()).when(userServices)
				.update(UserForTestsRepository.userWithIdAndCreatedAt(UserForTestsRepository.johnDoe(), 2L));

		final Response response = userResource.update(2L,
				JsonTestUtils.readJsonFile(
						FileTestNameUtils.getPathFileRequest(PATH_RESOURCE, "updateCustomerJohnDoe.json")));
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.NOT_FOUND.getCode())));
	}

	@Test
	public void updateUserPassword() {
		Mockito.when(securityContext.isUserInRole(Roles.ADMINISTRATOR.name())).thenReturn(true);

		final Response response = userResource.updatePassword(1L, UserTestUtils.getJsonWithPassword("123456"));
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.OK.getCode())));
		Assert.assertThat(response.getEntity().toString(), CoreMatchers.is(CoreMatchers.equalTo("")));

		Mockito.verify(userServices).updatePassword(1L, "123456");
	}

	@Test
	public void updateCustomerPasswordLoggedAsCustomerToBeUpdated() {
		setUpPrincipalUser(UserForTestsRepository.userWithIdAndCreatedAt(UserForTestsRepository.johnDoe(), 1L));
		Mockito.when(securityContext.isUserInRole(Roles.ADMINISTRATOR.name())).thenReturn(false);

		final Response response = userResource.updatePassword(1L, UserTestUtils.getJsonWithPassword("123456"));
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.OK.getCode())));
		Assert.assertThat(response.getEntity().toString(), CoreMatchers.is(CoreMatchers.equalTo("")));

		Mockito.verify(userServices).updatePassword(1L, "123456");
	}

	@Test
	public void updateCustomerPasswordLoggedAsOtherCustomer() {
		setUpPrincipalUser(UserForTestsRepository.userWithIdAndCreatedAt(UserForTestsRepository.mary(), 2L));
		Mockito.when(securityContext.isUserInRole(Roles.ADMINISTRATOR.name())).thenReturn(false);

		final Response response = userResource.updatePassword(1L, UserTestUtils.getJsonWithPassword("123456"));
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.FORBIDDEN.getCode())));
	}

	@Test
	public void findCustomerById() {
		Mockito.when(userServices.findById(1L))
				.thenReturn(UserForTestsRepository.userWithIdAndCreatedAt(UserForTestsRepository.johnDoe(), 1L));

		final Response response = userResource.findById(1L);
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.OK.getCode())));
		assertJsonResponseWithFile(response, "customerJohnDoeFound.json");
	}

	@Test
	public void findUserByIdNotFound() {
		Mockito.when(userServices.findById(1L)).thenThrow(new UserNotFoundException());

		final Response response = userResource.findById(1L);
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.NOT_FOUND.getCode())));
	}

	@Test
	public void findEmployeeByEmailAndPassword() {
		Mockito.when(userServices.findByEmailAndPassword(UserForTestsRepository.admin().getEmail(),
				UserForTestsRepository.admin().getPassword())).thenReturn(
						UserForTestsRepository.userWithIdAndCreatedAt(UserForTestsRepository.admin(), 1L));

		final Response response = userResource
				.findByEmailAndPassword(
						UserTestUtils.getJsonWithEmailAndPassword(UserForTestsRepository.admin().getEmail(),
								UserForTestsRepository.admin()
										.getPassword()));
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.OK.getCode())));
		assertJsonResponseWithFile(response, "employeeAdminFound.json");
	}

	@Test
	public void findUserByEmailAndPasswordNotFound() {
		Mockito.when(userServices.findByEmailAndPassword(UserForTestsRepository.admin().getEmail(),
				UserForTestsRepository.admin().getPassword())).thenThrow(
						new UserNotFoundException());

		final Response response = userResource
				.findByEmailAndPassword(
						UserTestUtils.getJsonWithEmailAndPassword(UserForTestsRepository.admin().getEmail(),
								UserForTestsRepository.admin()
										.getPassword()));
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.NOT_FOUND.getCode())));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void findByFilterNoFilter() {
		final List<User> users = new ArrayList<>();
		final List<User> allUsers = UserForTestsRepository.allUsers();
		for (int i = 1; i <= allUsers.size(); i++) {
			users.add(UserForTestsRepository.userWithIdAndCreatedAt(UserForTestsRepository.allUsers().get(i - 1),
					new Long(i)));
		}

		final MultivaluedMap<String, String> multiMap = Mockito.mock(MultivaluedMap.class);
		Mockito.when(uriInfo.getQueryParameters()).thenReturn(multiMap);

		Mockito.when(userServices.findByFilter((UserFilter) Mockito.anyObject())).thenReturn(
				new PaginatedData<User>(users.size(), users));

		final Response response = userResource.findByFilter();
		Assert.assertThat(response.getStatus(), CoreMatchers.is(CoreMatchers.equalTo(HTTPCode.OK.getCode())));
		assertJsonResponseWithFile(response, "usersAllInOnePage.json");
	}

	private void setUpPrincipalUser(final User user) {
		final Principal principal = Mockito.mock(Principal.class);
		Mockito.when(principal.getName()).thenReturn(user.getEmail());

		Mockito.when(securityContext.getUserPrincipal()).thenReturn(principal);
		Mockito.when(userServices.findByEmail(user.getEmail())).thenReturn(user);
	}

	private void assertJsonResponseWithFile(final Response response, final String fileName) {
		JsonTestUtils.assertJsonMatchesFileContent(response.getEntity().toString(),
				FileTestNameUtils.getPathFileResponse(PATH_RESOURCE, fileName));
	}

}
