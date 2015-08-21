package com.runpkg;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.t9apps.R;
import com.runpkg.utils.PinYin;
import com.runpkg.utils.RootCmd;
import com.runpkg.utils.T9Converter;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

public class Main extends Activity implements AdapterView.OnItemClickListener {
	String key;
	private TextView mInput;
	private ListView list;
	private ResultAdapter resultAdapter = new ResultAdapter();
	private List<ItemObject> docs = new ArrayList<ItemObject>();
	private List<ItemObject> items = new ArrayList<ItemObject>();

	SharedPreferences perf_applist;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mInput = (TextView) findViewById(R.id.digits);
		list = ((ListView) findViewById(R.id.list));
		list.setAdapter(resultAdapter);
		list.setOnItemClickListener(this);
		list.setStackFromBottom(true);
		PackageManager pm = getPackageManager();
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> resolveInfos = pm
		// GET_DISABLED_COMPONENTS 即使将应用程序改为disable也可以获取的到
				.queryIntentActivities(mainIntent,
						PackageManager.GET_DISABLED_COMPONENTS);

		for (ResolveInfo info : resolveInfos) {

			perf_applist = getSharedPreferences("pkgtable", MODE_PRIVATE);

			ApplicationInfo applicationInfo = info.activityInfo.applicationInfo;
			// Log.d("yan",
			// applicationInfo.packageName
			// + " "
			// + info.loadLabel(pm)
			// + "  "
			// + PinYin.getPinYin(info.loadLabel(pm).toString())
			// + "     "
			// + info.loadIcon(pm)
			// + "   "
			// + T9Converter.convert(PinYin.getPinYin(info
			// .loadLabel(pm).toString())) + ""
			// + info.activityInfo.name + sum++);
			// Log.d("yan", info.loadLabel(pm) + "  "
			// + getFirstNum(info.loadLabel(pm).toString()));
			ItemObject item = new ItemObject();
			item.setIcon(info.loadIcon(pm));
			item.setName(info.loadLabel(pm).toString());
			item.setPackageName(info.activityInfo.applicationInfo.packageName);
			item.setActivityName(info.activityInfo.name);
			item.setId(getFirstNum(info.loadLabel(pm).toString())
					+ T9Converter.convert(PinYin.getPinYin(info.loadLabel(pm)
							.toString())));
			item.setFirstNum(getFirstNum(info.loadLabel(pm).toString()));

			if (perf_applist.getInt(item.getPackageName(), 0) != 0) {
				item.setChecked(true);
			} else {
				item.setChecked(false);
			}

			docs.add(item);
		}

		list.scrollTo(0, list.getHeight());

		items = docs;

