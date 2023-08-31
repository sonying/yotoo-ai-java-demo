package com.tscale.demo.main;

import static com.yoyo.ui.common.enums.WeightStableEnum.WeightStable;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import android.widget.Button;
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
import com.next.androidintentlibrary.BrowserIntents;
import com.next.androidintentlibrary.SettingIntents;
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
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

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
import com.davidmiguel.numberkeyboard.NumberKeyboard;
import com.davidmiguel.numberkeyboard.NumberKeyboardListener;

public class MainActivity extends AppCompatActivity implements ResultItemAdapter.ActionItemListener , TADCallback, NumberKeyboardListener {

    private static final String SHOWCASE_ID = "99248215793";
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
    //private TextView search;
    private String matchingTag = "";

    // Tscale add args ================
    private static final int TEXTSTYLE_NORMAL = 0;
    private static final int TEXTSTYLE_BOLD = 1;
    private static final int TEXTSTYLE_UNDERLINE = 2;
    private static final int TEXTSTYLE_HIGHLIGHT = 4;
    private static final int TEXTSTYLE_STRIKETHROUGH = 8;
    private final int LABEL_WIDTH_432 = 432;
    private final int LABEL_HEIGHT_432 = 432;
    private Button clear_to_zero;
    private ImageView zeroStatus;
    private ImageView stableStatus;
    private ImageView tareStatus;
    private ImageView search_items;
    private ImageView favorite_lists;
    private ImageView setup_app_into;
    private ImageView web_to_yoyo;
    private TextView tare_func;
    private TextView weightDesc;
    private boolean tszar = false;
    private TScaleLabel printer;
    private static boolean flag = false;
    private static int baudrate = 9600;
    private JNIScale mScale = JNIScale.getScale(baudrate);
    long onWeightUpdate_startTime = 0;
    long onRightAuxButtonClicked_startTime = 0;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private final static String SHART = "data";
    private NumberKeyboard numberKeyboard;

    // Tscale add args end ==========
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
            //注冊稱重
            mScale.setCallback(this);
            TSLog.setDebug(false);
            sharedPreferences = getSharedPreferences(SHART, Activity.MODE_PRIVATE);
            editor = sharedPreferences.edit();
            boolean tareStatusFlag = sharedPreferences.getBoolean("tareStatus", false);
            if (tareStatusFlag) {
                editor.putBoolean("tareStatus", true).apply();
            } else {
                editor.putBoolean("tareStatus", false).commit();
            }
            //注冊打印
            printer = getUSBPrinter();
            boolean status = printer.usbConnect();
            printer.enableLabel(true);
            printer.setPaperRecycle(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        new MaterialShowcaseView.Builder(this)
                .setTarget(button)
                .setTitleText(R.string.ShowcaseView_title)
                .setDismissText(R.string.ShowcaseView_dis)
                .setDelay(100) // optional but starting animations immediately in onCreate can make them choppy
                .singleUse(SHOWCASE_ID) // provide a unique ID used to ensure it is only shown once
                .renderOverNavigationBar()
                .setTargetTouchable(true)
                .show();
    }

