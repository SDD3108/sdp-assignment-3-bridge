package kz.aitu.gateway.sender;

public final class SmsMessageSender extends SimulatedMessageSender {
    public static final int MAX_CHARACTERS = 160;

    public SmsMessageSender(){
        this(true);
    }
    public SmsMessageSender(boolean available) {
        super(available);
    }
    @Override public String getChannelName(){
        return "SMS";
    }
    @Override
    protected boolean accepts(String body){
        return body.codePointCount(0, body.length()) <= MAX_CHARACTERS;
    }
    @Override
    protected String formatPayload(String recipient, String title, String body) {
        return body;
    }
}
