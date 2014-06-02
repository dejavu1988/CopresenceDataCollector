package org.sesy.coco.datacollector;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sesy.coco.datacollector.log.ConfigureLog4J;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import wei.mark.standout.StandOutWindow;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;

public final class DataMonitor extends StandOutWindow{

	private static final int APP_SELECTOR_ID = -2;
	private static final int APP_SELECTOR_CODE = 2;
	public static final int APP_SELECTOR_FINISHED_CODE = 3;	
	private static final int APP_BIND_CODE = 5;
	private static final int APP_HELP_CODE = 10;
	public static final int APP_HELP_FINISHED_CODE = 11;
	private static final int APP_STATUS_CODE = 15;
	public static final int APP_STATUS_FINISHED_CODE = 16;
	public static final int APP_ALARM_CODE = 19;
	public static final int APP_MAIN_CODE = 17;
	public static final int APP_MAIN_FINISHED_CODE = 18;
	private static final int APP_REPORT_CODE = 12;
	public static final int APP_REPORT_FINISHED_CODE = 13;
	public static final int APP_BIND_FINISHED_CODE = 6;
	public static final int STARTUP_CODE = 4;
	public static final int VIEW_UPDATE_ID = -4;
	public static final int STATUS_UPDATE_CODE = 1;
	private  int preWidth, preHeight;
	private static int PX = 0;
	private static int PY = 0;
	//private static int PW = 0;
	//private static int PH = 0;
	private PrefManager pM;
	private StatusManager sM;
	private View view;
	private LinearLayout li, la, lr;
	private RelativeLayout rl;
	private Button ba, ba1, ba2, by, bo, bn, bc, bs;
	private ImageView im;
	private TextView tb, t1, t2, t3, tr, t0;
	private EditText et;
	//public static boolean winOn = false;
	// Definition of the one requestCode we use for receiving results.
    //static final private int GET_CODE = 0;
	private static boolean FullSize = true;
	public static boolean existsWindow = false;
	public static boolean On_Demand = false;
	public static boolean checkWindow = false;
	Logger log;
	Thread thr;
	
	WindowManager mWindowManager;
	//SparseArray<FolderModel> mFolders;
	Animation mFadeOut, mFadeIn;
	
	public static void showStatus(Context context) {
		sendData(context, DataMonitor.class, DISREGARD_ID, STARTUP_CODE,
				null, null, DISREGARD_ID);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();

		log = Logger.getLogger(DataMonitor.class);  
        ConfigureLog4J.configure(this);  
        LogManager.getRootLogger().setLevel((Level)Level.DEBUG);   
        log.info("onCreate");
        
        checkWindow = true;
		mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		pM = new PrefManager(getApplicationContext());
		sM = new StatusManager(getApplicationContext());
		
		Display display = mWindowManager.getDefaultDisplay(); 
		int width = display.getWidth();  // deprecated
		int height = display.getHeight();
		log.info("Display size: width="+width+", heigth="+height);
		//Toast.makeText(getApplicationContext(), "Display size: width="+width+", heigth="+height, Toast.LENGTH_LONG).show();
		if(width<=900 && width > 700){
			preWidth = 290;
			preHeight = 340;
		}else if(width<=700 && width > 500){
			preWidth = 220;
			preHeight = 250;
		}else if(width<=500 && width > 400){
			preWidth = 300;
			preHeight = 350;
		}else if(width<=400 && width > 300){
			preWidth = 250;
			preHeight = 340;
		}
		
		mFadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
		mFadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);

		int duration = 100;
		mFadeOut.setDuration(duration);
		mFadeIn.setDuration(duration);
		
