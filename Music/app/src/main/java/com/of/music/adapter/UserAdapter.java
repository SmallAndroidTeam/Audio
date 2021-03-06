package com.of.music.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import com.of.music.R;
import com.of.music.info.User;
import com.of.music.songListInformation.MusicIconLoader;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends BaseAdapter implements Filterable {

    private Context mContext;
    public static String key;
    /**
     * Contains the list of objects that represent the data of this Adapter.
     * Adapter数据源
     */
    private List<User> mDatas;

    private LayoutInflater mInflater;

    //过滤相关
    /**
     * This lock is also used by the filter
     * (see {@link #getFilter()} to make a synchronized copy of
     * the original array of data.
     * 过滤器上的锁可以同步复制原始数据。
     */
    private final Object mLock = new Object();

    // A copy of the original mObjects array, initialized from and then used instead as soon as
    // the mFilter ArrayFilter is used. mObjects will then only contain the filtered values.
    //对象数组的备份，当调用ArrayFilter的时候初始化和使用。此时，对象数组只包含已经过滤的数据。
    private ArrayList<User> mOriginalValues;
    private ArrayFilter mFilter;

    public UserAdapter(Context context, List<User> datas) {
        mContext = context;
        mDatas = datas;
        mInflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return mDatas.size() > 0 ? mDatas.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_user, parent, false);
            holder = new ViewHolder();
            holder.avatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
            holder.name = (TextView) convertView.findViewById(R.id.tv_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        User user = mDatas.get(position);
        Bitmap icon = MusicIconLoader.getInstance().load(user.getImage());

        holder.avatar.setImageBitmap(icon);
        holder.name.setText(user.getName());
        Log.i("music", "当前EditText的文本是"+key);
        String content = mDatas.get(position).getName().toString();
        if (!TextUtils.isEmpty(key) && content.contains(key)) {
            int index = content.indexOf(key);
            SpannableStringBuilder builder_qqId = new SpannableStringBuilder(content);
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.RED);
            builder_qqId.setSpan(colorSpan,  index, index + key.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.name.setText(builder_qqId);
            System.out.println("关键字成功");
        } else {
            holder.name.setText(content);
            System.out.println("关键字失败");
        }
        return convertView;
    }

    public class ViewHolder {
        ImageView avatar;
        TextView name;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }

    /**
     * 过滤数据的类
     */
    /**
     * <p>An array filter constrains the content of the array adapter with
     * a prefix. Each item that does not start with the supplied prefix
     * is removed from the list.</p>
     * <p/>
     * 一个带有首字母约束的数组过滤器，每一项不是以该首字母开头的都会被移除该list。
     */
    private class ArrayFilter extends Filter {
        //执行刷选
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();//过滤的结果
            //原始数据备份为空时，上锁，同步复制原始数据
            if (mOriginalValues == null) {
                synchronized (mLock) {
                    mOriginalValues = new ArrayList<>(mDatas);
                }
            }
            //当首字母为空时
            if (prefix == null || prefix.length() == 0) {
                ArrayList<User> list;
                synchronized (mLock) {//同步复制一个原始备份数据
                    list = new ArrayList<>(mOriginalValues);
                }
                results.values = list;
                results.count = list.size();//此时返回的results就是原始的数据，不进行过滤
            } else {
                String prefixString = prefix.toString().toLowerCase();//转化为小写

                ArrayList<User> values;
                synchronized (mLock) {//同步复制一个原始备份数据
                    values = new ArrayList<>(mOriginalValues);
                }
                final int count = values.size();
                final ArrayList<User> newValues = new ArrayList<>();

                for (int i = 0; i < count; i++) {
                    final User value = values.get(i);//从List<User>中拿到User对象
//                    final String valueText = value.toString().toLowerCase();
                    final String valueText = value.getName().toString().toLowerCase();//User对象的name属性作为过滤的参数
                    // First match against the whole, non-splitted value
                    if (valueText.startsWith(prefixString) || valueText.indexOf(prefixString.toString()) != -1) {//第一个字符是否匹配
                        newValues.add(value);//将这个item加入到数组对象中
                    } else {//处理首字符是空格
                        final String[] words = valueText.split(" ");
                        final int wordCount = words.length;

                        // Start at index 0, in case valueText starts with space(s)
                        for (int k = 0; k < wordCount; k++) {
                            if (words[k].startsWith(prefixString)) {//一旦找到匹配的就break，跳出for循环
                                newValues.add(value);
                                break;
                            }
                        }
                    }
                }
                results.values = newValues;//此时的results就是过滤后的List<User>数组
                results.count = newValues.size();
            }
            return results;
        }

        //刷选结果
        @Override
        protected void publishResults(CharSequence prefix, FilterResults results) {
            //noinspection unchecked
            mDatas = (List<User>) results.values;//此时，Adapter数据源就是过滤后的Results
            if (results.count > 0) {
                notifyDataSetChanged();//这个相当于从mDatas中删除了一些数据，只是数据的变化，故使用notifyDataSetChanged()
            } else {
                /**
                 * 数据容器变化 ----> notifyDataSetInValidated

                 容器中的数据变化  ---->  notifyDataSetChanged
                 */
                notifyDataSetInvalidated();//当results.count<=0时，此时数据源就是重新new出来的，说明原始的数据源已经失效了
            }
        }
    }
}
