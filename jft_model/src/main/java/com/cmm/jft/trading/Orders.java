/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cmm.jft.trading;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

import org.apache.log4j.Level;

import com.cmm.jft.db.DBObject;
import com.cmm.jft.security.Security;
import com.cmm.jft.trading.enums.ExecutionTypes;
import com.cmm.jft.trading.enums.OrderStatus;
import com.cmm.jft.trading.enums.OrderTypes;
import com.cmm.jft.trading.enums.OrderValidityTypes;
import com.cmm.jft.trading.enums.Side;
import com.cmm.jft.trading.enums.TradeTypes;
import com.cmm.jft.trading.enums.WorkingIndicator;
import com.cmm.jft.trading.exceptions.OrderException;
import com.cmm.logging.Logging;

/**
 *
 * <p>
 * <code>Orders</code>
 * </p>
 * 
 * @author Cristiano M Martins
 * @version Aug 6, 2013 2:00:40 AM
 */
@Entity
@Table(name = "Orders", schema="Trading"/*, uniqueConstraints = { @UniqueConstraint(columnNames = { "OrderSerial" }) }*/)
@NamedQueries({
    @NamedQuery(name = "Orders.findAll", query = "SELECT o FROM Orders o"),
    @NamedQuery(name = "Orders.findByOrderID", query = "SELECT o FROM Orders o WHERE o.orderID = :orderID"),
    })
public class Orders implements DBObject<Orders> {
    private static final long serialVersionUID = 1L;

    //@SequenceGenerator(name = "ORDERS_SEQ", sequenceName = "ORDERS_SEQ", allocationSize = 1, initialValue = 1)
    //@GeneratedValue(generator = "ORDERS_SEQ", strategy = GenerationType.AUTO)
    @Id
    @Basic(optional = false)
    @Column(name = "orderID", nullable = false)
    private Long orderID;

    @Column(name = "secOrderID", nullable = false)
    private Long secOrderID;
    
    /**
     * Unique identifier of the order as assigned by the market participant
     */
    @Column(name="ClOrdID", length=50, updatable=false, nullable=false)
    private String clOrdID;

    /**
     * Contains the ClOrdID of the replacement order. 
     * Conditionally required when ExecType = 5 (Replace).
     */
    @Column(name="OrigClOrdID", length=50, updatable=false)
    private String origClOrdID;
    
    @Column(name="traderID", updatable=false, length=50)
    private String traderID;

    @Column(name="brokerID", length=5, updatable=false)
    private String brokerID;
    

    @Column(name = "Price", precision = 19, scale = 6)
    private double price;

    @Column(name = "StopPrice", precision = 19, scale = 6)
    private double stopPrice;

    @Basic(optional = false)
    @Column(name = "Volume", nullable = false)
    private double volume;
    
    @Column(name = "ExecutedVolume")
    private Integer executedVolume;

    @Column(name="LeavesVolume")
    private double leavesVolume;
    
    @Column(name = "MaxFloor")
    private double maxFloor;
    
    @Column(name = "MinVolume")
    private double minVolume;
    

    // @Max(value=?) @Min(value=?)//if you know range of your decimal fields
    // consider using these annotations to enforce field validation
    @Column(name = "AvgPrice", precision = 19, scale = 6)
    private double avgPrice;

    @Column(name = "ProtectionPrice", precision = 19, scale = 6)
    private double protectionPrice;

