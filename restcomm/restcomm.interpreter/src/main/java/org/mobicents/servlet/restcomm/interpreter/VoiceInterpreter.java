/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2014, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */
package org.mobicents.servlet.restcomm.interpreter;

import static akka.pattern.Patterns.ask;
import static org.mobicents.servlet.restcomm.interpreter.rcml.Verbs.dial;
import static org.mobicents.servlet.restcomm.interpreter.rcml.Verbs.fax;
import static org.mobicents.servlet.restcomm.interpreter.rcml.Verbs.gather;
import static org.mobicents.servlet.restcomm.interpreter.rcml.Verbs.hangup;
import static org.mobicents.servlet.restcomm.interpreter.rcml.Verbs.pause;
import static org.mobicents.servlet.restcomm.interpreter.rcml.Verbs.play;
import static org.mobicents.servlet.restcomm.interpreter.rcml.Verbs.record;
import static org.mobicents.servlet.restcomm.interpreter.rcml.Verbs.redirect;
import static org.mobicents.servlet.restcomm.interpreter.rcml.Verbs.reject;
import static org.mobicents.servlet.restcomm.interpreter.rcml.Verbs.say;
import static org.mobicents.servlet.restcomm.interpreter.rcml.Verbs.sms;
import static org.mobicents.servlet.restcomm.interpreter.rcml.Verbs.email;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.apache.commons.configuration.Configuration;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.mobicents.servlet.restcomm.asr.AsrResponse;
import org.mobicents.servlet.restcomm.cache.DiskCacheResponse;
import org.mobicents.servlet.restcomm.dao.CallDetailRecordsDao;
import org.mobicents.servlet.restcomm.dao.DaoManager;
import org.mobicents.servlet.restcomm.dao.NotificationsDao;
import org.mobicents.servlet.restcomm.api.EmailResponse;
import org.mobicents.servlet.restcomm.entities.CallDetailRecord;
import org.mobicents.servlet.restcomm.entities.Notification;
import org.mobicents.servlet.restcomm.entities.Sid;
import org.mobicents.servlet.restcomm.fax.FaxResponse;
import org.mobicents.servlet.restcomm.fsm.Action;
import org.mobicents.servlet.restcomm.fsm.FiniteStateMachine;
import org.mobicents.servlet.restcomm.fsm.State;
import org.mobicents.servlet.restcomm.fsm.Transition;
import org.mobicents.servlet.restcomm.http.client.DownloaderResponse;
import org.mobicents.servlet.restcomm.http.client.HttpRequestDescriptor;
import org.mobicents.servlet.restcomm.interpreter.rcml.Attribute;
import org.mobicents.servlet.restcomm.interpreter.rcml.End;
import org.mobicents.servlet.restcomm.interpreter.rcml.GetNextVerb;
import org.mobicents.servlet.restcomm.interpreter.rcml.Nouns;
import org.mobicents.servlet.restcomm.interpreter.rcml.Tag;
import org.mobicents.servlet.restcomm.interpreter.rcml.ParserFailed;
import org.mobicents.servlet.restcomm.mscontrol.messages.MediaGroupResponse;
import org.mobicents.servlet.restcomm.mscontrol.messages.Mute;
import org.mobicents.servlet.restcomm.mscontrol.messages.Play;
import org.mobicents.servlet.restcomm.mscontrol.messages.StartRecording;
import org.mobicents.servlet.restcomm.mscontrol.messages.StopMediaGroup;
import org.mobicents.servlet.restcomm.mscontrol.messages.Unmute;
import org.mobicents.servlet.restcomm.patterns.Observe;
import org.mobicents.servlet.restcomm.patterns.StopObserving;
import org.mobicents.servlet.restcomm.sms.SmsServiceResponse;
import org.mobicents.servlet.restcomm.sms.SmsSessionResponse;
import org.mobicents.servlet.restcomm.telephony.AddParticipant;
import org.mobicents.servlet.restcomm.telephony.Answer;
import org.mobicents.servlet.restcomm.telephony.BridgeManagerResponse;
import org.mobicents.servlet.restcomm.telephony.BridgeStateChanged;
import org.mobicents.servlet.restcomm.telephony.CallInfo;
import org.mobicents.servlet.restcomm.telephony.CallManagerResponse;
import org.mobicents.servlet.restcomm.telephony.CallResponse;
import org.mobicents.servlet.restcomm.telephony.CallStateChanged;
import org.mobicents.servlet.restcomm.telephony.Cancel;
import org.mobicents.servlet.restcomm.telephony.ConferenceCenterResponse;
import org.mobicents.servlet.restcomm.telephony.ConferenceInfo;
import org.mobicents.servlet.restcomm.telephony.ConferenceModeratorPresent;
import org.mobicents.servlet.restcomm.telephony.ConferenceResponse;
import org.mobicents.servlet.restcomm.telephony.ConferenceStateChanged;
import org.mobicents.servlet.restcomm.telephony.CreateBridge;
import org.mobicents.servlet.restcomm.telephony.CreateCall;
import org.mobicents.servlet.restcomm.telephony.CreateConference;
import org.mobicents.servlet.restcomm.telephony.DestroyCall;
import org.mobicents.servlet.restcomm.telephony.DestroyConference;
import org.mobicents.servlet.restcomm.telephony.Dial;
import org.mobicents.servlet.restcomm.telephony.GetCallInfo;
import org.mobicents.servlet.restcomm.telephony.GetConferenceInfo;
import org.mobicents.servlet.restcomm.telephony.GetRelatedCall;
import org.mobicents.servlet.restcomm.telephony.Hangup;
import org.mobicents.servlet.restcomm.telephony.JoinCalls;
import org.mobicents.servlet.restcomm.telephony.Reject;
import org.mobicents.servlet.restcomm.telephony.RemoveParticipant;
import org.mobicents.servlet.restcomm.telephony.StartBridge;
import org.mobicents.servlet.restcomm.telephony.StopBridge;
import org.mobicents.servlet.restcomm.telephony.StopConference;
import org.mobicents.servlet.restcomm.telephony.CallFail;
import org.mobicents.servlet.restcomm.tts.api.SpeechSynthesizerResponse;
import org.mobicents.servlet.restcomm.util.UriUtils;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.ReceiveTimeout;
import akka.actor.UntypedActorContext;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.util.Timeout;

/**
 * @author thomas.quintana@telestax.com (Thomas Quintana)
 * @author jean.deruelle@telestax.com
 * @author gvagenas@telestax.com
 * @author amit.bhayani@telestax.com (Amit Bhayani)
 * @author pavel.slegr@telestax.com
 */
