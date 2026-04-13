package com.uitcnpm.se114project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Objects;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Objects.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED)) {

            // TODO: load từ DB
            AlarmHelper.setAlarm(context, 8, 0, "Uống thuốc", 1);
        }
    }
}
