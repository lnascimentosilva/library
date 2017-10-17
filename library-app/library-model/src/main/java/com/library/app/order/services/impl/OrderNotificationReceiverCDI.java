package com.library.app.order.services.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.library.app.order.model.Order;

@ApplicationScoped
public class OrderNotificationReceiverCDI {

	private Logger logger = LoggerFactory.getLogger(getClass());

	public void receiveEvent(@Observes final Order order) {
		logger.debug("Order notification received for order: {}", order);
	}

}