package com.example.helloworld;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.liveperson.api.LivePersonCallback;
import com.liveperson.infra.ConversationViewParams;
import com.liveperson.infra.InitLivePersonProperties;
import com.liveperson.infra.MonitoringInitParams;
import com.liveperson.infra.auth.LPAuthenticationParams;
import com.liveperson.infra.auth.LPAuthenticationType;
import com.liveperson.infra.callbacks.InitLivePersonCallBack;
import com.liveperson.messaging.TaskType;
import com.liveperson.messaging.model.AgentData;
import com.liveperson.messaging.sdk.api.LivePerson;


public class MainActivity extends AppCompatActivity {

    private static final String APP_ID = "com.example.helloworld";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
    }

    public void startUnauthFlow(View v) {
        String brandID = "73628819";
        String appInstallID = "1bf3b045-2bce-4c2f-a51b-2aab6142e526";

        final MonitoringInitParams monitoringInitParams = new MonitoringInitParams(appInstallID);
        LivePerson.initialize(MainActivity.this, new InitLivePersonProperties(brandID, APP_ID, monitoringInitParams, new InitLivePersonCallBack() {
            @Override
            public void onInitSucceed() {
                LPAuthenticationParams lpAuthenticationParams = new LPAuthenticationParams(LPAuthenticationType.UN_AUTH);

                LivePerson.showConversation(MainActivity.this, lpAuthenticationParams, new ConversationViewParams());
            }

            @Override
            public void onInitFailed(Exception e) {
                Toast.makeText(MainActivity.this, "Init Failed", Toast.LENGTH_SHORT).show();
            }
        }));
    }
}