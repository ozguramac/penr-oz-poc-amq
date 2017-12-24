package com.oz.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

public final class JmsProducerServlet extends HttpServlet {
	private static final long serialVersionUID = 4078390439882271186L;
	private static final Logger logger = Logger.getLogger(JmsProducerServlet.class);

	static final String JMS_COMM_ERR_MSG = "Problem with JMS Communication";
	static final String WRONG_USAGE_ERR_MSG = "Sorry but expecting mode and/or msg parameters...";
	static final String SUCCESS_MSG = "Your message is sent...";

	@Resource(name = "foo")
	private Topic fooTopic;

//	@Resource(name = "bar")
//	private Queue barQueue;

	@Resource
	private ConnectionFactory jmsConnFactory;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		Connection jmsConn = null;
		Session jmsSession = null;

		try {
			jmsConn = jmsConnFactory.createConnection();
			jmsConn.start();

			// Create a Session
			jmsSession = jmsConn.createSession(false, Session.AUTO_ACKNOWLEDGE);

			final Gson gson = new Gson();

			//Parse request
			final Request jsonReq = gson.fromJson(req.getReader(), Request.class);

			//Get mode
			final String mode = jsonReq.getMode();

			final PrintWriter pw = resp.getWriter();
			if ("PUBLISH_TO_TOPIC".equals(mode)) {
				final String msg = jsonReq.getMsg();

				// Create a MessageProducer from the Session to the Topic or Queue
				final MessageProducer producer = jmsSession.createProducer(fooTopic);
				producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

				// Create a message
				final TextMessage message = jmsSession.createTextMessage(msg);

				// Tell the producer to send the message
				producer.send(message);

				//Write back ack to response
				pw.println(SUCCESS_MSG);
			}
			//TODO: Other modes
			else {
				//Write back usage to response
				pw.println(WRONG_USAGE_ERR_MSG);
			}
			pw.flush();

		}
		catch (JMSException e) {
			throw new ServletException(JMS_COMM_ERR_MSG, e);
		}
		finally {
			try {
				if (jmsSession != null) {
					jmsSession.close();
				}

				if (jmsConn != null) {
					jmsConn.stop();
					jmsConn.close();
				}
			}
			catch (JMSException e) {
				logger.debug("Problem cleaning up JMS resources", e);
			}
		}
	}

	private static class Request {
		String getMode() {
			return mode;
		}
		String getMsg() {
			return msg;
		}

		private String mode;
		private String msg;
	}
}

