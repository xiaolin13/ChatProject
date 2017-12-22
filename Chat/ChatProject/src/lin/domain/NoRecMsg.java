package lin.domain;

public class NoRecMsg {

	private String sendName;
	private String recName;
	private String msg;
	
	public NoRecMsg(String sendName, String recName, String msg) {
		this.sendName = sendName;
		this.recName = recName;
		this.msg = msg;
	}
	public String getSendName() {
		return sendName;
	}
	public void setSendName(String sendName) {
		this.sendName = sendName;
	}
	public String getRecName() {
		return recName;
	}
	public void setRecName(String recName) {
		this.recName = recName;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
}
