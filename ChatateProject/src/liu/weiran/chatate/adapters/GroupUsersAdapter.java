package liu.weiran.chatate.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.avos.avoscloud.AVUser;
import liu.weiran.chatate.R;
import liu.weiran.chatate.util.ChatUtils;

import java.util.List;

public class GroupUsersAdapter extends BaseListAdapter<AVUser> {
	public GroupUsersAdapter(Context ctx, List<AVUser> datas) {
		super(ctx, datas);
	}

	@Override
	public View getView(int position, View conView, ViewGroup parent) {
		if (conView == null) {
			conView = View.inflate(ctx, R.layout.group_user_item, null);
		}
		AVUser user = items.get(position);
		ChatUtils.setUserView(conView, user);
		return conView;
	}
}
