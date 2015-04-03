package liu.weiran.chatate.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	public DatabaseHelper(Context context, String name, int version) {
		super(context, name, null, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		DatabaseMessage.createTable(db);
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		switch (newVersion) {
		case 4:
		case 3:
		case 2:
			DatabaseMessage.dropTable(db); // lazy solution
			DatabaseMessage.createTable(db);
		case 1:
			break;
		}
	}
}
