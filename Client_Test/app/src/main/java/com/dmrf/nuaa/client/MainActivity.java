package com.dmrf.nuaa.client;

import android.nfc.Tag;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private Button ok;
    private Button upload;
    private Button read;
    private EditText username;
    private EditText password;

    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ok=findViewById(R.id.ok);
        upload=findViewById(R.id.upload);
        read=findViewById(R.id.read);
        username=findViewById(R.id.username);
        password=findViewById(R.id.password);
        final String base_url = "http://172.18.92.228/";


        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name= String.valueOf(username.getText());
                String pass= String.valueOf(password.getText());
                String url1 = base_url + "register";
                SendMessage(url1,name,pass);
            }
        });

        upload.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String filepath = Environment.getExternalStorageDirectory().toString() + "/TEST/humans.txt";
                String url2 = base_url + "upload";
                SendFile(url2, filepath);
            }
        });

        read.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String filepath = Environment.getExternalStorageDirectory().toString() + "/TEST/humans.txt";
                readFile(filepath);
            }
        });
    }

    private void readFile(String filepath)
    {
        if (filepath == null) return;

        File file = new File(filepath);
        if (file.isDirectory()){
            Toast.makeText(MainActivity.this, filepath + "is Directory", Toast.LENGTH_SHORT).show();
            return;
        }else {
            try {
                InputStream is = new FileInputStream(file);
                if (is != null) {
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);

                    String line;
                    while ((line = br.readLine()) != null) {
                        Toast.makeText(MainActivity.this, line, Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (FileNotFoundException e) {
                Toast.makeText(MainActivity.this, filepath + " dosen't found", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(MainActivity.this, filepath + "read exception,"+ e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void SendFile(String url, String filepath)
    {
        MediaType TxtFile = MediaType.parse("text/plain; charset=utf-8");
//        MediaType Image = MediaType.parse("image/jpeg");
        File file = new File(filepath);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), RequestBody.create(TxtFile, file))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Server Wrong", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String res = response.body().string();
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (res.equals("0"))
                        {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this,"already registered",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else
                        {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this,"succeed",Toast.LENGTH_SHORT).show();
                                }
                            });

                        }

                    }
                });
            }
        });

    }

    private void SendMessage(String url,final String userName,String passWord)
    {
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("username", userName);
        formBuilder.add("password", passWord);
        Request request = new Request.Builder().url(url).post(formBuilder.build()).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,"Server Wrong",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException
            {
                final String res = response.body().string();
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (res.equals("0"))
                        {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this,"already registered",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else
                        {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this,"succeed",Toast.LENGTH_SHORT).show();
                                }
                            });

                        }

                    }
                });
            }
        });

    }
}
