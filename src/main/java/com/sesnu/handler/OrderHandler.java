package com.sesnu.handler;

import com.ib.client.OrderState;
import com.ib.client.OrderStatus;
import com.ib.controller.ApiController.IOrderHandler;

public class OrderHandler implements IOrderHandler {


	@Override
	public void orderState(OrderState orderState) {
		// TODO Auto-generated method stub
//		System.out.println("order status " + orderState.getStatus());
		System.out.println("order handler " + orderState);
	}


	@Override
	public void handle(int errorCode, String errorMsg) {
		// TODO Auto-generated method stub
		System.out.println("order handler " + errorMsg);

	}

	@Override
	public void orderStatus(OrderStatus status, double filled, double remaining, double avgFillPrice, int permId,
			int parentId, double lastFillPrice, int clientId, String whyHeld, double mktCapPrice) {
//		System.out.println("state " + status.name() + " clientId " + clientId + " parentId " + parentId);
		
		System.out.println("order handler " + status);
		
	}

}
