package com.sesnu.fireball.service;

import com.ib.client.Contract;
import com.ib.client.Order;
import com.ib.client.OrderType;
import com.ib.client.Types.Action;
import com.ib.controller.ApiController;
import com.sesnu.handler.AccountHandler;
import com.sesnu.handler.ConnectionHandler;
import com.sesnu.handler.LiveOrdersHandler;
import com.sesnu.handler.LoggerInHandler;
import com.sesnu.handler.LoggerOutHandler;
import com.sesnu.handler.OrderHandler;


public class Test {

	public static void main(String[] args) throws InterruptedException {
		
		Common common = new Common();
		
		ConnectionHandler conHandler = new ConnectionHandler(common);
		ApiController api = new ApiController(conHandler,new LoggerInHandler(),new LoggerOutHandler());
		api.connect(Util.getString("gatewayIp"), 4002, 3, null);
		
		while(!conHandler.isConnected() && !conHandler.isIbControllerOff()){
			Thread.sleep(5000);		
		}

//		AccountHandler acctHandler = new AccountHandler(null,common);
//		api.reqAccountUpdates(true,Util.getAcctNo(),acctHandler);
		
//		int orderId = api.getNextOrderId();
//		Order order = new Order();
//		order.orderId(orderId);
//		order.action(Action.BUY);	        
//		order.totalQuantity(100);	        
//		order.orderType(OrderType.MKT);		
//		order.transmit(true);
//		api.placeOrModifyOrder(getContract("AMD"), order, new OrderHandler());
//		
		
		LiveOrdersHandler liveOrdersHand = new LiveOrdersHandler(null);
		api.reqLiveOrders(liveOrdersHand);
		
		int parentOrderId = api.getNextOrderId();
		Order parentOrder = new Order();
		parentOrder.orderId(parentOrderId);
		parentOrder.action(Action.BUY);	        
		parentOrder.totalQuantity(100);	        
		parentOrder.orderType(OrderType.LMT);
		parentOrder.lmtPrice(214);			
		long expireTime = System.currentTimeMillis() + 120000; // wait 3 min
		parentOrder.goodTillDate(Util.formatTimeStamp(expireTime));
		parentOrder.tif("GTD");
		parentOrder.transmit(false);
		
		api.placeOrModifyOrder(getContract("NVDA"), parentOrder, new OrderHandler());
        
		Order takeProfit = new Order();
		int exitId = api.getNextOrderId();
        takeProfit.orderId(exitId);
        takeProfit.action(Action.SELL);
        takeProfit.orderType("LMT");
        takeProfit.totalQuantity(10);
        takeProfit.lmtPrice(217);
        takeProfit.parentId(parentOrderId);
        takeProfit.transmit(false);

        api.placeOrModifyOrder(getContract("NVDA"), takeProfit, new OrderHandler());
        
		Order stopLossOrder = new Order();
		int stpId = api.getNextOrderId();
		stopLossOrder.orderId(stpId);
		stopLossOrder.action(Action.SELL);
		stopLossOrder.orderType(OrderType.STP);
		//Stop trigger price
		stopLossOrder.auxPrice(213);
		stopLossOrder.totalQuantity(100);
		stopLossOrder.parentId(parentOrderId);
		stopLossOrder.transmit(true);
		
		api.placeOrModifyOrder(getContract("NVDA"), stopLossOrder, new OrderHandler());
		
		System.out.println(Util.formatTimeStamp(expireTime));
		
		
	}
	
	private static Contract getContract(String ticker){
		   Contract contract = new Contract();
	       contract.symbol(ticker); 

	       contract.secType("STK");

	       contract.exchange("SMART"); 
	       
	       contract.currency("USD");
	       
	       contract.primaryExch("ISLAND");
	       
	       return contract;
	}

}
