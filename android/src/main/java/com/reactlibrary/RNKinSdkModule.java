package com.reactlibrary;

import android.support.annotation.NonNull;
import android.widget.Toast;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.google.gson.Gson;

import org.json.JSONException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import kin.backupandrestore.BackupAndRestoreManager;
import kin.backupandrestore.BackupCallback;
import kin.backupandrestore.RestoreCallback;
import kin.backupandrestore.exception.BackupAndRestoreException;
import kin.sdk.Balance;
import kin.sdk.Environment;
import kin.sdk.KinAccount;
import kin.sdk.KinClient;
import kin.sdk.Transaction;
import kin.sdk.TransactionId;
import kin.sdk.WhitelistableTransaction;
import kin.sdk.exception.CreateAccountException;
import kin.sdk.exception.DeleteAccountException;
import kin.utils.Request;
import kin.utils.ResultCallback;
import okhttp3.Call;
import okhttp3.Response;

public class RNKinSdkModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    private KinClient kinClient;
    private Gson gson = new Gson();
    private BackupAndRestoreManager backupAndRestoreManager;
    private Constants constants = new Constants();

    public RNKinSdkModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        backupAndRestoreManager = new BackupAndRestoreManager(getCurrentActivity(), constants.REQ_CODE_BACKUP, constants.REQ_CODE_RESTORE);
    }

    @Override
    public String getName() {
        return "RNKinSdk";
    }

    private Environment getEnvironment(String environment) {
        Environment environ;
        switch (environment.toUpperCase()) {
            case "PRODUCTION":
                environ = Environment.PRODUCTION;
                break;
            default:
                environ = Environment.TEST;
                break;
        }
        return environ;
    }


    private void init(String config) {
        KinConfig kinConfig = gson.fromJson(config, KinConfig.class);
        getClient(kinConfig.getAppId(), kinConfig.getEnvironment());
    }

    private KinClient getClient(String appId, String environment) {
        Environment env = getEnvironment(environment);
        kinClient = new KinClient(getReactApplicationContext(), env, appId);
        return kinClient;
    }

    private KinClient getClient(String appId, Environment environment) {
        kinClient = new KinClient(getReactApplicationContext(), environment, appId);
        return kinClient;
    }

    @ReactMethod
    public void createUserAccount(String config, Callback cb) {
        Toast.makeText(getReactApplicationContext(), "About to create account for user", Toast.LENGTH_SHORT).show();
        init(config);
        try {
            KinAccount account = kinClient.addAccount();
            onBoardAccount(account, new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    cb.invoke(gson.toJson(new Error(e.getMessage(), e.getCause())), account.getPublicAddress(), kinClient.getAccountCount() - 1);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    cb.invoke(null, account.getPublicAddress(), kinClient.getAccountCount() - 1, response.body().string());
                }
            });
        } catch (CreateAccountException e) {
            cb.invoke(gson.toJson(new Error(e.getMessage(), e.getCause())));
        }
    }

    private KinAccount getUserAccount(int userIndex) {
        return kinClient.getAccount(userIndex);
    }

    @ReactMethod
    public void deleteAccount(String config, int accountIndex, Callback cb) {
        init(config);
        if (!kinClient.hasAccount())
            cb.invoke(gson.toJson(new Error("Kin client does not have any account")));
        try {
            kinClient.deleteAccount(accountIndex);
            cb.invoke(null, accountIndex);
        } catch (DeleteAccountException e) {
            cb.invoke(new Error(e.getMessage(), e.getCause()));
        }
    }

    @ReactMethod
    public void getPublicAddress(String config, int accountNumber, Callback cb) {
        init(config);
        KinAccount account = getUserAccount(accountNumber);
        cb.invoke(null, account.getPublicAddress());
    }

    @ReactMethod
    public void getUserBalance(String config, int accountIndex, final Callback cb) {
        init(config);
        KinAccount userAccount = getUserAccount(accountIndex);
        getAccountBalance(userAccount, cb);
    }

    private void getAccountBalance(KinAccount account, final Callback cb) {
        Request<Balance> balanceRequest = account.getBalance();
        balanceRequest.run(new ResultCallback<Balance>() {

            @Override
            public void onResult(Balance result) {
                cb.invoke(null, result.value().doubleValue());
            }

            @Override
            public void onError(Exception e) {
                cb.invoke(gson.toJson(new Error(e.getMessage(), e.getCause())));
            }
        });
    }

    @ReactMethod
    public void buildTransaction(String config, int accountNumber, String recipientAddress, double amount, final Callback cb) {
        init(config);
        try {
            buildTransaction(config, accountNumber, recipientAddress, amount, getCurrentMinimumFee(), cb);
        } catch (Exception e) {
            cb.invoke(gson.toJson(new Error(e.getMessage(), e.getCause())));
        }
    }

    @ReactMethod
    public void buildTransaction(String config, int accountNumber, String recipientAddress, double amount, int fee, final Callback cb) {
        buildTransaction(config, accountNumber, recipientAddress, amount, fee, null, cb);
    }

    @ReactMethod
    public void buildTransaction(String config, int accountNumber, String recipientAddress, double amount, int fee, String memo, final Callback cb) {
        init(config);
        buildTransaction(accountNumber, recipientAddress, amount, fee, memo, cb);
    }

    private void buildTransaction(int accountNumber, String recipientAddress, double amount, int fee, String memo, final Callback cb) {
        final KinAccount kinAccount = getUserAccount(accountNumber);
        Request<Transaction> transactionRequest = kinAccount.buildTransaction(recipientAddress, new BigDecimal(amount), fee, memo);
        transactionRequest.run(new ResultCallback<Transaction>() {
            @Override
            public void onResult(Transaction transaction) {
                Request<TransactionId> sendTransactionRequest = kinAccount.sendTransaction(transaction);
                sendTransactionRequest.run(new ResultCallback<TransactionId>() {
                    @Override
                    public void onResult(TransactionId transactionId) {
                        cb.invoke(null, gson.toJson(transactionId));
                    }

                    @Override
                    public void onError(Exception e) {
                        cb.invoke(gson.toJson(new Error(e.getMessage(), e.getCause())));
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                cb.invoke(gson.toJson(new Error(e.getMessage(), e.getCause())));
            }
        });
    }

    @ReactMethod
    public void buildWhitelistedTransaction(String config, int accountNumber, String recipientAddress, double amount, final Callback cb) {
        init(config);
        try {
            buildWhitelistedTransaction(config, accountNumber, recipientAddress, amount, getCurrentMinimumFee(), cb);
        } catch (Exception e) {
            cb.invoke(gson.toJson(new Error(e.getMessage(), e.getCause())));
        }
    }

    @ReactMethod
    public void buildWhitelistedTransaction(String config, int accountNumber, String recipientAddress, double amount, int fee, final Callback cb) {
        buildWhitelistedTransaction(config, accountNumber, recipientAddress, amount, fee, null, cb);
    }

    @ReactMethod
    public void buildWhitelistedTransaction(String config, int accountNumber, String recipientAddress, double amount, int fee, String memo, final Callback cb) {
        init(config);
        buildWhitelistedTransaction(accountNumber, recipientAddress, amount, fee, memo, cb);
    }

    private void buildWhitelistedTransaction(int accountNumber, String recipientAddress, double amount, int fee, String memo, final Callback cb) {
        final KinAccount kinAccount = getUserAccount(accountNumber);
        Request<Transaction> transactionRequest = kinAccount.buildTransaction(recipientAddress, new BigDecimal(amount), fee, memo);
        transactionRequest.run(new ResultCallback<Transaction>() {
            @Override
            public void onResult(Transaction transaction) {
                try {
                    whitelistTransaction(transaction.getWhitelistableTransaction(), kinAccount, cb);
                } catch (JSONException e) {
                    cb.invoke(gson.toJson(new Error(e.getMessage(), e.getCause())));
                }
            }

            @Override
            public void onError(Exception e) {
                cb.invoke(gson.toJson(new Error(e.getMessage(), e.getCause())));
            }
        });
    }

    private void whitelistTransaction(WhitelistableTransaction transaction, KinAccount kinAccount, Callback cb) throws JSONException {
        new WhitelistService().whitelistTransaction(transaction, new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                cb.invoke(gson.toJson(new Error(e.getMessage(), e.getCause())));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String whitelist = response.body().string();
                Request<TransactionId> transactionIdRequest = kinAccount.sendWhitelistTransaction(whitelist);
                transactionIdRequest.run(new ResultCallback<TransactionId>() {
                    @Override
                    public void onResult(TransactionId transactionId) {
                        cb.invoke(null, gson.toJson(transactionId), whitelist);
                    }

                    @Override
                    public void onError(Exception e) {
                        cb.invoke(gson.toJson(new Error(e.getMessage(), e.getCause())));
                    }
                });
            }
        });
    }

    @ReactMethod
    public void addPaymentListener(String config, int accountNumber, final Callback cb) {
        init(config);
        KinAccount account = getUserAccount(accountNumber);
        account.addPaymentListener(payment -> cb.invoke(gson.toJson(payment)));
    }

    @ReactMethod
    public void addBalanceListener(String config, int accountNumber, final Callback cb) {
        init(config);
        KinAccount account = getUserAccount(accountNumber);
        account.addBalanceListener(balance -> cb.invoke(gson.toJson(balance)));
    }

    @ReactMethod
    public void addAccountCreationListener(String config, int accountNumber, final Callback cb) {
        init(config);
        KinAccount account = getUserAccount(accountNumber);
        account.addAccountCreationListener(result -> cb.invoke(gson.toJson(result)));
    }

    @ReactMethod
    public void backup(String config, Callback cb) {
        init(config);
//        registerBackupCallback(cb);
        for(int i = 0; i < kinClient.getAccountCount(); i++) {
            backupAndRestoreManager.backup(kinClient, kinClient.getAccount(i));
        }
        cb.invoke(null, "Kin Backup of all accounts was successful");
    }

    @ReactMethod
    public void restore(String config, Callback cb) {
        init(config);
//        registerRestoreCallback(cb);
        backupAndRestoreManager.restore(kinClient);
        cb.invoke(null, "Kin Restore was successful");
    }

    private void registerBackupCallback(Callback cb) {
        backupAndRestoreManager.registerBackupCallback(new BackupCallback() {
            @Override
            public void onSuccess() {
                cb.invoke(null, "Kin backup Success");
            }

            @Override
            public void onCancel() {
                cb.invoke(new Error("Kin backup was cancelled"));
            }

            @Override
            public void onFailure(BackupAndRestoreException e) {
                cb.invoke(new Error(e.getMessage(), e.getCause()));
            }
        });
    }

    private void registerRestoreCallback(Callback cb) {
        backupAndRestoreManager.registerRestoreCallback(new RestoreCallback() {
            @Override
            public void onSuccess(KinClient kinClient, KinAccount kinAccount) {
                getClient(kinClient.getAppId(), kinClient.getEnvironment());
                Map<String, String> result = new HashMap<>();
                result.put("APP_ID", kinClient.getAppId());
                result.put("PUBLIC_ADDRESS", kinAccount.getPublicAddress());
                cb.invoke(null, gson.toJson(result));
            }

            @Override
            public void onCancel() {
                cb.invoke(new Error("Kin restore cancelled"));
            }

            @Override
            public void onFailure(BackupAndRestoreException e) {
                cb.invoke(new Error(e.getMessage(), e.getCause()));
            }
        });
    }

    @ReactMethod
    public void getCurrentMinimumFee(String config, Callback cb) {
        init(config);
        try {
            int fee = getCurrentMinimumFee();
            cb.invoke(null, fee);
        } catch (Exception ex) {
            cb.invoke(gson.toJson(new Error(ex.getMessage(), ex.getCause())));
        }
    }

    private int getCurrentMinimumFee() throws Exception {
        return (int) Math.ceil(kinClient.getMinimumFeeSync());
    }

    private void onBoardAccount(@NonNull KinAccount account, @NonNull okhttp3.Callback cb) {
        makeRequest(String.format(constants.URL_CREATE_ACCOUNT, account.getPublicAddress()), cb);
    }

    private void makeRequest(@NonNull String url, @NonNull okhttp3.Callback cb) {
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .get()
                .build();
        new okhttp3.OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build().newCall(request)
                .enqueue(cb);
    }

    @Override
    public void onCatalystInstanceDestroy() {
        super.onCatalystInstanceDestroy();
        backupAndRestoreManager.release();
    }
}