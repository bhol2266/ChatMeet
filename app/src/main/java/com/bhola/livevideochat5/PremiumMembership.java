package com.bhola.livevideochat5;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.bhola.livevideochat5.Models.SliderImageModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.common.collect.ImmutableList;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PremiumMembership extends AppCompatActivity {

    private ViewPager2 viewPager;
    private Handler handler;
    private Runnable runnable;
    private int currentPosition = 0;
    private boolean isUserTouched = false;
    private BillingClient billingClient;
    boolean isSuccess = false;
    ImageView card1_Check, card2_Check, card3_Check;
    LinearLayout progressBar;
    Button btnSubs;
    TextView subscriptionDetailText;
    ProductDetails selectedCard_productDetails = null;

    PurchasesUpdatedListener purchaseUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                boolean purchaseDone = false;
                for (Purchase purchase : purchases) {
                    if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged()) {
                        //first this is triggerd than onResume is called
                        AcknowledgePurchase(purchase);
                        purchaseDone = true;

                    }
                }
                if (!purchaseDone) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(PremiumMembership.this, "Payment failed! try again", Toast.LENGTH_SHORT).show();

                        }
                    }, 5000);
                }

            } else {
                // Handle any other error codes.
                Toast.makeText(PremiumMembership.this, "Something went wrong try again!", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        }
    };


    private void AcknowledgePurchase(Purchase purchase) {

        //in This first consume is called than Achnowledged is called

        AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                Log.d("handlePurchase", "Achnowledged Done: ");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        Log.d("handlePurchase", "run: " + purchase.getProducts().get(0));

                        int coins = 0;
                        String inputString = purchase.getProducts().get(0);
                        if (inputString.equals("1_week")) coins = 500;
                        if (inputString.equals("1_month")) coins = 2000;
                        if (inputString.equals("3_months")) coins = 10000;

                        savePurchaseDetails_inSharedPreference(purchase.getPurchaseToken(), coins, purchase.getPurchaseTime());


                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                                startActivity(new Intent(PremiumMembership.this, SplashScreen.class));
                            }
                        }, 2000);


                    }
                });

            }
        };

        if (!purchase.isAcknowledged()) {
            AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
            billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
        }

    }


    private void savePurchaseDetails_inSharedPreference(String purchaseToken, int coins, long purchaseTime) {
        //Reading purchase Token
        SharedPreferences sh = getSharedPreferences("UserInfo", MODE_PRIVATE);
        String a = sh.getString("purchaseToken", purchaseToken);

        // Creating purchase Token into SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putString("purchaseToken", purchaseToken);
        myEdit.putInt("coins", coins);

        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateString = dateFormat.format(currentDate);
        myEdit.putString("purchase_date", dateString);
        myEdit.commit();
        FirebaseUtil.addUserCoins(coins);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premium_membership);
        viewPager = findViewById(R.id.viewPager);
        progressBar = findViewById(R.id.progressBar);
        card1_Check = findViewById(R.id.card1_Check);
        card2_Check = findViewById(R.id.card2_Check);
        card3_Check = findViewById(R.id.card3_Check);
        subscriptionDetailText = findViewById(R.id.subscriptionDetailText);

        addUnderlineTerms_privacy();
        subscribeBtn();
        List<SliderImageModel> slideImages = new ArrayList<>();

        Drawable mm_slide_1 = ContextCompat.getDrawable(this, R.drawable.mm_slide_1);
        Drawable mm_slide_2 = ContextCompat.getDrawable(this, R.drawable.mm_slide_2);
        Drawable mm_slide_3 = ContextCompat.getDrawable(this, R.drawable.mm_slide_3);
        Drawable mm_slide_4 = ContextCompat.getDrawable(this, R.drawable.mm_slide_4);
        Drawable mm_slide_5 = ContextCompat.getDrawable(this, R.drawable.mm_slide_5);

        SliderImageModel slide1 = new SliderImageModel(mm_slide_1, "Unlimited messages", "Send messages to anyone you want");
        SliderImageModel slide2 = new SliderImageModel(mm_slide_2, "VIP Greetings", "All your greeting will be given preference in the chatlist");
        SliderImageModel slide3 = new SliderImageModel(mm_slide_3, "VIP Badge", "Become a VIP will be more attractive to the girl you want");
        SliderImageModel slide4 = new SliderImageModel(mm_slide_4, "Fresh Girl", "Becoming a VIP will give you priority to recommend  new girls");
        SliderImageModel slide5 = new SliderImageModel(mm_slide_5, "Monthly Gems", "Becoming a VIP will get extra coins");

        slideImages.add(slide1);
        slideImages.add(slide2);
        slideImages.add(slide3);
        slideImages.add(slide4);
        slideImages.add(slide5);


        SlideImageAdapter slideImageAdapter = new SlideImageAdapter(this, slideImages);
        viewPager.setAdapter(slideImageAdapter);

        ImageView back_arrow = findViewById(R.id.back_arrow);
        back_arrow.setOnClickListener(view -> {
            onBackPressed();
        });

        LinearLayout contactUs = findViewById(R.id.contactUs);
        contactUs.setOnClickListener(view -> {
            startActivity(new Intent(this, Feedback.class));
        });
        TextView coins = findViewById(R.id.coins);
        coins.setText(String.valueOf(MyApplication.coins));
        tabBtns();
        fullscreenMode();

        billingfunction();
    }

    private void subscribeBtn() {
        btnSubs = findViewById(R.id.btnContinue);
        btnSubs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedCard_productDetails == null) {
                    Toast.makeText(PremiumMembership.this, "Please select any card", Toast.LENGTH_SHORT).show();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    ImmutableList productDetailsParamsList =
                            ImmutableList.of(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                            // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                            .setProductDetails(selectedCard_productDetails)
                                            .setOfferToken(selectedCard_productDetails.getSubscriptionOfferDetails().get(0).getOfferToken())
                                            // to get an offer token, call ProductDetails.getSubscriptionOfferDetails()
                                            // for a list of offers that are available to the user
                                            .build()
                            );
                    BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(productDetailsParamsList)
                            .build();
                    billingClient.launchBillingFlow(PremiumMembership.this, billingFlowParams);
                }
            }
        });

    }

    private void billingfunction() {

        //Initialize
        billingClient = BillingClient.newBuilder(PremiumMembership.this)
                .setListener(purchaseUpdatedListener)
                .enablePendingPurchases()
                .build();


        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(new Runnable() {
            @Override
            public void run() {
                //start the connection after initializing the billing client
                connectGooglePlayBilling();
            }
        });
    }

    void connectGooglePlayBilling() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    getProductDetails();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                connectGooglePlayBilling();

                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });

    }


    private void getProductDetails() {

        List<String> productIds = new ArrayList<>();
        List<QueryProductDetailsParams.Product> list = new ArrayList<>();

        productIds.add("1_week");
        productIds.add("1_month");
        productIds.add("3_months");
// Add more product IDs as needed

        QueryProductDetailsParams.Builder queryBuilder = QueryProductDetailsParams.newBuilder();

        for (String productId : productIds) {
            QueryProductDetailsParams.Product product = QueryProductDetailsParams.Product.newBuilder().setProductId(productId).setProductType(BillingClient.ProductType.SUBS).build();
            list.add(product);
        }
        queryBuilder.setProductList(list);


        QueryProductDetailsParams queryProductDetailsParams = queryBuilder.build();

        billingClient.queryProductDetailsAsync(queryProductDetailsParams, new ProductDetailsResponseListener() {
            public void onProductDetailsResponse(BillingResult billingResult, List<ProductDetails> productDetailsList) {
                // Handle the product details response for multiple products

                ((Activity) PremiumMembership.this).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<Map<String, String>> productList = new ArrayList<>();

                        // Iterate through the productDetailsList
                        for (ProductDetails productDetails : productDetailsList) {
                            String validity = productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getBillingPeriod();

                            if (validity.equals("P1W")) {
                                setCard1(productDetails);
                            } else if (validity.equals("P1M")) {
                                setCard2(productDetails);

                            } else {
                                setCard3(productDetails);

                            }

                        }

                    }
                });
            }
        });


    }

    private void setCard1(ProductDetails productDetails) {

        String price = productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice();
        TextView card1_price_textview = findViewById(R.id.card1_price_textview);
        card1_price_textview.setText(price.replace(".00", ""));

        FrameLayout card1 = findViewById(R.id.card1);
        card1.setOnClickListener(view -> {
            selectedCard_productDetails = productDetails;
//            btnSubs.setText("SUBSCRIBE (1 WEEK + Coins 500)");
            card1_Check.setVisibility(View.VISIBLE);
            card2_Check.setVisibility(View.INVISIBLE);
            card3_Check.setVisibility(View.INVISIBLE);
            subscriptionDetailText.setText("Your subscription will renew at " + card1_price_textview.getText() + " at the end of every billing cycle. By tapping Subscribe, your payment will be charged to your Google account, and your subscription will automatically renew for the same package length at the same price until you cancel on your Google account settings at least 24 hours prior to the end of the current period. By tapping Continue, you agree to our");


        });


    }

    private void setCard2(ProductDetails productDetails) {
        String price = productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice();
        TextView card2_price_textview = findViewById(R.id.card2_price_textview);
        card2_price_textview.setText(price.replace(".00", ""));

        FrameLayout card2 = findViewById(R.id.card2);
        card2.setOnClickListener(view -> {
            selectedCard_productDetails = productDetails;
//            btnSubs.setText("SUBSCRIBE (1 MONTH + Coins 2000)");
            card1_Check.setVisibility(View.INVISIBLE);
            card2_Check.setVisibility(View.VISIBLE);
            card3_Check.setVisibility(View.INVISIBLE);
            subscriptionDetailText.setText("Your subscription will renew at " + card2_price_textview.getText() + " at the end of every billing cycle. By tapping Subscribe, your payment will be charged to your Google account, and your subscription will automatically renew for the same package length at the same price until you cancel on your Google account settings at least 24 hours prior to the end of the current period. By tapping Continue, you agree to our");


        });
    }

    private void setCard3(ProductDetails productDetails) {
        String price = productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice();
        TextView card3_price_textview = findViewById(R.id.card3_price_textview);
        card3_price_textview.setText(price.replace(".00", ""));

        FrameLayout card3 = findViewById(R.id.card3);
        card3.setOnClickListener(view -> {
            selectedCard_productDetails = productDetails;
//            btnSubs.setText("SUBSCRIBE (3 MONTH + Coins 10K)");

            card1_Check.setVisibility(View.INVISIBLE);
            card2_Check.setVisibility(View.INVISIBLE);
            card3_Check.setVisibility(View.VISIBLE);

            subscriptionDetailText.setText("Your subscription will renew at " + card3_price_textview.getText() + " at the end of every billing cycle. By tapping Subscribe, your payment will be charged to your Google account, and your subscription will automatically renew for the same package length at the same price until you cancel on your Google account settings at least 24 hours prior to the end of the current period. By tapping Continue, you agree to our");

        });
    }


    private void tabBtns() {

        TabLayout tabLayout = findViewById(R.id.tabLayout);

        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {

                    case 0:
                        View view5 = getLayoutInflater().inflate(R.layout.item_flip, null);
                        ImageView image5 = view5.findViewById(R.id.image);
                        image5.setImageResource(R.drawable.mm_chat_on);
                        TextView text5 = view5.findViewById(R.id.text);
                        text5.setText("Unlimited\nmessages");
                        tab.setCustomView(view5);

                        int color = ContextCompat.getColor(PremiumMembership.this, R.color.yellow_dim);
                        text5.setTextColor(color);
                        break;
                    case 1:

                        View view1 = getLayoutInflater().inflate(R.layout.item_flip, null);
                        ImageView image1 = view1.findViewById(R.id.image);
                        image1.setImageResource(R.drawable.mm_hello_off);
                        TextView text1 = view1.findViewById(R.id.text);
                        text1.setText("VIP\nGreetings");
                        tab.setCustomView(view1);

                        break;
                    case 2:
                        View view2 = getLayoutInflater().inflate(R.layout.item_flip, null);
                        ImageView image2 = view2.findViewById(R.id.image);
                        image2.setImageResource(R.drawable.mm_crown_off);
                        TextView text2 = view2.findViewById(R.id.text);
                        text2.setText("VIP\nBadge");
                        tab.setCustomView(view2);
                        break;


                    case 3:
                        View view3 = getLayoutInflater().inflate(R.layout.item_flip, null);
                        ImageView image3 = view3.findViewById(R.id.image);
                        image3.setImageResource(R.drawable.mm_hair_off);
                        tab.setCustomView(view3);
                        TextView text3 = view3.findViewById(R.id.text);
                        text3.setText("Fresh\nGirl");
                        break;


                    default:
                        View view4 = getLayoutInflater().inflate(R.layout.item_flip, null);
                        ImageView image4 = view4.findViewById(R.id.image);
                        image4.setImageResource(R.drawable.mm_diamond_off);
                        TextView text4 = view4.findViewById(R.id.text);
                        text4.setText("Monthly\nCoins");
                        tab.setCustomView(view4);

                        break;
                }
            }
        });
        tabLayoutMediator.attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Get the custom view of the selected tab
                View tabView = tab.getCustomView();
                if (tabView != null) {
                    int position = tab.getPosition();

                    TextView text = tabView.findViewById(R.id.text);

                    ImageView image = tabView.findViewById(R.id.image);

                    int color = ContextCompat.getColor(PremiumMembership.this, R.color.yellow);
                    text.setTextColor(color);

                    switch (position) {
                        case 0:
                            image.setImageResource(R.drawable.mm_chat_on);
                            break;
                        case 1:
                            image.setImageResource(R.drawable.mm_hello_on);
                            break;
                        case 2:
                            image.setImageResource(R.drawable.mm_crown_on);
                            break;
                        case 3:
                            image.setImageResource(R.drawable.mm_hair_on);
                            break;

                        default:
                            image.setImageResource(R.drawable.mm_diamond_on);
                            break;
                    }


                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Get the custom view of the unselected tab
                View tabView = tab.getCustomView();
                if (tabView != null) {
                    TextView text = tabView.findViewById(R.id.text);
                    ImageView image = tabView.findViewById(R.id.image);

                    int color = ContextCompat.getColor(PremiumMembership.this, R.color.brown);
                    text.setTextColor(color);

                    int position = tab.getPosition();


                    switch (position) {
                        case 0:
                            image.setImageResource(R.drawable.mm_chat_off);
                            break;
                        case 1:
                            image.setImageResource(R.drawable.mm_hello_off);
                            break;
                        case 2:
                            image.setImageResource(R.drawable.mm_crown_off);
                            break;
                        case 3:
                            image.setImageResource(R.drawable.mm_hair_off);
                            break;

                        default:
                            image.setImageResource(R.drawable.mm_diamond_off);
                            break;
                    }
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Tab reselected, no action needed
            }
        });
        autoPlaySlides();
    }

    private void autoPlaySlides() {

        handler = new Handler(Looper.getMainLooper());
        handler = new Handler(Looper.getMainLooper());

        // Runnable to auto-slide ViewPager
        runnable = new Runnable() {
            @Override
            public void run() {
                if (!isUserTouched) {
                    if (currentPosition == 4) {
                        currentPosition = 0;
                    } else {
                        currentPosition++;
                    }
                    viewPager.setCurrentItem(currentPosition, true);
                }
                handler.postDelayed(this, 2000); // 2 seconds delay
            }
        };

        handler.postDelayed(runnable, 2000); // 2 seconds delay

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                isUserTouched = state == ViewPager2.SCROLL_STATE_DRAGGING;
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    handler.postDelayed(runnable, 2000); // Start auto-sliding after 2 seconds
                } else {
                    handler.removeCallbacks(runnable); // Stop auto-sliding
                }
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPosition = position;
            }
        });


    }

    private void fullscreenMode() {
        // Make the status bar transparent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        }

        // Make the activity full-screen
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int statusBarHeight = getStatusBarHeight();
            LinearLayout layout = findViewById(R.id.parentLayout); // Replace with your layout ID
            layout.setPadding(0, statusBarHeight, 0, 0);
        }

    }

    private int getStatusBarHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    private void addUnderlineTerms_privacy() {
        TextView terms = findViewById(R.id.terms);
        TextView privaciy = findViewById(R.id.privacy);
        terms.setPaintFlags(terms.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        privaciy.setPaintFlags(privaciy.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PremiumMembership.this, Terms_Conditions.class));
            }
        });
        privaciy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PremiumMembership.this, PrivacyPolicy.class));

            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove callbacks to prevent memory leaks
        handler.removeCallbacksAndMessages(null);
    }

}

class SlideImageAdapter extends RecyclerView.Adapter<SlideImageAdapter.ViewHolder> {

    private Context context;
    private List<SliderImageModel> items;

    public SlideImageAdapter(Context context, List<SliderImageModel> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_premuim_membership_slider_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SliderImageModel sliderImageModel = items.get(position);
        holder.imageView.setImageDrawable(sliderImageModel.getImageDrawable());
        holder.title.setText(sliderImageModel.getTitle());
        holder.subTitle.setText(sliderImageModel.getSubTitle());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView title, subTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            subTitle = itemView.findViewById(R.id.subTitle);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
