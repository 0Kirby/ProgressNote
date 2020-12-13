package ren.imyan.language

import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import java.util.*


/**
 * @author EndureBlaze/炎忍 https://github.com.EndureBlaze
 * @data 2020-11-29 16:28
 * @website https://imyan.ren
 */
object LanguageUtil {

    @JvmStatic
    fun showLanguageDialog(context: Context, title: String, callback: () -> Unit) {
        val lanList = arrayOf(
                "Auto", "简体中文", "日本語"
        )
        AlertDialog.Builder(context)
                .setTitle(title)
                .setSingleChoiceItems(
                        lanList,
                        getCheckedItem(context)
                ) { dialog, which ->
                    val lan = when (which) {
                        0 -> "auto"
                        1 -> "zh-rCN"
                        2 -> "ja-jp"
                        else -> "auto"
                    }
                    setLanguage(context, lan)
                    dialog.dismiss()
                    callback()
                }
                .create()
                .show()
    }

    /**
     * 设置语言的值
     * @param context 上下文
     * @param lan 需要设置的语言
     */
    @JvmStatic
    fun setLanguage(context: Context, lan: String) {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit {
            putString("langName", lan)
            this.commit()
        }
    }

    /**
     * 获取应用于选择语言对话框的 checkedItem
     */
    @JvmStatic
    fun getCheckedItem(context: Context): Int =
            when (context.getSharedPreferences("settings", Context.MODE_PRIVATE).getString("langName", "cn")) {
                "auto" -> 0
                "zh-rCN" -> 1
                "ja-jp" -> 2
                else -> 0
            }

    /**
     * 获取当前设置的 Locale
     */
    @JvmStatic
    fun getLocale(context: Context): Locale =
        when (context.getSharedPreferences("settings", Context.MODE_PRIVATE).getString("langName", "cn")) {
            "auto" -> getSysLocale()
            "zh-rCN" -> Locale("zh", "CN")
            "ja-jp" -> Locale.JAPANESE
            else -> getSysLocale()
        }

    /**
     * 获取当前系统的 Locale
     */
    @JvmStatic
    fun getSysLocale(): Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        LocaleList.getDefault()[0]
    } else {
        Locale.getDefault()
    }
}