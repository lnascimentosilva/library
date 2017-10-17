package com.library.app.common.model;

public enum HTTPCode {
	CREATED(201), VALIDATION_ERROR(422), OK(200), NOT_FOUND(404), FORBIDDEN(403), INTERNAL_ERROR(500), UNAUTHORIZED(
			401);

	private int code;

	private HTTPCode(final int code) {
		this.code = code;
	}

	public int getCode() {
		return this.code;
	}
}
