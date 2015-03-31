package liu.weiran.chatate.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public class BaseListAdapter<T> extends BaseAdapter {

	Context ctx;
	LayoutInflater inflater;
	List<T> items = new ArrayList<T>();

	public BaseListAdapter(Context ctx) {
		initWithContext(ctx);
	}

	public void initWithContext(Context ctx) {
		this.ctx = ctx;
		inflater = LayoutInflater.from(ctx);
	}

	public BaseListAdapter(Context ctx, List<T> datas) {
		initWithContext(ctx);
		this.items = datas;
	}

	public void setDatas(List<T> datas) {
		this.items = datas;
	}

	public List<T> getDatas() {
		return items;
	}

	public void addAll(List<T> subDatas) {
		items.addAll(subDatas);
		notifyDataSetChanged();
	}

	public void add(T object) {
		items.add(object);
		notifyDataSetChanged();
	}

	public void remove(int position) {
		items.remove(position);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return null;
	}

	public void clear() {
		items.clear();
		notifyDataSetChanged();
	}
}
