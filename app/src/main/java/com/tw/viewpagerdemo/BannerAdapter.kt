package com.tw.viewpagerdemo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class BannerAdapter(
    private val context: Context?,
    imgList: MutableList<String>,
) : PagerAdapter(){

    lateinit var mListener: OnImgClickListener

    var TAG : String = this.javaClass.simpleName
    private var todosList = mutableListOf<String>()
    private var activity: Context? = null


    interface OnImgClickListener{

        fun onImgClick(examData: String, pos:Int)
    }

    init {
        todosList = imgList
        activity = context
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun getCount(): Int {
        return todosList.size ?:0
    }

    override fun instantiateItem(parent: ViewGroup, position: Int): Any {
        val data = todosList[position]
        val view = LayoutInflater.from(parent.context).inflate(R.layout.banner_row,parent,false)

        val imageview = view.findViewById<ImageView>(R.id.alert_image)

//        imageview.setImageResource(data)
        if (!data.isNullOrEmpty()){
            Glide.with(context!!)
                .load(data)
                .error(R.drawable.user_new)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fitCenter()
                .into(imageview)

        }


        // Add the view to the parent
        parent.addView(view)



        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View?)
    }

}