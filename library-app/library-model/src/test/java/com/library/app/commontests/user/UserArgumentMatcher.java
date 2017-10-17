package com.library.app.commontests.user;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import com.library.app.user.model.User;

public class UserArgumentMatcher extends ArgumentMatcher<User> {
	private User expectedUser;

	public static User userEq(final User expectedUser) {
		return Mockito.argThat(new UserArgumentMatcher(expectedUser));
	}

	public UserArgumentMatcher(final User expectedUser) {
		this.expectedUser = expectedUser;
	}

	@Override
	public boolean matches(final Object argument) {
		final User actualUser = (User) argument;

		Assert.assertThat(actualUser.getId(), CoreMatchers.is(CoreMatchers.equalTo(expectedUser.getId())));
		Assert.assertThat(actualUser.getName(), CoreMatchers.is(CoreMatchers.equalTo(expectedUser.getName())));
		Assert.assertThat(actualUser.getEmail(), CoreMatchers.is(CoreMatchers.equalTo(expectedUser.getEmail())));
		Assert.assertThat(actualUser.getPassword(), CoreMatchers.is(CoreMatchers.equalTo(expectedUser.getPassword())));

		return true;
	}

}
