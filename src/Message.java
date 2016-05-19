import java.io.Serializable;

public class Message implements Serializable{
	private static final long serialVersionUID = 1L;
	MsgType msgType;
	String msgContent;
	GameBoard g;
	
	public Message(){
	
	}
	
	public Message(MsgType msgType, String msgContent) {
		this.msgType = msgType;
		this.msgContent = msgContent;
	}
	
	public Message(MsgType msgType, GameBoard gm) {
		this.msgType = msgType;
		this.g = gm;
	}
}