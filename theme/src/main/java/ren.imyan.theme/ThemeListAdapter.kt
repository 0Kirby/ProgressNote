package ren.imyan.theme

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import ren.imyan.theme.databinding.ThemeListDialogLayoutBinding
import kotlin.properties.Delegates

var checkItem by Delegates.notNull<Int>()

class ThemeListAdapter constructor(private val context: Context, private val themeList: List<Int>) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var mConvertView: View? = convertView
        val holder: Holder
        if (mConvertView == null) {
            mConvertView = LayoutInflater.from(context).inflate(R.layout.theme_list_dialog_layout, null)
            holder = Holder(mConvertView)
            mConvertView.tag = holder
        } else {
            holder = mConvertView.tag as Holder
        }
        holder.imageView1.setImageResource(themeList[position])
        if (checkItem == position) {
            holder.imageView2.setImageResource(R.drawable.ic_done)
        }
        return mConvertView
    }

    override fun getItem(position: Int) = position


    override fun getItemId(position: Int) = themeList[position].toLong()

    override fun getCount() = themeList.size

    fun setCheckItem(checkItemPos: Int) {
        checkItem = checkItemPos
    }

    class Holder(convertView: View) {
        private val themeListDialogBinding = ThemeListDialogLayoutBinding.bind(convertView)
        var imageView1: ImageView = themeListDialogBinding.img1
        var imageView2: ImageView = themeListDialogBinding.img2
    }
}