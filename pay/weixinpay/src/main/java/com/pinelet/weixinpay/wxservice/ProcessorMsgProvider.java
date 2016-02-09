package com.pinelet.weixinpay.wxservice;

import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.pinelet.weixinpay.msg.AbsProcessMessage;
import com.pinelet.weixinpay.msg.PushSubscribeMsgImpl;

public class ProcessorMsgProvider {

	private static final String EVENT = "event";
	private static final String EV_SUBSCRIB = "subscribe";
	private static final String EV_UNSUBSCRIBE = "unsubscribe";
	private static final String EV_SCAN = "SCAN"; 
	private static XPath xpath = XPathFactory.newInstance().newXPath();
	private static Logger loger = LoggerFactory.getLogger(ProcessorMsgProvider.class); 
		
	public static AbsProcessMessage get(AsyncContext context, Document doc) {
		
		String type = null;
		try {
			type = ((Node)xpath.evaluate("/xml/MsgType", doc, XPathConstants.NODE)).getTextContent();
		} catch (DOMException | XPathExpressionException e) {
			loger.error("get message node /xml/MsgType failed..", e);
		}
		if (type == null) {
			try {
				context.getResponse().getWriter().append("").flush();
			} catch (IOException e) {}
			context.complete();
		}
		switch (type) {
		case EVENT :
			String event = null;
			try {
				event = ((Node)xpath.evaluate("/xml/Event", doc, XPathConstants.NODE)).getTextContent();
			} catch (DOMException | XPathExpressionException e) {
				loger.error("get message nod /xml/Event failed..", e);
			}
			switch (event) {
			case EV_SUBSCRIB :
				return new PushSubscribeMsgImpl(context, doc);
			case EV_UNSUBSCRIBE:
				return null;
			case EV_SCAN:
				return new PushSubscribeMsgImpl(context, doc);
			}	
		}
		return null;

	}

}
