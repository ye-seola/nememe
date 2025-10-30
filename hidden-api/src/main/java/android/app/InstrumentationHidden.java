package android.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import dev.rikka.tools.refine.RefineAs;

@RefineAs(Instrumentation.class)
public class InstrumentationHidden {
    public Instrumentation.ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {
        throw new RuntimeException("STUB");
    }
}
