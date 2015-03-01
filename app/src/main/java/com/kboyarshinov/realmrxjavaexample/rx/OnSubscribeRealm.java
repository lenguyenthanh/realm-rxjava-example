package com.kboyarshinov.realmrxjavaexample.rx;

import android.content.Context;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.exceptions.RealmException;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public abstract class OnSubscribeRealm<T extends RealmObject> implements Observable.OnSubscribe<T> {
    private Context context;
    private String fileName;

    public OnSubscribeRealm(Context context) {
        this.context = context;
        fileName = null;
    }

    public OnSubscribeRealm(Context context, String fileName) {
        this.context = context;
        this.fileName = fileName;
    }

    @Override
    public void call(final Subscriber<? super T> subscriber) {
        final Realm realm = fileName != null ? Realm.getInstance(context, fileName) : Realm.getInstance(context);

        T object = null;
        realm.beginTransaction();
        try {
            object = get(realm);
            realm.commitTransaction();
        } catch (RuntimeException e) {
            realm.cancelTransaction();
            subscriber.onError(new RealmException("Error during transaction.", e));
            subscriber.onCompleted();
        } catch (Error e) {
            realm.cancelTransaction();
            subscriber.onError(e);
            subscriber.onCompleted();
        }
        if (object != null)
            subscriber.onNext(object);
        subscriber.onCompleted();

        subscriber.add(Subscriptions.create(new Action0() {
            @Override
            public void call() {
                try {
                    realm.close();
                } catch (RealmException ex) {
                    subscriber.onError(ex);
                }
            }
        }));
    }

    public abstract T get(Realm realm);
}