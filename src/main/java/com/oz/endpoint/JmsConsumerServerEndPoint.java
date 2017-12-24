package com.oz.endpoint;

import java.io.IOException;
import java.util.Date;

import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

@ServerEndpoint(value = "/endpoint")
public final class JmsConsumerServerEndPoint {
	static final String PROBLEM_JMS_COMM = "Problem with JMS Communication";
	static final String ECHO_PREFIX = "Echo: ";
	private static final Logger logger = Logger.getLogger(JmsConsumerServerEndPoint.class);
	static final String NO_MESSAGE_RECEIVED = "No message received?!";
	
	@Resource(name = "foo")
	private Topic fooTopic;

	@Resource
	private ConnectionFactory jmsConnFactory;

	private Connection jmsConn = null;
	private javax.jms.Session jmsSession = null;

	@OnOpen
	public void onOpen(final Session session) {
		logger.info("Connected ... " + session.getId());

		try {
			jmsConn = jmsConnFactory.createConnection();
			jmsConn.start();
			jmsSession = jmsConn.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
		}
		catch (JMSException e) {
			throw new RuntimeException("Problem establishing JMS Session", e);
		}
	}

	@OnMessage
	public String onMessage(final String message, final Session session) {
		final Response resp;
		switch (message) {
		case "listen":
			try {
				// Create a MessageConsumer from the Session for the Topic or Queue
				final MessageConsumer consumer = jmsSession.createConsumer(fooTopic);

				// Wait to receive the message
				final Message msg = consumer.receive();
				if ( null == msg) {//Empty message
					resp = new Response(NO_MESSAGE_RECEIVED);
				}
				else if (msg instanceof TextMessage)
				{//Send back msg text to response
					resp = new Response((TextMessage)msg);
				}
				else {//Send back msg to response
					resp = new Response(msg);
				}
			}
			catch (JMSException e) {
				throw new RuntimeException(PROBLEM_JMS_COMM, e);
			}
			break;
		default: //Echo
			resp = new Response(ECHO_PREFIX + message);
			break;
		}
		return new Gson().toJson(resp);
	}

	@OnClose
	public void onClose(final Session session, final CloseReason closeReason) {
		logger.info(String.format("Session %s closed because of %s", session.getId(), closeReason));


		try {
			if (session.isOpen()) {
				session.close();
			}

			if (jmsSession != null) {
				jmsSession.close();
			}

			if (jmsConn != null) {
				jmsConn.stop();
				jmsConn.close();
			}
		}
		catch (JMSException | IOException e) {
			logger.debug("Problem closing resources", e);
		}
	}

	@OnError
	public void onError(final Session session, final Throwable thr) {
		logger.error(String.format("Error occurred while processing data for session %s: %s", session.getId(), thr.getMessage()));
	}

	private static class Response {
		@SuppressWarnings("unused") //Gson does not use public getter
		final String msg;
		@SuppressWarnings("unused") //Gson does not use public getter
		final String timeStamp;

		Response(final String msg) {
			this.msg = msg;
			this.timeStamp = new Date().toString();
		}

		Response(final TextMessage txtMsg) throws JMSException {
			this(txtMsg.getText());
		}

		Response(final Message msg) {
			this(msg.toString());
		}
	}
}
