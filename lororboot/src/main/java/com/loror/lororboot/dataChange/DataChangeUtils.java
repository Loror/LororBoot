package com.loror.lororboot.dataChange;

import android.support.annotation.IdRes;

import com.loror.lororboot.bind.BindHolder;
import com.loror.lororboot.bind.BindUtils;
import com.loror.lororboot.bind.DataChangeAble;

import java.util.List;

public class DataChangeUtils {

    /**
     * 设置bind数据
     */
    public static void setData(int id, Object value, Object tag, List<BindHolder> bindHolders, DataChangeAble changeAble) {
        BindHolder holder = BindUtils.findHolderById(bindHolders, id);
        if (holder != null) {
            holder.getField().setAccessible(true);
            try {
                holder.getField().set(changeAble, value);
                if (tag == null || tag.equals(holder.getTag())) {
                    changeAble.changeState(null);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 通知list刷新
     */
    public static void notifyListDataChangeById(@IdRes int id, Object tag, List<BindHolder> bindHolders, DataChangeAble changeAble) {
        BindHolder bindHolder = BindUtils.findHolderById(bindHolders, id);
        if (bindHolder != null) {
            bindHolder.resetCompareTag();
            if (tag == null || tag.equals(bindHolder.getTag())) {
                changeAble.changeState(null);
            }
        }
    }
}
