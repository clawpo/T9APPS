package com.runpkg;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import com.runpkg.utils.RootCmd;

import android.app.ActivityManager;
import android.app.Service;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class KillService extends Service {

	SharedPreferences perf_applist;
	Timer timer;
	Context cont;

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case 1:
				// Log.d("yan", "timer handler");
				perf_applist = getSharedPreferences("pkgtable", MODE_PRIVATE);

				Map<String, ?> tmpMap = perf_applist.getAll();

				Iterator<String> it = tmpMap.keySet().iterator();

				while (it.hasNext()) {
					String k = it.next().toString();
					// Log.d("yan", k + " = " + tmpMap.get(k));

					if (perf_applist.getInt(k, 0) != 0) {

						try {
							int res = cont.getPackageManager()
									.getApplicationEnabledSetting(k);
							if (res != cont.getPackageManager().COMPONENT_ENABLED_STATE_DISABLED) {

								if (RootCmd.haveRoot()) {
									ActivityManager am = (ActivityManager) cont
											.getSystemService(Context.ACTIVITY_SERVICE);
									List<RunningTaskInfo> tasks = am
											.getRunningTasks(100);
									if (!tasks.isEmpty()) {
										ComponentName topActivity = tasks
												.get(0).topActivity;
										if (!topActivity.getPackageName()
												.equals(k)) {
											String result = RootCmd
													.execRootCmd("pm disable "
															+ k);

											Log.d("yan", "Disable" + k);

										} else {
											Log.d("yan", k + "is topActivity");
										}

									}
								}
							} else {

							}
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				}
				// Log.d("yan", "Timer handler end");
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		Log.d("yan", "KillService oncreate");

		cont = this;

		TimerTask task = new TimerTask() {
			public void run() {
				Message message = new Message();
				message.what = 1;
				handler.sendMessage(message);

			}
		};

		timer = new Timer(true);
		timer.schedule(task, 10000, 5 * 60 * 1000); // 延时1000ms后执行，300s执行一次

		super.onCreate();

	}

}
