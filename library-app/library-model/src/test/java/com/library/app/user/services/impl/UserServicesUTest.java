package com.library.app.user.services.impl;

import java.util.Arrays;

import javax.validation.Validation;
import javax.validation.Validator;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.library.app.common.exception.FieldNotValidException;
import com.library.app.common.model.PaginatedData;
import com.library.app.common.utils.PasswordUtils;
import com.library.app.commontests.user.UserArgumentMatcher;
import com.library.app.commontests.user.UserForTestsRepository;
import com.library.app.user.exception.UserExistentException;
import com.library.app.user.exception.UserNotFoundException;
import com.library.app.user.model.User;
import com.library.app.user.model.filter.UserFilter;
import com.library.app.user.repository.UserRepository;
import com.library.app.user.services.UserServices;

public class UserServicesUTest {

	private Validator validator;
	private UserServices userServices;

	@Mock
	private UserRepository userRepository;

	@Before
	public void initTestCase() {
		validator = Validation.buildDefaultValidatorFactory().getValidator();

		MockitoAnnotations.initMocks(this);

		userServices = new UserServicesImpl();
		((UserServicesImpl) userServices).userRepository = userRepository;
		((UserServicesImpl) userServices).validator = validator;
	}

	@Test
	public void addUserWithNullName() {
		final User user = UserForTestsRepository.johnDoe();
		user.setName(null);
		addUserWithInvalidField(user, "name");
	}

	@Test
	public void addUserWithShortName() {
		final User user = UserForTestsRepository.johnDoe();
		user.setName("Jo");
		addUserWithInvalidField(user, "name");
	}

	@Test
	public void addUserWithNullEmail() {
		final User user = UserForTestsRepository.johnDoe();
		user.setEmail(null);
		addUserWithInvalidField(user, "email");
	}

	@Test
	public void addUserWithInvalidEmail() {
		final User user = UserForTestsRepository.johnDoe();
		user.setEmail("invalidemail");
		addUserWithInvalidField(user, "email");
	}

	@Test
	public void addUserWithNullPassword() {
		final User user = UserForTestsRepository.johnDoe();
		user.setPassword(null);
		addUserWithInvalidField(user, "password");
	}

	@Test(expected = UserExistentException.class)
	public void addExistentUser() {
		Mockito.when(userRepository.alreadyExists(UserForTestsRepository.johnDoe()))
				.thenThrow(new UserExistentException());

		userServices.add(UserForTestsRepository.johnDoe());
	}

	@Test
	public void addValidUser() {
		Mockito.when(userRepository.alreadyExists(UserForTestsRepository.johnDoe())).thenReturn(false);
		Mockito.when(userRepository
				.add(UserArgumentMatcher.userEq(
						UserForTestsRepository.userWithEncryptedPassword(UserForTestsRepository.johnDoe()))))
				.thenReturn(UserForTestsRepository.userWithIdAndCreatedAt(UserForTestsRepository.johnDoe(), 1L));

		final User user = userServices.add(UserForTestsRepository.johnDoe());
		Assert.assertThat(user.getId(), CoreMatchers.is(CoreMatchers.equalTo(1L)));
	}

	@Test(expected = UserNotFoundException.class)
	public void findUserByIdNotFound() {
		Mockito.when(userRepository.findById(1L)).thenReturn(null);

		userServices.findById(1L);
	}

	@Test
	public void findUserById() {
		Mockito.when(userRepository.findById(1L))
				.thenReturn(UserForTestsRepository.userWithIdAndCreatedAt(UserForTestsRepository.johnDoe(), 1L));

		final User user = userServices.findById(1L);
		Assert.assertThat(user, CoreMatchers.is(CoreMatchers.notNullValue()));
		Assert.assertThat(user.getName(),
				CoreMatchers.is(CoreMatchers.equalTo(UserForTestsRepository.johnDoe().getName())));
	}

	@Test
	public void updateUserWithNullName() {
		Mockito.when(userRepository.findById(1L))
				.thenReturn(UserForTestsRepository.userWithIdAndCreatedAt(UserForTestsRepository.johnDoe(), 1L));

		final User user = UserForTestsRepository.userWithIdAndCreatedAt(UserForTestsRepository.johnDoe(), 1L);
		user.setName(null);

		try {
			userServices.update(user);
		} catch (final FieldNotValidException e) {
			Assert.assertThat(e.getFieldName(), CoreMatchers.is(CoreMatchers.equalTo("name")));
		}
	}

	@Test(expected = UserExistentException.class)
	public void updateUserExistent() throws Exception {
		Mockito.when(userRepository.findById(1L))
				.thenReturn(UserForTestsRepository.userWithIdAndCreatedAt(UserForTestsRepository.johnDoe(), 1L));

		final User user = UserForTestsRepository.userWithIdAndCreatedAt(UserForTestsRepository.johnDoe(), 1L);
		Mockito.when(userRepository.alreadyExists(user)).thenReturn(true);

		userServices.update(user);
	}

	@Test(expected = UserNotFoundException.class)
	public void updateUserNotFound() throws Exception {
		final User user = UserForTestsRepository.userWithIdAndCreatedAt(UserForTestsRepository.johnDoe(), 1L);
		Mockito.when(userRepository.findById(1L)).thenReturn(null);

		userServices.update(user);
	}

