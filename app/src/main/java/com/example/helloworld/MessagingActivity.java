package com.example.helloworld;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.liveperson.infra.BadArgumentException;
import com.liveperson.infra.CampaignInfo;
import com.liveperson.infra.ConversationViewParams;
import com.liveperson.infra.ICallback;
import com.liveperson.infra.InitLivePersonProperties;
import com.liveperson.infra.LPConversationsHistoryStateToDisplay;
import com.liveperson.infra.MonitoringInitParams;
import com.liveperson.infra.auth.LPAuthenticationParams;
import com.liveperson.infra.auth.LPAuthenticationType;
import com.liveperson.infra.callbacks.InitLivePersonCallBack;
import com.liveperson.infra.model.LPWelcomeMessage;
import com.liveperson.infra.model.MessageOption;
import com.liveperson.messaging.controller.ClientProperties;
import com.liveperson.messaging.sdk.api.LivePerson;
import com.liveperson.messaging.sdk.api.model.ConsumerProfile;
import com.example.helloworld.notification.NotificationUI;
import com.example.helloworld.utils.SampleAppStorage;
import com.example.helloworld.utils.SampleAppUtils;
import com.liveperson.monitoring.MonitoringFactory;
import com.liveperson.infra.messaging_ui.MessagingUIFactory;
import com.liveperson.messaging.MessagingFactory;

import com.liveperson.monitoring.model.EngagementDetails;
import com.liveperson.monitoring.model.LPMonitoringIdentity;
import com.liveperson.monitoring.sdk.MonitoringParams;
import com.liveperson.monitoring.sdk.api.LivepersonMonitoring;
import com.liveperson.monitoring.sdk.callbacks.EngagementCallback;
import com.liveperson.monitoring.sdk.callbacks.MonitoringErrorType;
import com.liveperson.monitoring.sdk.callbacks.SdeCallback;
import com.liveperson.monitoring.sdk.responses.LPEngagementResponse;
import com.liveperson.monitoring.sdk.responses.LPSdeResponse;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * ***** Sample app class - Not related to Messaging SDK ****
 * <p>
 * The main activity of the sample app
 */
public class MessagingActivity extends AppCompatActivity {

	private static final String TAG = MessagingActivity.class.getSimpleName();

	public static final String CAMPAIGN_ID_KEY = "campaignId";
	public static final String ENGAGEMENT_ID_KEY = "engagementId";
	public static final String SESSION_ID_KEY = "sessionId";
	public static final String VISITOR_ID_KEY = "visitorId";
	public static final String ENGAGEMENT_CONTEXT_ID_KEY = "engagementContextId";

	private String currentCampaignId;
	private String currentEngagementId;
	private String currentSessionId;
	private String currentVisitorId;
	private String currentEngagementContextId;

	private Spinner authTypeSpinner;
	private EditText mFirstNameView;
	private EditText mLastNameView;
	private EditText mPhoneNumberView;
	private EditText mAuthCodeView;
	private EditText mPublicKey;
	private Button mOpenConversationButton;
	private TextView mTime;
	private TextView mDate;
	private CheckBox mCallbackToastCheckBox;
	private CheckBox mReadOnlyModeCheckBox;
	private EditText mCampaignIdEditText;
	private EditText mEngagementIdEditText;
	private EditText mSessionIdEditText;
	private EditText mVisitorIdEditText;
	private EditText mEngagementContextIdEditText;
	private boolean isFromPush;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_messaging);

		initSampleAppViews();
		initOpenConversationButton();
		initStartFragmentButton();

