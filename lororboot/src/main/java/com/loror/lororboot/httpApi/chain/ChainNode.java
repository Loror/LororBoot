package com.loror.lororboot.httpApi.chain;

import com.loror.lororboot.httpApi.Observable;
import com.loror.lororboot.httpApi.Observer;

/**
 * Date: 2020/6/4 8:59
 * Description: Create By Loror
 */
public class ChainNode<T> {

    private ObserverChain.Builder builder;
    private Observable<T> observable;
    private OnResult<T> onResult;
    private ChainNode next, previous;

    public interface OnResult<T> {
        boolean result(T t);
    }

    public ChainNode(ObserverChain.Builder builder, Observable<T> observable) {
        this.builder = builder;
        this.observable = observable;
    }

    public ChainNode<T> onResult(OnResult<T> onResult) {
        this.onResult = onResult;
        return this;
    }

    public <T> ChainNode<T> next(Observable<T> observable) {
        next = new ChainNode<T>(builder, observable);
        next.previous = this;
        return next;
    }

    /**
     * 开始运行链式接口
     */
    public void load() {
        ChainNode<?> first = this;
        while (first.previous != null) {
            first = first.previous;
        }
        first.start();
    }

    private void start() {
        if (observable != null) {
            observable.subscribe(new Observer<T>() {
                @Override
                public void success(T data) {
                    runNext(data);
                }

                @Override
                public void failed(int code, Throwable e) {
                    if (builder != null && builder.getOnErrorCollection() != null) {
                        builder.getOnErrorCollection().onError(e);
                    }
                }
            });
        } else {
            runNext(null);
        }
    }

    private void runNext(T data) {
        boolean runNext = onResult == null || onResult.result(data);
        if (next != null) {
            if (runNext) {
                next.start();
                return;
            }
        }
        if (builder != null && builder.getOnComplete() != null) {
            builder.getOnComplete().onComplete();
        }
    }
}
