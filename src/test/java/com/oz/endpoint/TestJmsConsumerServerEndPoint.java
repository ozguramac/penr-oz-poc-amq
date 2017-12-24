package com.oz.endpoint;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.TextMessage;
import javax.websocket.CloseReason;
import javax.websocket.Session;

import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@RunWith(MockitoJUnitRunner.class)
public class TestJmsConsumerServerEndPoint {
	
	@Mock(name = "jmsConnFactory")
	private ConnectionFactory mockJmsConnFactory;
	@InjectMocks
	private JmsConsumerServerEndPoint endPoint;

	@Mock
	private Session mockSession;

	@Mock
	private MessageConsumer mockConsumer;

	@Rule 
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public final void testOnOpen() throws Exception {
		Mockito.when(mockSession.getId()).thenReturn("TEST_SESSION");

		final javax.jms.Connection mockJmsConn = Mockito.mock(javax.jms.Connection.class);
		Mockito.when(mockJmsConnFactory.createConnection()).thenReturn(mockJmsConn);
		final javax.jms.Session mockJmsSession = Mockito.mock(javax.jms.Session.class);
		Mockito.when(mockJmsConn.createSession(Mockito.anyBoolean(), Mockito.anyInt())).thenReturn(mockJmsSession);
		Mockito.when(mockJmsSession.createConsumer(Mockito.any(Destination.class))).thenReturn(mockConsumer);
		
		endPoint.onOpen(mockSession);
	}

	@After
	public final void testOnClose() throws Exception {
		Mockito.when(mockSession.isOpen()).thenReturn(true);
		endPoint.onClose(mockSession, new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Testing..."));
	}

	@Test
	public final void testOnEchoMessage() throws Exception {
		final String someMsg = "some message";
		final String strResp = endPoint.onMessage(someMsg, mockSession);
		Assert.assertNotNull("Expecting response", strResp);
		final JsonObject resp = toResponse(strResp);
		Assert.assertEquals("Echo message", JmsConsumerServerEndPoint.ECHO_PREFIX+someMsg, getMsg(resp));
	}

	@Test
	public final void testOnListenEmptyMessage() throws Exception {
		final String strResp = endPoint.onMessage("listen", mockSession);
		Assert.assertNotNull("Expecting response", strResp);
		final JsonObject resp = toResponse(strResp);
		Assert.assertEquals("Message Text", JmsConsumerServerEndPoint.NO_MESSAGE_RECEIVED, getMsg(resp));
	}
	
	@Test
	public final void testOnListenMessage() throws Exception {
		final Message msg = Mockito.mock(Message.class);
		Mockito.when(mockConsumer.receive()).thenReturn(msg);
		final String msgTxt = "blah";
		Mockito.when(msg.toString()).thenReturn(msgTxt);

		final String strResp = endPoint.onMessage("listen", mockSession);
		Assert.assertNotNull("Expecting response", strResp);
		final JsonObject resp = toResponse(strResp);
		Assert.assertEquals("Message Text", msgTxt, getMsg(resp));
	}
	
	@Test
	public final void testOnListenTextMessage() throws Exception {
		final TextMessage msg = Mockito.mock(TextMessage.class);
		Mockito.when(mockConsumer.receive()).thenReturn(msg);
		final String msgTxt = "blah";
		Mockito.when(msg.getText()).thenReturn(msgTxt);

		final String strResp = endPoint.onMessage("listen", mockSession);
		Assert.assertNotNull("Expecting response", strResp);
		final JsonObject resp = toResponse(strResp);
		Assert.assertEquals("Message Text", msgTxt, getMsg(resp));
	}

	@Test
	public final void testOnListenCommError() throws Exception {
		final String commErrRsn = "Testing JMS Comm Error";
		final JMSException jmsException = new JMSException(commErrRsn);
		Mockito.when(mockConsumer.receive()).thenThrow(jmsException);

		thrown.expect(RuntimeException.class);
		thrown.expectMessage(JmsConsumerServerEndPoint.PROBLEM_JMS_COMM);
		thrown.expectCause(Is.is(jmsException));
		endPoint.onMessage("listen", mockSession);
	}

	private static JsonObject toResponse(final String strResp) {
		final JsonObject resp = new Gson().fromJson(strResp, JsonObject.class);
		Assert.assertNotNull("Expecting response", resp);
		Assert.assertNotNull("Expecting timestamp", resp.get("timeStamp"));
		return resp;
	}

	private static String getMsg(final JsonObject resp) {
		return resp.get("msg").getAsString();
	}
}