    private void initView() {
        btnActivate = findViewById(R.id.btn_activate);
        btnGetSdkVersion = findViewById(R.id.btn_get_sdk_version);
        btnGetShopInfo = findViewById(R.id.btn_get_shop_info);
        btnUpdateShopInfo = findViewById(R.id.btn_update_shop_info);
        btnSetCameraPoint = findViewById(R.id.btn_set_camera_point);
        search_items = findViewById(R.id.search_l);
        favorite_lists = findViewById(R.id.favorite_l);
        setup_app_into = findViewById(R.id.setup_app_into);
        web_to_yoyo = findViewById(R.id.web_to_yoyo);

        numberKeyboard = findViewById(R.id.numberKeyboard);
        price1 = findViewById(R.id.tv_top_unit_price);
        price2 = findViewById(R.id.tv_top_unit_price_2);
        tv_top_amount = findViewById(R.id.tv_top_amount);
        tv_top_amount_2 = findViewById(R.id.tv_top_amount_2);
        stableStatus = findViewById(R.id.stable_status);
        zeroStatus = findViewById(R.id.zero_status);
        tareStatus = findViewById(R.id.tare_status);
        clear_to_zero = findViewById(R.id.clear_to_zero);
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
        tare_func = findViewById(R.id.tare_func);
        weightDesc = findViewById(R.id.weightDesc);
        studyMoudle = findViewById(R.id.studyMoudle);
        commodityManager = findViewById(R.id.commodityManager);
        listView.setLayoutManager(new GridLayoutManager(this, 4));
        listView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        resultItemAdapter = new ResultItemAdapter(this, new ArrayList<>());
        resultItemAdapter.setListener(this);
        listView.setAdapter(resultItemAdapter);
        button = findViewById(R.id.bt);
        reCapture = findViewById(R.id.reCapture);
        //search = findViewById(R.id.search);
        //设置搜索最大数量,不设置默认-1为全部
        //DictionaryUtil.getInstance().setSearchSize(20);
        //申请权限，初始化，下载so等
        rxPermissionsAndroid11();
        initSdk();
        initListener();
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

        search_items.setOnClickListener(v -> checkSearch());
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

        tare_func.setOnClickListener(v -> {
            if (JNIScale.getScale() != null) {
                String tare1 = mScale.tare(sharedPreferences.getBoolean("tareStatus", false));
            }
        });

        clear_to_zero.setOnClickListener(v -> {
            Log.i("clear_to_zero","tszar value:" + tszar );
            if (!tszar) {
                mScale.zero();
            }
        });

        numberKeyboard.setListener(this);

        setup_app_into.setOnClickListener(v -> {
            SettingIntents.from(this).setting().show();
        });

        web_to_yoyo.setOnClickListener(v -> {
            BrowserIntents.from(this).openLink("http://www.yoyo.link/").show();
        });
    }

