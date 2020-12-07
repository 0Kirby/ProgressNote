package ren.imyan.language

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import androidx.annotation.RequiresApi
import java.util.*

/**
 * @author EndureBlaze/炎忍 https://github.com.EndureBlaze
 * @data 2020-11-29 16:26
 * @website https://imyan.ren
 */
open class ContextWrapper(base: Context?) : ContextWrapper(base) {
    companion object {
        //这里使用注解保证编译通过
        @RequiresApi(Build.VERSION_CODES.N)
        @JvmStatic
        fun wrap(context: Context, newLocale: Locale?): ContextWrapper {
            var mContext = context
            val res: Resources = mContext.resources
            val configuration: Configuration = res.configuration
            //注意 Android 7.0 前后的不同处理方法
            mContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                configuration.setLocale(newLocale)
                val localeList = LocaleList(newLocale)
                LocaleList.setDefault(localeList)
                configuration.setLocales(localeList)
                mContext.createConfigurationContext(configuration)
            } else {
                configuration.setLocale(newLocale)
                mContext.createConfigurationContext(configuration)
            }
            return ContextWrapper(mContext)
        }
    }
}