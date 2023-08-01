package com.tscale.demo.main;

import static com.yoyo.ui.common.enums.WeightStableEnum.WeightStable;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.AdaptScreenUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.google.gson.Gson;
import com.yoyo.ui.common.enums.MarkTypeEnum;
import com.yoyo.ui.common.enums.PriceUnitTypeEnum;
import com.yoyo.ui.common.enums.RecognitionModelEnum;
import com.yoyo.ui.common.enums.ScaleTypeEnum;
import com.yoyo.ui.common.enums.WeightStableEnum;
import com.yoyo.ui.common.enums.YoYoActivityTagEnum;
import com.yoyo.ui.constants.YoyoSdkConfig;
import com.yoyo.ui.view.dialog.DlgInputCode;
import com.yoyo.yoyobase.log.LogExtKt;
import com.yoyo.yoyobase.utils.ThreadManager;
import com.yoyo.yoyodata.bean.response.Reply;
import com.yoyo.yoyodata.constants.ReplyCode;
import com.yoyo.yoyodata.event.ErrorEvent;
import com.yoyo.yoyodata.event.MessageEvent;
import com.yoyo.yoyonet.bean.info.ErrorInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.tscale.demo.R;
import cn.smart.yoyolib.core.aidl.YoYoItemInfo;
import cn.smart.yoyolib.libs.YoYoUtils;
import cn.smart.yoyolib.libs.bean.request.AIMatchingRequest;
import cn.smart.yoyolib.libs.bean.request.ActivateInfo;
import cn.smart.yoyolib.libs.bean.request.ActivateRequest;
import cn.smart.yoyolib.libs.bean.request.AiMarkRequest;
import cn.smart.yoyolib.libs.bean.request.CameraAreaRequest;
import cn.smart.yoyolib.libs.bean.request.InitRequest;
import cn.smart.yoyolib.libs.bean.request.ScaleDataRequest;
import cn.smart.yoyolib.libs.bean.request.ShopBaseInfo;
import cn.smart.yoyolib.libs.bean.request.ShopInfoRequest;
import cn.smart.yoyolib.libs.bean.request.StudyDataRequest;
import cn.smart.yoyolib.libs.bean.response.GetDeviceInfoReply;
import cn.smart.yoyolib.libs.bean.response.GetVersionReply;
import cn.smart.yoyolib.libs.bean.response.MatchingReply;
import cn.smart.yoyolib.libs.callback.HttpCallbackListener;
import cn.smart.yoyolib.libs.callback.IYoYoAiListener;
import cn.smart.yoyolib.libs.callback.IYoyoSdkInitListener;
import cn.smart.yoyolib.libs.enums.SetStudyModeType;
import cn.smart.yoyolib.match.icon.IconMatchUtils;
import cn.smart.yoyolib.utils.DictionaryUtil;
import cn.smart.yoyolib.utils.NavigationBarUtils;
import cn.smart.yoyolib.utils.PathConstant;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

//import com.tscale.scale.exceptionCrashHandler.CrashHandler;
//import com.tscale.scale.utils.Utils;
import com.tscale.tslog.TSLog;
import com.tscale.tsscale.JNIScale;
import com.tscale.tsscale.ScaleSettings;
import com.tscale.tsscale.TADCallback;
import com.tscale.tsscale.weightHandling.TADWeight;
import com.tscale.tsutil.TSLanguageSettings;

