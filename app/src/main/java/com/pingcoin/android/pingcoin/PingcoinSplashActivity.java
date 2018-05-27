package com.pingcoin.android.pingcoin;

/**
 * Created by jmscdch on 16/05/18.
 */

public class PingcoinSplashActivity extends SplashPermissionActivity {
    @Override
    public Class getNextActivityClass() {
        return SelectCoin.class;
    }
}