package com.bhola.livevideochat5;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Acitivity_LanguageSeletor extends AppCompatActivity {


    RecyclerView recyclerView;
    ArrayList<LanguageModel> languageList;
    ArrayList<LanguageModel> languageListTemp; // is for search purpose
    public static LanguagePickerAdapter adapter;
    public static boolean saveBtnDisable = false;
    public static TextView saveBtn;
    public static ChosenLangugeAdapter chosenLangugeAdapter;
    ArrayList<String> Languagelist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acitivity_language_seletor);

        Languagelist = new ArrayList<>();
        searchBar();
        chosen_langs();
        setUpRecyclerview();
        saveButton();
        setUpChosen_recyclerview();

        ImageView backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void setUpChosen_recyclerview() {

        RecyclerView recyclerViewChosen = findViewById(R.id.recyclerViewChosen);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewChosen.setLayoutManager(layoutManager);

        ArrayList<LanguageModel> chosenList = new ArrayList<>();
        for (LanguageModel languageModel : languageList) {
            if (languageModel.isSelected()) {
                chosenList.add(languageModel);
            }
        }

        chosenLangugeAdapter = new ChosenLangugeAdapter(Acitivity_LanguageSeletor.this, chosenList);
        recyclerViewChosen.setAdapter(chosenLangugeAdapter);
    }

    private void saveButton() {

        saveBtn = findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (saveBtnDisable) {

                    Languagelist.clear();
                    for (LanguageModel languageModel : LanguagePickerAdapter.mlist) {
                        if (languageModel.isSelected()) {
                            Languagelist.add(languageModel.getLanguageName());
                        }
                    }
                    SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    String langugesWithComma = String.join(",", Languagelist);
                    UserProfileEdit.Language = langugesWithComma;
                    editor.putString("Language", langugesWithComma);
                    editor.apply();

                    new Utils().updateProfileonFireStore("language", langugesWithComma);
                    Toast.makeText(Acitivity_LanguageSeletor.this, "Saved", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
            }
        });

    }


    private void searchBar() {
        EditText searchEdit = findViewById(R.id.searchEdit);
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {


            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d("ASDf", "query: " + languageListTemp.size());

                List<LanguageModel> filteredItems = filterItems(charSequence.toString());
                // Update the RecyclerView with the filtered data
                adapter.updateData(filteredItems);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }


    public List<LanguageModel> filterItems(String query) {

        List<LanguageModel> temp = new ArrayList<>();
        temp.addAll(languageListTemp);
        List<LanguageModel> filteredItems = new ArrayList<>();

        // Iterate through the original list of items
        for (LanguageModel item : temp) {
            // Check if the item's data matches the search query
            if (item.getLanguageName().toLowerCase().contains(query.toLowerCase())) {
                filteredItems.add(item);
            }
        }

        return filteredItems;
    }

    private void chosen_langs() {


    }

    private void setUpRecyclerview() {
        recyclerView = findViewById(R.id.recyclerView);
        readLanguage_Assets(); // read language and add it to arratlist


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(Acitivity_LanguageSeletor.this);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new LanguagePickerAdapter(Acitivity_LanguageSeletor.this, languageList);
        recyclerView.setAdapter(adapter);

    }

    private void readLanguage_Assets() {

        try {
            // Get the input stream for the JSON file
            InputStream inputStream = getAssets().open("language.json");

            // Create a BufferedReader to read the JSON data
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }

            // Now, you have the JSON data in a StringBuilder
            String jsonData = stringBuilder.toString();

            JSONArray jsonArray = new JSONArray(jsonData);
            languageList = new ArrayList<>();
            languageListTemp = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String langName = jsonObject.getString("name");
                String code = jsonObject.getString("code");
                String nativeName = jsonObject.getString("nativeName");

                boolean selected = false;


                String[] selectedlanguage = UserProfileEdit.Language.split(",\\s*");

                for (String language : selectedlanguage) {
                    if (language.equals(langName)) {
                        selected = true;
                    }
                }
                LanguageModel languageModel = new LanguageModel(langName, code, nativeName, selected);
                languageList.add(languageModel);
            }
            languageListTemp.addAll(languageList);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

}


class LanguagePickerAdapter extends RecyclerView.Adapter<LanguagePickerAdapter.ImageViewHolder> {
    private final Context context;
    public static List<LanguageModel> mlist;

    public LanguagePickerAdapter(Context context, List<LanguageModel> mlist) {
        this.context = context;
        this.mlist = mlist;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.language_item, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        LanguageModel languageModel = mlist.get(position);
        holder.languageName.setText(languageModel.getLanguageName());
        holder.nativeName.setText(languageModel.getNativeName());


        holder.languageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (languageModel.isSelected()) {

                    holder.checkIcon.setVisibility(View.INVISIBLE);
                    int textColor = ContextCompat.getColor(context, R.color.white);
                    holder.languageName.setTextColor(textColor);

                    for (int i = 0; i < mlist.size(); i++) {
                        if (languageModel.getLanguageName().equals(mlist.get(i).getLanguageName())) {
                            mlist.get(i).setSelected(false);
                            notifyItemChanged(i);
                        }
                    }
                } else {
                    if (!checkLangugeNumber()) {
                        Toast.makeText(context, "Maximum 4 Languges", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    holder.checkIcon.setVisibility(View.VISIBLE);
                    int textColor = ContextCompat.getColor(context, R.color.themeColor);
                    holder.languageName.setTextColor(textColor);

                    for (int i = 0; i < mlist.size(); i++) {
                        if (languageModel.getLanguageName().equals(mlist.get(i).getLanguageName())) {
                            mlist.get(i).setSelected(true);
                            notifyItemChanged(i);
                        }
                    }

                }

                updateChosenLanguage();
                updateSaveBtn(view.getContext());

            }
        });