import com.tscale.labelprinter.CaysnLabelPrinter;
import com.tscale.labelprinter.HPRTLabelPrinter;
import com.tscale.labelprinter.TScaleLabel;
import com.tscale.receiptprinter.TScaleReceipt;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements ResultItemAdapter.ActionItemListener , TADCallback {

    private TextView price1;
    private TextView price2;

    private TextView tv_top_amount;
    private TextView tv_top_amount_2;

    private TextView sum1;
    private TextView sum2;

    private TextView salePriceDesc;
    private RecyclerView listView;
    private ResultItemAdapter resultItemAdapter;
    private YoYoItemInfo baseInfo;
    private int weight = 0;
    private LinearLayout llSearch;
    private boolean isSearch = false;

    private LinearLayout llTips;
    private TextView tvTips;
    private TextView saveLearning, getLearning, setLearning;
    private AppCompatButton btnActivate, btnGetSdkVersion, btnGetShopInfo, btnUpdateShopInfo, btnSetCameraPoint;
    private TextView studyMoudle;

    private TextView commodityManager;
    private ImageView button;
    private TextView reCapture;
    private TextView search;
    private String matchingTag = "";

    //tscale add args
    private boolean tszar = false;

    private TScaleLabel printer;

    private static boolean flag = false;

    private static int baudrate = 9600;
    private JNIScale mScale = JNIScale.getScale(baudrate);
    //private JNIScale mScale = JNIScale.getScale(115200);

    long onWeightUpdate_startTime = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NavigationBarUtils.hideNavigationBar(this);
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        initView();
        try{
            TSLanguageSettings.updateLocaleLanguage(this);
            mScale.setCallback(this);
            TSLog.setDebug(false);
            printer = getUSBPrinter();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        btnActivate = findViewById(R.id.btn_activate);
        btnGetSdkVersion = findViewById(R.id.btn_get_sdk_version);
        btnGetShopInfo = findViewById(R.id.btn_get_shop_info);
        btnUpdateShopInfo = findViewById(R.id.btn_update_shop_info);
        btnSetCameraPoint = findViewById(R.id.btn_set_camera_point);

        price1 = findViewById(R.id.tv_top_unit_price);
        price2 = findViewById(R.id.tv_top_unit_price_2);
        tv_top_amount = findViewById(R.id.tv_top_amount);
        tv_top_amount_2 = findViewById(R.id.tv_top_amount_2);
        sum1 = findViewById(R.id.tv_price_sum);
        sum2 = findViewById(R.id.tv_price_sum_2);
        salePriceDesc = findViewById(R.id.salePriceDesc);
        listView = findViewById(R.id.listView);
        llSearch = findViewById(R.id.llSearch);
        llTips = findViewById(R.id.llTips);
        tvTips = findViewById(R.id.tvTips);
        saveLearning = findViewById(R.id.saveLearning);
        getLearning = findViewById(R.id.getLearning);
        setLearning = findViewById(R.id.setLearning);

        studyMoudle = findViewById(R.id.studyMoudle);
        commodityManager = findViewById(R.id.commodityManager);
        listView.setLayoutManager(new GridLayoutManager(this, 4));
        listView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        resultItemAdapter = new ResultItemAdapter(this, new ArrayList<>());
        resultItemAdapter.setListener(this);
        listView.setAdapter(resultItemAdapter);
        button = findViewById(R.id.bt);
        reCapture = findViewById(R.id.reCapture);
        search = findViewById(R.id.search);
        //设置搜索最大数量,不设置默认-1为全部
        //DictionaryUtil.getInstance().setSearchSize(20);
        //申请权限，初始化，下载so等
        rxPermissionsAndroid11();
        initSdk();
        initListener();
        //TODO 此处调用仅为演示时设置固定重量，客户按需开发自己的取重，清零等，在取重代码中调用此方法传重量
        //checkWeight(1, WeightStableEnum.WeightStable);
    }

    private void initListener() {
        //跳转到由由设置页进行激活，门店信息，设置识别区域等
        button.setOnClickListener(v -> YoYoUtils.startYoYoActivity(MainActivity.this, YoYoActivityTagEnum.SETTING));
        //学习模式
        studyMoudle.setOnClickListener(v -> YoYoUtils.startYoYoActivity(MainActivity.this, YoYoActivityTagEnum.STUDY));

        //点击识别按钮进行拍照识别
        reCapture.setOnClickListener(v -> {
            YoYoUtils.aiMatching(new AIMatchingRequest(0, WeightStableEnum.WeightStable, RecognitionModelEnum.Forcibly));
            //置空搜索框值，隐藏键盘

            EventBus.getDefault().post(new MessageEvent(""));
            llSearch.setVisibility(View.GONE);
            isSearch = false;
        });

        //打开搜索键盘搜索商品
        search.setOnClickListener(v -> checkSearch());

        //保存学习记录，客户根据自己的逻辑调用此方法
        saveLearning.setOnClickListener(v -> {
            Reply<?> reply = YoYoUtils.saveStudyData();
            if (reply.getCode() == ReplyCode.Success) {
                Toast.makeText(MainActivity.this, "保存成功", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "保存失败", Toast.LENGTH_LONG).show();
            }
        });


        btnSetCameraPoint.setOnClickListener(v -> {
            DemoDialog dialog = new DemoDialog();
            Bundle bundle = new Bundle();
            bundle.putInt("type", 1);
            dialog.setArguments(bundle);
            dialog.setActionListener(map -> {
                LogExtKt.logI(new Gson().toJson(map));
                CameraAreaRequest request = new CameraAreaRequest();
                request.setLeftTopX((Integer) map.get("ltx"));
                request.setLeftTopY((Integer) map.get("lty"));
                request.setRightTopX((Integer) map.get("rtx"));
                request.setRightTopY((Integer) map.get("rty"));
                request.setLeftBottomX((Integer) map.get("rbx"));
                request.setLeftBottomY((Integer) map.get("rby"));
                request.setRightBottomX((Integer) map.get("lbx"));
                request.setRightBottomY((Integer) map.get("lby"));
                Reply<?> reply = YoYoUtils.setCameraArea(request);
                if (reply.getCode() == ReplyCode.Success) {
                    LogExtKt.logI("MainActivity: 设置成功");
                    Toast.makeText(MainActivity.this, "设置成功", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "设置成功", Toast.LENGTH_LONG).show();
                    LogExtKt.logI("MainActivity: 设置失败");
                }
                return null;
            });
            dialog.show(getSupportFragmentManager(), "point");
        });
        btnActivate.setOnClickListener(v -> {
            DlgInputCode inputCode = new DlgInputCode();
            inputCode.setListener(s -> {
                ActivateRequest request = new ActivateRequest();
                ActivateInfo info = new ActivateInfo();
                info.setCdKey(s);
                request.setActivateInfo(info);
                YoYoUtils.initActivate(request, new HttpCallbackListener<String>() {
                    @Override
                    public void onEventActivitySuccess(@Nullable Reply<String> reply) {
                        if (reply.getCode() == ReplyCode.Success) {
                            Toast.makeText(MainActivity.this, "设置成功", Toast.LENGTH_LONG).show();
                            LogExtKt.logI("激活成功");
                        } else {
                            Toast.makeText(MainActivity.this, "设置成功", Toast.LENGTH_LONG).show();
                            LogExtKt.logI("激活失败");
                        }
                    }

                    @Override
                    public void onEventActivityFailure(@Nullable Reply<ErrorInfo> reply) {
                        Toast.makeText(MainActivity.this, "设置成功", Toast.LENGTH_LONG).show();
                        LogExtKt.logI("激活失败");
                    }
                });
            });
            inputCode.show(getSupportFragmentManager(), "doCloudActivate");
        });
        btnGetSdkVersion.setOnClickListener(v -> {
            Reply<?> reply = YoYoUtils.getSDKVersion();
            if (reply.getCode() == ReplyCode.Success) {
                GetVersionReply response = (GetVersionReply) reply.getData();
                LogExtKt.logI(new Gson().toJson(response).trim());
            } else {
                Toast.makeText(MainActivity.this, "获取版本失败", Toast.LENGTH_LONG).show();
            }
        });
        btnUpdateShopInfo.setOnClickListener(v -> {
            DemoDialog dialog = new DemoDialog();
            Bundle bundle = new Bundle();
            bundle.putInt("type", 0);
            dialog.setArguments(bundle);
            dialog.setActionListener(stringObjectMap -> {
                ShopInfoRequest request = new ShopInfoRequest();
                ShopBaseInfo info = new ShopBaseInfo();
                info.setPhone(String.valueOf(stringObjectMap.get("phone")));
                info.setName(String.valueOf(stringObjectMap.get("name")));
                info.setContact(String.valueOf(stringObjectMap.get("person")));
                request.setShopInfo(info);
                YoYoUtils.editShopInfo(request, new HttpCallbackListener<String>() {
                    @Override
                    public void onEventActivitySuccess(@Nullable Reply<String> reply) {
                        if (reply != null) {
                            if (reply.getCode() == ReplyCode.Success) {
                                Toast.makeText(MainActivity.this, "更新成功", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(MainActivity.this, "更新失败", Toast.LENGTH_LONG).show();
                            }
                        }
                    }

                    @Override
                    public void onEventActivityFailure(@Nullable Reply<ErrorInfo> reply) {
                        Toast.makeText(MainActivity.this, "更新失败", Toast.LENGTH_LONG).show();
                    }
                });
                return null;
            });
            dialog.show(getSupportFragmentManager(), "point");
        });


        btnGetShopInfo.setOnClickListener(v -> {
            YoYoUtils.getDeviceInfo(new HttpCallbackListener<GetDeviceInfoReply>() {
                @Override
                public void onEventActivitySuccess(@Nullable Reply<GetDeviceInfoReply> reply) {
                    if (reply != null) {
                        if (reply.getCode() == ReplyCode.Success) {
                            Toast.makeText(MainActivity.this, new Gson().toJson(reply).trim(), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, "获取信息失败", Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onEventActivityFailure(@Nullable Reply<ErrorInfo> reply) {
                    Toast.makeText(MainActivity.this, "获取信息失败", Toast.LENGTH_LONG).show();
                }
            });
        });

        //保存学习记录，客户根据自己的逻辑调用此方法
        saveLearning.setOnClickListener(v -> {
            Reply<?> reply = YoYoUtils.saveStudyData();
            if (reply.getCode() == ReplyCode.Success) {
                Toast.makeText(MainActivity.this, "保存成功", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "保存失败", Toast.LENGTH_LONG).show();
            }
        });
        getLearning.setOnClickListener(v -> {
            Reply<?> reply = YoYoUtils.getStudyData();
            if (reply.getCode() == ReplyCode.Success) {
                LogExtKt.logI(String.valueOf(reply.getData()));
                byte[] bytes = (byte[]) reply.getData();
                FileUtils.createOrExistsFile(PathConstant.INSTANCE.getPathSkuExport() + File.separator + "data/study_export.zip");
                FileIOUtils.writeFileFromBytesByStream(PathConstant.INSTANCE.getPathSkuExport() + File.separator + "data/study_export.zip", bytes);
                Toast.makeText(MainActivity.this, "获取成功", Toast.LENGTH_LONG).show();
            } else {
                LogExtKt.logI(reply.getMessage());
                Toast.makeText(MainActivity.this, "获取失败", Toast.LENGTH_LONG).show();
            }
        });

        setLearning.setOnClickListener(v -> {
            List<File> list = FileUtils.listFilesInDir(PathConstant.INSTANCE.getPathSkuAdd());
            if (list.size() > 0) {
                byte[] bytes = FileIOUtils.readFile2BytesByStream(list.get(0).getAbsolutePath());
                Reply<?> reply = YoYoUtils.setStudyData(new StudyDataRequest(bytes, SetStudyModeType.MERGER));
                if (reply.getCode() == ReplyCode.Success) {
                    LogExtKt.logI(String.valueOf(reply.getData()));
                    Toast.makeText(MainActivity.this, "配置成功", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "配置失败", Toast.LENGTH_LONG).show();
                    LogExtKt.logI(reply.getMessage());
                }
            } else {
                Toast.makeText(MainActivity.this, "没有可配置的学习数据", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void initSdk() {
        //监听识别结果，so初始化的回调,正式的code和key请联系由由商务
        YoyoSdkConfig config = new YoyoSdkConfig();

//        config.setUseProxy(false); //是否启用代理，false-不启用，true启用
//        config.setDefaultProxyAccount("yoyo");//代理服务器的帐号
//        config.setDefaultProxyPassword("yoyo1234");//代理服务器的密码
//        config.setDefaultProxyIp("0.0.0.0");//代理服务器的IP
//        config.setDefaultProxyPort(8000); //代理服务器的端口
        //上面需要先启用代理，并设备正确的代理服务器信息，方可进行访问外网
//        config.setLoadSoDynamically(false);//使用静态加载方式加载so


        YoYoUtils.init(MainActivity.this, new InitRequest("台衡紧密测控(昆山)有限公司\t生鲜\tTH", "ae28e6fa784c9ede049b8ac7d8bcbce4"), new IYoYoAiListener() {
            @Override
            public void onEventAIMatching(Reply<MatchingReply> matchingReply) {
                if (matchingReply.getCode() == ReplyCode.Success) {
                    matchingTag = matchingReply.getData().getMatchingTag();
                    LogExtKt.logI("matchingTag===" + matchingTag);
                    List<String> resultList = matchingReply.getData().getPlus();
                    if (resultList != null && resultList.size() > 0) {
                        if (resultItemAdapter != null) {
                            runOnUiThread(() -> {
                                List<YoYoItemInfo> itemInfoList = new ArrayList<>();
                                for (int i = 0; i < resultList.size(); i++) {
                                    YoYoItemInfo itemInfoByPlu = DictionaryUtil.getInstance().findItemInfoByPlu(resultList.get(i));
                                    if (itemInfoByPlu != null) {
                                        itemInfoList.add(itemInfoByPlu);
                                    }
                                }
                                llTips.setVisibility(View.GONE);
                                listView.setVisibility(View.VISIBLE);
                                resultItemAdapter.updateData(itemInfoList);
                                //识别出来关闭键盘
                                if (isSearch) {
                                    llSearch.setVisibility(View.GONE);
                                    isSearch = false;
                                }
                            });

                        }
                    } else {
                        LogExtKt.logI("识别失败");
                        runOnUiThread(() -> {
                            llTips.setVisibility(View.VISIBLE);
                            listView.setVisibility(View.GONE);
                            tvTips.setText("小由没有识别出来～ \n\n 请使用人工搜索～");
                        });
                    }
                }
            }

            @Override
            public void onEventInit(Reply reply) {
                if (reply.getCode() == ReplyCode.Success) {
                    doTransData();
                }
            }

            @Override
            public void onEventActive(Reply reply) {
                if (reply.getCode() == ReplyCode.Success) {
                    LogExtKt.logI("设备已激活");
                } else {
                    LogExtKt.logE("设备未激活");
                }
            }

        }, config);
    }


    //申请Android11权限
    private void rxPermissionsAndroid11() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ) {
            if (!Environment.isExternalStorageManager()){
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }else{
                LogExtKt.logI("权限已开启");
            }
        } else {
            LogExtKt.logI("不是Android 11系统");
        }
    }

    private void checkSearch() {
        if (!isSearch) {
            llSearch.setVisibility(View.VISIBLE);  //tag_keyboard
            isSearch = true;
        } else {
            llSearch.setVisibility(View.GONE);
            resultItemAdapter.updateData(new ArrayList<>());
            isSearch = false;
            EventBus.getDefault().post(new MessageEvent(""));
            listView.setVisibility(View.GONE);
            llTips.setVisibility(View.VISIBLE);
        }
    }


    //按屏幕宽度适配
    @Override
    public Resources getResources() {
        return AdaptScreenUtils.adaptWidth(super.getResources(), 1920);
    }


    //传称
    private void doTransData() {
        ThreadManager.getExecutorService().execute(() -> {
            //读取演示数据 文件路径assets/transferData
            String data = IconMatchUtils.INSTANCE.readJson("transferData", MainActivity.this);
            List<YoYoItemInfo> itemInfoList = JSON.parseArray(data, YoYoItemInfo.class);
//            List<YoYoItemInfo> itemInfoList = GsonUtils.getGson().fromJson(data, new TypeToken<List<YoYoItemInfo>>() {
//            }.getType());

           /* List<YoYoItemInfo> itemInfoList = new ArrayList<>();
            for (int i =0 ;i<200;i++){
                YoYoItemInfo info = new YoYoItemInfo();
                info.plu = "100"+i;
                info.itemCode = "100"+i;
                info.itemName = "红富士"+i;
                info.isOn = 1;
                info.isLock = 0;
                info.unitType = PriceUnitType.NumberType;
                info.unitPrice = 1000;
                itemInfoList.add(info);
            }*/
            YoYoUtils.setScaleData(new ScaleDataRequest(itemInfoList, true, ScaleTypeEnum.Increments));
        });


    }


    //接收键盘输入的值
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (!event.getSearch().isEmpty()) {
            //查找商品
            TSLog.console(TSLog.i, "查找商品");
            List<YoYoItemInfo> search = DictionaryUtil.getInstance().getItemsBySearch(event.getSearch());
            //List<YoYoItemInfo> search = DictionaryUtil.getInstance().getItemsBySearch("dbc");
            if (resultItemAdapter != null) {
                TSLog.console(TSLog.i, "resultItemAdapter is not null" + ",size:" + search.size());
                llTips.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                resultItemAdapter.updateData(search);
            }
        } else {
            TSLog.console(TSLog.i, "不查找商品");
            llTips.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        }
    }

    //错误提示信息
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void errorToastShow(ErrorEvent event) {
        Toast.makeText(this, event.getErr(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        mScale = JNIScale.getScale(baudrate);
        mScale.setCallback(this);
        TSLog.console(TSLog.i, "onStart");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        resultItemAdapter.clean();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mScale.deleteScale();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        YoYoUtils.saveStudyData();
//        YoYoUtils.unInit();
        Process.killProcess(Process.myPid());
    }

    //重量的回调
    public void checkWeight(double weight, WeightStableEnum stable) {
        runOnUiThread(() -> {
            int w = (int) (weight * 1000);
            this.weight = w;
            updateWeight(w);
            YoYoUtils.aiMatching(new AIMatchingRequest(w, stable, RecognitionModelEnum.Auto));
            //拿走清理掉数据
            if (w <= 0) {
                resultItemAdapter.clean();
                baseInfo = null;
                llTips.setVisibility(View.VISIBLE);
                listView.setVisibility(View.GONE);
                salePriceDesc.setText("元/kg");
                tvTips.setText("装载台面上空空如也～\n\n" +
                        "请放置您要秤量的商品，小由帮您快速识别～");
            }

            updateView();
        });
    }

    private void updateWeight(int weight) {
        int d1 = weight / 1000;
        int d2 = weight % 1000;
        boolean isF = d2 < 0;
        d2 = Math.abs(d2);
        String part2 = "" + d2;

        for (int n = 0; n < 3 - (d2 + "").length(); n++) {
            part2 = "0" + part2;
        }

        tv_top_amount.setText((isF ? "-" : "") + d1 + ".");
        tv_top_amount_2.setText(part2);
    }


    //更新当前UI
    private void updateView() {
        if (baseInfo == null) {
            price1.setText("0.");
            price2.setText("00");

            sum1.setText("0.");
            sum2.setText("00");
        } else {
            long sum = baseInfo.unitPrice * weight;
            long s1 = sum / 100000;
            long s2 = sum % 100000 / 100;
            sum1.setText(s1 + ".");

            if (s2 < 10) {
                sum2.setText("0" + s2);
            } else {
                BigDecimal biga = new BigDecimal(s2);
                BigDecimal bigb = new BigDecimal(10);
                String divide = biga.divide(bigb, BigDecimal.ROUND_HALF_UP).toString();
                sum2.setText(divide);
            }

            if (baseInfo.unitType.getCode() == 0) {
                salePriceDesc.setText("元/kg");
            } else {
                salePriceDesc.setText("元/个");
            }


            int p1 = baseInfo.unitPrice / 100;
            int p2 = baseInfo.unitPrice % 100;

            price1.setText(p1 + ".");
            if (p2 < 10) {
                price2.setText("0" + p2);
            } else {
                price2.setText(p2 + "");
            }
        }
    }

    //标记输出结果
    @Override
    public void onEventItemClick(YoYoItemInfo info) {
        AiMarkRequest request = new AiMarkRequest();
        request.plu = info.plu;
        request.weight = weight;
        request.matchingTag = matchingTag;
        //传值以实际为主
        request.barCode = "231000101000";
        request.operatorName = "操作员";
        request.deviceNo = "1";
        request.price = info.unitPrice;
        if (info.unitType.getCode() == 0) {
            request.priceUnit = PriceUnitTypeEnum.WeightType;
            request.amount = (info.unitPrice * weight) / 1000;
        } else if (info.unitType.getCode() == 1) {
            request.priceUnit = PriceUnitTypeEnum.NumberType;
            request.amount = info.unitPrice;
        }
        //是否是搜索的商品
        if (isSearch) {
            request.type = MarkTypeEnum.MarkSearch;
            Reply reply = YoYoUtils.aiMark(request);
            LogExtKt.logI("请求结果:" + GsonUtils.getGson().toJson(reply));
        } else {
            //识别出的商品
            request.type = MarkTypeEnum.MarkMatchOut;
            Reply reply = YoYoUtils.aiMark(request);
            LogExtKt.logI("请求结果:" + GsonUtils.getGson().toJson(reply));
        }
        matchingTag = "";//标记完后清空
        this.baseInfo = info;
        llSearch.setVisibility(View.GONE);
        isSearch = false;
        EventBus.getDefault().post(new MessageEvent(""));
        resultItemAdapter.updateData(new ArrayList<>());

        llTips.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);
        Log.d("MainActivity", "您点击了" + info.itemName);

        //设置点击单价和总价
        updateView();
        updateWeight(1000);
    }

    // tscale functions
    @Override
    public void onWeightUpdate(TADWeight weight, boolean isStable, boolean isTared, boolean isZero) {
        //TSLog.console(TSLog.v, "Update Weight: " + weight.getWeightInGramString() + " : " + weight.getUnit() + ",isStable:" + isStable );
        Log.i("onWeightUpdate","Update Weight: " + weight.getWeightInGramString() + " : " + weight.getUnit() + ",isStable:" + isStable);
        //long startTime = SystemClock.elapsedRealtime();
        if( onWeightUpdate_startTime == 0 ) {
            onWeightUpdate_startTime = SystemClock.elapsedRealtime();
            checkWeight(weight.getWeightInGram(), isStable ? WeightStableEnum.WeightStable:WeightStableEnum.WeightNotStable);
            //TSLog.console(TSLog.i, "First exec onWeightUpdate");
        }else{
            long endTime = SystemClock.elapsedRealtime();
            if( endTime - onWeightUpdate_startTime > 100 ){
                onWeightUpdate_startTime = endTime;
                checkWeight(weight.getWeightInGram(), isStable ? WeightStableEnum.WeightStable:WeightStableEnum.WeightNotStable);
                //TSLog.console(TSLog.i, "more 100ms then exec onWeightUpdate");
            }else{
                //TSLog.console(TSLog.i, "not more 100ms then do nothing");
            }
            //onWeightUpdate_startTime = endTime;
        }
        //long endTime = SystemClock.elapsedRealtime();
        //long elapsedTime = endTime - startTime;
    }

    @Override
    public void scaleStatus(boolean isOk, int code, String message) {
        //TSLog.console(TSLog.d, "Mainactivity", " msg:" + message + " status:" + isOk +" code:" +  code );
        if (code == 1002) {
            tszar = true;
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    tszar = false;
                }
            }, 5 * 1000);
        }
    }

    @Override
    public void onVoltageUpdate(int voltage) {

    }

    public void onCalibrationSwitchEvent() {

    }

    @Override
    public void onQuickStable() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                /*
                if (hasReturnedZero) {
                    if (!mScale.getZeroFlag()) {
                        Toast.makeText(MainActivity.this, "QUICK STABLE TRIGGERED!", Toast.LENGTH_SHORT).show();
                    }
                    hasReturnedZero = false;
                }
                */
            }
        });
    }

    private TScaleLabel getUSBPrinter() {
        UsbManager usbManager = (UsbManager) this.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        TSLog.console(TSLog.i, deviceList.toString());//mVendorId=19267,mProductId=13624
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

        if (deviceList.size() > 0) {
            while (deviceIterator.hasNext()) {
                final UsbDevice device = deviceIterator.next();
                String devInfo = String.format(" VID:%04X PID:%04X",
                        device.getVendorId(), device.getProductId());
                TSLog.console(TSLog.d, devInfo);//VID:0EEF PID:0001
                TSLog.console(TSLog.d, "USBID:" + device.getProductId() + "----" + device.getVendorId());
                //AAA
                if (device.getVendorId() == TScaleLabel.CAYSN_LABEL_VENDOR_ID &&
                        device.getProductId() == TScaleLabel.CAYSN_LABEL_PRODUCT_ID) {
                    TSLog.console(TSLog.i, "Caysn Label Printer USB Found.");
                    Log.i("getUSBPrinter","Caysn Label Printer USB Found.");
                    return new CaysnLabelPrinter(this, TScaleLabel.TYPE_USB);
                }
                //BBB
                if (device.getVendorId() == TScaleReceipt.CAYSN_T9_VID &&
                        device.getProductId() == TScaleReceipt.CAYSN_T9_PID) {
                    TSLog.console(TSLog.i, "Caysn T8 Label Printer USB Found.");
                    Log.i("getUSBPrinter","Caysn T8 Label Printer USB Found.");
                    flag = true;
                    return new CaysnLabelPrinter(this, TScaleLabel.TYPE_USB);
                }
                //CCC
                if (device.getVendorId() == TScaleLabel.HPRT_LABEL_VENDOR_ID &&
                        device.getProductId() == TScaleLabel.HPRT_LABEL_PRODUCT_ID) {
                    TSLog.console(TSLog.i, "HPRT Label Printer USB Found.");
                    Log.i("getUSBPrinter","HPRT Label Printer USB Found.");
                    return new HPRTLabelPrinter(this, TScaleLabel.TYPE_USB);
                }
            }
        }
        return null;
    }
}