	@Test
	public void updateValidUser() throws Exception {
		final User user = UserForTestsRepository.userWithIdAndCreatedAt(UserForTestsRepository.johnDoe(), 1L);
		user.setPassword(null);
		Mockito.when(userRepository.findById(1L))
				.thenReturn(UserForTestsRepository.userWithIdAndCreatedAt(UserForTestsRepository.johnDoe(), 1L));

		userServices.update(user);

		final User expectedUser = UserForTestsRepository.userWithIdAndCreatedAt(UserForTestsRepository.johnDoe(), 1L);
		Mockito.verify(userRepository).update(UserArgumentMatcher.userEq(expectedUser));
	}

	@Test(expected = UserNotFoundException.class)
	public void updatePasswordUserNotFound() {
		Mockito.when(userRepository.findById(1L)).thenThrow(new UserNotFoundException());

		userServices.updatePassword(1L, "123456");
	}

	@Test
	public void updatePassword() throws Exception {
		final User user = UserForTestsRepository.userWithIdAndCreatedAt(UserForTestsRepository.johnDoe(), 1L);
		Mockito.when(userRepository.findById(1L)).thenReturn(user);

		userServices.updatePassword(1L, "654654");

		final User expectedUser = UserForTestsRepository.userWithIdAndCreatedAt(UserForTestsRepository.johnDoe(), 1L);
		expectedUser.setPassword(PasswordUtils.encryptPassword("654654"));
		Mockito.verify(userRepository).update(UserArgumentMatcher.userEq(expectedUser));
	}

	@Test(expected = UserNotFoundException.class)
	public void findUserByEmailNotFound() throws UserNotFoundException {
		Mockito.when(userRepository.findByEmail(UserForTestsRepository.johnDoe().getEmail())).thenReturn(null);

		userServices.findByEmail(UserForTestsRepository.johnDoe().getEmail());
	}

	@Test
	public void findUserByEmail() throws UserNotFoundException {
		Mockito.when(userRepository.findByEmail(UserForTestsRepository.johnDoe().getEmail()))
				.thenReturn(UserForTestsRepository.userWithIdAndCreatedAt(UserForTestsRepository.johnDoe(), 1L));

		final User user = userServices.findByEmail(UserForTestsRepository.johnDoe().getEmail());
		Assert.assertThat(user, CoreMatchers.is(CoreMatchers.notNullValue()));
		Assert.assertThat(user.getName(),
				CoreMatchers.is(CoreMatchers.equalTo(UserForTestsRepository.johnDoe().getName())));
	}

	@Test(expected = UserNotFoundException.class)
	public void findUserByEmailAndPasswordNotFound() {
		final User user = UserForTestsRepository.johnDoe();
		Mockito.when(userRepository.findByEmail(user.getEmail())).thenReturn(null);

		userServices.findByEmailAndPassword(user.getEmail(), user.getPassword());
	}

	@Test(expected = UserNotFoundException.class)
	public void findUserByEmailAndPasswordWithInvalidPassword() throws UserNotFoundException {
		final User user = UserForTestsRepository.johnDoe();
		user.setPassword("1111");

		User userReturned = UserForTestsRepository.userWithIdAndCreatedAt(UserForTestsRepository.johnDoe(), 1L);
		userReturned = UserForTestsRepository.userWithEncryptedPassword(userReturned);

		Mockito.when(userRepository.findByEmail(user.getEmail())).thenReturn(userReturned);

		userServices.findByEmailAndPassword(user.getEmail(), user.getPassword());
	}

	@Test
	public void findUserByEmailAndPassword() throws UserNotFoundException {
		User user = UserForTestsRepository.johnDoe();

		User userReturned = UserForTestsRepository.userWithIdAndCreatedAt(UserForTestsRepository.johnDoe(), 1L);
		userReturned = UserForTestsRepository.userWithEncryptedPassword(userReturned);

		Mockito.when(userRepository.findByEmail(user.getEmail())).thenReturn(userReturned);

		user = userServices.findByEmailAndPassword(user.getEmail(), user.getPassword());
		Assert.assertThat(user, CoreMatchers.is(CoreMatchers.notNullValue()));
		Assert.assertThat(user.getName(),
				CoreMatchers.is(CoreMatchers.equalTo(UserForTestsRepository.johnDoe().getName())));
	}

	@Test
	public void findUserByFilter() {
		final PaginatedData<User> users = new PaginatedData<>(1,
				Arrays.asList(UserForTestsRepository.userWithIdAndCreatedAt(UserForTestsRepository.johnDoe(), 1L)));
		Mockito.when(userRepository.findByFilter((UserFilter) Mockito.anyObject())).thenReturn(users);

		final PaginatedData<User> usersReturned = userServices.findByFilter(new UserFilter());
		Assert.assertThat(usersReturned.getNumberOfRows(), CoreMatchers.is(CoreMatchers.equalTo(1)));
		Assert.assertThat(usersReturned.getRow(0).getName(),
				CoreMatchers.is(CoreMatchers.equalTo(UserForTestsRepository.johnDoe().getName())));
	}

	private void addUserWithInvalidField(final User user, final String expectedInvalidFieldName) {
		try {
			userServices.add(user);
			Assert.fail("An error should have been thrown");
		} catch (final FieldNotValidException e) {
			Assert.assertThat(e.getFieldName(), CoreMatchers.is(CoreMatchers.equalTo(expectedInvalidFieldName)));
		}
	}

}
