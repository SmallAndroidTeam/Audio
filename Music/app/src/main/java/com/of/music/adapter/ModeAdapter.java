package com.of.music.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.of.music.R;
import com.of.music.defineViewd.AutoLocateHorizontalView;

import java.util.List;


public class ModeAdapter extends RecyclerView.Adapter<ModeAdapter.ModeViewHolder> implements AutoLocateHorizontalView.IAutoLocateHorizontalView {
    private Context context;
    private View view;
    private List<String> modes;
    public ModeAdapter(Context context, List<String> modes){
        this.context = context;
        this.modes = modes;
    }
    /*
     *
     * 这个方法是用来创建viewHolder的，就是引入xml传送到viewHolder
     *
     * */
    @Override
    public ModeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(context).inflate(R.layout.item_mode,parent,false);
        return new ModeViewHolder(view);
    }
    /*
     *
     * 这里，是我们操作item的地方
     *
     * */
    @Override
    public void onBindViewHolder(ModeViewHolder holder, int position) {
        holder.tvMode.setText(modes.get(position));
    }
    //返回item的size（数量）
    @Override
    public int getItemCount() {
        return  modes.size();
    }
    //返回item的view（视图）
    @Override
    public View getItemView() {
        return view;
    }
    //如果view被选中，则当前textview变大，否则保持为原样
    @Override
    public void onViewSelected(boolean isSelected, int pos, RecyclerView.ViewHolder holder, int itemWidth) {
        if(isSelected) {
            ((ModeViewHolder) holder).tvMode.setTextSize(20);
        }else{
            ((ModeViewHolder) holder).tvMode.setTextSize(14);
        }
    }
    //定义ModeViewHolder
    static class ModeViewHolder extends RecyclerView.ViewHolder{
        TextView tvMode;
        ModeViewHolder(View itemView) {
            super(itemView);
            tvMode = (TextView)itemView.findViewById(R.id.tv_mode);
        }
    }
}

