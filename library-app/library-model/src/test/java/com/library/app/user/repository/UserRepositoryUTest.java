package com.library.app.user.repository;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.library.app.common.model.PaginatedData;
import com.library.app.common.model.filter.PaginationData;
import com.library.app.common.model.filter.PaginationData.OrderMode;
import com.library.app.commontests.user.UserForTestsRepository;
import com.library.app.commontests.utils.TestBaseRepository;
import com.library.app.user.model.User;
import com.library.app.user.model.User.UserType;
import com.library.app.user.model.filter.UserFilter;

public class UserRepositoryUTest extends TestBaseRepository {

	private UserRepository userRepository;

	@Before
	public void initTestCase() {
		initializeTestDB();

		userRepository = new UserRepository();
		userRepository.em = em;
	}

	@After
	public void setDownTestCase() {
		closeEntityManager();
	}

	@Test
	public void addCustomerAndFindIt() {
		final Long userAddedId = dbCommandExecutor.executeCommand(() -> {
			return userRepository.add(UserForTestsRepository.johnDoe()).getId();
		});
		Assert.assertThat(userAddedId, CoreMatchers.is(CoreMatchers.notNullValue()));

		final User user = userRepository.findById(userAddedId);
		assertUser(user, UserForTestsRepository.johnDoe(), UserType.CUSTOMER);
	}

	@Test
	public void findUserByIdNotFound() {
		final User user = userRepository.findById(999L);
		Assert.assertThat(user, CoreMatchers.is(CoreMatchers.nullValue()));
	}

	@Test
	public void updateCustomer() {
		final Long userAddedId = dbCommandExecutor.executeCommand(() -> {
			return userRepository.add(UserForTestsRepository.johnDoe()).getId();
		});
		Assert.assertThat(userAddedId, CoreMatchers.is(CoreMatchers.notNullValue()));

		final User user = userRepository.findById(userAddedId);
		Assert.assertThat(user.getName(),
				CoreMatchers.is(CoreMatchers.equalTo(UserForTestsRepository.johnDoe().getName())));

		user.setName("New name");
		dbCommandExecutor.executeCommand(() -> {
			userRepository.update(user);
			return null;
		});

		final User userAfterUpdate = userRepository.findById(userAddedId);
		Assert.assertThat(userAfterUpdate.getName(), CoreMatchers.is(CoreMatchers.equalTo("New name")));
	}

	@Test
	public void alreadyExistsUserWithoutId() {
		dbCommandExecutor.executeCommand(() -> {
			return userRepository.add(UserForTestsRepository.johnDoe()).getId();
		});

		Assert.assertThat(userRepository.alreadyExists(UserForTestsRepository.johnDoe()),
				CoreMatchers.is(CoreMatchers.equalTo(true)));
		Assert.assertThat(userRepository.alreadyExists(UserForTestsRepository.admin()),
				CoreMatchers.is(CoreMatchers.equalTo(false)));
	}

	@Test
	public void alreadyExistsCategoryWithId() {
		final User customer = dbCommandExecutor.executeCommand(() -> {
			userRepository.add(UserForTestsRepository.admin());
			return userRepository.add(UserForTestsRepository.johnDoe());
		});

		Assert.assertFalse(userRepository.alreadyExists(customer));

		customer.setEmail(UserForTestsRepository.admin().getEmail());
		Assert.assertThat(userRepository.alreadyExists(customer), CoreMatchers.is(CoreMatchers.equalTo(true)));

		customer.setEmail("newemail@domain.com");
		Assert.assertThat(userRepository.alreadyExists(customer), CoreMatchers.is(CoreMatchers.equalTo(false)));
	}

	@Test
	public void findUserByEmail() {
		dbCommandExecutor.executeCommand(() -> {
			return userRepository.add(UserForTestsRepository.johnDoe());
		});

		final User user = userRepository.findByEmail(UserForTestsRepository.johnDoe().getEmail());
		this.assertUser(user, UserForTestsRepository.johnDoe(), UserType.CUSTOMER);
	}

	@Test
	public void findUserByEmailNotFound() {
		final User user = userRepository.findByEmail(UserForTestsRepository.johnDoe().getEmail());
		Assert.assertThat(user, CoreMatchers.is(CoreMatchers.nullValue()));
	}

