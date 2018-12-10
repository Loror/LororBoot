package com.loror.lororboot.views;

import com.loror.lororboot.bind.FieldControl;
import com.loror.lororboot.bind.Value;

public interface BindRefreshAble {
    void refresh(Value value);

    void find(FieldControl control);
}
