package com.example.berna.naotenhonome.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.berna.naotenhonome.R;
import com.example.berna.naotenhonome.model.ResponseDosen;
import com.example.berna.naotenhonome.model.ResponseDosenDetail;
import com.example.berna.naotenhonome.model.SemuadosenItem;
import com.example.berna.naotenhonome.util.api.BaseApiService;
import com.example.berna.naotenhonome.util.api.UtilsApi;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TambahMatkulActivity extends AppCompatActivity {

    @BindView(R.id.spinnerDosen)
    Spinner spinnerDosen;
    @BindView(R.id.etNamaMatkul)
    EditText etNamaMatkul;
    @BindView(R.id.btnSimpanMatkul)
    Button btnSimpanMatkul;
    ProgressDialog loading;

    Context mContext;
    BaseApiService mApiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_matkul);

        ButterKnife.bind(this);
        mContext = this;
        mApiService = UtilsApi.getAPIService();

        initSpinnerDosen();

        spinnerDosen.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedName = parent.getItemAtPosition(position).toString();
                Toast.makeText(mContext, "Selecione um item: " + selectedName, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnSimpanMatkul.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestSimpanMatkul();
            }
        });
    }

    private void initSpinnerDosen(){
        loading = ProgressDialog.show(mContext, null, "Por favor, aguarde...", true, false);
        
        mApiService.getSemuaDosen().enqueue(new Callback<ResponseDosen>() {
            @Override
            public void onResponse(Call<ResponseDosen> call, Response<ResponseDosen> response) {
                if (response.isSuccessful()) {
                    loading.dismiss();
                    List<SemuadosenItem> semuadosenItems = response.body().getSemuadosen();
                    List<String> listSpinner = new ArrayList<String>();
                    for (int i = 0; i < semuadosenItems.size(); i++){
                        listSpinner.add(semuadosenItems.get(i).getNama());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
                            android.R.layout.simple_spinner_item, listSpinner);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerDosen.setAdapter(adapter);
                } else {
                    loading.dismiss();
                    Toast.makeText(mContext, "ERro ao recuperar dados.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseDosen> call, Throwable t) {
                loading.dismiss();
                Toast.makeText(mContext, "Conexão ruim.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void requestDetailDosen(String namadosen){
        mApiService.getDetailDosen(namadosen).enqueue(new Callback<ResponseDosenDetail>() {
            @Override
            public void onResponse(Call<ResponseDosenDetail> call, Response<ResponseDosenDetail> response) {
                if (response.isSuccessful()){
                    if (response.body().isError()){
                        Toast.makeText(mContext, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        etNamaMatkul.setText(response.body().getMatkul());
                    }
                } else {
                    Toast.makeText(mContext, "Erro ao recuperar os dados", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseDosenDetail> call, Throwable t) {
                Toast.makeText(mContext, "Conexão ruim.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void requestSimpanMatkul(){
        loading = ProgressDialog.show(mContext, null, "Por favor, aguarde...", true, false);

        mApiService.simpanMatkulRequest(spinnerDosen.getSelectedItem().toString(),
                etNamaMatkul.getText().toString())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()){
                            loading.dismiss();
                            Toast.makeText(mContext, "Dados adicionados com sucesso.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(mContext, MatkulActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                            finish();
                        } else {
                            loading.dismiss();
                            Toast.makeText(mContext, "Falha ao adicionar os dados.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        loading.dismiss();
                        Toast.makeText(mContext, "Conexão ruim.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
