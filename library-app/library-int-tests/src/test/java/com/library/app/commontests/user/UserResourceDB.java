package com.library.app.commontests.user;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.library.app.user.services.UserServices;

@Path("/DB/users")
@Produces(MediaType.APPLICATION_JSON)
public class UserResourceDB {

	@Inject
	private UserServices userServices;

	@POST
	public void addAll() {
		UserForTestsRepository.allUsers().forEach(userServices::add);
	}

	@POST
	@Path("/admin")
	public void addAdmin() {
		userServices.add(UserForTestsRepository.admin());
	}

}