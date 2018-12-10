package com.loror.demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.loror.lororUtil.image.GifDecoder;
import com.loror.lororUtil.image.ImageUtil;
import com.loror.lororUtil.image.ReadImage;
import com.loror.lororUtil.image.ReadImageResult;
import com.loror.lororboot.bind.FieldControl;
import com.loror.lororboot.bind.Value;
import com.loror.lororboot.views.BindRefreshAble;

import java.io.InputStream;

// 如需实现支持bind的控件实现BindRefreshAble接口即可，此为demo
public class BindAbleGifView extends android.support.v7.widget.AppCompatImageView implements BindRefreshAble {
    private Context context;

    public BindAbleGifView(Context context) {
        this(context, null);
    }

    public BindAbleGifView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    // bind的value发生改变时回调
    @Override
    public void refresh(Value value) {
        Object o = value.value;
        if (o == null) {
            setImageResource(R.mipmap.ic_launcher);
        } else if (o instanceof Integer) {
            final int mipmap = (int) o;
            ImageUtil.with(context).setIsGif(true).from("gif:" + mipmap).to(this)
                    .setReadImage(new ReadImage() {
                        @Override
                        public ReadImageResult readImage(String path, int width, boolean gif) {
                            ReadImageResult result = new ReadImageResult();
                            @SuppressLint("ResourceType") InputStream resource = getResources().openRawResource(mipmap);
                            GifDecoder decoder = new GifDecoder(resource);
                            decoder.setWidthLimit(width);
                            decoder.decode();
                            if (decoder.getStatus() == GifDecoder.STATUS_FINISH) {
                                for (int i = 0; i < decoder.getFrameCount(); ++i) {
                                    result.addFrame(decoder.getFrame(i));
                                }
                            } else {
                                result.setErrorCode(1);
                            }
                            return result;
                        }
                    }).loadImage();
        } else if (o instanceof String) {
            String path = (String) o;
            ImageUtil.with(context).from(path).to(this).setIsGif(true).loadImage();
        }
    }

    //bind控件被找到时回调，如需实现双向绑定，通过传入的FieldControl对象实现
    @Override
    public void find(FieldControl control) {

    }
}
