/**
 * 
 */
package com.cmm.jft.engine;

import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.log4j.Level;

import com.cmm.jft.engine.enums.MatchTypes;
import com.cmm.jft.engine.match.OrderMatcher;
import com.cmm.jft.engine.message.MessageEncoder;
import com.cmm.jft.engine.message.MessageSender;
import com.cmm.jft.trading.OrderExecution;
import com.cmm.jft.trading.Orders;
import com.cmm.jft.trading.enums.ExecutionTypes;
import com.cmm.jft.trading.enums.OrderStatus;
import com.cmm.jft.trading.enums.Side;
import com.cmm.jft.trading.exceptions.OrderException;
import com.cmm.jft.trading.securities.Security;
import com.cmm.jft.trading.services.SecurityService;
import com.cmm.logging.Logging;

import quickfix.Message;
import quickfix.SessionID;

/**
 * <p><code>Book.java</code></p>
 * @author Cristiano M Martins
 * @version 26 de jul de 2015 02:23:19
 *
 */
public class Book implements MessageSender {

	private int orderCount;
	private Security security;
	private OrderMatcher orderMatcher;
	private HashSet<String> validOrderTypes;
	private PriorityBlockingQueue<Orders> buyQueue;
	private PriorityBlockingQueue<Orders> sellQueue;


	public Book(String symbol, HashSet<String> orderTypes, MatchTypes matchType){
		this.orderCount = 0;
		this.security = SecurityService.getInstance().provideSecurity(symbol);
		this.validOrderTypes = orderTypes;
		this.orderMatcher = new OrderMatcher();
		this.buyQueue = orderMatcher.getBuyQueue();
		this.sellQueue = orderMatcher.getSellQueue();
	}

	public Security getSecurity() {
		return security;
	}

	public int getOrderCount() {
		return orderCount;
	}

	public Security getSymbol() {
		return security;
	}

	public void getBookInfo() {

	}

	private boolean validateOrder(Orders order) {

		boolean valid = false;

		valid = isValidType(order);

		//verifica se a quantidade para o instrumento eh valida
		valid = valid && (order.getVolume() % security.getSecurityInfoID().getMinimalVolume()) == 0;

		//verifica se o preco esta correto
		valid = valid && order.getPrice()>0;

		return valid;
	}

	private boolean isValidType(Orders order) {

		boolean isValid = false;
		switch(order.getOrderType()) {

		case Market:
			isValid = true;
			break;

		case Limit:
			isValid = true;
			break;

		case Stop:
			isValid = true;
			break;

		case StopLimit:
			isValid = true;
			break;

		case MarketWithLeftOverAsLimit:
			isValid = true;
			break;

		default: isValid = false;
		}

		return isValid;
	}



	public boolean addOrder(Orders order, SessionID sessionID) {
		boolean added = false;

		try {
			if(added = validateOrder(order)) {
				order.setOrderStatus(OrderStatus.SUSPENDED);
			}

			//ainda nao adicionou no book, deve primeiro verificar se pode executar 
			//antes de inserir no book
			if(added) {
				
				//envia mensagem informando que a ordem foi aceita
				OrderExecution oe = new OrderExecution(ExecutionTypes.NEW, new Date(), order.getVolume(), order.getPrice());
				oe.setMessage("Order received");
				oe.setOrderID(order);
				
				order.addExecution(oe);

				sendMessage(MessageEncoder.getEncoder(sessionID).executionReport(oe), sessionID);
				orderCount++;
				
				if(order.getSide() == Side.BUY) {
					added = added && orderMatcher.addBuyOrder(order);
				}
				else {
					added = added && orderMatcher.addSellOrder(order);
				}
				
				
				
			}

		}catch(OrderException e) {
			added = false;
			OrderExecution oe = new OrderExecution(ExecutionTypes.NEW, new Date(), order.getVolume(), order.getPrice());
			oe.setMessage("Order received");
			oe.setOrderID(order);
			sendMessage(MessageEncoder.getEncoder(sessionID).executionReport(oe), sessionID);
			Logging.getInstance().log(getClass(), e, Level.ERROR);
		}

		return added;
	}

	public void calcelOrder(Orders order) {
		
	}


	/* (non-Javadoc)
	 * @see com.cmm.jft.engine.message.MessageSender#sendMessage(quickfix.Message, quickfix.SessionID)
	 */
	@Override
	public boolean sendMessage(Message message, SessionID sessionID) {
		return MessageRepository.getInstance().addMessage(message, sessionID);
	}	


}
