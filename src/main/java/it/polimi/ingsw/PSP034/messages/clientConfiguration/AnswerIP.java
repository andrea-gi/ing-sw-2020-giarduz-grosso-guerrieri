package it.polimi.ingsw.PSP034.messages.clientConfiguration;

public class AnswerIP extends AnswerClientConfig{
    static final long serialVersionUID = 98112518520L;
    private final String ip;
    private final int port;

    public AnswerIP(String ip, int port){
        this.ip = ip;
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public String getIp() {
        return ip;
    }
}