    @Column(name = "Duration", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date duration;

    @Column(name = "OrderDateTime", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date orderDateTime;

    @Column(name = "InsertDate", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date insertDate;

    @Column(name = "InsertTime", nullable = false)
    @Temporal(TemporalType.TIME)
    private Date insertTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "OrderStatus", nullable = false, length=50)
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "WorkingIndicator", nullable = false,length=50)
    private WorkingIndicator workingIndicator;

    @Enumerated(EnumType.STRING)
    @Column(name = "OrderValidityType", nullable = false, length=50)
    private OrderValidityTypes validityType;

    @Enumerated(EnumType.STRING)
    @Basic(optional = false)
    @Column(name = "OrderType", nullable = false, length=50)
    private OrderTypes orderType;

    @Enumerated(EnumType.STRING)
    @Basic(optional = false)
    @Column(name = "TradeType", nullable = false, updatable = false)
    private TradeTypes tradeType;

    @Enumerated(EnumType.STRING)
    @Basic(optional = false)
    @Column(name = "Side", nullable = false, updatable = false, length=10)
    private Side side;

    @Column(name="Comment", length=250)
    private String comment;

    @Column(name="senderLocation", length=50, updatable=false)
    private String senderLocation;

    @JoinColumn(name = "securityID", referencedColumnName = "securityID", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Security securityID;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "orderID")
    private List<OrderEvent> eventsList;



    public Orders() {
	this.price =  0d;
	this.duration = new Date();
	init();
    }


    /**
     * @param limitPrice
     * @param volume
     * @param orderType
     * @param tradeType
     * @param side
     * @throws OrderException 
     */
    public Orders(long orderID, String clOrdID, Security security,  Side side, double price, int volume, 
	    OrderTypes orderType, TradeTypes tradeType) throws OrderException {



	if(orderID <=0 || clOrdID == null || security == null || side == null){
	    throw new OrderException(
		    String.format(
			    "Invalid field value: orderID %4$d, clOrdID: %1$s, security: %2$s, side: %3$s", 
			    clOrdID, security, side, orderID));
	}

	this.orderID = orderID;
	this.secOrderID = orderID;
	this.clOrdID = clOrdID;

	switch(orderType){
	case Limit:
	    this.price = price;
	    break;
	case Market:
	    break;
	case Stop:
	case StopLimit:
	    if(price <=0){
		throw new OrderException(
			String.format("Invalid price for type: %1$f %2$s", price, orderType));
	    }
	    this.stopPrice = price;
	}

	this.securityID = security;
	this.volume = volume;
	this.leavesVolume = volume;
	this.orderType = orderType;
	this.tradeType = tradeType;
	this.side = side;
	init();
    }


    private void init(){

	this.avgPrice = 0;
	this.executedVolume = 0;

	this.comment = "";
	this.validityType = OrderValidityTypes.DAY;
	this.workingIndicator = WorkingIndicator.No_Working;
	this.orderDateTime = new Date();
	this.orderStatus = OrderStatus.CREATED;
	this.eventsList = new ArrayList<OrderEvent>();
    }



    private void refreshOrder() throws OrderException {

	try {
	    // calculate the order avg price and adjusts the last executed price
	    calculateOrderValues();

	    //adjust the state in according to actual order state
	    if(orderStatus == OrderStatus.NEW || orderStatus == OrderStatus.PARTIALLY_FILLED || orderStatus == OrderStatus.REPLACED){
		// order have not yet been executed
		if (leavesVolume > 0 && leavesVolume < volume) {
		    setOrderStatus(OrderStatus.PARTIALLY_FILLED);
		} else if (leavesVolume == 0) {//order was totally executed
		    setOrderStatus(OrderStatus.FILLED);
		} else if (leavesVolume < 0) {// error!!!!!
		    throw new OrderException("ExecutedVolume is greater than order volume: " + executedVolume);
		}
	    }

	} catch (Exception e) {
	    Logging.getInstance().log(getClass(), e, Level.ERROR);
	    throw new OrderException(e);
	}

    }

    public double calculateOrderValues() {
	int sumVolume = 0;
	double sumTotal = 0d;
	for (OrderEvent oe : eventsList) {
	    if(oe.getExecutionType() == ExecutionTypes.TRADE) {
		sumVolume += oe.getVolume();
		sumTotal += oe.getPrice() * oe.getVolume();
	    }
	}

	// adjusts the execution control values
	executedVolume = sumVolume;
	leavesVolume = volume - executedVolume;

	//only TRADE events are relevant
	long execs = eventsList.stream().filter(e -> e.getExecutionType() == ExecutionTypes.TRADE).count();
	execs = execs > 0 ? execs : 1;
	avgPrice = (sumTotal/execs);

	return avgPrice;
    }	

    public void setOrderStatus(OrderStatus status) throws OrderException {

	boolean invalidState = true;
	switch(this.orderStatus) {
	case CREATED:
	    if(status == OrderStatus.SUSPENDED || 
	    status == OrderStatus.REJECTED) {
		this.orderStatus = status;
		invalidState = false;
	    }
	    break;

	case SUSPENDED:
	    if(status == OrderStatus.NEW || status == OrderStatus.REJECTED) {
		this.orderStatus = status;
		invalidState = false;
	    }			

	case NEW:
	    if(status == OrderStatus.REPLACED || 
	    status == OrderStatus.EXPIRED || 
	    status == OrderStatus.FILLED || 
	    status == OrderStatus.CANCELED) {
		this.orderStatus = status;
		invalidState = false;
	    }
	    break;

	case REPLACED:
	    if(status == OrderStatus.EXPIRED || 
	    status == OrderStatus.FILLED ||
	    status == OrderStatus.PARTIALLY_FILLED ||
	    status == OrderStatus.CANCELED) {
		this.orderStatus = status;
		invalidState = false;
	    }
	    break;

	case EXPIRED:
	    this.orderStatus = OrderStatus.EXPIRED;
	    invalidState = false;
	    break;

	case CANCELED:
	    this.orderStatus = OrderStatus.CANCELED;
	    invalidState = false;
	    break;

	case FILLED:
	    this.orderStatus = OrderStatus.FILLED;
	    invalidState = false;
	    break;

	case PARTIALLY_FILLED:
	    if(status == OrderStatus.EXPIRED || 
	    status == OrderStatus.FILLED ||
	    status == OrderStatus.PARTIALLY_FILLED ||
	    status == OrderStatus.CANCELED) {
		this.orderStatus = status;
		invalidState = false;
	    }
	    break;

	default:
	    throw new OrderException("Invalid OrderStatus: " + status);
	}

	if(invalidState) {
	    throw new OrderException("Can't set status from: " + orderStatus + " to: " + status);
	}

    }
    
    
    
    /**
     * @return the orderID
     */
    public Long getOrderID() {
	return orderID;
    }
    
    /**
     * Adjusts orderID only if orderID has not been adjusted yet. 
     * @param orderID the orderID to set
     */
    public void setOrderID(Long orderID) {
	if(this.orderID <=0 && orderID >0) {
	    this.orderID = orderID;
	}
    }

    
    /**
     * @return the secOrderID
     */
    public Long getSecOrderID() {
	return secOrderID;
    }
    
    /**
     * @param secOrderID the secOrderID to set
     */
    public void setSecOrderID(Long secOrderID) {
	this.secOrderID = secOrderID;
    }
    
    /**
     * @return the traderID
     */
    public String getTraderID() {
	return this.traderID;
    }
    
    /**
     * @param traderID the traderID to set
     */
    public void setTraderID(String traderID) {
	this.traderID = traderID;
    }

    /**
     * @return the clOrdID
     */
    public String getClOrdID() {
	return clOrdID;
    }

    /**
     * @param clOrdID the clOrdID to set
     */
    public void setClOrdID(String clOrdID) {
	this.clOrdID = clOrdID;
    }

    public void setOrigClOrdID(String origClOrdID) {
	this.origClOrdID = origClOrdID;
    }

    public String getOrigClOrdID() {
	return origClOrdID;
    }

    /**
     * @return the brokerID
     */
    public String getBrokerID() {
	return this.brokerID;
    }

    /**
     * @param brokerID the brokerID to set
     */
    public void setBrokerID(String brokerID) {
	this.brokerID = brokerID;
    }
    
    
    


    public double getOrderValue() {
	return price * volume;
    }

    public double getExecutedOrderValue() {
	return avgPrice * executedVolume;
    }

    /**
     * @return the price
     */
    public Double getPrice() {
	return this.price;
    }

    public double getLeavesVolume() {
	return leavesVolume;
    }

    public double getVolume() {
	return volume;
    }
    
    /**
     * @return the minVolume
     */
    public double getMinVolume() {
	return minVolume;
    }
    
    /**
     * @param minVolume the minVolume to set
     */
    public void setMinVolume(double minVolume) {
	this.minVolume = minVolume;
    }

    public double getAvgPrice() {
	return avgPrice;
    }

    public double getStopPrice() {
	return stopPrice;
    }

    public void setStopPrice(double stopPrice) {
	this.stopPrice = stopPrice;
    }

    public double getProtectionPrice() {
	return protectionPrice;
    }

    public void setProtectionPrice(double protectionPrice) {
	this.protectionPrice = protectionPrice;
    }

    public double getMaxFloor(){
	return this.maxFloor;
    }

    public void setMaxFloor(double maxFloor){
	this.maxFloor = maxFloor;
    }

    /**
     * @return the orderType
     */
    public OrderTypes getOrderType() {
	return this.orderType;
    }

    public Integer getExecutedVolume() {
	return executedVolume;
    }

    public Date getDuration() {
	return duration;
    }

    public void setDuration(Date duration) {
	this.duration = duration;
    }

    /**
     * @return the orderDateTime
     */
    public Date getOrderDateTime() {
	return this.orderDateTime;
    }

    public void setOrderDateTime(Date orderDateTime) {
	this.orderDateTime = orderDateTime;
    }

    public OrderValidityTypes getValidityType() {
	return validityType;
    }

    public void setValidityType(OrderValidityTypes validityType) {
	this.validityType = validityType;
    }

    public OrderStatus getOrderStatus() {
	return orderStatus;
    }

    public WorkingIndicator getWorkingIndicator() {
	return workingIndicator;
    }

    public void setWorkingIndicator(WorkingIndicator workingIndicator) {
	this.workingIndicator = workingIndicator;
    }

    public Side getSide() {
	return side;
    }

    public Security getSecurityID() {
	return securityID;
    }

    /**
     * @return the eventsList
     */
    public List<OrderEvent> getEventsList() {
	return this.eventsList;
    }

    /**
     * @return the comment
     */
    public String getComment() {
	return this.comment;
    }

    public void setComment(String comment) {
	this.comment = comment;
    }

    public TradeTypes getTradeType() {
	return tradeType;
    }

    public void setTradeType(TradeTypes tradeType) {
	this.tradeType = tradeType;
    }

    /**
     * @return the senderLocation
     */
    public String getSenderLocation() {
	return this.senderLocation;
    }

    
    /**
     * @param senderLocation the senderLocation to set
     */
    public void setSenderLocation(String senderLocation) {
	this.senderLocation = senderLocation;
    }

    /**
     * @param orderType the orderType to set
     */
    public void setOrderType(OrderTypes orderType) {
	this.orderType = orderType;
    }

    /**
     * @param price the price to set
     */
    public void setPrice(double price) {
	this.price = price;
    }

    /**
     * @param securityID the securityID to set
     */
    public void setSecurityID(Security securityID) {
	this.securityID = securityID;
    }

    /**
     * @param side the side to set
     */
    public void setSide(Side side) {
	this.side = side;
    }

    /**
     * @param volume the volume to set
     */
    public void setVolume(double volume) {
	this.volume = volume;
    }

    /**
     * @return the insertDate
     */
    public Date getInsertDate() {
	return insertDate;
    }
    /**
     * @return the insertTime
     */
    public Date getInsertTime() {
	return insertTime;
    }
    /**
     * @param insertDate the insertDate to set
     */
    public void setInsertDate(Date insertDate) {
	this.insertDate = insertDate;
    }

    /**
     * @param insertTime the insertTime to set
     */
    public void setInsertTime(Date insertTime) {
	this.insertTime = insertTime;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	int hash = (int) (47 * (price + volume + orderDateTime.getTime()));
	return hash;
    }

    @Override
    public boolean equals(Object otherOrder) {
	boolean eq = false;
	if(otherOrder instanceof Orders) {
	    eq = this.clOrdID == ((Orders)otherOrder).clOrdID;
	}

	return eq;
    }


    @Override
    public String toString() {
	return "Orders ["
		+ (orderID != null ? "orderID=" + orderID + ", " : "")
		+ "price="
		+ price
		+ ", stopPrice="
		+ stopPrice
		+ ", "
		+ ("volume=" + volume + ", ")
		+ "leavesVolume="
		+ leavesVolume
		+ ", "
		+ "avgPrice=" + avgPrice + ", "
		+ "protectionPrice="
		+ protectionPrice
		+ ", maxFloor="
		+ maxFloor
		+ ", "
		+ (executedVolume != null ? "executedVolume=" + executedVolume
			+ ", " : "")
		+ (duration != null ? "duration=" + duration + ", " : "")
		+ (orderDateTime != null ? "orderDateTime=" + new SimpleDateFormat("hh:MM:ss,S").format(orderDateTime)
			+ ", " : "")
		+ (orderStatus != null ? "orderStatus=" + orderStatus + ", "
			: "")
		+ (validityType != null ? "validityType=" + validityType + ", "
			: "")
		+ (orderType != null ? "orderType=" + orderType + ", " : "")
		+ (tradeType != null ? "tradeType=" + tradeType + ", " : "")
		+ (side != null ? "side=" + side + ", " : "")
		+ (comment != null ? "comment=" + comment + ", " : "")
		+ (clOrdID != null ? "clOrdID=" + clOrdID + ", " : "")
		+ (origClOrdID != null ? "origClOrdID=" + origClOrdID + ", " : "")
		+ (securityID != null ? "securityID=" + securityID + ", " : "")
		+ (eventsList != null ? "eventsList=" + eventsList : "") + "]";
    }

    /**
     * 
     * @param execution
     * @return
     * @throws OrderException
     */
    public boolean addExecution(OrderEvent execution) throws OrderException {

	boolean added = true;

	try {
	    switch(execution.getExecutionType()) {
	    case CANCELED:
		cancelOrder(execution);
		break;
	    case EXPIRED:
		expireOrder(execution);
		break;
	    case NEW:
		newOrder(execution);
		break;
	    case REJECTED:
		rejectOrder(execution);
		break;
	    case REPLACE:
		replaceOrder(execution);
		break;
	    case RESTATED:
		restateOrder(execution);
		break;
	    case SUSPENDED:
		break;
	    case TRADE:
		tradeOrder(execution);
		break;
	    case TRADE_CANCEL:
		cancelTrade(execution);
		break;
	    }	
	    eventsList.add(execution);
	}
	catch(OrderException e) {
	    added = false;
	    throw e;
	}

	return added;
    }

    private void cancelOrder(OrderEvent execution) throws OrderException {

	setOrderStatus(OrderStatus.CANCELED);
	workingIndicator = WorkingIndicator.No_Working;
	refreshOrder();

    }

    private void expireOrder(OrderEvent execution) throws OrderException {

	setOrderStatus(OrderStatus.EXPIRED);
	refreshOrder();

    }

    private void newOrder(OrderEvent execution) throws OrderException {

	setOrderStatus(OrderStatus.NEW);
	refreshOrder();

    }

    private void rejectOrder(OrderEvent execution) throws OrderException {

	setOrderStatus(OrderStatus.REJECTED);
	refreshOrder();

    }

    private void replaceOrder(OrderEvent replace) throws OrderException {

	setOrderStatus(OrderStatus.REPLACED);

	if(replace.getOrderType() != null) {
	    this.orderType = replace.getOrderType();
	}

	if(replace.getPrice() > 0 && replace.getPrice() != price) {
	    changePrice(replace.getPrice());
	}

	if(replace.getVolume() >0 && replace.getVolume() != volume) {
	    changeVolume(replace.getVolume());
	}
	refreshOrder();
    }
    
    private void restateOrder(OrderEvent restate) {
	this.secOrderID++;
	this.orderDateTime = restate.getExecutionDateTime();
    }

    private void tradeOrder(OrderEvent execution) throws OrderException {

	if(orderStatus == OrderStatus.NEW || orderStatus == OrderStatus.PARTIALLY_FILLED || orderStatus == OrderStatus.REPLACED){
	    //volume executado eh menor que o volume total e menor que o volume atual
	    if(execution.getVolume() <= volume && execution.getVolume() <= (volume-executedVolume)){
		eventsList.add(execution);
		//ajusta o estado da ordem
		refreshOrder();
	    } else {
		throw new OrderException("Invalid volume: " + execution.getVolume());
	    }			
	} else {
	    throw new OrderException("");
	}


    }

    private void cancelTrade(OrderEvent execution) throws OrderException {
	setOrderStatus(OrderStatus.NEW);
	refreshOrder();
    }

    private boolean changePrice(double price) throws OrderException {
	boolean ret = false;
	try {
	    OrderEvent oe = new OrderEvent(ExecutionTypes.REPLACE, new Date(), 0, price);
	    oe.setMessage(String.format("Price replaced from %.4f to %.4f", this.price, price));

	    setOrderStatus(OrderStatus.REPLACED);
	    this.price = price;

	    eventsList.add(oe);
	    ret = true;
	}catch(OrderException e) {
	    throw e;
	}

	return ret;
    }

    private boolean changeVolume(double volume) throws OrderException {
	boolean ret = false;
	try {
	    // verifica se o volume esta de acordo com o lote padrao do simbolo,
	    // caso n esteja, lanca exception
	    if (volume < securityID.getSecurityInfoID().getMinVolume() || 
		    volume > securityID.getSecurityInfoID().getMaxVolume()) {
		throw new OrderException("Invalid Volume:" + volume);
	    }

	    OrderEvent oe = new OrderEvent(ExecutionTypes.REPLACE, new Date(), 0, volume);
	    oe.setMessage(String.format("Volume replaced from %.3f to %.3f", this.volume, volume));
	    eventsList.add(oe);

	    setOrderStatus(OrderStatus.REPLACED);
	    this.volume = volume;
	    ret = true;
	}catch(OrderException e) {
	    throw e;
	}

	return ret;
    }
    
    public void changeToMarket() throws OrderException {
	setOrderType(OrderTypes.Market);
	setPrice(0);
    }

}
