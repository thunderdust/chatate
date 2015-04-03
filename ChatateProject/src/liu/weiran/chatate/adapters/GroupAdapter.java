package liu.weiran.chatate.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import liu.weiran.chatate.avobject.ChatGroup;
import liu.weiran.chatate.ui.views.ViewHolder;
import liu.weiran.chatate.R;

import java.util.List;

public class GroupAdapter extends BaseListAdapter<ChatGroup> {
	public GroupAdapter(Context ctx, List<ChatGroup> datas) {
		super(ctx, datas);
	}

	@Override
	public View getView(int position, View conView, ViewGroup parent) {
		if (conView == null) {
			// conView = View.inflate(ctx, R.layout.group_item,null);
			conView = inflater.inflate(R.layout.group_item, null);
		}
		TextView nameView = ViewHolder.findViewById(conView, R.id.name);
		ChatGroup chatGroup = items.get(position);
		nameView.setText(chatGroup.getGroupDesc());
		return conView;
	}
}
