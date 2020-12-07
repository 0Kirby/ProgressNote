package cn.zerokirby.note.util

import androidx.annotation.StringRes
import ren.imyan.base.ActivityCollector

/**
 * @author EndureBlaze/炎忍 https://github.com.EndureBlaze
 * @data 2020-12-07 18:31
 * @website https://imyan.ren
 */

/**
 * 获取当前设置语言的字符串
 * @param id 要获取的字符串在 string.xml 的 id
 * @return 获取好的字符串 String 型
 */
fun getLocalString(@StringRes id: Int): String =
        ActivityCollector.currActivity().resources.getString(id)