    private void initSdk() {
        //监听识别结果，so初始化的回调,正式的code和key请联系由由商务
        YoyoSdkConfig config = new YoyoSdkConfig();

        config.setUseProxy(false); //是否启用代理，false-不启用，true启用
        config.setDefaultProxyAccount("yoyo");//代理服务器的帐号
        config.setDefaultProxyPassword("yoyo1234");//代理服务器的密码
        config.setDefaultProxyIp("0.0.0.0");//代理服务器的IP
        config.setDefaultProxyPort(8000); //代理服务器的端口
        //上面需要先启用代理，并设备正确的代理服务器信息，方可进行访问外网
        config.setLoadSoDynamically(false);//使用静态加载方式加载so

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

        }, config, new IYoyoSdkInitListener() {
            @Override
            public void onSOInitSuccess(Reply<?> reply) {

            }
        });
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
        if( this.weight > 0) {
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
        }else{
            Toast.makeText(this, "無重量！！", Toast.LENGTH_SHORT).show();
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
            String data = IconMatchUtils.INSTANCE.readJson("fpTransferData", MainActivity.this);
            List<YoYoItemInfo> itemInfoList = JSON.parseArray(data, YoYoItemInfo.class);
            for (YoYoItemInfo yoYoItemInfo : itemInfoList) {
                yoYoItemInfo.setIsOn(1);
                yoYoItemInfo.setIsLock(0);
                yoYoItemInfo.unitType = PriceUnitTypeEnum.WeightType;
                yoYoItemInfo.itemCode = yoYoItemInfo.plu;
                yoYoItemInfo.unitPrice = yoYoItemInfo.unitPrice * 100;
            }
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
        //printer = getUSBPrinter();
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
        if(printer != null)
            printer.disconnect();
//      YoYoUtils.unInit();
        Process.killProcess(Process.myPid());
    }

    //重量的回调
    public void checkWeight(TADWeight weight,boolean isStable, boolean isTared, boolean isZero) {
        runOnUiThread(() -> {
            double tscale_weight = Double.valueOf(weight.getWeightInGramString()).doubleValue();
            String wStr = weight.getWeightString();
            int w = (int) (tscale_weight);
            this.weight = w;
            if( wStr.contains("UL") ){
                weightDesc.setText("UL");
            } else if ( wStr.contains("OL") ) {
                weightDesc.setText("OL");
            } else {
                updateStatus(isStable,isTared,isZero);
                updateWeight(w);
            }
            YoYoUtils.aiMatching(new AIMatchingRequest(w, isStable ? WeightStableEnum.WeightStable : WeightStableEnum.WeightNotStable, RecognitionModelEnum.Auto));
            //拿走清理掉数据
            if (w <= 0) {
                resultItemAdapter.clean();
                baseInfo = null;
                llTips.setVisibility(View.VISIBLE);
                listView.setVisibility(View.GONE);
                salePriceDesc.setText("元/kg");
                tvTips.setText("请放置您要秤量的商品，小由帮您快速识别～");
            }
            updateView();
        });
    }

    private void updateWeight(int weight) {
        //Log.i("updateWeight","updateWeight:" + weight );
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
        weightDesc.setText("kg");
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

    private void updateStatus(boolean isStable, boolean isTared, boolean isZero) {
        final boolean stable = isStable;
        final boolean tared = isTared;
        final boolean zero = isZero;
        if (zero) {
            if (zeroStatus.getVisibility() != View.VISIBLE) {
                zeroStatus.setVisibility(View.VISIBLE);
            }
        } else {
            if (zeroStatus.getVisibility() != View.GONE) {
                zeroStatus.setVisibility(View.GONE);
            }
        }
        if (stable) {
            if (stableStatus.getVisibility() != View.VISIBLE) {
                stableStatus.setVisibility(View.VISIBLE);
            }
        } else {
            if (stableStatus.getVisibility() != View.GONE) {
                stableStatus.setVisibility(View.GONE);
            }
        }
        if (tared) {
            if (tareStatus.getVisibility() != View.VISIBLE) {
                tareStatus.setVisibility(View.VISIBLE);
            }
        } else {
            if (tareStatus.getVisibility() != View.GONE) {
                tareStatus.setVisibility(View.GONE);
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
        Log.d("MainActivity", "您点击了" + info.itemName + ",tupian:" + info.tupian);

        //设置点击单价和总价
        updateView();
        updateWeight(weight);
        //調用打印
        printer_iteminfo(info,request);
    }

    //Only test by HPRT Label Printer USB
    public void printer_iteminfo(YoYoItemInfo info,AiMarkRequest request) {
        String plu = info.plu;
        String itemName = info.itemName;
        String Sum_cost = sum1.getText().toString() + sum2.getText().toString();
        String Single_cost = price1.getText().toString() + price2.getText().toString();
        String Weight_l = tv_top_amount.getText().toString() + tv_top_amount_2.getText().toString();
        final int fontSize = 20;
/*
        Log.i("printer_iteminfo","plu:" + plu);
        Log.i("printer_iteminfo","itemName:" + itemName);
        Log.i("printer_iteminfo","Sum_cost:" + Sum_cost);
        Log.i("printer_iteminfo","Single_cost:" + Single_cost);
        Log.i("printer_iteminfo","Weight_l:" + Weight_l);
*/
        TSLog.console(TSLog.d, "Image Print Test Clicked..");
        if( this.printer != null ) {
            //Log.i("printer_iteminfo","printer is not null!!");
            this.printer.pageBegin(0, 0, LABEL_WIDTH_432, LABEL_HEIGHT_432, 0);
            try {
                InputStream file = getResources().openRawResource(R.raw.er);
                Bitmap image = BitmapFactory.decodeStream(file);
                file.close();
                this.printer.addText(60, 60, 0, TEXTSTYLE_BOLD, "台衡精密测控股份有限公司");
                this.printer.addText(10, 120, 0, 0, "PLU编号:" + plu);
                this.printer.addText(10, 180, 0, 0, "产品名称:" + itemName);
                this.printer.addText(10, 240, 0, 0, "单价:" + Single_cost + " rmb/kg  重量:" + Weight_l + " kg");
                //this.printer.addText(10, 220, 22, 0, "CODE128");
                //this.printer.addBarcode(10, 160, 8, 50, 2, 0, "This Is CODE128".getBytes());
                this.printer.addText(10, 400, fontSize, TEXTSTYLE_BOLD, "总价：" + Sum_cost + " RMB");
                this.printer.addBitmapImage(image, 280, 430);
                this.printer.pagePrint(1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //this.printer.pageEnd();
            //this.printer.pagePrint(1);
            //this.printer.reset();
        }else{
            Log.i("printer_iteminfo","Error ,printer is null!");
        }
    }

    // tscale functions
    @Override
    public void onWeightUpdate(TADWeight weight, boolean isStable, boolean isTared, boolean isZero) {
        //TSLog.console(TSLog.v, "Update Weight: " + weight.getWeightInGramString() + " : " + weight.getUnit() + ",isStable:" + isStable );
        //Log.i("onWeightUpdate","Update Weight: " + weight.getWeightInGramString() + " : " + weight.getUnit() + ",isStable:" + isStable + ",isTared:" + isTared + ",isZero:" + isZero);
        //Log.i("onWeightUpdate", "Get weight string:" + weight.getWeightString() + " sign:" + weight.getWeightSign() + " gram:" + String.valueOf(weight.getWeightInGram()));
        //long startTime = SystemClock.elapsedRealtime();
        if( onWeightUpdate_startTime == 0 ) {
            onWeightUpdate_startTime = SystemClock.elapsedRealtime();
            checkWeight(weight,isStable,isTared,isZero);
            //TSLog.console(TSLog.i, "First exec onWeightUpdate");
        }else{
            long endTime = SystemClock.elapsedRealtime();
            if( endTime - onWeightUpdate_startTime > 100 ){
                onWeightUpdate_startTime = endTime;
                checkWeight(weight,isStable,isTared,isZero);
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
                if (device.getVendorId() == TScaleLabel.CAYSN_LABEL_VENDOR_ID &&
                        device.getProductId() == TScaleLabel.CAYSN_LABEL_PRODUCT_ID) {
                    TSLog.console(TSLog.i, "Caysn Label Printer USB Found.");
                    Log.i("getUSBPrinter","Caysn Label Printer USB Found.");
                    return new CaysnLabelPrinter(this, TScaleLabel.TYPE_USB);
                }
                if (device.getVendorId() == TScaleReceipt.CAYSN_T9_VID &&
                        device.getProductId() == TScaleReceipt.CAYSN_T9_PID) {
                    TSLog.console(TSLog.i, "Caysn T8 Label Printer USB Found.");
                    Log.i("getUSBPrinter","Caysn T8 Label Printer USB Found.");
                    flag = true;
                    return new CaysnLabelPrinter(this, TScaleLabel.TYPE_USB);
                }
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

    @Override
    public void onNumberClicked(int number) {
        //Toast.makeText(this, "Number keyboard:" + number , Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLeftAuxButtonClicked() {
        //Toast.makeText(this, "left aux keyboard!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRightAuxButtonClicked() {
        //Toast.makeText(this, "right aux keyboard!", Toast.LENGTH_SHORT).show();
        if( onRightAuxButtonClicked_startTime == 0 ) {
            onRightAuxButtonClicked_startTime = SystemClock.elapsedRealtime();
            if (!(printer.getClass().getSimpleName().equals("HPRTLabelPrinter")) && !printer.getHasPaperStatus()) {
                Toast.makeText(this, "The printer is out of paper", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!printer.getPaperPeeledStatus()) {
                Toast.makeText(this, "The printer paper is not removed", Toast.LENGTH_SHORT).show();
                return;
            }
            printer.feedLabel();
        }else{
            long endTime = SystemClock.elapsedRealtime();
            if( endTime - onRightAuxButtonClicked_startTime > 1000 ){
                onRightAuxButtonClicked_startTime = endTime;
                if (!(printer.getClass().getSimpleName().equals("HPRTLabelPrinter")) && !printer.getHasPaperStatus()) {
                    Toast.makeText(this, "The printer is out of paper", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!printer.getPaperPeeledStatus()) {
                    Toast.makeText(this, "The printer paper is not removed", Toast.LENGTH_SHORT).show();
                    return;
                }
                printer.feedLabel();
            }else{
                //TSLog.console(TSLog.i, "not more 100ms then do nothing");
            }
            //onWeightUpdate_startTime = endTime;
        }
    }

    @Override
    public void onModifierButtonClicked(int number) {
        // Keyboard has no modifiers
    }
}
