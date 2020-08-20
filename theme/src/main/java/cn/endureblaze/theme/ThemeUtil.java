package cn.endureblaze.theme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.GridView;

import androidx.appcompat.app.AlertDialog;

import java.util.Arrays;
import java.util.List;

public class ThemeUtil {
    public final static int BLUE_THEME = 0;
    public final static int WHITE_THEME = 1;
    public final static int DARK_THEME = 2;
    public final static int YELLOW_THEME = 3;
    public final static int ORANGE_THEME = 4;
    public final static int RED_THEME = 5;
    public final static int PURPLE_THEME = 6;
    public final static int INDIGO_THEME = 7;
    public final static int TEAL_THEME = 8;
    public final static int GREEN_THEME = 9;
    public final static int BROWN_THEME = 10;
    public final static int BLUEGREY_THEME = 11;


    public final static String FILE_NAME = "theme";

    public static void showThemeDialog(final Activity activity, final Class<?> toClass) {
        SharedPreferences theme_id = activity.getSharedPreferences(ThemeUtil.FILE_NAME, 0);
        final int itemSelected = theme_id.getInt("themeId", 0);
        AlertDialog.Builder theme = new AlertDialog.Builder(activity);
        theme.setTitle("主题");
        Integer[] res = new Integer[]{
                R.drawable.theme_blue,
                R.drawable.theme_white,
                R.drawable.theme_dark,
                R.drawable.theme_yellow,
                R.drawable.theme_orange,
                R.drawable.theme_red,
                R.drawable.theme_purple,
                R.drawable.theme_indigo,
                R.drawable.theme_teal,
                R.drawable.theme_green,
                R.drawable.theme_brown,
                R.drawable.theme_bluegrey

        };
        List<Integer> list = Arrays.asList(res);
        ThemeListAdapter adapter = new ThemeListAdapter(activity, list);
        adapter.setCheckItem(itemSelected);
        GridView gridView = (GridView) LayoutInflater.from(activity).inflate(R.layout.dialog_sw_theme, null);
        gridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        gridView.setCacheColorHint(0);
        gridView.setAdapter(adapter);
        theme.setView(gridView);
        final AlertDialog dialog = theme.show();
        gridView.setOnItemClickListener(
                (parent, view, position, id) -> {
                    dialog.dismiss();
                    if (itemSelected != position) {
                        ThemeUtil.setThemeByName(activity, position);
                        Intent intent = new Intent(activity, toClass);
                        activity.startActivity(intent);
                        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);//假装没退出过...
                        activity.finish();
                    }
                }
        );
    }

    public static void setClassTheme(Context context) {
        Theme(context, getThemeId(context));
    }

    public static int getThemeId(Context context) {
        SharedPreferences theme = context.getSharedPreferences(FILE_NAME, 0);
        return theme.getInt("themeId", 0);
    }

    public static void setThemeByName(Context context, int i) {
        SharedPreferences theme = context.getSharedPreferences(FILE_NAME, 0);
        SharedPreferences.Editor edit = theme.edit();
        edit.putInt("themeId", i);
        edit.apply();
    }

    private static void Theme(Context context, int themeId) {
        switch (themeId) {
            case BLUE_THEME:
                context.setTheme(R.style.BlueAppTheme);
                break;
            case RED_THEME:
                context.setTheme(R.style.RedAppTheme);
                break;
            case PURPLE_THEME:
                context.setTheme(R.style.PurpleAppTheme);
                break;
            case INDIGO_THEME:
                context.setTheme(R.style.IndigoAppTheme);
                break;
            case TEAL_THEME:
                context.setTheme(R.style.TealAppTheme);
                break;
            case GREEN_THEME:
                context.setTheme(R.style.GreenAppTheme);
                break;
            case ORANGE_THEME:
                context.setTheme(R.style.OrangeAppTheme);
                break;
            case BROWN_THEME:
                context.setTheme(R.style.BrownAppTheme);
                break;
            case BLUEGREY_THEME:
                context.setTheme(R.style.BlueGreyAppTheme);
                break;
            case YELLOW_THEME:
                context.setTheme(R.style.YellowAppTheme);
                break;
            case WHITE_THEME:
                context.setTheme(R.style.WhiteAppTheme);
                break;
            case DARK_THEME:
                context.setTheme(R.style.DarkAppTheme);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + themeId);
        }
    }

    public static void sw2DarkTheme(Activity activity, final Class<?> toClass) {
        SharedPreferences sw2dark = activity.getSharedPreferences(FILE_NAME, 0);
        SharedPreferences.Editor sw2dark_edit = sw2dark.edit();
        sw2dark_edit.putInt("before_app_theme", getThemeId(activity));
        sw2dark_edit.apply();
        sw2dark_edit.commit();
        ThemeUtil.setThemeByName(activity, DARK_THEME);
        Intent intent = new Intent(activity, toClass);
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        activity.finish();
    }

    public static void sw2AppTheme(Activity activity, final Class<?> toClass) {
        SharedPreferences sw2app = activity.getSharedPreferences(FILE_NAME, 0);
        ThemeUtil.setThemeByName(activity, sw2app.getInt("before_app_theme", 0));
        Intent intent = new Intent(activity, toClass);
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        activity.finish();
    }

    public static int getColorPrimary(Context activity) {
        TypedValue typedValue = new TypedValue();
        activity.getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        return typedValue.data;
    }

    public static int getDarkColorPrimary(Context activity) {

        TypedValue typedValue = new TypedValue();
        activity.getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        return typedValue.data;
    }

    public static int getAttrColor(Context context, int id) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(id, typedValue, true);
        return typedValue.data;
    }

    private static boolean isDarkMode(Context context) {
        return getDarkModeStatus(context);
    }

    //检查当前系统是否已开启暗黑模式
    private static boolean getDarkModeStatus(Context context) {
        int mode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return mode == Configuration.UI_MODE_NIGHT_YES;
    }

}
