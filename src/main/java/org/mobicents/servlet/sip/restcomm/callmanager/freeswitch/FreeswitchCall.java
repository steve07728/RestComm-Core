package org.mobicents.servlet.sip.restcomm.callmanager.freeswitch;

import org.apache.log4j.Logger;

import org.freeswitch.esl.client.inbound.Client;
import org.freeswitch.esl.client.transport.CommandResponse;
import org.freeswitch.esl.client.transport.SendMsg;

import org.mobicents.servlet.sip.restcomm.callmanager.Call;
import org.mobicents.servlet.sip.restcomm.callmanager.CallException;
import org.mobicents.servlet.sip.restcomm.callmanager.CallManager;
import org.mobicents.servlet.sip.restcomm.callmanager.Conference;
import org.mobicents.servlet.sip.restcomm.callmanager.DtmfDetector;
import org.mobicents.servlet.sip.restcomm.callmanager.MediaPlayer;
import org.mobicents.servlet.sip.restcomm.callmanager.MediaRecorder;
import org.mobicents.servlet.sip.restcomm.callmanager.SpeechSynthesizer;
import org.mobicents.servlet.sip.restcomm.fsm.FSM;
import org.mobicents.servlet.sip.restcomm.fsm.State;

public final class FreeswitchCall extends FSM implements Call {
  private static final Logger LOGGER = Logger.getLogger(FreeswitchCall.class);
  //Call Directions.
  private static final String INBOUND = "inbound";
  private static final String OUTBOUND_DIAL = "outbound-dial";
  // Call states.
  private static final State IDLE = new State("idle");
  private static final State QUEUED = new State("queued");
  private static final State RINGING = new State("ringing");
  private static final State IN_PROGRESS = new State("in-progress");
  private static final State COMPLETED = new State("completed");
  private static final State BUSY = new State("busy");
  private static final State FAILED = new State("failed");
  private static final State NO_ANSWER = new State("no-answer");
  private static final State CANCELLED = new State("cancelled");
  static {
    IDLE.addTransition(RINGING);
    IDLE.addTransition(QUEUED);
    QUEUED.addTransition(IN_PROGRESS);
    QUEUED.addTransition(FAILED);
    RINGING.addTransition(IN_PROGRESS);
    RINGING.addTransition(FAILED);
    RINGING.addTransition(CANCELLED);
    IN_PROGRESS.addTransition(COMPLETED);
    IN_PROGRESS.addTransition(FAILED);
  }
	  
  private final Client client;
  private final String uuid;
  
  private String direction;
  
  public FreeswitchCall(final Client client, final String uuid) {
    super(IDLE);
    this.client = client;
    this.uuid = uuid;
    this.direction = null;
  }
  
  public void alert() {
	assertState(IDLE);
	direction = INBOUND;
    final CommandResponse response = execute("ring_ready", null);
    if(response.isOk()) {
      setState(RINGING);
    } else {
      setState(FAILED);
    }
  }

  @Override public void answer() throws CallException {
	assertState(RINGING);
    final CommandResponse response = execute("answer", null);
    if(!response.isOk()) {
      setState(FAILED);
      throw new CallException(response.getReplyText());
    } 
    // Wait for call to finish being established.
    synchronized(this) {
      try {
        wait(30 * 1000);
      } catch(final InterruptedException exception) {
        // Nothing to do.
      }
    }
    // Make sure nothing went wrong.
    assertState(IN_PROGRESS);
  }

  @Override public void bridge(final Call call) throws CallException {
    
  }

  @Override public void connect() throws CallException {
    // Nothing to do.
  }
  
  private CommandResponse execute(final String application, final String arguments) {
    final SendMsg message = new SendMsg(uuid);
    message.addCallCommand("execute");
    message.addExecuteAppName(application);
    if(arguments != null) {
      message.addExecuteAppArg(arguments);
    }
    return client.sendMessage(message);
  }

  @Override public void dial() throws CallException {
    
  }

  @Override public void disconnect() throws CallException {
    // Nothing to do.
  }

  @Override public CallManager getCallManager() {
    return null;
  }

  @Override public String getDirection() {
    return direction;
  }

  @Override public String getId() {
    return uuid;
  }

  @Override public String getOriginator() {
    return null;
  }
  
  @Override public MediaPlayer getPlayer() {
    return null;
  }

  @Override public String getRecipient() {
    return null;
  }
  
  @Override public MediaRecorder getRecorder() {
    return null;
  }
  
  @Override public DtmfDetector getSignalDetector() {
    return null;
  }

  @Override public SpeechSynthesizer getSpeechSynthesizer() {
    return null;
  }

  @Override public String getStatus() {
    return null;
  }

  @Override public void hangup() {
	assertState(IN_PROGRESS);
    final CommandResponse response = execute("hangup", null);
    if(!response.isOk()) {
      LOGGER.error(response.getReplyText());
    }
    setState(COMPLETED);
  }

  @Override public boolean isBridged() {
    return false;
  }

  @Override public boolean isConnected() {
    return false;
  }

  @Override public boolean isInConference() {
    return false;
  }

  @Override public void join(final Conference conference) throws CallException {
    
  }

  @Override public void leave(final Conference conference) throws CallException {
    
  }

  @Override public void reject() {
	assertState(RINGING);
    final CommandResponse response = execute("respond", "486");
    if(!response.isOk()) {
      LOGGER.error(response.getReplyText());
    }
    setState(CANCELLED);
  }

  @Override public void unbridge(final Call call) throws CallException {
    
  }
}
