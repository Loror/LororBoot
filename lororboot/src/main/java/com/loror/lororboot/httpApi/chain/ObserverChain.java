package com.loror.lororboot.httpApi.chain;

import com.loror.lororboot.httpApi.Observable;

/**
 * Date: 2020/6/4 8:58
 * Description: Create By Loror
 */
public class ObserverChain {

    private ObserverChain.Builder builder;

    public interface OnErrorCollection {
        void onError(Throwable throwable);
    }

    public interface OnComplete {
        void onComplete();
    }

    public static class Builder {
        private OnErrorCollection onErrorCollection;
        private OnComplete onComplete;

        public Builder setOnErrorCollection(OnErrorCollection onErrorCollection) {
            this.onErrorCollection = onErrorCollection;
            return this;
        }

        public Builder setOnComplete(OnComplete onComplete) {
            this.onComplete = onComplete;
            return this;
        }

        protected OnComplete getOnComplete() {
            return onComplete;
        }

        protected OnErrorCollection getOnErrorCollection() {
            return onErrorCollection;
        }

        public ObserverChain build() {
            ObserverChain chain = new ObserverChain();
            chain.builder = this;
            return chain;
        }
    }

    public <T> ChainNode<T> begin(Observable<T> observable) {
        return new ChainNode<T>(builder, observable);
    }

}