        if (languageModel.isSelected()) {
            holder.checkIcon.setVisibility(View.VISIBLE);
            int textColor = ContextCompat.getColor(context, R.color.themeColor);
            holder.languageName.setTextColor(textColor);

        } else {
            holder.checkIcon.setVisibility(View.INVISIBLE);
            int textColor = ContextCompat.getColor(context, R.color.white);
            holder.languageName.setTextColor(textColor);
        }
    }

    private void updateChosenLanguage() {

        ArrayList<LanguageModel> chosenList = new ArrayList<>();
        for (LanguageModel languageModel : mlist) {
            if (languageModel.isSelected()) {
                chosenList.add(languageModel);
            }
        }
        Acitivity_LanguageSeletor.chosenLangugeAdapter.updateData(chosenList);
    }

    private boolean checkLangugeNumber() {
        int count = 0;
        for (LanguageModel languageModel : mlist) {
            if (languageModel.isSelected()) {
                count = count + 1;
            }
        }
        if (count >= 4) {
            return false;
        } else {
            return true;
        }
    }


    @Override
    public int getItemCount() {
        return mlist.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView checkIcon;
        TextView languageName, nativeName;
        RelativeLayout languageLayout;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            languageName = itemView.findViewById(R.id.languageName);
            nativeName = itemView.findViewById(R.id.nativeName);
            checkIcon = itemView.findViewById(R.id.checkIcon);
            languageLayout = itemView.findViewById(R.id.languageLayout);
        }

    }

    public void updateData(List<LanguageModel> newData) {
        mlist.clear();
        mlist.addAll(newData);
        notifyDataSetChanged();
    }

    public static void updateSaveBtn(Context context) {

        Acitivity_LanguageSeletor.saveBtnDisable = false;

        for (LanguageModel languageModel : LanguagePickerAdapter.mlist) {
            if (languageModel.isSelected()) {
                int themeColor = context.getResources().getColor(R.color.themeColor); // Replace with your color resource or a specific color value
                Acitivity_LanguageSeletor.saveBtn.setTextColor(themeColor);
                Acitivity_LanguageSeletor.saveBtnDisable = true;
                break;
            }
        }
        if (!Acitivity_LanguageSeletor.saveBtnDisable) {
            int semiblack = context.getResources().getColor(R.color.white); // Replace with your color resource or a specific color value
            Acitivity_LanguageSeletor.saveBtn.setTextColor(semiblack);
        }
    }


}


class ChosenLangugeAdapter extends RecyclerView.Adapter<ChosenLangugeAdapter.ImageViewHolder> {
    private final Context context;
    public static List<LanguageModel> mlist;

    public ChosenLangugeAdapter(Context context, List<LanguageModel> mlist) {
        this.context = context;
        this.mlist = mlist;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chosen_language_item, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        LanguageModel languageModel = mlist.get(position);
        holder.languageName.setText(languageModel.getLanguageName());
        holder.cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < LanguagePickerAdapter.mlist.size(); i++) {
                    LanguageModel model = LanguagePickerAdapter.mlist.get(i);
                    if (model.getLanguageName().equals(languageModel.getLanguageName())) {
                        LanguagePickerAdapter.mlist.get(i).setSelected(false);
                        Acitivity_LanguageSeletor.adapter.notifyItemChanged(i);
                        mlist.remove(holder.getAbsoluteAdapterPosition());
                        notifyItemRemoved(holder.getAbsoluteAdapterPosition());
                        LanguagePickerAdapter.updateSaveBtn(view.getContext());
                    }

                }

            }
        });


    }


    @Override
    public int getItemCount() {
        return mlist.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView cross;
        TextView languageName;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            languageName = itemView.findViewById(R.id.languageName);
            cross = itemView.findViewById(R.id.crossIcon);

        }

    }

    public void updateData(List<LanguageModel> newData) {
        mlist.clear();
        mlist.addAll(newData);
        notifyDataSetChanged();
    }


}


class LanguageModel {
    private String languageName, code, nativeName;
    boolean selected;


    public LanguageModel() {
    }

    public LanguageModel(String languageName, String code, String nativeName, boolean selected) {
        this.languageName = languageName;
        this.code = code;
        this.nativeName = nativeName;
        this.selected = selected;
    }

    public String getLanguageName() {
        return languageName;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNativeName() {
        return nativeName;
    }

    public void setNativeName(String nativeName) {
        this.nativeName = nativeName;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
