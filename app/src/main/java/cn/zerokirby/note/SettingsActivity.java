package cn.zerokirby.note;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Objects;

import cn.zerokirby.note.util.AppUtil;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            findPreference("version").setSummary(String.format("%s (Build %d) (%s)", AppUtil.getVersionName(getActivity()), AppUtil.getVersionCode(getActivity()), AppUtil.getPackageName(getActivity())));
        }
    }
}