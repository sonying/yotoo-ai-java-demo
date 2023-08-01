package com.tscale.demo.main;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.ArrayList;
import java.util.List;

import com.tscale.demo.R;
import cn.smart.yoyolib.core.aidl.YoYoItemInfo;
import cn.smart.yoyolib.utils.PathConstant;
import cn.smart.yoyolib.utils.YoyoFileUtils;

import com.tscale.tslog.TSLog;

public class ResultItemAdapter extends RecyclerView.Adapter<ResultItemAdapter.ResultHolder> {

    private List<YoYoItemInfo> list = new ArrayList<>();
    private Context context;

    public ResultItemAdapter(Context context, List<YoYoItemInfo> list) {
        if (list != null) {
            this.list.addAll(list);
        }
        this.context = context;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ResultHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
//        View view =;
        TSLog.console(TSLog.i, "onCreateViewHolder");
        return new ResultHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_shop_result, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ResultHolder resultHolder, int i) {

        TSLog.console(TSLog.i, "onBindViewHolder");
        if (list.isEmpty() || list.size() <= i) {
            return;
        }
        resultHolder.ivNum.setVisibility(View.GONE);

        YoYoItemInfo info = list.get(i);

        if (!TextUtils.isEmpty(info.tupian)) {
            if (YoyoFileUtils.checkImageExist(info.tupian)) {
                Glide.with(context).load(PathConstant.INSTANCE.getPathShortCuts() + info.getTupian()).transition(DrawableTransitionOptions.withCrossFade()).into(resultHolder.iv_image);
            } else {
                Glide.with(context).load(R.mipmap.yoyo11).into(resultHolder.iv_image);
            }
        } else {
           Glide.with(context).load(R.mipmap.yoyo11).into(resultHolder.iv_image);
        }

        if (info != null) {
            if (!TextUtils.isEmpty(info.itemName)) {
                resultHolder.name.setText(info.itemName);
            }
            if (info.unitType.getCode() == 0) {
                resultHolder.price.setText("单价    " + priceFormat(info.unitPrice) + "元/kg");
            } else {
                resultHolder.price.setText("单价    " + priceFormat(info.unitPrice) + "元/个");
            }
            resultHolder.plu.setText("编号  " + info.plu);
        }
        resultHolder.root.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEventItemClick(info);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateData(List<YoYoItemInfo> list) {
        this.list.clear();
        if (list != null) {
            this.list.addAll(list);
        }
        notifyDataSetChanged();
    }

    public void clean() {
        list.clear();
        notifyDataSetChanged();
    }

    class ResultHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView price;
        TextView plu;
        View root;
        ImageView ivNum;
        ImageView iv_image;

        public ResultHolder(@NonNull View itemView) {
            super(itemView);
            iv_image = itemView.findViewById(R.id.iv_image);
            root = itemView.findViewById(R.id.root);
            name = itemView.findViewById(R.id.name);
            price = itemView.findViewById(R.id.price);
            plu = itemView.findViewById(R.id.plu);
            ivNum = itemView.findViewById(R.id.ivNum);
        }
    }

    public String priceFormat(int price) {
        int p = price / 100;
        int p2 = price % 100;
        return p + "." + p2;
    }

    public interface ActionItemListener {
        void onEventItemClick(YoYoItemInfo info);
    }

    private ActionItemListener listener;

    public void setListener(ActionItemListener listener) {
        this.listener = listener;
    }
}