		thr = new Thread(null, dTask, "StatusMonitor");
        thr.start();
	}
	
	Runnable dTask = new Runnable() {
	        public void run() {
	        	while(checkWindow){
	        		try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        		Window win= getWindow(DEFAULT_ID);
	    			existsWindow = (win != null);
	    			//log.info("existsWindow: "+existsWindow);
	        	}
	        }
	};
	
	public synchronized void stopThread(){
    	if(thr != null){
    		Thread moribund = thr;
    		thr = null;
    		moribund.interrupt();
    	}
    }
	
	@Override
	public String getAppName() {
		return "Status Monitor";
	}
	
	@Override
	public int getAppIcon() {
		return R.drawable.ic_launcher;
	}

	@Override
	public void createAndAttachView(final int id, FrameLayout frame) {
		//LayoutInflater inflater = LayoutInflater.from(this);
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.widget, frame, true);
		rl = (RelativeLayout) view.findViewById(R.id.feedback);
		li = (LinearLayout) view.findViewById(R.id.initlayout);
		la = (LinearLayout) view.findViewById(R.id.asklayout);
		lr = (LinearLayout) view.findViewById(R.id.remindlayout);
		ba = (Button) view.findViewById(R.id.ask_button);
		ba1 = (Button) view.findViewById(R.id.ask_button1);
		ba2 = (Button) view.findViewById(R.id.ask_button2);
		by = (Button) view.findViewById(R.id.yes_button);
		bo = (Button) view.findViewById(R.id.o_button);
		bn = (Button) view.findViewById(R.id.no_button);
		bc = (Button) view.findViewById(R.id.cancel_button);
		bs = (Button) view.findViewById(R.id.set_button);
		im = (ImageView) view.findViewById(R.id.indicator);
		tb = (TextView) view.findViewById(R.id.status);
		tr = (TextView) view.findViewById(R.id.remindinfo);
		t0 = (TextView) view.findViewById(R.id.statinfo);
		t1 = (TextView) view.findViewById(R.id.widgetinfo1);
		t2 = (TextView) view.findViewById(R.id.bindname);
		t3 = (TextView) view.findViewById(R.id.widgetinfo2);
		et = (EditText) view.findViewById(R.id.taskCommentText);
		
		ba.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				log.info("Ask button clicked");
				li.setVisibility(View.GONE);
				lr.setVisibility(View.GONE);
				la.setVisibility(View.VISIBLE);
				int c = pM.getColocationCounter();
				int n = pM.getNoncolocationCounter();
				int sum = c + n;
				float cp = (sum == 0)? 0.0f:(float) ( c * 1000 / sum ) / 10;
				float np = (sum == 0)? 0.0f:(float) ( n * 1000 / sum ) / 10;
				//t0.setText("This device has recorded "+c+"("+cp+"%) Colocation, "+n+"("+np+"%) Non-colocation.");
				t0.setText("");
				t2.setText(pM.getBindName());
				On_Demand = true;				
				
			}
			
		});
		
		ba1.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				log.info("Ask1 button clicked");
				
				new Thread(new Runnable(){
                	@Override
                	public void run(){
                		sendData(id, DataMonitor.class, DEFAULT_ID, APP_BIND_CODE, null);
                	}
                }).start();
				
			}
			
		});
		
		ba2.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				log.info("Ask2 button clicked");
				
				new Thread(new Runnable(){
                	@Override
                	public void run(){
                		sendData(id, DataMonitor.class, DEFAULT_ID, APP_STATUS_CODE, null);
                	}
                }).start();
				
			}
			
		});
		
		by.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				log.info("Yes button clicked");
				pM.updateTaskComment(et.getText().toString());
				et.setText("");
				On_Demand = false;
				AlarmService.alarmStatus = false;
		  	  	if(AlarmService.vib != null){
			  		AlarmService.vib.cancel();
			  	}
			  	if(AlarmService.r != null){
			  		AlarmService.r.stop();
			  	}  
				new Thread(new Runnable(){
                	@Override
                	public void run(){
                		Intent clickIntentY = new Intent(DataMonitor.this, TriggerService.class);
              	      	clickIntentY.putExtra("gt", 1);
              	      	startService(clickIntentY);
              	      sM.getStatus();
              		sM.updateWidgetStatus();
                	}
                }).start();
				
			}
			
		});
		
		/*bo.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				log.info("O button clicked");
				On_Demand = false;
				AlarmService.alarmStatus = false;
		  	  	if(AlarmService.vib != null){
			  		AlarmService.vib.cancel();
			  	}
			  	if(AlarmService.r != null){
			  		AlarmService.r.stop();
			  	}  
				new Thread(new Runnable(){
                	@Override
                	public void run(){
                		Intent clickIntentO = new Intent(DataMonitor.this, TriggerService.class);
              	      	clickIntentO.putExtra("gt", 2);
              	      	startService(clickIntentO);
                	}
                }).start();
				
			}
			
		});*/
		
		bn.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				log.info("No button clicked");
				pM.updateTaskComment(et.getText().toString());
				et.setText("");
				On_Demand = false;
				AlarmService.alarmStatus = false;
		  	  	if(AlarmService.vib != null){
			  		AlarmService.vib.cancel();
			  	}
			  	if(AlarmService.r != null){
			  		AlarmService.r.stop();
			  	}  
				new Thread(new Runnable(){
                	@Override
                	public void run(){
                		Intent clickIntentN = new Intent(DataMonitor.this, TriggerService.class);
              	      	clickIntentN.putExtra("gt", 3);
              	      	startService(clickIntentN);
              	      sM.getStatus();
              		sM.updateWidgetStatus();
                	}
                }).start();
				
			}
			
		});
		
		bc.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				log.info("Cancel button clicked");
				li.setVisibility(View.GONE);
				lr.setVisibility(View.VISIBLE);
				la.setVisibility(View.GONE);
				et.setText("");
				On_Demand = false;
				AlarmService.alarmStatus = false;
		  	  	if(AlarmService.vib != null){
			  		AlarmService.vib.cancel();
			  	}
			  	if(AlarmService.r != null){
			  		AlarmService.r.stop();
			  	}  
			  	
			}
			
		});
		
		bs.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				log.info("Setting button clicked");
				
				new Thread(new Runnable(){
                	@Override
                	public void run(){
                		sendData(id, DataMonitor.class, DEFAULT_ID, APP_SELECTOR_CODE, null);
                	}
                }).start();
				
			}
			
		});
		
		log.info("createAndAttachView");
	}

	Handler blinkHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	if(AlarmService.alarmStatus){
                switch (msg.what) {
                	case 0:
                		rl.setBackgroundColor(0x80FFFFFF);
                        break;
                	case 1:
                		rl.setBackgroundResource(R.drawable.bg_whiteborder);
                		break;

                	}
                super.handleMessage(msg);
        	}else{
        		rl.setBackgroundResource(R.drawable.bg_whiteborder);
        	}
        }
    };
	

	@Override
	public Animation getShowAnimation(int id) {
		if (isExistingId(id)) {
			// restore
			return AnimationUtils.loadAnimation(this,
					android.R.anim.slide_in_left);
		} else {
			// show
			return super.getShowAnimation(id);
		}
	}
	
	@Override
	public StandOutLayoutParams getParams(int id, Window window) {
		return new StandOutLayoutParams(id, preWidth, preHeight, 
				StandOutLayoutParams.RIGHT, StandOutLayoutParams.AUTO_POSITION);
	}
	
	@Override
	public int getFlags(int id) {
		return super.getFlags(id) | StandOutFlags.FLAG_BODY_MOVE_ENABLE
					| StandOutFlags.FLAG_WINDOW_EDGE_LIMITS_ENABLE
					| StandOutFlags.FLAG_WINDOW_FOCUSABLE_DISABLE;
	}
	
	@Override
	public void onReceiveData(int id, int requestCode, Bundle data,
			Class<? extends StandOutWindow> fromCls, int fromId) {		
		
		switch (requestCode) {
		case APP_SELECTOR_CODE:
			log.info("setting window received");
			if (true) {
				// app selector receives data
				//Window window = show(APP_SELECTOR_ID);
				//window.data.putInt("fromId", fromId);
				final Window window = getWindow(DEFAULT_ID);
				final StandOutLayoutParams params = (StandOutLayoutParams) window.getLayoutParams();
				PX = params.x; PY = params.y;
				//PW = params.width; PH = params.height;
				final View folderView = window.findViewById(R.id.feedback);
				final ImageView screenshot = (ImageView) window
						.findViewById(R.id.preview);
				
				if (FullSize){
					FullSize = false;

					final Drawable drawable = getResources().getDrawable(
							R.drawable.ic_menu_archive);

					screenshot.setImageDrawable(drawable);

					mFadeOut.setAnimationListener(new AnimationListener() {

						@Override
						public void onAnimationStart(Animation animation) {
						}

						@Override
						public void onAnimationRepeat(Animation animation) {
						}

						@Override
						public void onAnimationEnd(Animation animation) {
							folderView.setVisibility(View.GONE);

							// post so that the widget is invisible
							// before
							// anything else happens
							screenshot.post(new Runnable() {

								@Override
								public void run() {
									// preview should be centered
									// vertically
									params.x = 0; params.y = 0;
									//params.y = params.y + params.height / 2
									//		- drawable.getIntrinsicHeight() / 2;

									params.width = drawable.getIntrinsicWidth();
									params.height = drawable
											.getIntrinsicHeight();

									updateViewLayout(DEFAULT_ID, params);

									screenshot.setVisibility(View.VISIBLE);
									screenshot.startAnimation(mFadeIn);
								}
							});
						}
					});

					folderView.startAnimation(mFadeOut);
				}
				Intent intent = new Intent();
				intent.setClass(this, SettingActivity.class);
				intent.putExtra("wid", DEFAULT_ID);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
			break;
			
		case STARTUP_CODE:
			show(DEFAULT_ID);	
			
			log.info("startup window received");
			break;
		
		case APP_SELECTOR_FINISHED_CODE:
			log.info("setting window end received");
			if (!FullSize) {
				FullSize = true;
				
				final Window window = getWindow(DEFAULT_ID);
				final ImageView screenshot = (ImageView) window
						.findViewById(R.id.preview);
				final View folderView = window.findViewById(R.id.feedback);
				final StandOutLayoutParams params = (StandOutLayoutParams) window.getLayoutParams();

				mFadeOut.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
						//Log.d("FloatingFolder", "Animation started");
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						//Log.d("FloatingFolder", "Animation ended");
						screenshot.setVisibility(View.GONE);

						// post so that screenshot is invisible
						// before anything else happens
						screenshot.post(new Runnable() {

							@Override
							public void run() {
								StandOutLayoutParams originalParams = getParams(DEFAULT_ID, window);

								Drawable drawable = screenshot
										.getDrawable();
								screenshot.setImageDrawable(null);
								params.x = PX; params.y = PY;
								/*params.y = params.y - originalParams.height
										/ 2 + drawable.getIntrinsicHeight()
										/ 2;*/

								//params.width = PW;
								//params.height = PH;
								params.width = originalParams.width;
								params.height = originalParams.height;

								updateViewLayout(DEFAULT_ID, params);

								folderView.setVisibility(View.VISIBLE);

								folderView.startAnimation(mFadeIn);
							}
						});
					}
				});

				screenshot.startAnimation(mFadeOut);
			}				
			break;
			
		case APP_BIND_CODE:
			log.info("bind window received");
			if (true) {
				// app bind receives data
				//Window window = show(APP_SELECTOR_ID);
				//window.data.putInt("fromId", fromId);
				final Window window = getWindow(DEFAULT_ID);
				final StandOutLayoutParams params = (StandOutLayoutParams) window.getLayoutParams();
				PX = params.x; PY = params.y;
				//PW = params.width; PH = params.height;
				final View folderView = window.findViewById(R.id.feedback);
				final ImageView screenshot = (ImageView) window
						.findViewById(R.id.preview);
				
				if (FullSize){
					FullSize = false;

					final Drawable drawable = getResources().getDrawable(
							R.drawable.ic_menu_archive);

					screenshot.setImageDrawable(drawable);

					mFadeOut.setAnimationListener(new AnimationListener() {

						@Override
						public void onAnimationStart(Animation animation) {
						}

						@Override
						public void onAnimationRepeat(Animation animation) {
						}

						@Override
						public void onAnimationEnd(Animation animation) {
							folderView.setVisibility(View.GONE);

							// post so that the widget is invisible
							// before
							// anything else happens
							screenshot.post(new Runnable() {

								@Override
								public void run() {
									// preview should be centered
									// vertically
									params.x = 0; params.y = 0;
									//params.y = params.y + params.height / 2
									//		- drawable.getIntrinsicHeight() / 2;

									params.width = drawable.getIntrinsicWidth();
									params.height = drawable
											.getIntrinsicHeight();

									updateViewLayout(DEFAULT_ID, params);

									screenshot.setVisibility(View.VISIBLE);
									screenshot.startAnimation(mFadeIn);
								}
							});
						}
					});

					folderView.startAnimation(mFadeOut);
				}
				Intent intent = new Intent();
				intent.setClass(this, BindActivity.class);
				intent.putExtra("wid", DEFAULT_ID);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
			break;
			
		case APP_BIND_FINISHED_CODE:
			log.info("bind window end received");
			if (!FullSize) {
				FullSize = true;
				
				final Window window = getWindow(DEFAULT_ID);
				final ImageView screenshot = (ImageView) window
						.findViewById(R.id.preview);
				final View folderView = window.findViewById(R.id.feedback);
				final StandOutLayoutParams params = (StandOutLayoutParams) window.getLayoutParams();

				mFadeOut.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
						//Log.d("FloatingFolder", "Animation started");
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						//Log.d("FloatingFolder", "Animation ended");
						screenshot.setVisibility(View.GONE);

						// post so that screenshot is invisible
						// before anything else happens
						screenshot.post(new Runnable() {

							@Override
							public void run() {
								StandOutLayoutParams originalParams = getParams(DEFAULT_ID, window);

								Drawable drawable = screenshot
										.getDrawable();
								screenshot.setImageDrawable(null);
								params.x = PX; params.y = PY;
								/*params.y = params.y - originalParams.height
										/ 2 + drawable.getIntrinsicHeight()
										/ 2;*/

								//params.width = PW;
								//params.height = PH;
								params.width = originalParams.width;
								params.height = originalParams.height;

								updateViewLayout(DEFAULT_ID, params);

								folderView.setVisibility(View.VISIBLE);

								folderView.startAnimation(mFadeIn);
							}
						});
					}
				});

				screenshot.startAnimation(mFadeOut);
			}				
			break;
			
		case APP_HELP_CODE:
			log.info("help window received");
			if (true) {
				// app bind receives data
				//Window window = show(APP_SELECTOR_ID);
				//window.data.putInt("fromId", fromId);
				final Window window = getWindow(DEFAULT_ID);
				final StandOutLayoutParams params = (StandOutLayoutParams) window.getLayoutParams();
				PX = params.x; PY = params.y;
				//PW = params.width;	PH = params.height;
				final View folderView = window.findViewById(R.id.feedback);
				final ImageView screenshot = (ImageView) window
						.findViewById(R.id.preview);
				
				if (FullSize){
					FullSize = false;

					final Drawable drawable = getResources().getDrawable(
							R.drawable.ic_menu_archive);

					screenshot.setImageDrawable(drawable);

					mFadeOut.setAnimationListener(new AnimationListener() {

						@Override
						public void onAnimationStart(Animation animation) {
						}

						@Override
						public void onAnimationRepeat(Animation animation) {
						}

						@Override
						public void onAnimationEnd(Animation animation) {
							folderView.setVisibility(View.GONE);

							// post so that the widget is invisible
							// before
							// anything else happens
							screenshot.post(new Runnable() {

								@Override
								public void run() {
									// preview should be centered
									// vertically
									params.x = 0; params.y = 0;
									//params.y = params.y + params.height / 2
									//		- drawable.getIntrinsicHeight() / 2;

									params.width = drawable.getIntrinsicWidth();
									params.height = drawable
											.getIntrinsicHeight();

									updateViewLayout(DEFAULT_ID, params);

									screenshot.setVisibility(View.VISIBLE);
									screenshot.startAnimation(mFadeIn);
								}
							});
						}
					});

					folderView.startAnimation(mFadeOut);
				}
				Intent intent = new Intent();
				intent.setClass(this, HelpActivity.class);
				intent.putExtra("wid", DEFAULT_ID);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
			break;
			
		case APP_HELP_FINISHED_CODE:
			log.info("help window end received");
			if (!FullSize) {
				FullSize = true;
				
				final Window window = getWindow(DEFAULT_ID);
				final ImageView screenshot = (ImageView) window
						.findViewById(R.id.preview);
				final View folderView = window.findViewById(R.id.feedback);
				final StandOutLayoutParams params = (StandOutLayoutParams) window.getLayoutParams();

				mFadeOut.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
						//Log.d("FloatingFolder", "Animation started");
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						//Log.d("FloatingFolder", "Animation ended");
						screenshot.setVisibility(View.GONE);

						// post so that screenshot is invisible
						// before anything else happens
						screenshot.post(new Runnable() {

							@Override
							public void run() {
								StandOutLayoutParams originalParams = getParams(DEFAULT_ID, window);

								Drawable drawable = screenshot
										.getDrawable();
								screenshot.setImageDrawable(null);
								params.x = PX; params.y = PY;
								/*params.y = params.y - originalParams.height
										/ 2 + drawable.getIntrinsicHeight()
										/ 2;*/

								//params.width = PW;
								//params.height = PH;
								params.width = originalParams.width;
								params.height = originalParams.height;

								updateViewLayout(DEFAULT_ID, params);

								folderView.setVisibility(View.VISIBLE);

								folderView.startAnimation(mFadeIn);
							}
						});
					}
				});

				screenshot.startAnimation(mFadeOut);
			}				
			break;
		
		case APP_STATUS_CODE:
			log.info("status window received");
			if (true) {
				// app bind receives data
				//Window window = show(APP_SELECTOR_ID);
				//window.data.putInt("fromId", fromId);
				final Window window = getWindow(DEFAULT_ID);
				final StandOutLayoutParams params = (StandOutLayoutParams) window.getLayoutParams();
				PX = params.x; PY = params.y;
				//PW = params.width;	PH = params.height;
				final View folderView = window.findViewById(R.id.feedback);
				final ImageView screenshot = (ImageView) window
						.findViewById(R.id.preview);
				
				if (FullSize){
					FullSize = false;

					final Drawable drawable = getResources().getDrawable(
							R.drawable.ic_menu_archive);

					screenshot.setImageDrawable(drawable);

					mFadeOut.setAnimationListener(new AnimationListener() {

						@Override
						public void onAnimationStart(Animation animation) {
						}

						@Override
						public void onAnimationRepeat(Animation animation) {
						}

						@Override
						public void onAnimationEnd(Animation animation) {
							folderView.setVisibility(View.GONE);

							// post so that the widget is invisible
							// before
							// anything else happens
							screenshot.post(new Runnable() {

								@Override
								public void run() {
									// preview should be centered
									// vertically
									params.x = 0; params.y = 0;
									//params.y = params.y + params.height / 2
									//		- drawable.getIntrinsicHeight() / 2;

									params.width = drawable.getIntrinsicWidth();
									params.height = drawable
											.getIntrinsicHeight();

									updateViewLayout(DEFAULT_ID, params);

									screenshot.setVisibility(View.VISIBLE);
									screenshot.startAnimation(mFadeIn);
								}
							});
						}
					});

					folderView.startAnimation(mFadeOut);
				}
				Intent intent = new Intent();
				intent.setClass(this, StatusActivity.class);
				intent.putExtra("wid", DEFAULT_ID);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
			break;
			
		case APP_STATUS_FINISHED_CODE:
			log.info("status window end received");
			if (!FullSize) {
				FullSize = true;
				
				final Window window = getWindow(DEFAULT_ID);
				final ImageView screenshot = (ImageView) window
						.findViewById(R.id.preview);
				final View folderView = window.findViewById(R.id.feedback);
				final StandOutLayoutParams params = (StandOutLayoutParams) window.getLayoutParams();

				mFadeOut.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
						//Log.d("FloatingFolder", "Animation started");
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						//Log.d("FloatingFolder", "Animation ended");
						screenshot.setVisibility(View.GONE);

						// post so that screenshot is invisible
						// before anything else happens
						screenshot.post(new Runnable() {

							@Override
							public void run() {
								StandOutLayoutParams originalParams = getParams(DEFAULT_ID, window);

								Drawable drawable = screenshot
										.getDrawable();
								screenshot.setImageDrawable(null);
								params.x = PX; params.y = PY;
								/*params.y = params.y - originalParams.height
										/ 2 + drawable.getIntrinsicHeight()
										/ 2;*/

								//params.width = PW;
								//params.height = PH;
								params.width = originalParams.width;
								params.height = originalParams.height;

								updateViewLayout(DEFAULT_ID, params);

								folderView.setVisibility(View.VISIBLE);

								folderView.startAnimation(mFadeIn);
							}
						});
					}
				});

				screenshot.startAnimation(mFadeOut);
			}				
			break;
			
		case APP_REPORT_CODE:
			log.info("report window received");
			if (true) {
				// app bind receives data
				//Window window = show(APP_SELECTOR_ID);
				//window.data.putInt("fromId", fromId);
				final Window window = getWindow(DEFAULT_ID);
				final StandOutLayoutParams params = (StandOutLayoutParams) window.getLayoutParams();
				PX = params.x; PY = params.y;
				//PW = params.width; PH = params.height;
				final View folderView = window.findViewById(R.id.feedback);
				final ImageView screenshot = (ImageView) window
						.findViewById(R.id.preview);
				
				if (FullSize){
					FullSize = false;

					final Drawable drawable = getResources().getDrawable(
							R.drawable.ic_menu_archive);

					screenshot.setImageDrawable(drawable);

					mFadeOut.setAnimationListener(new AnimationListener() {

						@Override
						public void onAnimationStart(Animation animation) {
						}

						@Override
						public void onAnimationRepeat(Animation animation) {
						}

						@Override
						public void onAnimationEnd(Animation animation) {
							folderView.setVisibility(View.GONE);

							// post so that the widget is invisible
							// before
							// anything else happens
							screenshot.post(new Runnable() {

								@Override
								public void run() {
									// preview should be centered
									// vertically
									params.x = 0; params.y = 0;
									//params.y = params.y + params.height / 2
									//		- drawable.getIntrinsicHeight() / 2;

									params.width = drawable.getIntrinsicWidth();
									params.height = drawable
											.getIntrinsicHeight();

									updateViewLayout(DEFAULT_ID, params);

									screenshot.setVisibility(View.VISIBLE);
									screenshot.startAnimation(mFadeIn);
								}
							});
						}
					});

					folderView.startAnimation(mFadeOut);
				}
				Intent intent = new Intent();
				intent.setClass(this, ReportErrActivity.class);
				intent.putExtra("wid", DEFAULT_ID);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
			break;
			
		case APP_REPORT_FINISHED_CODE:
			log.info("report window end received");
			if (!FullSize) {
				FullSize = true;
				
				final Window window = getWindow(DEFAULT_ID);
				final ImageView screenshot = (ImageView) window
						.findViewById(R.id.preview);
				final View folderView = window.findViewById(R.id.feedback);
				final StandOutLayoutParams params = (StandOutLayoutParams) window.getLayoutParams();

				mFadeOut.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
						//Log.d("FloatingFolder", "Animation started");
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						//Log.d("FloatingFolder", "Animation ended");
						screenshot.setVisibility(View.GONE);

						// post so that screenshot is invisible
						// before anything else happens
						screenshot.post(new Runnable() {

							@Override
							public void run() {
								StandOutLayoutParams originalParams = getParams(DEFAULT_ID, window);

								Drawable drawable = screenshot
										.getDrawable();
								screenshot.setImageDrawable(null);
								params.x = PX; params.y = PY;
								/*params.y = params.y - originalParams.height
										/ 2 + drawable.getIntrinsicHeight()
										/ 2;*/

								//params.width = PW;
								//params.height = PH;
								params.width = originalParams.width;
								params.height = originalParams.height;

								updateViewLayout(DEFAULT_ID, params);

								folderView.setVisibility(View.VISIBLE);

								folderView.startAnimation(mFadeIn);
							}
						});
					}
				});

				screenshot.startAnimation(mFadeOut);
			}				
			break;
			
		case APP_MAIN_CODE:
			log.info("main window received");
			if (true) {
				// app bind receives data
				//Window window = show(APP_SELECTOR_ID);
				//window.data.putInt("fromId", fromId);
				final Window window = getWindow(DEFAULT_ID);
				final StandOutLayoutParams params = (StandOutLayoutParams) window.getLayoutParams();
				PX = params.x; PY = params.y;
				//PW = params.width;	PH = params.height;
				final View folderView = window.findViewById(R.id.feedback);
				final ImageView screenshot = (ImageView) window
						.findViewById(R.id.preview);
				
				if (FullSize){
					FullSize = false;

					final Drawable drawable = getResources().getDrawable(
							R.drawable.ic_menu_archive);

					screenshot.setImageDrawable(drawable);

					mFadeOut.setAnimationListener(new AnimationListener() {

						@Override
						public void onAnimationStart(Animation animation) {
						}

						@Override
						public void onAnimationRepeat(Animation animation) {
						}

						@Override
						public void onAnimationEnd(Animation animation) {
							folderView.setVisibility(View.GONE);

							// post so that the widget is invisible
							// before
							// anything else happens
							screenshot.post(new Runnable() {

								@Override
								public void run() {
									// preview should be centered
									// vertically
									params.x = 0; params.y = 0;
									//params.y = params.y + params.height / 2
									//		- drawable.getIntrinsicHeight() / 2;

									params.width = drawable.getIntrinsicWidth();
									params.height = drawable
											.getIntrinsicHeight();

									updateViewLayout(DEFAULT_ID, params);

									screenshot.setVisibility(View.VISIBLE);
									screenshot.startAnimation(mFadeIn);
								}
							});
						}
					});

					folderView.startAnimation(mFadeOut);
				}
				Intent intent = new Intent();
				intent.setClass(this, MainActivity.class);
				intent.putExtra("wid", DEFAULT_ID);
				intent.putExtra("blink", 1);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
			break;
			
		case APP_MAIN_FINISHED_CODE:
			log.info("main window end received");
			if (!FullSize) {
				FullSize = true;
				
				final Window window = getWindow(DEFAULT_ID);
				final ImageView screenshot = (ImageView) window
						.findViewById(R.id.preview);
				final View folderView = window.findViewById(R.id.feedback);
				final StandOutLayoutParams params = (StandOutLayoutParams) window.getLayoutParams();

				mFadeOut.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
						//Log.d("FloatingFolder", "Animation started");
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						//Log.d("FloatingFolder", "Animation ended");
						screenshot.setVisibility(View.GONE);

						// post so that screenshot is invisible
						// before anything else happens
						screenshot.post(new Runnable() {

							@Override
							public void run() {
								StandOutLayoutParams originalParams = getParams(DEFAULT_ID, window);

								Drawable drawable = screenshot
										.getDrawable();
								screenshot.setImageDrawable(null);
								params.x = PX; params.y = PY;
								/*params.y = params.y - originalParams.height
										/ 2 + drawable.getIntrinsicHeight()
										/ 2;*/

								//params.width = PW;
								//params.height = PH;
								params.width = originalParams.width;
								params.height = originalParams.height;

								updateViewLayout(DEFAULT_ID, params);

								folderView.setVisibility(View.VISIBLE);

								folderView.startAnimation(mFadeIn);
							}
						});
					}
				});

				screenshot.startAnimation(mFadeOut);
			}				
			break;
			
		case APP_ALARM_CODE:
			log.info("widget alarm received");
			if (!FullSize) {
				FullSize = true;
				
				final Window window = getWindow(DEFAULT_ID);
				final ImageView screenshot = (ImageView) window
						.findViewById(R.id.preview);
				final View folderView = window.findViewById(R.id.feedback);
				final StandOutLayoutParams params = (StandOutLayoutParams) window.getLayoutParams();

				mFadeOut.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
						//Log.d("FloatingFolder", "Animation started");
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						//Log.d("FloatingFolder", "Animation ended");
						screenshot.setVisibility(View.GONE);

						// post so that screenshot is invisible
						// before anything else happens
						screenshot.post(new Runnable() {

							@Override
							public void run() {
								StandOutLayoutParams originalParams = getParams(DEFAULT_ID, window);

								Drawable drawable = screenshot
										.getDrawable();
								screenshot.setImageDrawable(null);
								//params.x = PX; params.y = PY;
								params.y = params.y - originalParams.height
										/ 2 + drawable.getIntrinsicHeight()
										/ 2;

								//params.width = PW;
								//params.height = PH;
								params.width = originalParams.width;
								params.height = originalParams.height;

								updateViewLayout(DEFAULT_ID, params);

								folderView.setVisibility(View.VISIBLE);

								folderView.startAnimation(mFadeIn);
							}
						});
					}
				});

				screenshot.startAnimation(mFadeOut);
			}			
			
			for (int i=0; i<16; i++)
		    {
		        Message msg = new Message();
		         if(i % 2 == 0){
		             msg.what = 0;
		         }
		        else{
		            msg.what=1;
		        }

		        blinkHandler.sendMessageDelayed(msg, i*1000);
		    }
			
			break;
			
		case STATUS_UPDATE_CODE:
			getWindow(DEFAULT_ID);
			int status = data.getInt("status");
			
			switch(status){
			case Constants.STATUS_READY:
				la.setVisibility(View.GONE);
				lr.setVisibility(View.GONE);
				li.setVisibility(View.VISIBLE);
				im.setImageResource(R.drawable.green);
				im.setVisibility(View.VISIBLE);
				tb.setVisibility(View.GONE);
				ba.setVisibility(View.VISIBLE);
				ba1.setVisibility(View.GONE);
				ba2.setVisibility(View.GONE);
				break;
			case Constants.STATUS_BLOCKED:
				la.setVisibility(View.GONE);
				lr.setVisibility(View.GONE);
				li.setVisibility(View.VISIBLE);
				im.setImageResource(R.drawable.red);
				im.setVisibility(View.VISIBLE);
				tb.setVisibility(View.GONE);
				ba.setVisibility(View.GONE);
				ba1.setVisibility(View.GONE);
				ba2.setVisibility(View.VISIBLE);
				if(!pM.getBindPref()){
					tb.setText("Bind");
					tb.setTextColor(Color.RED);
					im.setVisibility(View.GONE);
					tb.setVisibility(View.VISIBLE);
					ba.setVisibility(View.GONE);
					ba1.setVisibility(View.VISIBLE);
					ba2.setVisibility(View.GONE);
				}
				break;
			case Constants.STATUS_WAITGT:
				la.setVisibility(View.VISIBLE);
				lr.setVisibility(View.GONE);
				li.setVisibility(View.GONE);
				im.setImageResource(R.drawable.green);
				t2.setText(pM.getBindName());
				ba.setVisibility(View.VISIBLE);
				ba1.setVisibility(View.GONE);
				ba2.setVisibility(View.GONE);
				break;
			case Constants.STATUS_SCAN:
				la.setVisibility(View.GONE);
				lr.setVisibility(View.GONE);
				li.setVisibility(View.VISIBLE);
				im.setImageResource(R.drawable.hourglass);
				im.setVisibility(View.VISIBLE);
				tb.setVisibility(View.GONE);
				ba.setVisibility(View.GONE);
				ba1.setVisibility(View.GONE);
				ba2.setVisibility(View.VISIBLE);
				break;
			case Constants.STATUS_COM:
				la.setVisibility(View.GONE);
				lr.setVisibility(View.GONE);
				li.setVisibility(View.VISIBLE);
				im.setImageResource(R.drawable.hourglass);
				im.setVisibility(View.VISIBLE);
				tb.setVisibility(View.GONE);
				ba.setVisibility(View.GONE);
				ba1.setVisibility(View.GONE);
				ba2.setVisibility(View.VISIBLE);
				break;
			default:
				break;
			}
			
			break;
		default:
			break;
		}
	}
	
	@Override
	public boolean onClose(int id, Window window) {
		checkWindow = false;
		stopThread();
		return false;
	}
	
	@Override
	public void onResize(int id, Window window, View view, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			final StandOutLayoutParams params = (StandOutLayoutParams) window
					.getLayoutParams();
			PX = params.x; PY = params.y;
			//PW = params.width; PH = params.height;
		}
	}
	
	@Override
	public boolean onTouchBody(final int id, final Window window,
			final View view, MotionEvent event) {
		if (id != APP_SELECTOR_ID
				&& event.getAction() == MotionEvent.ACTION_MOVE) {
			final StandOutLayoutParams params = (StandOutLayoutParams) window
					.getLayoutParams();
			
			final View folderView = window.findViewById(R.id.feedback);
			final ImageView screenshot = (ImageView) window
					.findViewById(R.id.preview);

			// if touch edge
			if (params.x <= 0) {
				// first time touch edge
				if (FullSize){
					FullSize = false;

					final Drawable drawable = getResources().getDrawable(
							R.drawable.ic_menu_archive);

					screenshot.setImageDrawable(drawable);

					mFadeOut.setAnimationListener(new AnimationListener() {

						@Override
						public void onAnimationStart(Animation animation) {
						}

						@Override
						public void onAnimationRepeat(Animation animation) {
						}

						@Override
						public void onAnimationEnd(Animation animation) {
							folderView.setVisibility(View.GONE);

							// post so that the widget is invisible
							// before
							// anything else happens
							screenshot.post(new Runnable() {

								@Override
								public void run() {
									// preview should be centered
									// vertically
									params.y = params.y + params.height / 2
											- drawable.getIntrinsicHeight() / 2;

									params.width = drawable.getIntrinsicWidth();
									params.height = drawable
											.getIntrinsicHeight();

									updateViewLayout(id, params);

									screenshot.setVisibility(View.VISIBLE);
									screenshot.startAnimation(mFadeIn);
								}
							});
						}
					});

					folderView.startAnimation(mFadeOut);
				}
			} else { // not touch edge

				// first time not touch edge
				if (!FullSize) {
					FullSize = true;

					mFadeOut.setAnimationListener(new AnimationListener() {

						@Override
						public void onAnimationStart(Animation animation) {
							//Log.d("FloatingFolder", "Animation started");
						}

						@Override
						public void onAnimationRepeat(Animation animation) {
						}

						@Override
						public void onAnimationEnd(Animation animation) {
							//Log.d("FloatingFolder", "Animation ended");
							screenshot.setVisibility(View.GONE);

							// post so that screenshot is invisible
							// before anything else happens
							screenshot.post(new Runnable() {

								@Override
								public void run() {
									StandOutLayoutParams originalParams = getParams(id, window);

									Drawable drawable = screenshot
											.getDrawable();
									screenshot.setImageDrawable(null);

									//params.x = PX; 
									//params.y = PY;
									
									//if(PW == 0 || PH == 0){
										params.width = originalParams.width;
										params.height = originalParams.height;
									//}else{
										//params.width = PW;
										//params.height = PH;
									//}
									
									params.y = params.y - originalParams.height
											/ 2 + drawable.getIntrinsicHeight()
											/ 2;

									

									updateViewLayout(id, params);

									folderView.setVisibility(View.VISIBLE);

									folderView.startAnimation(mFadeIn);
								}
							});
						}
					});

					screenshot.startAnimation(mFadeOut);
				}
			}
		}

		return false;
	}

	public String getPersistentNotificationMessage(int id) {
		PackageInfo pInfo = null;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String ver = pInfo.versionName;
		return "Your current app version is v"+ ver;
	}

	public Intent getPersistentNotificationIntent(int id) {		
		return StandOutWindow.getMainIntent(this, DataMonitor.class);
	}
	
	@Override
	public List<DropDownListItem> getDropDownItems(final int id) {
		List<DropDownListItem> items = new ArrayList<DropDownListItem>();
		
		// add
		
		items.add(new DropDownListItem(android.R.drawable.ic_menu_preferences,
				"Status", new Runnable() {

					@Override
					public void run() {
						// show app selector
						sendData(id, DataMonitor.class, DEFAULT_ID, APP_STATUS_CODE, null);
						/*Intent intent = new Intent();
						intent.setClass(this, SettingActivity.class);						
						startActivity(intent);*/
					}
				}));
		items.add(new DropDownListItem(android.R.drawable.ic_menu_preferences,
				"Setting", new Runnable() {

					@Override
					public void run() {
						// show app selector
						sendData(id, DataMonitor.class, DEFAULT_ID, APP_SELECTOR_CODE, null);
						/*Intent intent = new Intent();
						intent.setClass(this, SettingActivity.class);						
						startActivity(intent);*/
					}
				}));
		items.add(new DropDownListItem(android.R.drawable.ic_menu_preferences,
				"Bind/Unbind", new Runnable() {

					@Override
					public void run() {
						// show app selector
						sendData(id, DataMonitor.class, DEFAULT_ID, APP_BIND_CODE, null);
						/*Intent intent = new Intent();
						intent.setClass(this, SettingActivity.class);						
						startActivity(intent);*/
					}
				}));
		items.add(new DropDownListItem(android.R.drawable.ic_menu_help,
				"Report Error", new Runnable() {

					@Override
					public void run() {
						/*Toast.makeText(
								DataMonitor.this,
								getAppName()
										+ " is a demonstration of Data Collection.",
								Toast.LENGTH_SHORT).show();*/
						//sendData(id, DataMonitor.class, DEFAULT_ID, APP_HELP_CODE, null);
						/*Intent intent = new Intent();
						intent.setClass(getApplicationContext(), ReportErrActivity.class);	
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);*/
						sendData(id, DataMonitor.class, DEFAULT_ID, APP_REPORT_CODE, null);
					}
				}));
		/*items.add(new DropDownListItem(android.R.drawable.ic_menu_help,
				"Upload logs", new Runnable() {

					@Override
					public void run() {
						/*Toast.makeText(
								DataMonitor.this,
								getAppName()
										+ " is a demonstration of Data Collection.",
								Toast.LENGTH_SHORT).show();*/
						//sendData(id, DataMonitor.class, DEFAULT_ID, APP_HELP_CODE, null);
						/*Intent intent = new Intent();
						intent.setClass(getApplicationContext(), ReportLogActivity.class);	
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);
					}
				}));*/
		items.add(new DropDownListItem(android.R.drawable.ic_menu_help,
				"Help", new Runnable() {

					@Override
					public void run() {
						// show app selector
						sendData(id, DataMonitor.class, DEFAULT_ID, APP_HELP_CODE, null);
						/*Intent intent = new Intent();
						intent.setClass(this, SettingActivity.class);						
						startActivity(intent);*/
					}
				}));
		items.add(new DropDownListItem(
				android.R.drawable.ic_menu_close_clear_cancel, "Quit ", new Runnable() {

					@Override
					public void run() {
						
						stopService(new Intent(getApplicationContext(), DaemonService.class));
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						checkWindow = false;
						stopThread();
						
						closeAll();
					}
				}));
		return items;
	}
	

}
