package liu.weiran.chatate.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.avos.avoscloud.AVUser;
import liu.weiran.chatate.avobject.User;
import liu.weiran.chatate.base.MyApplication;
import liu.weiran.chatate.entity.Message;
import liu.weiran.chatate.entity.RoomType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatabaseMessage {
	public static final String MESSAGES = "messages";
	public static final String FROM_PEER_ID = "fromPeerId";
	public static final String CONVID = "convid";
	public static final String TIMESTAMP = "timestamp";
	public static final String OBJECT_ID = "objectId";
	public static final String CONTENT = "content";
	public static final String STATUS = "status";
	public static final String TYPE = "type";
	public static final String TO_PEER_ID = "toPeerId";
	public static final String ROOM_TYPE = "roomType";
	public static final String OWNER_ID = "ownerId";
	public static final String READ_STATUS = "readStatus";

	public static void createTable(SQLiteDatabase db) {
		db.execSQL("create table if not exists messages (id integer primary key, objectId varchar(63) unique not null,"
				+ "ownerId varchar(255) not null,fromPeerId varchar(255) not null, convid varchar(255) not null ,"
				+ "toPeerId varchar(255), content varchar(1023),"
				+ " status integer,type integer,roomType integer,readStatus integer,timestamp varchar(63))");
	}

	public static void dropTable(SQLiteDatabase db) {
		db.execSQL("drop table if exists messages");
	}

	public static int insertMessage(Message Message) {
		List<Message> Messages = new ArrayList<Message>();
		Messages.add(Message);
		return insertMessages(Messages);
	}

	public static int insertMessages(List<Message> Messages) {
		DatabaseHelper dbHelper = new DatabaseHelper(MyApplication.mCtx,
				MyApplication.DB_NAME, MyApplication.DB_VERSION);
		if (Messages == null || Messages.size() == 0) {
			return 0;
		}
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.beginTransaction();
		int n = 0;
		try {
			for (Message Message : Messages) {
				ContentValues cv = new ContentValues();
				cv.put(OBJECT_ID, Message.getObjectId());
				cv.put(TIMESTAMP, Message.getTimestamp() + "");
				cv.put(FROM_PEER_ID, Message.getFromPeerId());
				cv.put(STATUS, Message.getStatus().getValue());
				cv.put(ROOM_TYPE, Message.getRoomType().getValue());
				cv.put(CONVID, Message.getConvid());
				cv.put(READ_STATUS, Message.getReadStatus().getValue());
				cv.put(TO_PEER_ID, Message.getToPeerId());
				cv.put(OWNER_ID, User.getCurrentUserID());
				cv.put(TYPE, Message.getType().getValue());
				cv.put(CONTENT, Message.getContent());
				db.insert(MESSAGES, null, cv);
				n++;
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			db.close();
		}
		return n;
	}

	public static List<Message> getMessages(DatabaseHelper dbHelper,
			String convid, int size) {
		List<Message> Messages = new ArrayList<Message>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		assert db != null;
		Cursor c = db.query(MESSAGES, null, "convid=?",
				new String[] { convid }, null, null, TIMESTAMP + " desc", size
						+ "");
		while (c.moveToNext()) {
			Message msg = createMessageByCursor(c);
			Messages.add(msg);
		}
		c.close();
		Collections.reverse(Messages);
		db.close();
		return Messages;
	}

	public static Message createMessageByCursor(Cursor c) {
		Message msg = new Message();
		msg.setFromPeerId(c.getString(c.getColumnIndex(FROM_PEER_ID)));
		msg.setContent(c.getString(c.getColumnIndex(CONTENT)));
		Message.Status status = Message.Status.fromInt(c.getInt(c
				.getColumnIndex(STATUS)));
		msg.setStatus(status);
		msg.setConvid(c.getString(c.getColumnIndex(CONVID)));
		msg.setObjectId(c.getString(c.getColumnIndex(OBJECT_ID)));
		int readStatusInt = c.getInt(c.getColumnIndex(READ_STATUS));
		msg.setReadStatus(Message.ReadStatus.fromInt(readStatusInt));
		int roomTypeInt = c.getInt(c.getColumnIndex(ROOM_TYPE));
		RoomType roomType = RoomType.fromInt(roomTypeInt);
		msg.setRoomType(roomType);
		String toPeerId = c.getString(c.getColumnIndex(TO_PEER_ID));
		msg.setToPeerId(toPeerId);
		msg.setTimestamp(Long.parseLong(c.getString(c.getColumnIndex(TIMESTAMP))));
		Message.Type type = Message.Type.fromInt(c.getInt(c
				.getColumnIndex(TYPE)));
		msg.setType(type);
		return msg;
	}

	public static List<Message> getRecentMessages(String ownerId) {
		DatabaseHelper dbHelper = new DatabaseHelper(MyApplication.mCtx,
				MyApplication.DB_NAME, MyApplication.DB_VERSION);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = db.query(true, MESSAGES, null, "ownerId=?",
				new String[] { ownerId }, CONVID, null, TIMESTAMP + " desc",
				null);
		List<Message> Messages = new ArrayList<Message>();
		while (c.moveToNext()) {
			Message Message = createMessageByCursor(c);
			Messages.add(Message);
		}
		c.close();
		db.close();
		return Messages;
	}

	public static int updateStatusAndTimestamp(String objecctId,
			Message.Status status, long timestamp) {
		ContentValues cv = new ContentValues();
		cv.put(STATUS, status.getValue());
		cv.put(TIMESTAMP, timestamp + "");
		String objectId = objecctId;
		return updateMessage(objectId, cv);
	}

	public static int updateMessage(String objectId, ContentValues cv) {
		DatabaseHelper dbHelper = new DatabaseHelper(MyApplication.mCtx,
				MyApplication.DB_NAME, MyApplication.DB_VERSION);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int updateN = db.update(MESSAGES, cv, "objectId=?",
				new String[] { objectId });
		db.close();
		return updateN;
	}

	public static int updateStatus(String objectId, Message.Status status) {
		ContentValues cv = new ContentValues();
		cv.put(STATUS, status.getValue());
		return updateMessage(objectId, cv);
	}

	public static void markMessagesAsHaveRead(List<Message> msgs) {
		DatabaseHelper dbHelper = new DatabaseHelper(MyApplication.mCtx,
				MyApplication.DB_NAME, MyApplication.DB_VERSION);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.beginTransaction();
		for (Message msg : msgs) {
			msg.setReadStatus(Message.ReadStatus.HaveRead);
			ContentValues cv = new ContentValues();
			cv.put(READ_STATUS, msg.getReadStatus().getValue());
			db.update(MESSAGES, cv, "objectId=?",
					new String[] { msg.getObjectId() });
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}

	public static int getUnreadCount(SQLiteDatabase db, String convid) {
		int count = 0;
		Cursor cursor = db
				.rawQuery(
						"select count(*) from messages where convid=? and readStatus=?",
						new String[] { convid,
								Message.ReadStatus.Unread.getValue() + "" });
		if (cursor.moveToNext()) {
			count = cursor.getInt(0);
		}
		cursor.close();
		return count;
	}

	public static void updateContent(String objectId, String url) {
		ContentValues cv = new ContentValues();
		cv.put(CONTENT, url);
		updateMessage(objectId, cv);
	}
}