//		handlePush(getIntent());

		//MonitoringFactory.INSTANCE.getMonitoring().getApplicationContext()

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		handlePush(intent);
	}

	/**
	 * Init Views
	 */
	private void initSampleAppViews() {
		initAuthTypeSpinner();

		mFirstNameView = findViewById(R.id.first_name);
		mFirstNameView.setText(SampleAppStorage.getInstance(this).getFirstName());

		mLastNameView = findViewById(R.id.last_name);
		mLastNameView.setText(SampleAppStorage.getInstance(this).getLastName());

		mPhoneNumberView = findViewById(R.id.phone_number);
		mPhoneNumberView.setText(SampleAppStorage.getInstance(this).getPhoneNumber());

		mAuthCodeView = findViewById(R.id.auth_code);
		mAuthCodeView.setText(SampleAppStorage.getInstance(this).getAuthCode());

		mPublicKey = findViewById(R.id.public_key);
		mPublicKey.setText(SampleAppStorage.getInstance(this).getPublicKey());

		String sdkVersion = String.format("SDK version %1$s ", LivePerson.getSDKVersion());
		((TextView) findViewById(R.id.sdk_version)).setText(sdkVersion);

		mTime = findViewById(R.id.time_sample_textView);
		mDate = findViewById(R.id.date_sample_textView);

		mCallbackToastCheckBox = findViewById(R.id.check_box_toasts);
		mReadOnlyModeCheckBox = findViewById(R.id.check_box_read_only);

		mCampaignIdEditText = findViewById(R.id.campaign_id);
		mCampaignIdEditText.setText(getIntent().getStringExtra(CAMPAIGN_ID_KEY));

		mEngagementIdEditText = findViewById(R.id.engagement_id);
		mEngagementIdEditText.setText(getIntent().getStringExtra(ENGAGEMENT_ID_KEY));

		mSessionIdEditText = findViewById(R.id.session_id);
		mSessionIdEditText.setText(getIntent().getStringExtra(SESSION_ID_KEY));

		mVisitorIdEditText = findViewById(R.id.visitor_id);
		mVisitorIdEditText.setText(getIntent().getStringExtra(VISITOR_ID_KEY));

		mEngagementContextIdEditText = findViewById(R.id.engagement_context_id);
		mEngagementContextIdEditText.setText(getIntent().getStringExtra(ENGAGEMENT_CONTEXT_ID_KEY));

		updateTime();
		initLocaleSpinner();

		setBadgeButton();
	}

	private void initAuthTypeSpinner() {
		authTypeSpinner = findViewById(R.id.auth_type_spinner);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.supported_auth_types));
		authTypeSpinner.setAdapter(adapter);
		authTypeSpinner.setSelection(SampleAppStorage.getInstance(this).getAuthenticateItemPosition());
	}

	private void setBadgeButton() {
		(findViewById(R.id.badge)).setOnClickListener(v -> LivePerson.getUnreadMessagesCount(SampleAppStorage.SDK_SAMPLE_FCM_APP_ID, getLPAuthenticationParams(), new ICallback<Integer, Exception>() {
			@Override
			public void onSuccess(Integer value) {
				Toast.makeText(MessagingActivity.this, "New badge value: " + value, Toast.LENGTH_LONG).show();
				updateToolBar(value);
			}

			@Override
			public void onError(Exception exception) {
				Toast.makeText(MessagingActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
			}
		}));
	}

	private void updateTime() {
		Locale locale = getLocale();
		DateFormat formatTime = DateFormat.getTimeInstance(DateFormat.MEDIUM, locale);
		DateFormat formatDate = DateFormat.getDateInstance(DateFormat.LONG, locale);
		Date date = new Date(System.currentTimeMillis());
		mDate.setText(formatDate.format(date));
		mTime.setText(formatTime.format(date));
	}

	private void initLocaleSpinner() {
		final EditText language = findViewById(R.id.language_editText);
		final EditText country = findViewById(R.id.country_editText);

		final Spinner localeSpinner = findViewById(R.id.spinner_locale);
		ArrayAdapter<String> localeSpinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.supported_locales));
		localeSpinner.setAdapter(localeSpinnerArrayAdapter);

		Button updateLocale = findViewById(R.id.update_language_button);
		updateLocale.setOnClickListener(v -> {
			String selectedLocale = (String) localeSpinner.getSelectedItem();
			String[] lang_reg = selectedLocale.split("-");

			if (lang_reg.length >= 2) {
				Log.i(TAG, "createLocale: " + lang_reg[0] + "-" + lang_reg[1]);
				createLocale(lang_reg[0], lang_reg[1]);

			} else if (lang_reg.length == 1) {

				String lang = lang_reg[0];

				if (TextUtils.isEmpty(lang)) {
					Log.i(TAG, "createLocale: taking custom locale from edit text.. ");
					createLocale(language.getText().toString(), country.getText().toString());
				} else {
					Log.i(TAG, "createLocale: " + lang + "-null");
					createLocale(lang, null);
				}
			}

			updateTime();
		});

		final Button clear = findViewById(R.id.clear_locale_button);

		clear.setOnClickListener(v -> {
			language.setText(null);
			country.setText(null);
			localeSpinner.setSelection(0);
		});
	}

	/**
	 * Save the user input such as: account, first name, last name, phone number and auth code
	 */
	private void saveAccountAndUserSettings() {
		String firstName = mFirstNameView.getText().toString().trim();
		String lastName = mLastNameView.getText().toString().trim();
		String phoneNumber = mPhoneNumberView.getText().toString().trim();
		String authCode = mAuthCodeView.getText().toString().trim();
		String publicKey = mPublicKey.getText().toString().trim();
		SampleAppStorage.getInstance(this).setAuthenticateItemPosition(authTypeSpinner.getSelectedItemPosition());
		SampleAppStorage.getInstance(this).setFirstName(firstName);
		SampleAppStorage.getInstance(this).setLastName(lastName);
		SampleAppStorage.getInstance(this).setPhoneNumber(phoneNumber);
		SampleAppStorage.getInstance(this).setAuthCode(authCode);
		SampleAppStorage.getInstance(this).setPublicKey(publicKey);
	}

	/**
	 * Set the listener on the "open_conversation" button (Activity mode)
	 */
	private void initOpenConversationButton() {
		mOpenConversationButton = findViewById(R.id.button_start_activity);
		mOpenConversationButton.setOnClickListener(v -> {
			//Sample app setting - used to initialize the SDK with "Activity mode" when entering from push notification
			SampleAppStorage.getInstance(MessagingActivity.this).setSDKMode(SampleAppStorage.SDKMode.ACTIVITY);
			SampleAppUtils.disableButtonAndChangeText(mOpenConversationButton, getString(R.string.initializing));
			saveAccountAndUserSettings();
			removeNotification();
			initActivityConversation();
		});
	}

	/**
	 * Set the listener on the "Open Fragment" button (Fragment mode)
	 */
	private void initStartFragmentButton() {
		Button openFragmentButton = findViewById(R.id.button_start_fragment);
		openFragmentButton.setOnClickListener(v -> {
			//Sample app setting - used to initialize the SDK with "Fragment mode" when entering from push notification
			SampleAppStorage.getInstance(MessagingActivity.this).setSDKMode(SampleAppStorage.SDKMode.FRAGMENT);
			saveAccountAndUserSettings();
			removeNotification();
			MainApplication.getInstance().setShowToastOnCallback(mCallbackToastCheckBox.isChecked());
			openFragmentContainer();
		});
	}

	private void removeNotification() {
		NotificationUI.hideNotification(this);
	}

	/**
	 * Initialize the Messaging SDK and start the SDK in "Activity Mode"
	 */
	private void initActivityConversation() {

		MonitoringInitParams monitoringInitParams =  new MonitoringInitParams("1bf3b045-2bce-4c2f-a51b-2aab6142e526");

		LivePerson.initialize(MessagingActivity.this, new InitLivePersonProperties(SampleAppStorage.getInstance(MessagingActivity.this).getAccount(), SampleAppStorage.SDK_SAMPLE_FCM_APP_ID, monitoringInitParams, new InitLivePersonCallBack() {
			@Override
			public void onInitSucceed() {
				Log.i(TAG, "SDK initialize completed with Activity mode");
				//we are not setting a call back here - we'll listen to callbacks with broadcast receiver
				// in main application class.
				//setCallBack();
				MainApplication.getInstance().setShowToastOnCallback(mCallbackToastCheckBox.isChecked());

				// you can't register pusher before initialization
//				SampleAppUtils.handleGCMRegistration(MessagingActivity.this);

//				LPMonitoringIdentity identity = new LPMonitoringIdentity("6593b8cf-01ef-49da-93b8-cf01ef19daa8","LivePerson");
				LPMonitoringIdentity identity = new LPMonitoringIdentity("","");

				ArrayList identities = new ArrayList();
				identities.add(identity);

				JSONArray entryPoints = new JSONArray();
				JSONArray engagementAttributes = new JSONArray();

				try {
					JSONObject engagement = new JSONObject();

					engagement.put("type", "ctmrinfo");

					JSONObject engagementStatus = new JSONObject();

					engagementStatus.put("customerId", "16161616");

					engagement.put("info", engagementStatus);

					engagementAttributes.put(engagement);

				} catch (Exception e) {
					Log.e(TAG, e.getMessage());
				}

				MonitoringParams monitoringParms = new MonitoringParams("", entryPoints, engagementAttributes);

// 				LivepersonMonitoring.sendSde(MessagingActivity.this, identities, monitoringParms, new SdeCallback() {
//					@Override
//					public void onSuccess(@NotNull LPSdeResponse lpSdeResponse) {
//						Log.d(TAG, lpSdeResponse.toString());
//					}
//
//					@Override
//					public void onError(@NotNull MonitoringErrorType monitoringErrorType, @org.jetbrains.annotations.Nullable Exception e) {
//						Log.e(TAG, monitoringErrorType.toString());
//					}
//				});

				Log.i(TAG, " Before LivepersonMonitoring.getEngagement");
				LivepersonMonitoring.getEngagement(MessagingActivity.this, identities, monitoringParms, new EngagementCallback() {

					@Override
					public void onSuccess(@NotNull LPEngagementResponse lpEngagementResponse) {

						Log.i(TAG, "Engagement onSuccess");
						List<EngagementDetails> engagementList = lpEngagementResponse.getEngagementDetailsList();


						if (engagementList != null && !engagementList.isEmpty()) {

							EngagementDetails engagementDetails = engagementList.get(0);

							Log.i(TAG, "-------------------------------------------------");
							Log.i(TAG, engagementDetails.getCampaignId());
							Log.i(TAG, engagementDetails.getConnectorId());
							Log.i(TAG, engagementDetails.getContextId());
							Log.i(TAG, engagementDetails.getConversationId());
							Log.i(TAG, engagementDetails.getEngagementId());
							Log.i(TAG, engagementDetails.getStatus());
							Log.i(TAG, "-------------------------------------------------");

							currentCampaignId = engagementDetails.getCampaignId();
							currentEngagementId = engagementDetails.getEngagementId();
							currentEngagementContextId = engagementDetails.getContextId();
							currentSessionId = lpEngagementResponse.getSessionId();
							currentVisitorId = lpEngagementResponse.getVisitorId();
						}

						runOnUiThread(() -> {
							openActivity();
							SampleAppUtils.enableButtonAndChangeText(mOpenConversationButton, getString(R.string.open_activity));
						});
					}

					@Override
					public void onError(@NotNull MonitoringErrorType monitoringErrorType, @org.jetbrains.annotations.Nullable Exception e) {
						Log.d(TAG, monitoringErrorType.toString());
					}
				});

			}

			@Override
			public void onInitFailed(Exception e) {
				runOnUiThread(() -> {
					SampleAppUtils.enableButtonAndChangeText(mOpenConversationButton, getString(R.string.open_activity));
					Toast.makeText(MessagingActivity.this, "Init Failed", Toast.LENGTH_SHORT).show();
				});
			}
		}));
	}


	/**
	 * Start {@link FragmentContainerActivity} that handles the SDK the Messaging SDK and start the SDK in "Fragment Mode"
	 */
	private void openFragmentContainer() {
		storeData();
		Intent in = new Intent(MessagingActivity.this, FragmentContainerActivity.class);
		in.putExtra(FragmentContainerActivity.KEY_READ_ONLY, isReadOnly());
		in.putExtra(NotificationUI.NOTIFICATION_EXTRA, isFromPush);
		in.putExtra(FragmentContainerActivity.KEY_AUTH_TYPE, getAuthType().getStorageVal());
		startActivity(in);
		isFromPush = false;
	}

	/**
	 * Calling to "showConversation" API
	 */
	private void openActivity()  {

//		storeData();

//		if (isFromPush) {
//			LivePerson.setPushNotificationTapped();
//			isFromPush = false;
//		}

		CampaignInfo campaignInfo = null;

		try {
			if (currentCampaignId != null && currentCampaignId.length() > 0 &&
					currentEngagementId != null && currentEngagementId.length() > 0)
			campaignInfo = new CampaignInfo(Long.valueOf(currentCampaignId), Long.valueOf(currentEngagementId),
					currentEngagementContextId, currentVisitorId, currentVisitorId);
		} catch (BadArgumentException e) {
//			Log.e(TAG, e.getMessage());
		}


		ConversationViewParams params = new ConversationViewParams(isReadOnly())
				.setHistoryConversationsStateToDisplay(LPConversationsHistoryStateToDisplay.ALL)
				.setCampaignInfo(campaignInfo)
				.setReadOnlyMode(isReadOnly());

//        setWelcomeMessage(params);  //This method sets the welcome message with quick replies. Uncomment this line to enable this feature.

		LivePerson.showConversation(MessagingActivity.this, getLPAuthenticationParams(), params);

		ConsumerProfile consumerProfile = new ConsumerProfile.Builder()
				.setFirstName(mFirstNameView.getText().toString())
				.setLastName(mLastNameView.getText().toString())
				.setPhoneNumber(mPhoneNumberView.getText().toString())
				.build();
		LivePerson.setUserProfile(consumerProfile);

		//Constructing the notification builder for the upload/download foreground service and passing it to the SDK.
//		Notification.Builder uploadBuilder = NotificationUI.createUploadNotificationBuilder(getApplicationContext());
//		Notification.Builder downloadBuilder = NotificationUI.createDownloadNotificationBuilder(getApplicationContext());
//		LivePerson.setImageServiceUploadNotificationBuilder(uploadBuilder);
//		LivePerson.setImageServiceDownloadNotificationBuilder(downloadBuilder);
	}

	@SuppressWarnings("unused")
	private void setWelcomeMessage(ConversationViewParams params) {
		LPWelcomeMessage lpWelcomeMessage = new LPWelcomeMessage("Welcome Message");
		List<MessageOption> optionItems = new ArrayList<>();
		optionItems.add(new MessageOption("bill", "bill"));
		optionItems.add(new MessageOption("sales", "sales"));
		optionItems.add(new MessageOption("support", "support"));
		try {
			lpWelcomeMessage.setMessageOptions(optionItems);
		} catch (Exception e) {
			e.printStackTrace();
		}
		lpWelcomeMessage.setNumberOfItemsPerRow(8);
		lpWelcomeMessage.setMessageFrequency(LPWelcomeMessage.MessageFrequency.EVERY_CONVERSATION);
		params.setLpWelcomeMessage(lpWelcomeMessage);
	}

	private boolean isReadOnly() {
		return mReadOnlyModeCheckBox.isChecked();
	}

	/**
	 * If we initiated from a push message we show the screen that was in use the previous session (fragment/activity)
	 * Activity mode is the default
	 */
	private void handlePush(Intent intent) {
		isFromPush = intent.getBooleanExtra(NotificationUI.NOTIFICATION_EXTRA, false);

		//Check if we came from Push Notification
		if (isFromPush) {
			clearPushNotifications();
			switch (SampleAppStorage.getInstance(this).getSDKMode()) {
				//Initialize the SDK with "Activity mode"
				case ACTIVITY:
					initActivityConversation();
					break;
				//Initialize the SDK with "Fragment mode"
				case FRAGMENT:
					openFragmentContainer();
					break;
			}
		}
	}

	/**
	 * Hide any shown notification
	 */
	private void clearPushNotifications() {
		((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NotificationUI.PUSH_NOTIFICATION_ID);
	}

	protected void createLocale(String language, @Nullable String country) {
		Resources resources = getBaseContext().getResources();
		Configuration configuration = resources.getConfiguration();
		Locale customLocale;

		if (TextUtils.isEmpty(language)) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				language = resources.getConfiguration().getLocales().get(0).getCountry();
			} else {
				language = resources.getConfiguration().locale.getCountry();
			}
		}

		if (TextUtils.isEmpty(country)) {
			customLocale = new Locale(language);
		} else {
			customLocale = new Locale(language, country);
		}
		Locale.setDefault(customLocale);

		configuration.setLocale(customLocale);
		resources.updateConfiguration(configuration, resources.getDisplayMetrics());

		Locale locale = getLocale();
		Log.d(TAG, "country = " + locale.getCountry() + ", language = " + locale.getLanguage());
	}

	private Locale getLocale() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			return getBaseContext().getResources().getConfiguration().getLocales().get(0);
		} else {
			return getBaseContext().getResources().getConfiguration().locale;
		}
	}

	IntentFilter unreadMessagesCounterFilter = new IntentFilter(LivePerson.ACTION_LP_UPDATE_NUM_UNREAD_MESSAGES_ACTION);
	BroadcastReceiver unreadMessagesCounter = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int newValue = intent.getIntExtra(LivePerson.ACTION_LP_UPDATE_NUM_UNREAD_MESSAGES_EXTRA, 0);
			Log.d(TAG, "Got new value for unread messages counter: " + newValue);
			updateToolBar(newValue);
		}
	};

	private void updateToolBar(final int newValue) {
		runOnUiThread(() -> {
			if (newValue > 0) {
				setTitle(getResources().getString(R.string.messaging_title) + " (" + newValue + ") ");
			} else {
				setTitle(R.string.messaging_title);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		LPAuthenticationParams lpAuthenticationParams = getLPAuthenticationParams();
		if (lpAuthenticationParams.getAuthType() == LPAuthenticationType.AUTH && TextUtils.isEmpty(lpAuthenticationParams.getAuthKey())) {
			lpAuthenticationParams = null;
		}
		LivePerson.getUnreadMessagesCount(SampleAppStorage.getInstance(MessagingActivity.this).getAccount(), lpAuthenticationParams, new ICallback<Integer, Exception>() {
			@Override
			public void onSuccess(Integer count) {
				updateToolBar(count);
			}

			@Override
			public void onError(Exception e) {
				Log.e(TAG, "Failed to get unread messages count");
			}
		});
		registerReceiver(unreadMessagesCounter, unreadMessagesCounterFilter);
	}

	@Override
	protected void onPause() {
		unregisterReceiver(unreadMessagesCounter);
		super.onPause();
	}

	private LPAuthenticationParams getLPAuthenticationParams() {
		LPAuthenticationParams authParams = new LPAuthenticationParams(getAuthType());
		String authCode = mAuthCodeView.getText().toString().trim();
		String publicKey = mPublicKey.getText().toString().trim();
		authParams.setAuthKey(authCode);
		authParams.addCertificatePinningKey(publicKey);
		return authParams;
	}

	/**
	 * LPAuthenticationType.SIGN_UP is deprecated since July 2019.
	 *
	 * @return The auth type.
	 */
	private LPAuthenticationType getAuthType() {
		LPAuthenticationType authType;
		switch (authTypeSpinner.getSelectedItemPosition()) {
			case 0:
				authType = LPAuthenticationType.AUTH;
				break;
			case 1:
				authType = LPAuthenticationType.UN_AUTH;
				break;
			case 2:
			default:
				authType = LPAuthenticationType.SIGN_UP;
				break;
		}

		return authType;
	}

	private void storeData() {

		// Store CampaignId if available
		if (!TextUtils.isEmpty(mCampaignIdEditText.getText().toString())) {
			SampleAppStorage.getInstance(this).setCampaignId(Long.valueOf(mCampaignIdEditText.getText().toString()));
		} else {
			SampleAppStorage.getInstance(this).setCampaignId(null);
		}

		// Store EngagementId if available
		if (!TextUtils.isEmpty(mEngagementIdEditText.getText().toString())) {
			SampleAppStorage.getInstance(this).setEngagementId(Long.valueOf(mEngagementIdEditText.getText().toString()));
		} else {
			SampleAppStorage.getInstance(this).setEngagementId(null);
		}

		// Store SessionId if available
		if (!TextUtils.isEmpty(mSessionIdEditText.getText().toString())) {
			SampleAppStorage.getInstance(this).setSessionId(mSessionIdEditText.getText().toString());
		} else {
			SampleAppStorage.getInstance(this).setSessionId(null);
		}

		// Store VisitorId if available
		if (!TextUtils.isEmpty(mVisitorIdEditText.getText().toString())) {
			SampleAppStorage.getInstance(this).setVisitorId(mVisitorIdEditText.getText().toString());
		} else {
			SampleAppStorage.getInstance(this).setVisitorId(null);
		}

		// Store EngagementContextId if available
		if (!TextUtils.isEmpty(mEngagementContextIdEditText.getText().toString())) {
			SampleAppStorage.getInstance(this).setInteractionContextId(mEngagementContextIdEditText.getText().toString());
		} else {
			SampleAppStorage.getInstance(this).setInteractionContextId(null);
		}

	}
}
