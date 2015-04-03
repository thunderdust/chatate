package liu.weiran.chatate.entity;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import liu.weiran.chatate.service.ChatService;
import liu.weiran.chatate.util.ChatUtils;
import liu.weiran.chatate.util.PathUtils;
import liu.weiran.chatate.util.Utils;

import java.io.IOException;

public class MessageBuilder {
	Message msg;

	public MessageBuilder() {
		msg = new Message();
	}

	public void target(RoomType roomType, String toId) {
		String convid;
		msg.setRoomType(roomType);
		if (roomType == RoomType.Single) {
			msg.setToPeerId(toId);
			msg.setRequestReceipt(true);
			convid = ChatUtils.convid(ChatService.getSelfID(), toId);
		} else {
			convid = toId;
		}
		msg.setConvid(convid);
		msg.setReadStatus(Message.ReadStatus.HaveRead);
	}

	public void text(String content) {
		msg.setType(Message.Type.Text);
		msg.setContent(content);
	}

	private void file(Message.Type type, String objectId) {
		msg.setType(type);
		msg.setObjectId(objectId);
	}

	public void image(String objectId) {
		file(Message.Type.Image, objectId);
	}

	public void annotation(String address, double latitude, double longitude) {
		String content = address + "&" + latitude + "&" + longitude;
		msg.setContent(content);
		msg.setType(Message.Type.Annotation);
	}

	public void audio(String objectId) {
		file(Message.Type.Audio, objectId);
	}

	public Message preBuild() {
		msg.setStatus(Message.Status.SendStart);
		msg.setTimestamp(System.currentTimeMillis());
		msg.setFromPeerId(ChatService.getSelfID());
		if (msg.getObjectId() == null) {
			msg.setObjectId(Utils.uuid());
		}
		return msg;
	}

	public static String uploadMsg(Message msg) throws IOException, AVException {
		if (msg.getType() != Message.Type.Audio && msg.getType() != Message.Type.Image) {
			return null;
		}
		String objectId = msg.getObjectId();
		if (objectId == null) {
			throw new NullPointerException("objectId mustn't be null");
		}
		String filePath = PathUtils.getChatFilePath(objectId);
		AVFile file = AVFile.withAbsoluteLocalPath(objectId, filePath);
		file.save();
		String url = file.getUrl();
		msg.setContent(url);
		return url;
	}
}
