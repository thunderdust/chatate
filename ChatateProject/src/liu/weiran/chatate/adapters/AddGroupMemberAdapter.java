package liu.weiran.chatate.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import com.avos.avoscloud.AVUser;
import liu.weiran.chatate.R;
import liu.weiran.chatate.service.CacheService;
import liu.weiran.chatate.ui.views.ViewHolder;
import liu.weiran.chatate.util.ChatUtils;

import java.util.List;

public class AddGroupMemberAdapter extends CheckListAdapter<String> {

	public AddGroupMemberAdapter(Context ctx, List<String> datas) {
		super(ctx, datas);
	}

	@Override
	public View getView(final int position, View conView, ViewGroup parent) {
		if (conView == null) {
			conView = View.inflate(ctx, R.layout.group_add_members_item, null);
		}
		String userId = items.get(position);
		AVUser user = CacheService.lookupUser(userId);
		ChatUtils.setUserView(conView, user);
		CheckBox checkBox = ViewHolder.findViewById(conView, R.id.checkbox);
		setCheckBox(checkBox, position);
		checkBox.setOnCheckedChangeListener(new CheckListener(position));
		return conView;
	}
}
