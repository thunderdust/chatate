package liu.weiran.chatate.entity;

import java.util.Map;

import com.avos.avoscloud.im.v2.AVIMMessageField;
import com.avos.avoscloud.im.v2.AVIMMessageType;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;

@AVIMMessageType(type = 1)
public class AVIMAnnotationMessage extends AVIMTypedMessage {

	@AVIMMessageField(name = "_lcannotation")
	String text;
	int startOffset;
	int endOffset;
	int bookID;
	String comment;

	@AVIMMessageField(name = "_lcannoattrs")
	Map<String, Object> attrs;

	public String getText() {
		return this.text;
	}

	public String getComment() {
		return this.comment;
	}

	public int getStart() {
		return this.startOffset;
	}

	public int getEnd() {
		return this.endOffset;
	}

	public int getBookID() {
		return this.bookID;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setBookID(int id) {
		this.bookID = id;
	}

	public void setStart(int s) {
		this.startOffset = s;
	}

	public void setEnd(int e) {
		this.setEnd(e);
	}

	public Map<String, Object> getAttrs() {
		return this.attrs;
	}

	public void setAttrs(Map<String, Object> attr) {
		this.attrs = attr;
	}

}