		Intent intent = new Intent();
		intent.setAction("com.runpkg.KillService");
		startService(intent);

	}

	public void onBtnClick(View view) {
		switch (view.getId()) {
		case R.id.btn1:
			delChar();
			break;
		default:
			addChar(((TextView) view).getText().toString());
		}
		String searchText = mInput.getText().toString();
		search(searchText);
	}

	private String getFirstNum(String input) {
		String value = null;
		StringBuilder num = new StringBuilder();
		Pattern p = Pattern.compile("[\u4e00-\u9fa5]+");
		Matcher m = p.matcher(input);
		while (m.find()) {
			value = m.group(0);
		}
		if (null != value) {
			for (int i = 0; i < value.length(); i++) {
				// 取得每个汉字
				String hanzi = value.substring(i, i + 1);
				// 汉字转拼音
				String pinyin = PinYin.getPinYin(hanzi);
				// 拼音转数字
				String shuzi = T9Converter.convert(pinyin);
				// 取首字母对应的数字
				num.append(shuzi.substring(0, 1));
			}
		}
		return num.toString();
	}

	private void search(String query) {
		List<ItemObject> search = new ArrayList<ItemObject>();

		for (int i = docs.size() - 1; i >= 0; i--) {
			if (docs.get(i).getId().contains(query)) {
				search.add(docs.get(i));
			}
		}
		items = search;
		resultAdapter.notifyDataSetChanged();
		// list.smoothScrollToPosition(0);
		list.scrollTo(0, 0);
	}

	private void addChar(String c) {
		c = c.toLowerCase(Locale.CHINA);
		mInput.setText(mInput.getText() + String.valueOf(c.charAt(0)));
	}

	private void delChar() {
		String text = mInput.getText().toString();
		if (text.length() > 0) {
			mInput.setText(null);
		}
	}

	private static class ViewHolder {
		public TextView name;
		public ImageView icon;
		public CheckBox toggle;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		ItemObject item = (ItemObject) list.getAdapter().getItem(position);

		// ComponentName cn = ComponentName.unflattenFromString(item
		// .getPackageName());

		int res = this.getPackageManager().getApplicationEnabledSetting(
				item.getPackageName());

		if (res == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {

			if (RootCmd.haveRoot()) {
				String result = RootCmd.execRootCmd("pm enable "
						+ item.getPackageName());
				Log.d("yan", result);
			}
		}

		Intent intent = new Intent();
		intent = this.getPackageManager().getLaunchIntentForPackage(
				item.getPackageName());
		startActivity(intent);

		// finish();
	}

	@Override
	protected void onResume() {

		delChar();
		String searchText = mInput.getText().toString();
		search(searchText);

		super.onResume();
	}

	public void onBackPressed() {
		moveTaskToBack(true);
		return;
	}

	private class ResultAdapter extends BaseAdapter {

		@Override
		public synchronized int getCount() {
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
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			final ViewHolder viewHolder;
			final ItemObject item = items.get(position);
			View rowView;
			if (convertView != null) {
				rowView = convertView;
				viewHolder = (ViewHolder) convertView.getTag();
			} else {
				viewHolder = new ViewHolder();
				rowView = LayoutInflater.from(getBaseContext()).inflate(
						R.layout.list_item, parent, false);
			}
			viewHolder.name = (TextView) rowView.findViewById(R.id.name);
			viewHolder.icon = (ImageView) rowView.findViewById(R.id.icon);
			viewHolder.toggle = (CheckBox) rowView.findViewById(R.id.toggle);
			viewHolder.toggle
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {

							String pkgName = item.getPackageName();

							item.setChecked(isChecked);

							if (isChecked) {

								Editor editor = perf_applist.edit();
								editor.putInt(pkgName, 1);
								editor.commit();

								if (item.isChecked()) {
									viewHolder.toggle
											.setBackgroundResource(R.drawable.switch_close);
									notifyDataSetChanged();
								}

							} else {

								Editor editor = perf_applist.edit();
								editor.putInt(pkgName, 0);
								editor.commit();

								if (!item.isChecked()) {
									viewHolder.toggle
											.setBackgroundResource(R.drawable.switch_open);
									notifyDataSetChanged();
								}

							}

						}
					});
			if (item.isChecked()) {
				viewHolder.toggle
						.setBackgroundResource(R.drawable.switch_close);
				notifyDataSetChanged();
			} else {
				viewHolder.toggle.setBackgroundResource(R.drawable.switch_open);
				notifyDataSetChanged();
			}
			rowView.setTag(viewHolder);
			ViewHolder holder = (ViewHolder) rowView.getTag();
			holder.name.setText(item.getName());
			holder.icon.setImageDrawable(item.getIcon());
			return rowView;
		}

	}

	private class ItemObject {
		private String name;
		private Drawable icon;
		private String packageName;
		private String id;
		private String activityName;
		private String firstNum;
		private boolean checked;

		public boolean isChecked() {
			return checked;
		}

		public void setChecked(boolean checked) {
			this.checked = checked;
		}

		public String getFirstNum() {
			return firstNum;
		}

		public void setFirstNum(String firstNum) {
			this.firstNum = firstNum;
		}

		public String getActivityName() {
			return activityName;
		}

		public void setActivityName(String activityName) {
			this.activityName = activityName;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getPackageName() {
			return packageName;
		}

		public void setPackageName(String packageName) {
			this.packageName = packageName;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Drawable getIcon() {
			return icon;
		}

		public void setIcon(Drawable icon) {
			this.icon = icon;
		}

	}
}

// TODO

// 第一次点击问题
// 杀进程会卡
// root问题
// list显示问题