public final class VoiceInterpreter extends BaseVoiceInterpreter {
    // Logger.
    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);
    // States for the FSM.
    private final State startDialing;
    private final State processingDialChildren;
    private final State acquiringOutboundCallInfo;
    private final State forking;
    // private final State joiningCalls;
    private final State creatingBridge;
    private final State initializingBridge;
    private final State bridging;
    private final State bridged;
    private final State finishDialing;
    private final State acquiringConferenceInfo;
    private final State joiningConference;
    private final State conferencing;
    private final State finishConferencing;
    private final State downloadingRcml;
    private final State downloadingFallbackRcml;
    private final State initializingCall;
    // private final State initializedCall;
    private final State ready;
    private final State notFound;
    private final State rejecting;
    private final State finished;

    // FSM.
    // The conference manager.
    private final ActorRef conferenceManager;

    // State for outbound calls.
    private boolean isForking;
    private List<ActorRef> dialBranches;
    private List<Tag> dialChildren;
    private Map<ActorRef, Tag> dialChildrenWithAttributes;

    // The conferencing stuff.
    private ActorRef conference;
    private ConferenceInfo conferenceInfo;
    private ConferenceStateChanged.State conferenceState;
    private boolean muteCall;
    private boolean startConferenceOnEnter = true;
    private ActorRef confSubVoiceInterpreter;
    private Attribute dialRecordAttribute;
    private boolean dialActionExecuted = false;
    private ActorRef sender;
    private boolean liveCallModification = false;
    private boolean recordingCall = true;
    protected boolean isParserFailed = false;

    // Call bridging
    private final ActorRef bridgeManager;
    private ActorRef bridge;

    public VoiceInterpreter(final Configuration configuration, final Sid account, final Sid phone, final String version,
                            final URI url, final String method, final URI fallbackUrl, final String fallbackMethod, final URI statusCallback,
                            final String statusCallbackMethod, final String emailAddress, final ActorRef callManager,
                            final ActorRef conferenceManager, final ActorRef bridgeManager, final ActorRef sms, final DaoManager storage, final ActorRef monitoring) {
        super();
        final ActorRef source = self();
        downloadingRcml = new State("downloading rcml", new DownloadingRcml(source), null, null);
        downloadingFallbackRcml = new State("downloading fallback rcml", new DownloadingFallbackRcml(source), null, null);
        initializingCall = new State("initializing call", new InitializingCall(source), null, null);
        // initializedCall = new State("initialized call", new InitializedCall(source), new PostInitializedCall(source));
        ready = new State("ready", new Ready(source), null, null);
        notFound = new State("notFound", new NotFound(source), null, null);
        rejecting = new State("rejecting", new Rejecting(source), null, null);
        startDialing = new State("start dialing", new StartDialing(source), null, null);
        processingDialChildren = new State("processing dial children", new ProcessingDialChildren(source), null, null);
        acquiringOutboundCallInfo = new State("acquiring outbound call info", new AcquiringOutboundCallInfo(source), null, null);
        forking = new State("forking", new Forking(source), null, null);
        // joiningCalls = new State("joining calls", new JoiningCalls(source), null);
        this.creatingBridge = new State("creating bridge", new CreatingBridge(source), null, null);
        this.initializingBridge = new State("initializing bridge", new InitializingBridge(source), null, null);
        this.bridging = new State("bridging", new Bridging(source), null, null);
        bridged = new State("bridged", new Bridged(source), null, null);
        finishDialing = new State("finish dialing", new FinishDialing(source), null, null);
        acquiringConferenceInfo = new State("acquiring conference info", new AcquiringConferenceInfo(source), null, null);
        joiningConference = new State("joining conference", new JoiningConference(source), null, null);
        conferencing = new State("conferencing", new Conferencing(source), null, null);
        finishConferencing = new State("finish conferencing", new FinishConferencing(source), null, null);
        finished = new State("finished", new Finished(source), null, null);
        /*
         * dialing = new State("dialing", null, null); bridging = new State("bridging", null, null); conferencing = new
         * State("conferencing", null, null);
         */
        transitions.add(new Transition(acquiringAsrInfo, finished));
        transitions.add(new Transition(acquiringSynthesizerInfo, finished));
        transitions.add(new Transition(acquiringCallInfo, initializingCall));
        transitions.add(new Transition(acquiringCallInfo, downloadingRcml));
        transitions.add(new Transition(acquiringCallInfo, finished));
        transitions.add(new Transition(initializingCall, downloadingRcml));
        transitions.add(new Transition(initializingCall, ready));
        transitions.add(new Transition(initializingCall, finishDialing));
        transitions.add(new Transition(initializingCall, hangingUp));
        transitions.add(new Transition(initializingCall, finished));
        transitions.add(new Transition(downloadingRcml, ready));
        transitions.add(new Transition(downloadingRcml, notFound));
        transitions.add(new Transition(downloadingRcml, downloadingFallbackRcml));
        transitions.add(new Transition(downloadingRcml, hangingUp));
        transitions.add(new Transition(downloadingRcml, finished));
        transitions.add(new Transition(downloadingFallbackRcml, ready));
        transitions.add(new Transition(downloadingFallbackRcml, hangingUp));
        transitions.add(new Transition(downloadingFallbackRcml, finished));
        transitions.add(new Transition(downloadingFallbackRcml, notFound));
        transitions.add(new Transition(ready, initializingCall));
        transitions.add(new Transition(ready, faxing));
        transitions.add(new Transition(ready, sendingEmail));
        transitions.add(new Transition(ready, pausing));
        transitions.add(new Transition(ready, checkingCache));
        transitions.add(new Transition(ready, caching));
        transitions.add(new Transition(ready, synthesizing));
        transitions.add(new Transition(ready, rejecting));
        transitions.add(new Transition(ready, redirecting));
        transitions.add(new Transition(ready, processingGatherChildren));
        transitions.add(new Transition(ready, creatingRecording));
        transitions.add(new Transition(ready, creatingSmsSession));
        transitions.add(new Transition(ready, startDialing));
        transitions.add(new Transition(ready, hangingUp));
        transitions.add(new Transition(ready, finished));
        transitions.add(new Transition(pausing, ready));
        transitions.add(new Transition(pausing, finished));
        transitions.add(new Transition(rejecting, finished));
        transitions.add(new Transition(faxing, ready));
        transitions.add(new Transition(faxing, finished));
        transitions.add(new Transition(sendingEmail, ready));
        transitions.add(new Transition(sendingEmail, finished));
        transitions.add(new Transition(sendingEmail, finishDialing));
        transitions.add(new Transition(caching, finished));
        transitions.add(new Transition(playing, ready));
        transitions.add(new Transition(playing, finished));
        transitions.add(new Transition(synthesizing, finished));
        transitions.add(new Transition(redirecting, ready));
        transitions.add(new Transition(redirecting, finished));
        transitions.add(new Transition(creatingRecording, finished));
        transitions.add(new Transition(finishRecording, ready));
        transitions.add(new Transition(finishRecording, finished));
        transitions.add(new Transition(processingGatherChildren, finished));
        transitions.add(new Transition(gathering, finished));
        transitions.add(new Transition(finishGathering, ready));
        transitions.add(new Transition(finishGathering, finishGathering));
        transitions.add(new Transition(finishGathering, finished));
        transitions.add(new Transition(creatingSmsSession, finished));
        transitions.add(new Transition(sendingSms, ready));
        transitions.add(new Transition(sendingSms, startDialing));
        transitions.add(new Transition(sendingSms, finished));
        transitions.add(new Transition(startDialing, processingDialChildren));
        transitions.add(new Transition(startDialing, acquiringConferenceInfo));
        transitions.add(new Transition(startDialing, faxing));
        transitions.add(new Transition(startDialing, sendingEmail));
        transitions.add(new Transition(startDialing, pausing));
        transitions.add(new Transition(startDialing, checkingCache));
        transitions.add(new Transition(startDialing, caching));
        transitions.add(new Transition(startDialing, synthesizing));
        transitions.add(new Transition(startDialing, redirecting));
        transitions.add(new Transition(startDialing, processingGatherChildren));
        transitions.add(new Transition(startDialing, creatingRecording));
        transitions.add(new Transition(startDialing, creatingSmsSession));
        transitions.add(new Transition(startDialing, startDialing));
        transitions.add(new Transition(startDialing, hangingUp));
        transitions.add(new Transition(startDialing, finished));
        transitions.add(new Transition(processingDialChildren, processingDialChildren));
        transitions.add(new Transition(processingDialChildren, forking));
        transitions.add(new Transition(processingDialChildren, hangingUp));
        transitions.add(new Transition(processingDialChildren, finished));
        transitions.add(new Transition(forking, acquiringOutboundCallInfo));
        transitions.add(new Transition(forking, finishDialing));
        transitions.add(new Transition(forking, hangingUp));
        transitions.add(new Transition(forking, finished));
        // transitions.add(new Transition(acquiringOutboundCallInfo, joiningCalls));
        transitions.add(new Transition(acquiringOutboundCallInfo, hangingUp));
        transitions.add(new Transition(acquiringOutboundCallInfo, finished));
        transitions.add(new Transition(acquiringOutboundCallInfo, creatingBridge));
        transitions.add(new Transition(creatingBridge, initializingBridge));
        transitions.add(new Transition(creatingBridge, finishDialing));
        transitions.add(new Transition(initializingBridge, bridging));
        transitions.add(new Transition(initializingBridge, hangingUp));
        transitions.add(new Transition(bridging, bridged));
        transitions.add(new Transition(bridging, finishDialing));
        // transitions.add(new Transition(joiningCalls, finishDialing));
        // transitions.add(new Transition(joiningCalls, bridged));
        // transitions.add(new Transition(joiningCalls, hangingUp));
        // transitions.add(new Transition(joiningCalls, finished));
        transitions.add(new Transition(bridged, finishDialing));
        transitions.add(new Transition(bridged, finished));
        transitions.add(new Transition(finishDialing, ready));
        transitions.add(new Transition(finishDialing, faxing));
        transitions.add(new Transition(finishDialing, sendingEmail));
        transitions.add(new Transition(finishDialing, pausing));
        transitions.add(new Transition(finishDialing, checkingCache));
        transitions.add(new Transition(finishDialing, caching));
        transitions.add(new Transition(finishDialing, synthesizing));
        transitions.add(new Transition(finishDialing, redirecting));
        transitions.add(new Transition(finishDialing, processingGatherChildren));
        transitions.add(new Transition(finishDialing, creatingRecording));
        transitions.add(new Transition(finishDialing, creatingSmsSession));
        transitions.add(new Transition(finishDialing, startDialing));
        transitions.add(new Transition(finishDialing, hangingUp));
        transitions.add(new Transition(finishDialing, finished));
        transitions.add(new Transition(finishDialing, initializingCall));
        transitions.add(new Transition(acquiringConferenceInfo, joiningConference));
        transitions.add(new Transition(acquiringConferenceInfo, hangingUp));
        transitions.add(new Transition(acquiringConferenceInfo, finished));
        transitions.add(new Transition(joiningConference, conferencing));
        transitions.add(new Transition(joiningConference, hangingUp));
        transitions.add(new Transition(joiningConference, finished));
        transitions.add(new Transition(conferencing, finishConferencing));
        transitions.add(new Transition(conferencing, hangingUp));
        transitions.add(new Transition(conferencing, finished));
        transitions.add(new Transition(finishConferencing, ready));
        transitions.add(new Transition(finishConferencing, faxing));
        transitions.add(new Transition(finishConferencing, sendingEmail));
        transitions.add(new Transition(finishConferencing, pausing));
        transitions.add(new Transition(finishConferencing, checkingCache));
        transitions.add(new Transition(finishConferencing, caching));
        transitions.add(new Transition(finishConferencing, synthesizing));
        transitions.add(new Transition(finishConferencing, redirecting));
        transitions.add(new Transition(finishConferencing, processingGatherChildren));
        transitions.add(new Transition(finishConferencing, creatingRecording));
        transitions.add(new Transition(finishConferencing, creatingSmsSession));
        transitions.add(new Transition(finishConferencing, startDialing));
        transitions.add(new Transition(finishConferencing, hangingUp));
        transitions.add(new Transition(finishConferencing, finished));
        transitions.add(new Transition(hangingUp, finished));
        transitions.add(new Transition(hangingUp, finishDialing));
        transitions.add(new Transition(uninitialized, finished));
        // Initialize the FSM.
        this.fsm = new FiniteStateMachine(uninitialized, transitions);
        // Initialize the runtime stuff.
        this.accountId = account;
        this.phoneId = phone;
        this.version = version;
        this.url = url;
        this.method = method;
        this.fallbackUrl = fallbackUrl;
        this.fallbackMethod = fallbackMethod;
        this.statusCallback = statusCallback;
        this.statusCallbackMethod = statusCallbackMethod;
        this.emailAddress = emailAddress;
        this.configuration = configuration;
        this.callManager = callManager;
        this.conferenceManager = conferenceManager;
        this.bridgeManager = bridgeManager;
        this.asrService = asr(configuration.subset("speech-recognizer"));
        this.faxService = fax(configuration.subset("fax-service"));
        this.smsService = sms;
        this.smsSessions = new HashMap<Sid, ActorRef>();
        this.storage = storage;
        this.synthesizer = tts(configuration.subset("speech-synthesizer"));
        final Configuration runtime = configuration.subset("runtime-settings");
        String path = runtime.getString("cache-path");
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        path = path + accountId.toString();
        cachePath = path;
        String uri = runtime.getString("cache-uri");
        if (!uri.endsWith("/")) {
            uri = uri + "/";
        }
        try {
            uri = UriUtils.resolve(new URI(uri)).toString();
        } catch (URISyntaxException e) {
            logger.error("URISyntaxException while trying to resolve Cache URI: " + e);
        }
        uri = uri + accountId.toString();
        this.cache = cache(path, uri);
        this.downloader = downloader();
        this.monitoring = monitoring;
    }

    private boolean is(State state) {
        return this.fsm.state().equals(state);
    }

    private Notification notification(final int log, final int error, final String message) {
        final Notification.Builder builder = Notification.builder();
        final Sid sid = Sid.generate(Sid.Type.NOTIFICATION);
        builder.setSid(sid);
        builder.setAccountSid(accountId);
        builder.setCallSid(callInfo.sid());
        builder.setApiVersion(version);
        builder.setLog(log);
        builder.setErrorCode(error);
        String base = configuration.subset("runtime-settings").getString("error-dictionary-uri");
        try {
            base = UriUtils.resolve(new URI(base)).toString();
        } catch (URISyntaxException e) {
            logger.error("URISyntaxException when trying to resolve Error-Dictionary URI: " + e);
        }
        StringBuilder buffer = new StringBuilder();
        buffer.append(base);
        if (!base.endsWith("/")) {
            buffer.append("/");
        }
        buffer.append(error).append(".html");
        final URI info = URI.create(buffer.toString());
        builder.setMoreInfo(info);
        builder.setMessageText(message);
        final DateTime now = DateTime.now();
        builder.setMessageDate(now);
        if (request != null) {
            builder.setRequestUrl(request.getUri());
            builder.setRequestMethod(request.getMethod());
            builder.setRequestVariables(request.getParametersAsString());
        }
        if (response != null) {
            builder.setResponseHeaders(response.getHeadersAsString());
            final String type = response.getContentType();
            if (type.contains("text/xml") || type.contains("application/xml") || type.contains("text/html")) {
                try {
                    builder.setResponseBody(response.getContentAsString());
                } catch (final IOException exception) {
                    logger.error(
                            "There was an error while reading the contents of the resource " + "located @ " + url.toString(),
                            exception);
                }
            }
        }
        buffer = new StringBuilder();
        buffer.append("/").append(version).append("/Accounts/");
        buffer.append(accountId.toString()).append("/Notifications/");
        buffer.append(sid.toString());
        final URI uri = URI.create(buffer.toString());
        builder.setUri(uri);
        return builder.build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onReceive(final Object message) throws Exception {
        final Class<?> klass = message.getClass();
        final State state = fsm.state();
        sender = sender();
        ActorRef self = self();

        if (logger.isInfoEnabled()) {
            logger.info(" ********** VoiceInterpreter's " + self().path() + " Current State: " + state.toString());
            logger.info(" ********** VoiceInterpreter's " + self().path() + " Processing Message: " + klass.getName());
        }

        if (StartInterpreter.class.equals(klass)) {
            fsm.transition(message, acquiringAsrInfo);
        } else if (AsrResponse.class.equals(klass)) {
            if (outstandingAsrRequests > 0) {
                asrResponse(message);
            } else {
                fsm.transition(message, acquiringSynthesizerInfo);
            }
        } else if (SpeechSynthesizerResponse.class.equals(klass)) {
            if (acquiringSynthesizerInfo.equals(state)) {
                fsm.transition(message, acquiringCallInfo);
            } else if (processingGatherChildren.equals(state) || processingGather) {
                final SpeechSynthesizerResponse<URI> response = (SpeechSynthesizerResponse<URI>) message;
                if (response.succeeded()) {
                    fsm.transition(message, processingGatherChildren);
                } else {
                    fsm.transition(message, hangingUp);
                }
            } else if (synthesizing.equals(state)) {
                final SpeechSynthesizerResponse<URI> response = (SpeechSynthesizerResponse<URI>) message;
                if (response.succeeded()) {
                    fsm.transition(message, caching);
                } else {
                    fsm.transition(message, hangingUp);
                }
            }
        } else if (CallResponse.class.equals(klass)) {
            if (forking.equals(state)) {
                // Allow updating of the callInfo at the VoiceInterpreter so that we can do Dial SIP Screening
                // (https://bitbucket.org/telestax/telscale-restcomm/issue/132/implement-twilio-sip-out) accurately from latest
                // response received
                final CallResponse<CallInfo> response = (CallResponse<CallInfo>) message;
                // Check from whom is the message (initial call or outbound call) and update info accordingly
                if (sender == call) {
                    callInfo = response.get();
                } else {
                    outboundCallInfo = response.get();
                }
            } else if (acquiringCallInfo.equals(state)) {
                final CallResponse<CallInfo> response = (CallResponse<CallInfo>) message;
                // Check from whom is the message (initial call or outbound call) and update info accordingly
                if (sender == call) {
                    callInfo = response.get();
                } else {
                    outboundCallInfo = response.get();
                }
                final String direction = callInfo.direction();
                if ("inbound".equals(direction)) {
                    fsm.transition(message, downloadingRcml);
                } else {
                    fsm.transition(message, initializingCall);
                }
            } else if (acquiringOutboundCallInfo.equals(state)) {
                final CallResponse<CallInfo> response = (CallResponse<CallInfo>) message;
                this.outboundCallInfo = response.get();
                fsm.transition(message, creatingBridge);
            }
        } else if (CallStateChanged.class.equals(klass)) {
            final CallStateChanged event = (CallStateChanged) message;
            callState = event.state();
            logger.info("VoiceInterpreter received CallStateChanged event: "+callState);
            if (CallStateChanged.State.RINGING == event.state()) {
                if (forking.equals(state)) {
                    outboundCall = sender;
                }
                // update db and callback statusCallback url.
            } else if (CallStateChanged.State.IN_PROGRESS == event.state()) {
                if (initializingCall.equals(state) || rejecting.equals(state)) {
                    if (parser != null) {
                        fsm.transition(message, ready);
                    } else {
                        fsm.transition(message, downloadingRcml);
                    }
                } else if (joiningConference.equals(state)) {
                    fsm.transition(message, conferencing);
                } else if (forking.equals(state)) {
                    if (outboundCall == null || !sender.equals(call)) {
                        outboundCall = sender;
                    }
                    fsm.transition(message, acquiringOutboundCallInfo);
                } else if (conferencing.equals(state)) {
                    // Call left the conference successfully
                    if (!liveCallModification) {
                        // Hang up the call
                        final Hangup hangup = new Hangup();
                        call.tell(hangup, self);
                    } else {
                        // XXX start processing new RCML and give instructions to call
                    }
                }
            } else if (CallStateChanged.State.NO_ANSWER == event.state() || CallStateChanged.State.COMPLETED == event.state()
                    || CallStateChanged.State.FAILED == event.state()) {
                if (bridging.equals(state)) {
                    fsm.transition(message, finishDialing);
                } else if (bridged.equals(state) && (sender.equals(outboundCall) || outboundCall != null)) {
                    fsm.transition(message, finishDialing);
                } else
                    // changed for https://bitbucket.org/telestax/telscale-restcomm/issue/132/ so that we can do Dial SIP Screening
                    if (forking.equals(state) && ((dialBranches != null && dialBranches.contains(sender)) || outboundCall == null)) {
                        fsm.transition(message, finishDialing);
                    } else if (creatingRecording.equals(state)) {
                        // Ask callMediaGroup to stop recording so we have the recording file available
                        // Issue #197: https://telestax.atlassian.net/browse/RESTCOMM-197
                        call.tell(new StopMediaGroup(), null);
                        fsm.transition(message, finishRecording);
                    } else if ((bridged.equals(state) || forking.equals(state)) && call == sender()) {
                        if (!dialActionExecuted) {
                            fsm.transition(message, finishDialing);
                        }
                    } else if (finishDialing.equals(state)) {
                        removeDialBranch(message, sender);
                        return;
                    } else {
                        if (!finishDialing.equals(state))
                            fsm.transition(message, finished);
                    }
            } else if (CallStateChanged.State.BUSY == event.state()) {
                if (forking.equals(state)) {
                    if (sender==call) {
                        //Move to finishDialing to clear the call and cancel all branches
                        fsm.transition(message, finishDialing);
                    } else if (dialBranches.contains(sender)) {
                        Attribute attribute = null;
                        if (verb != null) {
                            attribute = verb.attribute("action");
                        }
                        //Busy was sent by one of the branches, remove the branch, execute dial action and continue
                        logger.info("call state changed. New call state: " + ((CallStateChanged) message).state() + "Sender in the dialBranches: " + dialBranches.contains(sender));
                        ActorRef branch = dialBranches.remove(dialBranches.indexOf(sender));
                        logger.info("Will cancel branch: " + branch.toString());
                        branch.tell(new Cancel(), self());
                        if (dialBranches.size() > 0) {
                            return;
                        } else if (attribute == null) {
                            fsm.transition(message, finishDialing);
                        } else  {
                            fsm.transition(ReceiveTimeout.getInstance(), finishDialing);
                        }
                    }
                } else {
                    fsm.transition(message, finishDialing);
                }
            } else if (CallStateChanged.State.CANCELED == event.state()) {
                if (state == initializingBridge || state == acquiringOutboundCallInfo || state == bridging) {
                    return;
                } else {
                    if (sender == call) {
                        //Move to finished state only if the call actor send the Cancel.
                        fsm.transition(message, finished);
                    } else {
                        //Do nothing, this is a Cancel from a dial branch previously canceled
                        return;
                    }
                }
            }
        } else if (CallManagerResponse.class.equals(klass)) {
            final CallManagerResponse<ActorRef> response = (CallManagerResponse<ActorRef>) message;
            if (response.succeeded()) {
                if (startDialing.equals(state)) {
                    fsm.transition(message, processingDialChildren);
                } else if (processingDialChildren.equals(state)) {
                    fsm.transition(message, processingDialChildren);
                }
            } else {
                fsm.transition(message, hangingUp);
            }
        } else if (StartForking.class.equals(klass)) {
            fsm.transition(message, processingDialChildren);
        } else if (ConferenceCenterResponse.class.equals(klass)) {
            if (startDialing.equals(state)) {
                fsm.transition(message, acquiringConferenceInfo);
            }
        } else if (Fork.class.equals(klass)) {
            if (processingDialChildren.equals(state)) {
                fsm.transition(message, forking);
            }
        } else if (ConferenceResponse.class.equals(klass)) {
            if (acquiringConferenceInfo.equals(state)) {
                fsm.transition(message, joiningConference);
            }
        } else if (ConferenceStateChanged.class.equals(klass)) {
            final ConferenceStateChanged event = (ConferenceStateChanged) message;
            switch (event.state()) {
                case RUNNING_MODERATOR_PRESENT:
                    conferenceState = event.state();
                    conferenceStateModeratorPresent(message);
                    break;
                default:
                    break;
            }

            // !!IMPORTANT!!
            // Do not listen to COMPLETED nor FAILED conference state changes
            // When a conference stops it will ask all its calls to Leave
            // Then the call state will change and the voice interpreter will take proper action then
        } else if (DownloaderResponse.class.equals(klass)) {
            final DownloaderResponse response = (DownloaderResponse) message;
            if (logger.isDebugEnabled()) {
                logger.debug("Rcml URI : " + response.get().getURI() + "response succeeded " + response.succeeded()
                        + ", statusCode " + response.get().getStatusCode());
            }
            if (response.succeeded() && HttpStatus.SC_OK == response.get().getStatusCode()) {
                if (dialBranches == null || dialBranches.size()==0) {
                    logger.info("Downloader response is success, moving to Ready state");
                    fsm.transition(message, ready);
                } else {
                    return;
                }
            } else if (downloadingRcml.equals(state) && fallbackUrl != null) {
                fsm.transition(message, downloadingFallbackRcml);
            } else if (response.succeeded() && HttpStatus.SC_NOT_FOUND == response.get().getStatusCode()) {
                fsm.transition(message, notFound);
            } else {
                call.tell(new CallFail(response.error()), self);
//                    fsm.transition(message, finished);
            }
        } else if (DiskCacheResponse.class.equals(klass)) {
            final DiskCacheResponse response = (DiskCacheResponse) message;
            if (response.succeeded()) {
                if (caching.equals(state) || checkingCache.equals(state)) {
                    if (play.equals(verb.name()) || say.equals(verb.name())) {
                        fsm.transition(message, playing);
                    } else if (fax.equals(verb.name())) {
                        fsm.transition(message, faxing);
                    } else if (email.equals(verb.name())) {
                        fsm.transition(message, sendingEmail);
                    }
                } else if (processingGatherChildren.equals(state)) {
                    fsm.transition(message, processingGatherChildren);
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("DiskCacheResponse is " + response.toString());
                }
                if (checkingCache.equals(state) || processingGatherChildren.equals(state)) {
                    fsm.transition(message, synthesizing);
                } else {
                    fsm.transition(message, hangingUp);
                }
            }
        } else if (ParserFailed.class.equals(klass)) {
          logger.info("ParserFailed received. Will stop the call");
            isParserFailed = true;
            fsm.transition(message, hangingUp);
        } else if (Tag.class.equals(klass)) {
            // final Tag verb = (Tag) message;
            verb = (Tag) message;
            if (CallStateChanged.State.RINGING == callState) {
                if (reject.equals(verb.name())) {
                    fsm.transition(message, rejecting);
                } else if (pause.equals(verb.name())) {
                    fsm.transition(message, pausing);
                } else {
                    fsm.transition(message, initializingCall);
                }
            } else if (dial.equals(verb.name())) {
                dialRecordAttribute = verb.attribute("record");
                fsm.transition(message, startDialing);
            } else if (fax.equals(verb.name())) {
                fsm.transition(message, caching);
            } else if (play.equals(verb.name())) {
                fsm.transition(message, caching);
            } else if (say.equals(verb.name())) {
                // fsm.transition(message, synthesizing);
                fsm.transition(message, checkingCache);
            } else if (gather.equals(verb.name())) {
                gatherVerb = verb;
                fsm.transition(message, processingGatherChildren);
            } else if (pause.equals(verb.name())) {
                fsm.transition(message, pausing);
            } else if (hangup.equals(verb.name())) {
                fsm.transition(message, hangingUp);
            } else if (redirect.equals(verb.name())) {
                fsm.transition(message, redirecting);
            } else if (record.equals(verb.name())) {
                fsm.transition(message, creatingRecording);
            } else if (sms.equals(verb.name())) {
                fsm.transition(message, creatingSmsSession);
            } else if (email.equals(verb.name())) {
                fsm.transition(message, sendingEmail);
            } else {
                invalidVerb(verb);
            }
        } else if (End.class.equals(klass)) {
            if (callState.equals(CallStateChanged.State.COMPLETED)) {
                fsm.transition(message, finished);
            } else {
                if (!isParserFailed) {
                    logger.info("End tag received will move to hangup the call");
                    fsm.transition(message, hangingUp);
                } else {
                    logger.info("End tag received but parser failed earlier so hangup would have been already sent to the call");
                }

            }
        } else if (StartGathering.class.equals(klass)) {
            fsm.transition(message, gathering);
        } else if (MediaGroupResponse.class.equals(klass)) {
            final MediaGroupResponse<String> response = (MediaGroupResponse<String>) message;
            logger.info("MediaGroupResponse, succeeded: " + response.succeeded() + "  " + response.cause());
            if (response.succeeded()) {
                if (playingRejectionPrompt.equals(state)) {
                    fsm.transition(message, hangingUp);
                } else if (playing.equals(state)) {
                    fsm.transition(message, ready);
                } else if (creatingRecording.equals(state)) {
                    fsm.transition(message, finishRecording);
                } // This is either MMS collected digits or SIP INFO DTMF. If the DTMF is from SIP INFO, then more DTMF might
                // come later
                else if (gathering.equals(state) || (finishGathering.equals(state) && !super.dtmfReceived)) {
                    final MediaGroupResponse<String> dtmfResponse = (MediaGroupResponse<String>) message;
                    if (sender == call) {
                        // DTMF using SIP INFO, check if all digits collected here
                        collectedDigits.append(dtmfResponse.get());
                        // Collected digits == requested num of digits the complete the collect digits
                        if (numberOfDigits != Short.MAX_VALUE) {
                            if (collectedDigits.length() == numberOfDigits) {
                                dtmfReceived = true;
                                fsm.transition(message, finishGathering);
                            } else {
                                dtmfReceived = false;
                                return;
                            }
                        } else {
                            // If collected digits have finish on key at the end then complete the collect digits
                            if (collectedDigits.toString().endsWith(finishOnKey)) {
                                dtmfReceived = true;
                                fsm.transition(message, finishGathering);
                            } else {
                                dtmfReceived = false;
                                return;
                            }
                        }
                    } else {
                        collectedDigits.append(dtmfResponse.get());
                        fsm.transition(message, finishGathering);
                    }
                } else if (bridging.equals(state)) {
                    // Finally proceed with call bridging
                    final JoinCalls bridgeCalls = new JoinCalls(call, outboundCall);
                    bridge.tell(bridgeCalls, self);
                }
            } else {
                fsm.transition(message, hangingUp);
            }
        } else if (SmsServiceResponse.class.equals(klass)) {
            final SmsServiceResponse<ActorRef> response = (SmsServiceResponse<ActorRef>) message;
            if (response.succeeded()) {
                if (creatingSmsSession.equals(state)) {
                    fsm.transition(message, sendingSms);
                }
            } else {
                fsm.transition(message, hangingUp);
            }
        }
        // else if(AsrResponse.class.equals(klass)) {
        // asrResponse(message);
        // }
        else if (SmsSessionResponse.class.equals(klass)) {
            smsResponse(message);
        } else if (FaxResponse.class.equals(klass)) {
            fsm.transition(message, ready);
        } else if (EmailResponse.class.equals(klass)) {
            final EmailResponse<?> response = (EmailResponse<?>) message;
            if (!response.succeeded()) {
                logger.error(
                        "There was an error while sending an email :" + response.error(),
                        response.cause());
            }
            fsm.transition(message, ready);
        } else if (StopInterpreter.class.equals(klass)) {
            this.liveCallModification = ((StopInterpreter) message).isLiveCallModification();
            if (CallStateChanged.State.IN_PROGRESS.equals(callState) && !liveCallModification) {
                fsm.transition(message, hangingUp);
            } else {
                fsm.transition(message, finished);
            }
        } else if (message instanceof ReceiveTimeout) {
            if (pausing.equals(state)) {
                fsm.transition(message, ready);
            } else if (conferencing.equals(state)) {
                fsm.transition(message, finishConferencing);
            } else if (forking.equals(state)) {
                fsm.transition(message, finishDialing);
            } else if (bridged.equals(state)) {
                fsm.transition(message, finishDialing);
            } else if (bridging.equals(state)) {
                fsm.transition(message, finishDialing);
            }
        } else if (BridgeManagerResponse.class.equals(klass)) {
            onBridgeManagerResponse((BridgeManagerResponse) message, self, sender);
        } else if (BridgeStateChanged.class.equals(klass)) {
            onBridgeStateChanged((BridgeStateChanged) message, self, sender);
        } else if (GetRelatedCall.class.equals(klass)) {
            onGetRelatedCall((GetRelatedCall) message, self, sender);
        }
    }

    private void removeDialBranch(Object message, ActorRef sender) {
        Attribute attribute = null;
        if (verb != null) {
            attribute = verb.attribute("action");
        }
        if (dialBranches != null && dialBranches.contains(sender)) {
            logger.info("Dial branch new call state: " + ((CallStateChanged) message).state().toString() + " call path: " + sender().path() + " VI state: " + fsm.state());
            dialBranches.remove(sender);
//            if (attribute != null) {
//                executeDialAction(message, sender);
//            }
            sender.tell(new Cancel(), self());
            if (dialBranches.size() > 0) {
                //Wait to check the response from the other branches
                return;
            } else {
                dialChildren = null;
//                outboundCall = null;
                callback();
                //Since there are no more branches, ask for the next RCML
                if (attribute == null) {
                    final GetNextVerb next = GetNextVerb.instance();
                    parser.tell(next, self());
                } else {
                    executeDialAction(message,sender);
                }
            }
        } else if (attribute != null) {
            executeDialAction(message, sender);
        }
    }

    private void onBridgeManagerResponse(BridgeManagerResponse message, ActorRef self, ActorRef sender) throws Exception {
        if (is(creatingBridge)) {
            this.bridge = message.get();
            fsm.transition(message, initializingBridge);
        }
    }

    private void onBridgeStateChanged(BridgeStateChanged message, ActorRef self, ActorRef sender) throws Exception {
        switch (message.getState()) {
            case READY:
                if (is(initializingBridge)) {
                    fsm.transition(message, bridging);
                }
                break;
            case BRIDGED:
                if (is(bridging)) {
                    fsm.transition(message, bridged);
                }
                break;

            case FAILED:
                if (is(initializingBridge)) {
                    fsm.transition(message, hangingUp);
                }
            default:
                break;
        }
    }

    private void onGetRelatedCall(GetRelatedCall message, ActorRef self, ActorRef sender) {
        final ActorRef callActor = message.call();
        if (outboundCall != null) {
            if (callActor.equals(outboundCall)) {
                sender.tell(call, self);
            } else if (callActor.equals(call)) {
                sender.tell(outboundCall, self);
            }
        } else {
            // If previously that was a p2p call that changed to conference (for hold)
            // and now it changes again to a new url, the outbound call is null since
            // When we joined the call to the conference, we made outboundCall = null;
            sender.tell(new org.mobicents.servlet.restcomm.telephony.NotFound(), sender);
        }
    }

    private void conferenceStateModeratorPresent(final Object message) {
        logger.info("VoiceInterpreter#conferenceStateModeratorPresent will unmute the call: " + call.path().toString());
        call.tell(new Unmute(), self());

        if (confSubVoiceInterpreter != null) {
            logger.info("VoiceInterpreter stopping confSubVoiceInterpreter");

            // Stop the conference back ground music
            final StopInterpreter stop = new StopInterpreter();
            confSubVoiceInterpreter.tell(stop, self());
        }
    }

    List<NameValuePair> parameters() {
        final List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        final String callSid = callInfo.sid().toString();
        parameters.add(new BasicNameValuePair("CallSid", callSid));
        if (outboundCallInfo != null) {
            final String outboundCallSid = outboundCallInfo.sid().toString();
            parameters.add(new BasicNameValuePair("OutboundCallSid", outboundCallSid));
        }
        final String accountSid = accountId.toString();
        parameters.add(new BasicNameValuePair("AccountSid", accountSid));
        final String from = e164(callInfo.from());
        parameters.add(new BasicNameValuePair("From", from));
        final String to = e164(callInfo.to());
        parameters.add(new BasicNameValuePair("To", to));
        final String state = callState.toString();
        parameters.add(new BasicNameValuePair("CallStatus", state));
        parameters.add(new BasicNameValuePair("ApiVersion", version));
        final String direction = callInfo.direction();
        parameters.add(new BasicNameValuePair("Direction", direction));
        final String callerName = (callInfo.fromName() == null || callInfo.fromName().isEmpty()) ? "null" : callInfo.fromName();
        parameters.add(new BasicNameValuePair("CallerName", callerName));
        final String forwardedFrom = (callInfo.forwardedFrom() == null || callInfo.forwardedFrom().isEmpty()) ? "null" : callInfo.forwardedFrom();
        parameters.add(new BasicNameValuePair("ForwardedFrom", forwardedFrom));
        // logger.info("Type " + callInfo.type());
        SipServletResponse lastResponse = callInfo.lastResponse();
        if (CreateCall.Type.SIP == callInfo.type()) {
            // Adding SIP OUT Headers and SipCallId for
            // https://bitbucket.org/telestax/telscale-restcomm/issue/132/implement-twilio-sip-out
            // logger.info("lastResponse " + lastResponse);
            if (lastResponse != null) {
                final int statusCode = lastResponse.getStatus();
                final String method = lastResponse.getMethod();
                // See https://www.twilio.com/docs/sip/receiving-sip-headers
                // Headers on the final SIP response message (any 4xx or 5xx message or the final BYE/200) are posted to the
                // Dial action URL.
                if ((statusCode >= 400 && "INVITE".equalsIgnoreCase(method))
                        || (statusCode >= 200 && statusCode < 300 && "BYE".equalsIgnoreCase(method))) {
                    final String sipCallId = lastResponse.getCallId();
                    parameters.add(new BasicNameValuePair("DialSipCallId", sipCallId));
                    parameters.add(new BasicNameValuePair("DialSipResponseCode", "" + statusCode));
                    processCustomHeaders(lastResponse, "DialSipHeader_", parameters);
                }
            }
        }

        if (lastResponse == null) {
            // Restcomm VoiceInterpreter should check the INVITE for custom headers and pass them to RVD
            // https://telestax.atlassian.net/browse/RESTCOMM-710
            final SipServletRequest invite = callInfo.invite();
            // For outbound calls created with Calls REST API, the invite at this point will be null
            if (invite != null)
                processCustomHeaders(invite, "SipHeader_", parameters);
        } else {
            processCustomHeaders(lastResponse, "SipHeader_", parameters);
        }

        return parameters;
    }

    private void processCustomHeaders(SipServletMessage sipMessage, String prefix, List<NameValuePair> parameters) {
        Iterator<String> headerNames = sipMessage.getHeaderNames();
        while (headerNames.hasNext()) {
            String headerName = headerNames.next();
            if (headerName.startsWith("X-")) {
                logger.debug("%%%%%%%%%%% Indetified customer header: " + headerName);
                parameters.add(new BasicNameValuePair(prefix + headerName, sipMessage.getHeader(headerName)));
            }
        }
    }

    private abstract class AbstractAction implements Action {
        protected final ActorRef source;

        public AbstractAction(final ActorRef source) {
            super();
            this.source = source;
        }

        protected Tag conference(final Tag container) {
            final List<Tag> children = container.children();
            for (final Tag child : children) {
                if (Nouns.conference.equals(child.name())) {
                    return child;
                }
            }
            return null;
        }
    }

    private final class InitializingCall extends AbstractAction {
        public InitializingCall(final ActorRef source) {
            super(source);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void execute(final Object message) throws Exception {
            final Class<?> klass = message.getClass();

            if (CallResponse.class.equals(klass)) {
                // Update the interpreter state.
                final CallResponse<CallInfo> response = (CallResponse<CallInfo>) message;
                callInfo = response.get();
                callState = callInfo.state();
                if (callState.name().equalsIgnoreCase(CallStateChanged.State.IN_PROGRESS.name())) {
                    final CallStateChanged event = new CallStateChanged(CallStateChanged.State.IN_PROGRESS);
                    source.tell(event, source);
                    // fsm.transition(event, acquiringCallMediaGroup);
                    return;
                }

                // Update the storage.
                if (callRecord != null) {
                    callRecord = callRecord.setStatus(callState.toString());
                    final CallDetailRecordsDao records = storage.getCallDetailRecordsDao();
                    records.updateCallDetailRecord(callRecord);
                }

                // Update the application.
                callback();

                // Start dialing.
                call.tell(new Dial(), source);
            } else if (Tag.class.equals(klass)) {
                // Update the interpreter state.
                verb = (Tag) message;

                // Answer the call.
                call.tell(new Answer(), source);
            }
        }
    }

    private final class DownloadingRcml extends AbstractAction {
        public DownloadingRcml(final ActorRef source) {
            super(source);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void execute(final Object message) throws Exception {
            final Class<?> klass = message.getClass();
            final CallDetailRecordsDao records = storage.getCallDetailRecordsDao();
            if (CallResponse.class.equals(klass)) {
                final CallResponse<CallInfo> response = (CallResponse<CallInfo>) message;
                callInfo = response.get();
                callState = callInfo.state();
                if (callInfo.direction().equals("inbound")) {
                    callRecord = records.getCallDetailRecord(callInfo.sid());
                    if (callRecord == null) {
                        // Create a call detail record for the call.
                        final CallDetailRecord.Builder builder = CallDetailRecord.builder();
                        builder.setSid(callInfo.sid());
                        builder.setDateCreated(callInfo.dateCreated());
                        builder.setAccountSid(accountId);
                        builder.setTo(callInfo.to());
                        if (callInfo.fromName() != null) {
                            builder.setCallerName(callInfo.fromName());
                        } else {
                            builder.setCallerName("Unknown");
                        }
                        if (callInfo.from() != null) {
                            builder.setFrom(callInfo.from());
                        } else {
                            builder.setFrom("Unknown");
                        }
                        builder.setForwardedFrom(callInfo.forwardedFrom());
                        builder.setPhoneNumberSid(phoneId);
                        builder.setStatus(callState.toString());
                        final DateTime now = DateTime.now();
                        builder.setStartTime(now);
                        builder.setDirection(callInfo.direction());
                        builder.setApiVersion(version);
                        builder.setPrice(new BigDecimal("0.00"));
                        // TODO implement currency property to be read from Configuration
                        builder.setPriceUnit(Currency.getInstance("USD"));
                        final StringBuilder buffer = new StringBuilder();
                        buffer.append("/").append(version).append("/Accounts/");
                        buffer.append(accountId.toString()).append("/Calls/");
                        buffer.append(callInfo.sid().toString());
                        final URI uri = URI.create(buffer.toString());
                        builder.setUri(uri);

                        builder.setCallPath(call.path().toString());

                        callRecord = builder.build();
                        records.addCallDetailRecord(callRecord);
                        //
                        // if (liveCallModification) {
                        // logger.info("There is no CallRecord for this call but this is a LiveCallModificatin request. Will acquire call media group");
                        // fsm.transition(message, acquiringCallMediaGroup);
                        // return;
                        // }
                    }

                    // Update the application.
                    callback();
                }
            }
            // Ask the downloader to get us the application that will be executed.
            final List<NameValuePair> parameters = parameters();
            request = new HttpRequestDescriptor(url, method, parameters);
            downloader.tell(request, source);
        }
    }

    private final class DownloadingFallbackRcml extends AbstractAction {
        public DownloadingFallbackRcml(final ActorRef source) {
            super(source);
        }

        @Override
        public void execute(final Object message) throws Exception {
            final Class<?> klass = message.getClass();
            // Notify the account of the issue.
            if (DownloaderResponse.class.equals(klass)) {
                final DownloaderResponse result = (DownloaderResponse) message;
                final Throwable cause = result.cause();
                Notification notification = null;
                if (cause instanceof ClientProtocolException) {
                    notification = notification(ERROR_NOTIFICATION, 11206, cause.getMessage());
                } else if (cause instanceof IOException) {
                    notification = notification(ERROR_NOTIFICATION, 11205, cause.getMessage());
                } else if (cause instanceof URISyntaxException) {
                    notification = notification(ERROR_NOTIFICATION, 11100, cause.getMessage());
                }
                if (notification != null) {
                    final NotificationsDao notifications = storage.getNotificationsDao();
                    notifications.addNotification(notification);
                    sendMail(notification);
                }
            }
            // Try to use the fall back url and method.
            final List<NameValuePair> parameters = parameters();
            request = new HttpRequestDescriptor(fallbackUrl, fallbackMethod, parameters);
            downloader.tell(request, source);
        }
    }

    private final class Ready extends AbstractAction {
        public Ready(final ActorRef source) {
            super(source);
        }

        @Override
        public void execute(final Object message) throws IOException {
            final UntypedActorContext context = getContext();
            final State state = fsm.state();
            if (initializingCall.equals(state)) {
                // Update the interpreter state.
                final CallStateChanged event = (CallStateChanged) message;
                callState = event.state();

                // Update the application.
                callback();

                // Update the storage.
                if (callRecord != null) {
                    callRecord = callRecord.setStatus(callState.toString());
                    callRecord = callRecord.setStartTime(DateTime.now());
                    final CallDetailRecordsDao records = storage.getCallDetailRecordsDao();
                    records.updateCallDetailRecord(callRecord);
                }

                // Handle pending verbs.
                source.tell(verb, source);
                return;
            } else if (downloadingRcml.equals(state) || downloadingFallbackRcml.equals(state) || redirecting.equals(state)
                    || finishGathering.equals(state) || finishRecording.equals(state) || sendingSms.equals(state)
                    || finishDialing.equals(state) || finishConferencing.equals(state)) {
                response = ((DownloaderResponse) message).get();
                if (parser != null) {
                    context.stop(parser);
                    parser = null;
                }
                final String type = response.getContentType();
                if (type != null) {
                        if (type.contains("text/xml") || type.contains("application/xml") || type.contains("text/html")) {
                            parser = parser(response.getContentAsString());
                        } else if (type.contains("audio/wav") || type.contains("audio/wave") || type.contains("audio/x-wav")) {
                            parser = parser("<Play>" + request.getUri() + "</Play>");
                        } else if (type.contains("text/plain")) {
                            parser = parser("<Say>" + response.getContentAsString() + "</Say>");
                        }
                } else {
                    if (call != null) {
                        call.tell(new Hangup(), null);
                    }
                    final StopInterpreter stop = new StopInterpreter();
                    source.tell(stop, source);
                    return;
                }
            } else if (pausing.equals(state)) {
                context.setReceiveTimeout(Duration.Undefined());
            }
            // Ask the parser for the next action to take.
            final GetNextVerb next = GetNextVerb.instance();
            if (parser != null) {
                parser.tell(next, source);
            } else {
                logger.info("Parser is null");
            }
        }
    }

    private final class NotFound extends AbstractAction {
        public NotFound(final ActorRef source) {
            super(source);
        }

        @Override
        public void execute(final Object message) throws Exception {
            final DownloaderResponse response = (DownloaderResponse) message;
            if (logger.isDebugEnabled()) {
                logger.debug("response succeeded " + response.succeeded() + ", statusCode " + response.get().getStatusCode());
            }
            final Notification notification = notification(WARNING_NOTIFICATION, 21402, "URL Not Found : "
                    + response.get().getURI());
            final NotificationsDao notifications = storage.getNotificationsDao();
            notifications.addNotification(notification);
            // Hang up the call.
            call.tell(new org.mobicents.servlet.restcomm.telephony.NotFound(), source);
        }
    }

    private final class Rejecting extends AbstractAction {
        public Rejecting(final ActorRef source) {
            super(source);
        }

        @Override
        public void execute(final Object message) throws Exception {
            final Class<?> klass = message.getClass();
            if (Tag.class.equals(klass)) {
                verb = (Tag) message;
            }
            String reason = "rejected";
            Attribute attribute = verb.attribute("reason");
            if (attribute != null) {
                reason = attribute.value();
                if (reason != null && !reason.isEmpty()) {
                    if ("rejected".equalsIgnoreCase(reason)) {
                        reason = "rejected";
                    } else if ("busy".equalsIgnoreCase(reason)) {
                        reason = "busy";
                    } else {
                        reason = "rejected";
                    }
                } else {
                    reason = "rejected";
                }
            }
            // Reject the call.
            if ("rejected".equals(reason)) {
                call.tell(new Answer(), source);
            } else {
                call.tell(new Reject(), source);
            }
        }
    }

    private abstract class AbstractDialAction extends AbstractAction {
        public AbstractDialAction(final ActorRef source) {
            super(source);
        }

        protected String callerId(final Tag container) {
            // Parse "from".
            String callerId = null;

            // Issue 210: https://telestax.atlassian.net/browse/RESTCOMM-210
            final boolean useInitialFromAsCallerId = configuration.subset("runtime-settings").getBoolean("from-address-to-proxied-calls");

            Attribute attribute = verb.attribute("callerId");
            if (attribute != null) {
                callerId = attribute.value();
                if (callerId != null && !callerId.isEmpty()) {
                    callerId = e164(callerId);
                    if (callerId == null) {
                        callerId = verb.attribute("callerId").value();
                        final NotificationsDao notifications = storage.getNotificationsDao();
                        final Notification notification = notification(ERROR_NOTIFICATION, 13214, callerId
                                + " is an invalid callerId.");
                        notifications.addNotification(notification);
                        sendMail(notification);
                        final StopInterpreter stop = new StopInterpreter();
                        source.tell(stop, source);
                        return null;
                    }
                }
            }

            if (callerId == null && useInitialFromAsCallerId)
                callerId = callInfo.from();

            return callerId;
        }

        protected int timeout(final Tag container) {
            int timeout = 30;
            Attribute attribute = container.attribute("timeout");
            if (attribute != null) {
                final String value = attribute.value();
                if (value != null && !value.isEmpty()) {
                    try {
                        timeout = Integer.parseInt(value);
                    } catch (final NumberFormatException exception) {
                        final NotificationsDao notifications = storage.getNotificationsDao();
                        final Notification notification = notification(WARNING_NOTIFICATION, 13212, value
                                + " is not a valid timeout value for <Dial>");
                        notifications.addNotification(notification);
                    }
                }
            }
            return timeout;
        }

        protected int timeLimit(final Tag container) {
            int timeLimit = 14400;
            Attribute attribute = container.attribute("timeLimit");
            if (attribute != null) {
                final String value = attribute.value();
                if (value != null && !value.isEmpty()) {
                    try {
                        timeLimit = Integer.parseInt(value);
                    } catch (final NumberFormatException exception) {
                        final NotificationsDao notifications = storage.getNotificationsDao();
                        final Notification notification = notification(WARNING_NOTIFICATION, 13216, value
                                + " is not a valid timeLimit value for <Dial>");
                        notifications.addNotification(notification);
                    }
                }
            }
            return timeLimit;
        }
    }

    private final class StartDialing extends AbstractDialAction {

        public StartDialing(final ActorRef source) {
            super(source);
        }

        @Override
        public void execute(final Object message) throws Exception {
            final Class<?> klass = message.getClass();
            if (Tag.class.equals(klass)) {
                verb = (Tag) message;
            }
            final String text = verb.text();
            if (text != null && !text.isEmpty()) {
                // Build the appropriate tag for the text, such as Number, Client or SIP
                final Tag.Builder builder = Tag.builder();
                // Read the next tag.
                if (text.contains("@")) {
                    builder.setName(Nouns.SIP);
                } else if (text.startsWith("client")) {
                    builder.setName(Nouns.client);
                } else {
                    builder.setName(Nouns.number);
                }
                builder.setText(text);
                Tag numberTag = builder.build();

                // Change the Dial verb to include the Tag we created before
                Tag.Builder tagBuilder = Tag.builder();
                tagBuilder.addChild(numberTag);
                tagBuilder.setIterable(verb.isIterable());
                tagBuilder.setName(verb.name());
                tagBuilder.setParent(verb.parent());
                for (Attribute attribute : verb.attributes()) {
                    if (attribute != null)
                        tagBuilder.addAttribute(attribute);
                }
                verb = null;
                verb = tagBuilder.build();
            }

            if (verb.hasChildren()) {
                // Handle conferencing.
                final Tag child = conference(verb);
                if (child != null) {
                    final String name = child.text();
                    final StringBuilder buffer = new StringBuilder();
                    buffer.append(accountId.toString()).append(":").append(name);
                    final CreateConference create = new CreateConference(buffer.toString());
                    conferenceManager.tell(create, source);
                } else {
                    // Handle forking.
                    dialBranches = new ArrayList<ActorRef>();
                    dialChildren = new ArrayList<Tag>(verb.children());
                    dialChildrenWithAttributes = new HashMap<ActorRef, Tag>();
                    isForking = true;
                    final StartForking start = StartForking.instance();
                    source.tell(start, source);
                }
            } else {
                // Ask the parser for the next action to take.
                final GetNextVerb next = GetNextVerb.instance();
                parser.tell(next, source);
            }
        }
    }

    private final class ProcessingDialChildren extends AbstractDialAction {
        public ProcessingDialChildren(final ActorRef source) {
            super(source);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void execute(final Object message) throws Exception {
            Class<?> klass = message.getClass();
            if (CallManagerResponse.class.equals(klass)) {
                final CallManagerResponse<ActorRef> response = (CallManagerResponse<ActorRef>) message;
                final ActorRef branch = response.get();
                dialBranches.add(branch);
                Tag child = dialChildren.get(0);
                if (child.hasAttributes()) {
                    dialChildrenWithAttributes.put(branch, child);
                }
                dialChildren.remove(child);
            }
            if (!dialChildren.isEmpty()) {
                CreateCall create = null;
                final Tag child = dialChildren.get(0);
                if (Nouns.client.equals(child.name())) {
                    if (call != null && callInfo != null) {
                        create = new CreateCall(e164(callerId(verb)), e164(child.text()), null, null, false, timeout(verb),
                                CreateCall.Type.CLIENT, accountId, callInfo.sid());
                    } else {
                        create = new CreateCall(e164(callerId(verb)), e164(child.text()), null, null, false, timeout(verb),
                                CreateCall.Type.CLIENT, accountId, null);
                    }
                } else if (Nouns.number.equals(child.name())) {
                    if (call != null && callInfo != null) {
                        create = new CreateCall(e164(callerId(verb)), e164(child.text()), null, null, false, timeout(verb),
                                CreateCall.Type.PSTN, accountId, callInfo.sid());
                    } else {
                        create = new CreateCall(e164(callerId(verb)), e164(child.text()), null, null, false, timeout(verb),
                                CreateCall.Type.PSTN, accountId, null);
                    }
                } else if (Nouns.uri.equals(child.name())) {
                    if (call != null && callInfo != null) {
                        create = new CreateCall(e164(callerId(verb)), e164(child.text()), null, null, false, timeout(verb),
                                CreateCall.Type.SIP, accountId, callInfo.sid());
                    } else {
                        create = new CreateCall(e164(callerId(verb)), e164(child.text()), null, null, false, timeout(verb),
                                CreateCall.Type.SIP, accountId, null);
                    }
                } else if (Nouns.SIP.equals(child.name())) {
                    // https://bitbucket.org/telestax/telscale-restcomm/issue/132/implement-twilio-sip-out
                    String username = null;
                    String password = null;
                    if (child.attribute("username") != null) {
                        username = child.attribute("username").value();
                    }
                    if (child.attribute("password") != null) {
                        password = child.attribute("password").value();
                    }
                    if (call != null && callInfo != null) {
                        create = new CreateCall(e164(callerId(verb)), e164(child.text()), username, password, false, timeout(verb),
                                CreateCall.Type.SIP, accountId, callInfo.sid());
                    } else {
                        create = new CreateCall(e164(callerId(verb)), e164(child.text()), username, password, false, timeout(verb),
                                CreateCall.Type.SIP, accountId, null);
                    }
                }
                callManager.tell(create, source);
            } else {
                // Fork.
                final Fork fork = Fork.instance();
                source.tell(fork, source);
                dialChildren = null;
            }
        }
    }

    private final class Forking extends AbstractDialAction {
        public Forking(final ActorRef source) {
            super(source);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void execute(final Object message) throws Exception {
            final Class<?> klass = message.getClass();
            if (CallManagerResponse.class.equals(klass)) {
                final CallManagerResponse<ActorRef> response = (CallManagerResponse<ActorRef>) message;
                outboundCall = response.get();
                outboundCall.tell(new Observe(source), source);
                outboundCall.tell(new Dial(), source);
            } else if (Fork.class.equals(klass)) {
                final Observe observe = new Observe(source);
                final Dial dial = new Dial();
                for (final ActorRef branch : dialBranches) {
                    branch.tell(observe, source);
                    branch.tell(dial, source);
                }
            }
            String path = configuration.subset("runtime-settings").getString("prompts-uri");
            if (!path.endsWith("/")) {
                path += "/";
            }
            path += "ringing.wav";
            URI uri = null;
            try {
                uri = UriUtils.resolve(new URI(path));
            } catch (final Exception exception) {
                final Notification notification = notification(ERROR_NOTIFICATION, 12400, exception.getMessage());
                final NotificationsDao notifications = storage.getNotificationsDao();
                notifications.addNotification(notification);
                sendMail(notification);
                final StopInterpreter stop = new StopInterpreter();
                source.tell(stop, source);
                return;
            }
            final Play play = new Play(uri, Short.MAX_VALUE);
            call.tell(play, source);
            final UntypedActorContext context = getContext();
            context.setReceiveTimeout(Duration.create(timeout(verb), TimeUnit.SECONDS));
        }
    }

    private final class AcquiringOutboundCallInfo extends AbstractDialAction {
        public AcquiringOutboundCallInfo(final ActorRef source) {
            super(source);
        }

        @Override
        public void execute(final Object message) throws Exception {
            if (isForking) {
                dialBranches.remove(outboundCall);
                for (final ActorRef branch : dialBranches) {
                    branch.tell(new Cancel(), null);
                    // Race condition here. Correct way is to ask Call to Cancel and when done Call will notify Observer with
                    // CallStateChanged.Cancelled then
                    // ask CallManager to destroy the call.
                    // callManager.tell(new DestroyCall(branch), source);
                }
                dialBranches = null;
            }
            outboundCall.tell(new GetCallInfo(), source);
        }
    }

    private final class Bridged extends AbstractDialAction {
        public Bridged(final ActorRef source) {
            super(source);
        }

        @Override
        public void execute(final Object message) throws Exception {
            final int timeLimit = timeLimit(verb);
            final UntypedActorContext context = getContext();
            context.setReceiveTimeout(Duration.create(timeLimit, TimeUnit.SECONDS));

            if (dialRecordAttribute != null && "true".equalsIgnoreCase(dialRecordAttribute.value())) {
                logger.info("Start recording of the bridge");
                record(bridge);
            }
        }
    }

    private void record(ActorRef target) {
        Configuration runtimeSettings = configuration.subset("runtime-settings");
        recordingSid = Sid.generate(Sid.Type.RECORDING);
        String path = runtimeSettings.getString("recordings-path");
        String httpRecordingUri = runtimeSettings.getString("recordings-uri");
        if (!path.endsWith("/")) {
            path += "/";
        }
        if (!httpRecordingUri.endsWith("/")) {
            httpRecordingUri += "/";
        }
        path += recordingSid.toString() + ".wav";
        httpRecordingUri += recordingSid.toString() + ".wav";
        this.recordingUri = URI.create(path);
        try {
            this.publicRecordingUri = UriUtils.resolve(new URI(httpRecordingUri));
        } catch (URISyntaxException e) {
            logger.error("URISyntaxException when trying to resolve Recording URI: " + e);
        }
        recordingCall = true;
        StartRecording message = new StartRecording(accountId, callInfo.sid(), runtimeSettings, storage, recordingSid,
                recordingUri);
        target.tell(message, null);
    }

    private void recordCall() {
        logger.info("Start recording of the call");
        record(call);
    }

    private void recordConference() {
        logger.info("Start recording of the conference");
        record(conference);
    }

    @SuppressWarnings("unchecked")
    private void executeDialAction(final Object message, final ActorRef outboundCall) {
        if (!dialActionExecuted && verb != null) {
            logger.info("Proceeding to execute Dial Action attribute");
            this.dialActionExecuted = true;
            final List<NameValuePair> parameters = parameters();

            Attribute attribute = verb.attribute("action");

            if (call != null) {
                try {
                    logger.info("Trying to get outboundCall Info");
                    final Timeout expires = new Timeout(Duration.create(5, TimeUnit.SECONDS));
                    Future<Object> future = (Future<Object>) ask(call, new GetCallInfo(), expires);
                    CallResponse<CallInfo> callResponse = (CallResponse<CallInfo>) Await.result(future,
                            Duration.create(10, TimeUnit.SECONDS));
                    callInfo = callResponse.get();
                } catch (Exception e) {
                    logger.error("Timeout waiting for inbound call info: \n" + e);
                }
            }

            if (outboundCall != null) {
                try {
                    logger.info("Trying to get outboundCall Info");
                    final Timeout expires = new Timeout(Duration.create(10, TimeUnit.SECONDS));
                    Future<Object> future = (Future<Object>) ask(outboundCall, new GetCallInfo(), expires);
                    CallResponse<CallInfo> callResponse = (CallResponse<CallInfo>) Await.result(future,
                            Duration.create(10, TimeUnit.SECONDS));
                    outboundCallInfo = callResponse.get();
                    final long dialRingDuration = new Interval(this.outboundCallInfo.dateCreated(), this.outboundCallInfo.dateConUpdated()).toDuration()
                            .getStandardSeconds();
                    parameters.add(new BasicNameValuePair("DialRingDuration", String.valueOf(dialRingDuration)));
                } catch (Exception e) {
                    logger.error("Timeout waiting for outbound call info: \n" + e);
                }
            }

            // Handle Failed Calls
            if (message instanceof CallManagerResponse && !(((CallManagerResponse<ActorRef>) message).succeeded())) {
                if (outboundCallInfo != null) {
                    parameters.add(new BasicNameValuePair("DialCallSid", (outboundCallInfo.sid() == null) ? "null" : outboundCallInfo.sid().toString()));
                } else {
                    parameters.add(new BasicNameValuePair("DialCallSid", "null"));
                }
                parameters.add(new BasicNameValuePair("DialCallStatus", CallStateChanged.State.FAILED.toString()));
                parameters.add(new BasicNameValuePair("DialCallDuration", "0"));
                parameters.add(new BasicNameValuePair("RecordingUrl", null));
                parameters.add(new BasicNameValuePair("PublicRecordingUrl", null));
            }
            // Handle No-Answer calls
            else if (message instanceof ReceiveTimeout) {
                if (outboundCallInfo != null) {
                    final String dialCallSid = this.outboundCallInfo.sid().toString();
                    long dialCallDuration;
                    if (outboundCallInfo.state().toString().equalsIgnoreCase("Completed")) {
                        dialCallDuration = new Interval(this.outboundCallInfo.dateConUpdated(), DateTime.now()).toDuration()
                                .getStandardSeconds();
                    } else {
                        dialCallDuration = 0L;
                    }
                    final String recordingUrl = this.recordingUri == null ? null : this.recordingUri.toString();
                    final String publicRecordingUrl = this.publicRecordingUri == null ? null : this.publicRecordingUri.toString();

                    parameters.add(new BasicNameValuePair("DialCallSid", dialCallSid));
                    // parameters.add(new BasicNameValuePair("DialCallStatus", dialCallStatus == null ? null : dialCallStatus
                    // .toString()));
                    parameters.add(new BasicNameValuePair("DialCallStatus", outboundCallInfo.state().toString()));
                    parameters.add(new BasicNameValuePair("DialCallDuration", String.valueOf(dialCallDuration)));
                    parameters.add(new BasicNameValuePair("RecordingUrl", recordingUrl));
                    parameters.add(new BasicNameValuePair("PublicRecordingUrl", publicRecordingUrl));
                } else {
                    parameters.add(new BasicNameValuePair("DialCallSid", "null"));
                    parameters.add(new BasicNameValuePair("DialCallStatus", CallStateChanged.State.NO_ANSWER.toString()));
                    parameters.add(new BasicNameValuePair("DialCallDuration", "0"));
                    parameters.add(new BasicNameValuePair("RecordingUrl", null));
                    parameters.add(new BasicNameValuePair("PublicRecordingUrl", null));
                }
            } else {
                // Handle the rest of the cases
                if (outboundCallInfo != null) {
                    final String dialCallSid = this.outboundCallInfo.sid().toString();
                    final CallStateChanged.State dialCallStatus = this.outboundCallInfo.state();
                    long dialCallDuration = 0L;
                    //In some cases, such as when the outbound dial is busy, the dialCallDuration wont be possbile to be calculated and will throw exception
                    try {
                        dialCallDuration = new Interval(this.outboundCallInfo.dateConUpdated(), DateTime.now()).toDuration()
                                .getStandardSeconds();
                    } catch (Exception e) {}
                    final String recordingUrl = this.recordingUri == null ? null : this.recordingUri.toString();
                    final String publicRecordingUrl = this.publicRecordingUri == null ? null : this.publicRecordingUri.toString();

                    parameters.add(new BasicNameValuePair("DialCallSid", dialCallSid));
                    // If Caller sent the BYE request, at the time we execute this method, the outbound call status is still in
                    // progress
                    if (callInfo.state().equals(CallStateChanged.State.COMPLETED)) {
                        parameters.add(new BasicNameValuePair("DialCallStatus", callInfo.state().toString()));
                    } else {
                        parameters.add(new BasicNameValuePair("DialCallStatus", dialCallStatus == null ? null : dialCallStatus
                                .toString()));
                    }
                    if (callState == CallStateChanged.State.BUSY)
                        parameters.add(new BasicNameValuePair("DialCallDuration", "0"));
                    else
                        parameters.add(new BasicNameValuePair("DialCallDuration", String.valueOf(dialCallDuration)));
                    parameters.add(new BasicNameValuePair("RecordingUrl", recordingUrl));
                    parameters.add(new BasicNameValuePair("PublicRecordingUrl", publicRecordingUrl));
                } else {
                    parameters.add(new BasicNameValuePair("DialCallSid", "null"));
                    parameters.add(new BasicNameValuePair("DialCallStatus", "null"));
                    parameters.add(new BasicNameValuePair("DialCallDuration", "0"));
                    parameters.add(new BasicNameValuePair("RecordingUrl", null));
                    parameters.add(new BasicNameValuePair("PublicRecordingUrl", "null"));
                }
            }

            final NotificationsDao notifications = storage.getNotificationsDao();
            if (attribute != null) {
                logger.info("Executing Dial Action attribute.");
                String action = attribute.value();
                if (action != null && !action.isEmpty()) {
                    URI target = null;
                    try {
                        target = URI.create(action);
                    } catch (final Exception exception) {
                        final Notification notification = notification(ERROR_NOTIFICATION, 11100, action + " is an invalid URI.");
                        notifications.addNotification(notification);
                        sendMail(notification);
                        final StopInterpreter stop = new StopInterpreter();
                        self().tell(stop, self());
                        return;
                    }
                    final URI base = request.getUri();
                    final URI uri = UriUtils.resolve(base, target);
                    // Parse "method".
                    String method = "POST";
                    attribute = verb.attribute("method");
                    if (attribute != null) {
                        method = attribute.value();
                        if (method != null && !method.isEmpty()) {
                            if (!"GET".equalsIgnoreCase(method) && !"POST".equalsIgnoreCase(method)) {
                                final Notification notification = notification(WARNING_NOTIFICATION, 13210, method
                                        + " is not a valid HTTP method for <Dial>");
                                notifications.addNotification(notification);
                                method = "POST";
                            }
                        } else {
                            method = "POST";
                        }
                    }
                    logger.info("Dial Action URL: " + uri.toString() + " Method: " + method);
                    logger.debug("Dial Action parameters: \n" + parameters);
                    // Redirect to the action url.
                    request = new HttpRequestDescriptor(uri, method, parameters);
                    // Tell the downloader to send the Dial Parameters to the Action url but we don't need a reply back so sender ==
                    // null
                    downloader.tell(request, self());
                    return;
                }
            }
        } else if (verb == null) {
            logger.info("Dial action didn't executed because verb is null");
        }
    }

    private final class FinishDialing extends AbstractDialAction {
        public FinishDialing(final ActorRef source) {
            super(source);
        }

        @Override
        public void execute(final Object message) throws Exception {
            final State state = fsm.state();
            logger.info("FinishDialing, current state: " + state);
            Attribute attribute = null;
            if (verb != null) {
                attribute = verb.attribute("action");
            }

            if (message instanceof ReceiveTimeout && dialBranches != null) {
                logger.info("Received timeout, will cancel branches, current VoiceIntepreter state: " + state);
                //The forking timeout reached, we have to cancel all dial branches
                final UntypedActorContext context = getContext();
                context.setReceiveTimeout(Duration.Undefined());

                if (dialBranches != null) {
                    Iterator<ActorRef> dialBranchesIterator = dialBranches.iterator();
                    while (dialBranchesIterator.hasNext()) {
                        ActorRef branch = dialBranchesIterator.next();
    //                    if (attribute != null) {
    //                        executeDialAction(message, branch);
    //                    }
                        branch.tell(new Cancel(), source);
                        logger.info("Canceled branch: " + branch.path());
                    }
                }
                if (dialBranches != null && dialBranches.size() > 0) {
                    dialBranches = null;
                }
//                    call.tell(new StopMediaGroup(), null);
                if (attribute == null) {
                    logger.info("Will ask for the next verb from parser");
                    final GetNextVerb next = GetNextVerb.instance();
                    parser.tell(next, source);
                } else {
                    if (outboundCall != null) {
                        executeDialAction(message,outboundCall);
                    } else {
                        executeDialAction(message,null);
                    }
                }
                dialChildren = null;
                outboundCall = null;
                callback();
                return;
            }

            if (message instanceof CallStateChanged) {
                logger.info("CallStateChanged state: "+((CallStateChanged)message).state().toString()+" ,sender: "+sender().path());
                if (forking.equals(state) || finishDialing.equals(state)) {
                    if (sender.equals(call)) {
                        //Initial call wants to finish dialing
                        logger.info("Sender == call: " + sender.equals(call));
                        final UntypedActorContext context = getContext();
                        context.setReceiveTimeout(Duration.Undefined());

                        Iterator<ActorRef> dialBranchesIterator = dialBranches.iterator();
                        while (dialBranchesIterator.hasNext()) {
                            ActorRef branch = dialBranchesIterator.next();
//                            if (attribute != null) {
//                                executeDialAction(message, branch);
//                            }
                            branch.tell(new Cancel(), source);
                        }
                        if (dialBranches.size() > 0) {
                            dialBranches = null;
                        }
                        //Since initial call wants to finish dialling there is no point to check form RCML next
//                        if (attribute == null) {
//                            final GetNextVerb next = GetNextVerb.instance();
//                            parser.tell(next, source);
//                        }
                        //Instead move the FSM to finished
                        dialChildren = null;
                        outboundCall = null;
                        callback();
                        fsm.transition(message, finished);
                        return;
                    } else if (dialBranches != null && dialBranches.contains(sender)) {
                        removeDialBranch(message, sender);
                        return;
                    } else {
                        // Ask the parser for the next action to take.
                        final GetNextVerb next = GetNextVerb.instance();
                        if (parser != null) {
                            parser.tell(next, source);
                        }

                        dialChildren = null;
                        outboundCall = null;
                        return;
                    }
                } else if (bridged.equals(state)) {
                    logger.info("finishDialing state=bridged, will hangup outboundCall");
                    outboundCall.tell(new Hangup(), source);
                } else {
                    logger.debug("FinishDialing, State: " + state);
                    logger.debug("State is not FORKING and not Bridged");
                    logger.debug("Sender is initial call: " + sender.equals(call));
                    logger.debug("Sender in the dialBranches: " + dialBranches.contains(sender));
                }
            }

            if (sender == call) {
                if (outboundCall != null) {
                    outboundCall.tell(new Hangup(), self());
                }
            } else {
                call.tell(new Hangup(), self());
            }

            if (recordingCall && sender == call) {
                recordingCall = false;
            }

            // Stop the bridge. Cleanup will be handled by BridgeManager.
            if (bridge != null) {
                final StopBridge stopBridge = new StopBridge();
                bridge.tell(stopBridge, super.source);
                bridge = null;
            }

            if (attribute != null) {
                logger.info("Executing Dial Action url");
                if (outboundCall != null) {
                    executeDialAction(message, outboundCall);
                } else {
                    logger.info("Executing Dial Action url");
                    executeDialAction(message, null);
                }
                callback();
                return;
            } else {
                logger.info("Action attribute is null.");
            }

            // Ask the parser for the next action to take.
            final GetNextVerb next = GetNextVerb.instance();
            if (parser != null) {
                parser.tell(next, source);
            }

            dialChildren = null;
            outboundCall = null;
        }
    }

    private final class AcquiringConferenceInfo extends AbstractDialAction {
        public AcquiringConferenceInfo(final ActorRef source) {
            super(source);
        }

        @Override
        public void execute(final Object message) throws Exception {
            final ConferenceCenterResponse response = (ConferenceCenterResponse) message;
            conference = response.get();
            final GetConferenceInfo request = new GetConferenceInfo();
            conference.tell(new Observe(source), source);
            conference.tell(new GetConferenceInfo(), source);
        }
    }

    private final class JoiningConference extends AbstractDialAction {
        public JoiningConference(final ActorRef source) {
            super(source);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void execute(final Object message) throws Exception {
            final ConferenceResponse<ConferenceInfo> response = (ConferenceResponse<ConferenceInfo>) message;
            conferenceInfo = response.get();
            conferenceState = conferenceInfo.state();
            final Tag child = conference(verb);

            // If there is room join the conference.
            int max = 40;
            Attribute attribute = child.attribute("maxParticipants");
            if (attribute != null) {
                final String value = attribute.value();
                if (value != null && !value.isEmpty()) {
                    try {
                        max = Integer.parseInt(value);
                    } catch (final NumberFormatException ignored) {
                    }
                }
            }

            if (conferenceInfo.participants().size() < max) {
                // Play beep.
                boolean beep = true;
                attribute = child.attribute("beep");
                if (attribute != null) {
                    final String value = attribute.value();
                    if (value != null && !value.isEmpty()) {
                        beep = Boolean.parseBoolean(value);
                    }
                }

                // Only play beep if conference is already running
                // Do not play it while participants are listening to background music
                if (beep && ConferenceStateChanged.State.RUNNING_MODERATOR_PRESENT.equals(conferenceInfo.state())) {
                    String path = configuration.subset("runtime-settings").getString("prompts-uri");
                    if (!path.endsWith("/")) {
                        path += "/";
                    }
                    String entryAudio = configuration.subset("runtime-settings").getString("conference-entry-audio");
                    path += entryAudio == null || entryAudio.equals("") ? "beep.wav" : entryAudio;
                    URI uri = null;
                    try {
                        uri = UriUtils.resolve(new URI(path));
                    } catch (final Exception exception) {
                        final Notification notification = notification(ERROR_NOTIFICATION, 12400, exception.getMessage());
                        final NotificationsDao notifications = storage.getNotificationsDao();
                        notifications.addNotification(notification);
                        sendMail(notification);
                        final StopInterpreter stop = new StopInterpreter();
                        source.tell(stop, source);
                        return;
                    }
                    final Play play = new Play(uri, 1);
                    conference.tell(play, source);
                }
                // Join the conference.
                final AddParticipant request = new AddParticipant(call);
                conference.tell(request, source);
            } else {
                // Ask the parser for the next action to take.
                final GetNextVerb next = GetNextVerb.instance();
                parser.tell(next, source);
            }
        }
    }

    private final class Conferencing extends AbstractDialAction {
        public Conferencing(final ActorRef source) {
            super(source);
        }

        @Override
        public void execute(final Object message) throws Exception {
            final NotificationsDao notifications = storage.getNotificationsDao();
            final Tag child = conference(verb);

            // Mute
            Attribute attribute = child.attribute("muted");
            if (attribute != null) {
                final String value = attribute.value();
                if (value != null && !value.isEmpty()) {
                    muteCall = Boolean.parseBoolean(value);
                }
            }

            if (muteCall) {
                final Mute mute = new Mute();
                call.tell(mute, source);
            }

            // Parse start conference.
            attribute = child.attribute("startConferenceOnEnter");
            if (attribute != null) {
                final String value = attribute.value();
                if (value != null && !value.isEmpty()) {
                    startConferenceOnEnter = Boolean.parseBoolean(value);
                }
            }

            if (!startConferenceOnEnter && conferenceState == ConferenceStateChanged.State.RUNNING_MODERATOR_ABSENT) {
                if (!muteCall) {
                    final Mute mute = new Mute();
                    logger.info("Muting the call as startConferenceOnEnter =" + startConferenceOnEnter + " callMuted = "
                            + muteCall);
                    call.tell(mute, source);
                }

                // Only play background music if conference is not doing that already
                // If conference state is RUNNING_MODERATOR_ABSENT and participants > 0 then BG music is playing already
                logger.info("Play background music? " + conferenceInfo.participants().size());
                boolean playBackground = conferenceInfo.participants().size() == 1;
                if (playBackground) {
                    // Parse wait url.
                    URI waitUrl = new URL(
                            "http://127.0.0.1:8080/restcomm/music/rock/nickleus_-_original_guitar_song_200907251723.wav")
                            .toURI();
                    attribute = child.attribute("waitUrl");
                    if (attribute != null) {
                        String value = attribute.value();
                        if (value != null && !value.isEmpty()) {
                            try {
                                waitUrl = URI.create(value);
                            } catch (final Exception exception) {
                                final Notification notification = notification(ERROR_NOTIFICATION, 13233, method
                                        + " is not a valid waitUrl value for <Conference>");
                                notifications.addNotification(notification);
                                sendMail(notification);
                                final StopInterpreter stop = new StopInterpreter();
                                source.tell(stop, source);

                                // TODO shouldn't we return here?
                            }
                        }
                    }

                    final URI base = request.getUri();
                    waitUrl = UriUtils.resolve(base, waitUrl);
                    // Parse method.
                    String method = "POST";
                    attribute = child.attribute("waitMethod");
                    if (attribute != null) {
                        method = attribute.value();
                        if (method != null && !method.isEmpty()) {
                            if (!"GET".equalsIgnoreCase(method) && !"POST".equalsIgnoreCase(method)) {
                                final Notification notification = notification(WARNING_NOTIFICATION, 13234, method
                                        + " is not a valid waitMethod value for <Conference>");
                                notifications.addNotification(notification);
                                method = "POST";
                            }
                        } else {
                            method = "POST";
                        }
                    }

                    // Tell conference to play music to participants on hold
                    if (waitUrl != null) {
                        conference.tell(new Play(waitUrl, Short.MAX_VALUE), super.source);
                    }
                }
            } else if (conferenceState == ConferenceStateChanged.State.RUNNING_MODERATOR_ABSENT) {
                // Tell the conference the moderator is now present
                // Causes background music to stop playing and all participants will be unmuted
                conference.tell(new ConferenceModeratorPresent(), source);

                // Check if moderator wants to record the conference
                Attribute record = verb.attribute("record");
                if (record != null && "true".equalsIgnoreCase(record.value())) {
                    // XXX get record limit etc from dial verb
                    recordConference();
                }
            }

            // Set timer.
            final int timeLimit = timeLimit(verb);
            final UntypedActorContext context = getContext();
            context.setReceiveTimeout(Duration.create(timeLimit, TimeUnit.SECONDS));
        }
    }

    private final class FinishConferencing extends AbstractDialAction {
        public FinishConferencing(final ActorRef source) {
            super(source);
        }

        @Override
        public void execute(final Object message) throws Exception {
            if (message instanceof ReceiveTimeout) {
                final UntypedActorContext context = getContext();
                context.setReceiveTimeout(Duration.Undefined());
                final RemoveParticipant remove = new RemoveParticipant(call);
                conference.tell(remove, source);
            }

            // Clean up
            if (ConferenceStateChanged.class.equals(message)) {
                // Destroy conference if state changed to completed (last participant in call)
                ConferenceStateChanged confStateChanged = (ConferenceStateChanged) message;
                if (ConferenceStateChanged.State.COMPLETED.equals(confStateChanged.state())) {
                    DestroyConference destroyConference = new DestroyConference(conferenceInfo.name());
                    conferenceManager.tell(destroyConference, super.source);
                }
            }
            conference = null;

            // Parse remaining conference attributes.
            final NotificationsDao notifications = storage.getNotificationsDao();

            // Parse "action".
            Attribute attribute = verb.attribute("action");
            if (attribute != null) {
                String action = attribute.value();
                if (action != null && !action.isEmpty()) {
                    URI target = null;
                    try {
                        target = URI.create(action);
                    } catch (final Exception exception) {
                        final Notification notification = notification(ERROR_NOTIFICATION, 11100, action
                                + " is an invalid URI.");
                        notifications.addNotification(notification);
                        sendMail(notification);
                        final StopInterpreter stop = new StopInterpreter();
                        source.tell(stop, source);
                        return;
                    }
                    final URI base = request.getUri();
                    final URI uri = UriUtils.resolve(base, target);
                    // Parse "method".
                    String method = "POST";
                    attribute = verb.attribute("method");
                    if (attribute != null) {
                        method = attribute.value();
                        if (method != null && !method.isEmpty()) {
                            if (!"GET".equalsIgnoreCase(method) && !"POST".equalsIgnoreCase(method)) {
                                final Notification notification = notification(WARNING_NOTIFICATION, 13210, method
                                        + " is not a valid HTTP method for <Dial>");
                                notifications.addNotification(notification);
                                method = "POST";
                            }
                        } else {
                            method = "POST";
                        }
                    }
                    // Redirect to the action url.
                    final List<NameValuePair> parameters = parameters();
                    request = new HttpRequestDescriptor(uri, method, parameters);
                    downloader.tell(request, source);
                    return;
                }
            }

            // Ask the parser for the next action to take.
            final GetNextVerb next = GetNextVerb.instance();
            parser.tell(next, source);
        }
    }

    private final class Finished extends AbstractAction {
        public Finished(final ActorRef source) {
            super(source);
        }

        @Override
        public void execute(final Object message) throws Exception {
            logger.info("At Finished state, state: " + fsm.state());
            final Class<?> klass = message.getClass();
            if (CallStateChanged.class.equals(klass)) {
                final CallStateChanged event = (CallStateChanged) message;
                callState = event.state();
            }
                if (callRecord != null) {
                    callRecord = callRecord.setStatus(callState.toString());
                    final DateTime end = DateTime.now();
                    callRecord = callRecord.setEndTime(end);
                    final int seconds = (int) (end.getMillis() - callRecord.getStartTime().getMillis()) / 1000;
                    callRecord = callRecord.setDuration(seconds);
                    final CallDetailRecordsDao records = storage.getCallDetailRecordsDao();
                    records.updateCallDetailRecord(callRecord);
                }
                if (!dialActionExecuted)
                    executeDialAction(message, outboundCall);
                callback(true);

            // XXX review bridge cleanup!!

            // Cleanup bridge
            if ((bridge != null) && (is(forking) || is(acquiringOutboundCallInfo) || is(bridged))) {
                // Stop the bridge
                bridge.tell(new StopBridge(), super.source);
                bridge = null;
            }
                // Cleanup the outbound call if necessary.
                // XXX verify if this code is still necessary
                if (outboundCall != null && !liveCallModification) {
                    outboundCall.tell(new StopObserving(source), null);
                    outboundCall.tell(new Hangup(), null);
                }

            // If the call is in a conference remove it.
            if (conference != null) {
                // Parse "endConferenceOnExit"
                boolean endOnExit = false;
                final Tag child = conference(verb);
                Attribute attribute = child.attribute("endConferenceOnExit");

                if (attribute != null) {
                    final String value = attribute.value();
                    if (value != null && !value.isEmpty()) {
                        endOnExit = Boolean.parseBoolean(value);
                    }
                }

                // Stop Observing the conference
                conference.tell(new StopObserving(super.source), null);

                // Play beep when participant leave the conference.
                // Do not play beep if this was the last participant to leave the conference, because there is no one to listen to the beep.
                if(conferenceInfo.participants() != null && conferenceInfo.participants().size() !=0 ){
                    String path = configuration.subset("runtime-settings").getString("prompts-uri");
                    if (!path.endsWith("/")) {
                        path += "/";
                    }
                    String exitAudio = configuration.subset("runtime-settings").getString("conference-exit-audio");
                    path += exitAudio == null || exitAudio.equals("") ? "alert.wav" : exitAudio;
                    URI uri = null;
                    try {
                        uri = UriUtils.resolve(new URI(path));
                    } catch (final Exception exception) {
                        final Notification notification = notification(ERROR_NOTIFICATION, 12400, exception.getMessage());
                        final NotificationsDao notifications = storage.getNotificationsDao();
                        notifications.addNotification(notification);
                        sendMail(notification);
                        final StopInterpreter stop = new StopInterpreter();
                        source.tell(stop, source);
                        return;
                    }
                    final Play play = new Play(uri, 1);
                    conference.tell(play, source);
                }

                if (endOnExit) {
                    // Stop the conference if endConferenceOnExit is true
                    final StopConference stop = new StopConference();
                    conference.tell(stop, super.source);
                } else {
                    conference.tell(new RemoveParticipant(call), source);
                }
            }

            if (!liveCallModification) {
                // Destroy the Call(s).
                callManager.tell(new DestroyCall(call), super.source);
                if (outboundCall != null) {
                    callManager.tell(new DestroyCall(outboundCall), super.source);
                }
            } else {
                // Make sure the media operations of the call are stopped
                // so we can start processing a new RestComm application
                call.tell(new StopMediaGroup(), super.source);
            }

            // Stop the dependencies.
            final UntypedActorContext context = getContext();
            if (mailerNotify != null)
                context.stop(mailerNotify);
            if (mailerService != null)
                context.stop(mailerService);
            context.stop(downloader);
            context.stop(asrService);
            context.stop(faxService);
            context.stop(cache);
            context.stop(synthesizer);

            // Stop the interpreter.
            postCleanup();
        }
    }


    @Override
    public void postStop() {
        if (!fsm.state().equals(uninitialized)) {
            logger.info("VoiceIntepreter: " + self().path()
                    + "At the postStop() method. Will clean up Voice Interpreter. Keep calls: " + liveCallModification);
            if (fsm.state().equals(bridged) && outboundCall != null && !liveCallModification) {
                logger.info("At postStop(), will clean up outbound call");
                outboundCall.tell(new Hangup(), null);
                callManager.tell(new DestroyCall(outboundCall), null);
                outboundCall = null;
            }

            if (call != null && !liveCallModification) {
                logger.info("At postStop(), will clean up call");
                callManager.tell(new DestroyCall(call), null);
                call = null;
            }

            getContext().stop(self());
            postCleanup();
        }
        super.postStop();
    }

    private final class CreatingBridge extends AbstractAction {

        public CreatingBridge(ActorRef source) {
            super(source);
        }

        @Override
        public void execute(Object message) throws Exception {
            final CreateBridge create = new CreateBridge();
            bridgeManager.tell(create, super.source);
        }

    }

    private final class InitializingBridge extends AbstractAction {

        public InitializingBridge(ActorRef source) {
            super(source);
        }

        @Override
        public void execute(Object message) throws Exception {
            // Start monitoring bridge state changes
            final Observe observe = new Observe(super.source);
            bridge.tell(observe, super.source);

            // Initialize bridge
            final StartBridge start = new StartBridge();
            bridge.tell(start, super.source);
        }

    }

    private final class Bridging extends AbstractAction {

        public Bridging(ActorRef source) {
            super(source);
        }

        private ActorRef buildSubVoiceInterpreter(Tag child) throws MalformedURLException, URISyntaxException {
//            URI url = new URL(child.attribute("url").value()).toURI();
            URI url = null;
            if (request != null) {
                final URI base = request.getUri();
                url = UriUtils.resolve(base, new URI(child.attribute("url").value()));
            } else {
                url = UriUtils.resolve(new URI(child.attribute("url").value()));
            }
            String method;
            if (child.hasAttribute("method")) {
                method = child.attribute("method").value().toUpperCase();
            } else {
                method = "POST";
            }

            final SubVoiceInterpreterBuilder builder = new SubVoiceInterpreterBuilder(getContext().system());
            builder.setConfiguration(configuration);
            builder.setStorage(storage);
            builder.setCallManager(super.source);
            builder.setSmsService(smsService);
            builder.setAccount(accountId);
            builder.setVersion(version);
            builder.setUrl(url);
            builder.setMethod(method);
            return builder.build();
        }

        @Override
        public void execute(Object message) throws Exception {
            logger.info("Joining call from:" + callInfo.from() + " to: " + callInfo.to() + " with outboundCall from: "
                    + outboundCallInfo.from() + " to: " + outboundCallInfo.to());

            // Check for any Dial verbs with url attributes (call screening url)
            Tag child = dialChildrenWithAttributes.get(outboundCall);
            if (child != null && child.attribute("url") != null) {
                final ActorRef interpreter = buildSubVoiceInterpreter(child);
                StartInterpreter start = new StartInterpreter(outboundCall);
                Timeout expires = new Timeout(Duration.create(6000, TimeUnit.SECONDS));
                Future<Object> future = (Future<Object>) ask(interpreter, start, expires);
                Object object = Await.result(future, Duration.create(6000 * 10, TimeUnit.SECONDS));

                if (!End.class.equals(object.getClass())) {
                    fsm.transition(message, hangingUp);
                    return;
                }

                // Stop SubVoiceInterpreter
                outboundCall.tell(new StopObserving(interpreter), null);
                getContext().stop(interpreter);
            }

            // Stop ringing from inbound call
            final StopMediaGroup stop = new StopMediaGroup();
            call.tell(stop, super.source);

            // Wait for a Media Group Response to finally ask Bridge to join calls
            // Check method onReceive() for MediaGroupResponse
        }

    }
}
