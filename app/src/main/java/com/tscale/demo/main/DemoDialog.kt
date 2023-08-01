package com.tscale.demo.main

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.tscale.demo.R
import com.blankj.utilcode.util.ScreenUtils


import com.yoyo.ui.view.dialog.BaseDialogFragment

class DemoDialog : DialogFragment() {
    private var type = 0
    private var clPoint: ConstraintLayout? = null
    private var clShopInfo: LinearLayoutCompat? = null
    private var okBtn: AppCompatButton? = null
    private var edtName: AppCompatEditText? = null
    private var edtPerson: AppCompatEditText? = null
    private var edtPhone: AppCompatEditText? = null
    private var edtLtX: AppCompatEditText? = null
    private var edtLtY: AppCompatEditText? = null
    private var edtRtX: AppCompatEditText? = null
    private var edtRtY: AppCompatEditText? = null
    private var edtRbX: AppCompatEditText? = null
    private var edtRbY: AppCompatEditText? = null
    private var edtLbX: AppCompatEditText? = null
    private var edtLbY: AppCompatEditText? = null
    private var root: View? = null


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.let {
            val attributes: WindowManager.LayoutParams = it.attributes
            attributes.width = ScreenUtils.getScreenWidth() / 2
            attributes.height = ScreenUtils.getScreenHeight() / 2
            attributes.gravity = Gravity.CENTER
            it.attributes = attributes
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.dialog_demo_camera_point_view, container, false)
        root?.let {
            clPoint = it.findViewById(R.id.cl_point)
            clShopInfo = it.findViewById(R.id.cl_shop_info)
            edtName = it.findViewById(R.id.edt_name)
            edtPerson = it.findViewById(R.id.edt_person)
            edtPhone = it.findViewById(R.id.edt_phone)
            edtLtX = it.findViewById(R.id.edt_lt_x)
            edtLtY = it.findViewById(R.id.edt_lt_y)
            edtRtX = it.findViewById(R.id.edt_rt_x)
            edtRtY = it.findViewById(R.id.edt_rt_y)
            edtRbX = it.findViewById(R.id.edt_rb_x)
            edtRbY = it.findViewById(R.id.edt_rb_y)
            edtLbX = it.findViewById(R.id.edt_lb_x)
            edtLbY = it.findViewById(R.id.edt_lb_y)
            okBtn = it.findViewById(R.id.btn_ok)
            okBtn?.setOnClickListener {
                val map = mutableMapOf<String, Any>()
                if (type == 0) {
                    map.clear()
                    map["name"] = edtName?.text.toString()
                    map["person"] = edtPerson?.text.toString()
                    map["phone"] = edtPhone?.text.toString()
                    action.invoke(map)
                    dismissAllowingStateLoss()
                } else {
                    map.clear()
                    map["ltx"] = edtLtX?.text.toString()
                    map["lty"] = edtLtY?.text.toString()
                    map["rtx"] = edtRtX?.text.toString()
                    map["rty"] = edtRtY?.text.toString()
                    map["rbx"] = edtRbX?.text.toString()
                    map["rby"] = edtRbY?.text.toString()
                    map["lbx"] = edtLbX?.text.toString()
                    map["lby"] = edtLbY?.text.toString()
                    action.invoke(map)
                    dismissAllowingStateLoss()
                }
            }
        }
        val bundle = arguments
        bundle?.apply {
            type = bundle.getInt("type")
            when (type) {
                0 -> {
                    clPoint?.visibility = View.GONE
                    clShopInfo?.visibility = View.VISIBLE
                }
                else -> {
                    clPoint?.visibility = View.VISIBLE
                    clShopInfo?.visibility = View.GONE
                }
            }
        }
        return root
    }


    fun setActionListener(action: (map: MutableMap<String, Any>) -> Unit) {
        this.action = action
    }

    private lateinit var action: (map: MutableMap<String, Any>) -> Unit
}