	@Test
	public void findByFilterWithPagingOrderingByNameDescending() {
		loadDataForFindByFilter();

		UserFilter userFilter = new UserFilter();
		userFilter.setPaginationData(new PaginationData(0, 2, "name", OrderMode.DESCENDING));

		PaginatedData<User> result = userRepository.findByFilter(userFilter);
		Assert.assertThat(result.getNumberOfRows(), CoreMatchers.is(CoreMatchers.equalTo(3)));
		Assert.assertThat(result.getRows().size(), CoreMatchers.is(CoreMatchers.equalTo(2)));
		Assert.assertThat(result.getRow(0).getName(),
				CoreMatchers.is(CoreMatchers.equalTo(UserForTestsRepository.mary().getName())));
		Assert.assertThat(result.getRow(1).getName(),
				CoreMatchers.is(CoreMatchers.equalTo(UserForTestsRepository.johnDoe().getName())));

		userFilter = new UserFilter();
		userFilter.setPaginationData(new PaginationData(2, 2, "name", OrderMode.DESCENDING));

		result = userRepository.findByFilter(userFilter);
		Assert.assertThat(result.getNumberOfRows(), CoreMatchers.is(CoreMatchers.equalTo(3)));
		Assert.assertThat(result.getRows().size(), CoreMatchers.is(CoreMatchers.equalTo(1)));
		Assert.assertThat(result.getRow(0).getName(),
				CoreMatchers.is(CoreMatchers.equalTo(UserForTestsRepository.admin().getName())));
	}

	@Test
	public void findByFilterFilteringByName() {
		loadDataForFindByFilter();

		final UserFilter userFilter = new UserFilter();
		userFilter.setName("m");
		userFilter.setPaginationData(new PaginationData(0, 2, "name", OrderMode.ASCENDING));

		final PaginatedData<User> result = userRepository.findByFilter(userFilter);
		Assert.assertThat(result.getNumberOfRows(), CoreMatchers.is(CoreMatchers.equalTo(2)));
		Assert.assertThat(result.getRows().size(), CoreMatchers.is(CoreMatchers.equalTo(2)));
		Assert.assertThat(result.getRow(0).getName(),
				CoreMatchers.is(CoreMatchers.equalTo(UserForTestsRepository.admin().getName())));
		Assert.assertThat(result.getRow(1).getName(),
				CoreMatchers.is(CoreMatchers.equalTo(UserForTestsRepository.mary().getName())));
	}

	@Test
	public void findByFilterFilteringByNameAndType() {
		loadDataForFindByFilter();

		final UserFilter userFilter = new UserFilter();
		userFilter.setName("m");
		userFilter.setUserType(UserType.EMPLOYEE);
		userFilter.setPaginationData(new PaginationData(0, 2, "name", OrderMode.ASCENDING));

		final PaginatedData<User> result = userRepository.findByFilter(userFilter);
		Assert.assertThat(result.getNumberOfRows(), CoreMatchers.is(CoreMatchers.equalTo(1)));
		Assert.assertThat(result.getRows().size(), CoreMatchers.is(CoreMatchers.equalTo(1)));
		Assert.assertThat(result.getRow(0).getName(),
				CoreMatchers.is(CoreMatchers.equalTo(UserForTestsRepository.admin().getName())));
	}

	private void loadDataForFindByFilter() {
		dbCommandExecutor.executeCommand(() -> {
			UserForTestsRepository.allUsers().forEach(userRepository::add);
			return null;
		});
	}

	private void assertUser(final User actualUser, final User expectedUser, final UserType expectedUserType) {
		Assert.assertThat(actualUser.getName(), CoreMatchers.is(CoreMatchers.equalTo(expectedUser.getName())));
		Assert.assertThat(actualUser.getEmail(), CoreMatchers.is(CoreMatchers.equalTo(expectedUser.getEmail())));
		Assert.assertThat(actualUser.getRoles().toArray(),
				CoreMatchers.is(CoreMatchers.equalTo(expectedUser.getRoles().toArray())));
		Assert.assertThat(actualUser.getCreatedAt(), CoreMatchers.is(CoreMatchers.notNullValue()));
		Assert.assertThat(actualUser.getPassword(), CoreMatchers.is(expectedUser.getPassword()));
		Assert.assertThat(actualUser.getUserType(), CoreMatchers.is(CoreMatchers.equalTo(expectedUserType)));
	}

}
