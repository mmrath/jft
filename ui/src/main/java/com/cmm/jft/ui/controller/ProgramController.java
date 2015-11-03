/**
 * 
 */
package com.cmm.jft.ui.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.cmm.jft.ui.utils.FormUtils;
import com.cmm.jft.ui.utils.ImageIcons;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * <p><code>ProgramController.java</code></p>
 * @author Cristiano M Martins
 * @version Nov 1, 2015 9:31:55 PM
 *
 */
public class ProgramController implements Initializable {

	@FXML
	private Button btnPlaceOrder;
	
	@FXML
	private Button btnOrderManager;
	
	@FXML
	private Button btnBook;
	
	@FXML
	private Button btnDOM;
	
	@FXML
	private Button btnTimeSales;
	
	
	/* (non-Javadoc)
	 * @see javafx.fxml.Initializable#initialize(java.net.URL, java.util.ResourceBundle)
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		
		btnBook.setGraphic(ImageIcons.getBookImage());
		btnDOM.setGraphic(ImageIcons.getDOMImage());
		btnOrderManager.setGraphic(ImageIcons.getOrderManagerImage());
		btnPlaceOrder.setGraphic(ImageIcons.getPlaceOrderImage());
		btnTimeSales.setGraphic(ImageIcons.getTimeSalesImage());
		
		btnBook.setTooltip(new Tooltip("Book"));
		btnDOM.setTooltip(new Tooltip("DOM"));
		btnOrderManager.setTooltip(new Tooltip("Order Manager"));
		btnPlaceOrder.setTooltip(new Tooltip("Place Order"));
		btnTimeSales.setTooltip(new Tooltip("Time & Sales"));
		
		btnBook.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				FormUtils.getInstance().openForm("../../../../../forms/BookForm.fxml", "Book");
			}
		});
		
		btnDOM.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				FormUtils.getInstance().openForm("../../../../../forms/DOMForm.fxml", "DOM");
			}
		});
		
		btnOrderManager.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				FormUtils.getInstance().openForm("../../../../../forms/OrderManagerForm.fxml", "Order Manager");
			}
		});
		
		btnPlaceOrder.setOnAction(new EventHandler<ActionEvent>() {
			/* (non-Javadoc)
			 * @see javafx.event.EventHandler#handle(javafx.event.Event)
			 */
			@Override
			public void handle(ActionEvent event) {
				FormUtils.getInstance().openForm("../../../../../forms/PlaceOrderForm.fxml", "Place Order");
			}
		});
		
		btnTimeSales.setOnAction(new EventHandler<ActionEvent>() {
			/* (non-Javadoc)
			 * @see javafx.event.EventHandler#handle(javafx.event.Event)
			 */
			@Override
			public void handle(ActionEvent event) {
				FormUtils.getInstance().openForm("../../../../../forms/TimeSalesForm.fxml", "Time & Sales");
			}
		});
		
	}

}