package com.phonar;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

import com.phonar.utils.CommonUtils;

@ReportsCrashes(formKey = "dE9uNkhPMEJ5czZBYUtLdzRiN0duMGc6MQ")
public class PhonarApplication extends Application {

    @Override
    public void onCreate() {
        if (CommonUtils.PRODUCTION_MODE) {
            // The following line triggers the initialization of ACRA
            ACRA.init(this);
        }
        super.onCreate();
    }

}
